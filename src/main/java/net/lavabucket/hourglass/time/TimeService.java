/*
 * Copyright (C) 2021 Nick Iacullo
 *
 * This file is part of Hourglass.
 *
 * Hourglass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hourglass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hourglass.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.lavabucket.hourglass.time;

import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.lavabucket.hourglass.HourglassMod;
import net.lavabucket.hourglass.utils.TimeUtils;
import net.lavabucket.hourglass.utils.VanillaAccessHelper;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;

/**
 * Handles the Hourglass time and sleep functionality for a level.
 */
public class TimeService {

    private static final Logger LOGGER = LogManager.getLogger();

    // The largest number of lunar cycles that can be stored in an int
    private static final int overflowThreshold = 11184 * TimeUtils.LUNAR_CYCLE_LENGTH;

    public final ServerLevel level;
    public final HourglassSleepStatus sleepStatus;

    private double timeDecimalAccumulator = 0;

    /**
     * Creates a new instance.
     *
     * @param level  the ServerLevel whose time this object should manage
     */
    public TimeService(ServerLevel level) {
        this.level = level;
        this.sleepStatus = new HourglassSleepStatus(() -> SERVER_CONFIG.enableSleepFeature.get());

        VanillaAccessHelper.setSleepStatus(level, this.sleepStatus);
    }

    /**
     * The vanilla server increments time every tick. This mod conflicts with vanilla time. Call
     * this method at the end of every tick to undo vanilla time increment.
     */
    public void undoVanillaTimeTicks() {
        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            level.setDayTime(level.getDayTime() - 1);
        }
    }

    /**
     * Performs all time, sleep, and weather calculations. Should run once per tick.
     */
    public void tick() {
        if (level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) == false) {
            return;
        }

        long oldTime = level.getDayTime();
        long time = elapseTime();
        long elapsedTime = time - oldTime;
        preventTimeOverflow();
        broadcastTime();

        progressWeather(elapsedTime);
        updateRandomTickSpeed(elapsedTime);
        if (BooleanUtils.isTrue(SERVER_CONFIG.enableSleepFeature.get())) {
            if (!sleepStatus.allAwake() && TimeUtils.crossedMorning(oldTime, time)) {
                LOGGER.debug(HourglassMod.MARKER, "Sleep cycle complete on dimension: {}.", level.dimension().location());
                net.minecraftforge.event.ForgeEventFactory.onSleepFinished(level, time, time);
                VanillaAccessHelper.wakeUpAllPlayers(level);

                if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)
                        && BooleanUtils.isTrue(SERVER_CONFIG.clearWeatherOnWake.get())) {
                    VanillaAccessHelper.stopWeather(level);
                }
            }
        }
    }

    /**
     * Prevents time value from getting too large by essentially keeping it modulo a multiple of the
     * lunar cycle.
     */
    private void preventTimeOverflow() {
        long time = level.getDayTime();
        if (time > overflowThreshold) {
            level.setDayTime(time - overflowThreshold);
        }
    }

    /**
     * Elapse time in level based on the current time multiplier. Should be called during every tick.
     *
     * @return  the new day time
     */
    private long elapseTime() {
        long oldTime = level.getDayTime();

        double multiplier = getMultiplier(oldTime);
        long integralMultiplier = (long) multiplier;
        double fractionalMultiplier = multiplier - integralMultiplier;

        timeDecimalAccumulator += fractionalMultiplier;
        int overflow = (int) timeDecimalAccumulator;
        timeDecimalAccumulator -= overflow;

        long timeToAdd = integralMultiplier + overflow;
        timeToAdd = correctForOvershoot(timeToAdd);

        long newTime = oldTime + timeToAdd;
        level.setDayTime(newTime); // Subtract 1 to compensate for vanilla
        return newTime;
    }

    /**
     * Check to see if the time speed multiplier will change after elapsing timeToAdd amount of
     * time, and correct for any overshooting (or undershooting) based on the new multiplier.
     *
     * Stateful, overwrites the current {@link #timeDecimalAccumulator}.
     *
     * TODO: Make this stateless
     *
     * @param timeToAdd  the proposed time to elapse
     * @return  the corrected time to elapse
     */
    private long correctForOvershoot(long timeToAdd) {
        long oldTime = level.getDayTime();
        long currentTimeOfDay = oldTime % TimeUtils.DAY_LENGTH;
        double multiplier = getMultiplier(oldTime);

        // day to night transition
        long distanceToDayEnd = TimeUtils.DAYTIME_END - currentTimeOfDay;
        if (currentTimeOfDay < TimeUtils.DAYTIME_END && timeToAdd > distanceToDayEnd) {
            double newMultiplier = getMultiplier(oldTime + timeToAdd);
            double percentagePassedBoundary = (timeToAdd + timeDecimalAccumulator - distanceToDayEnd) / multiplier;

            double timeToAddAfterBoundary = newMultiplier * percentagePassedBoundary;
            timeDecimalAccumulator = timeToAddAfterBoundary - (int) timeToAddAfterBoundary;
            return distanceToDayEnd + (int) timeToAddAfterBoundary;
        }

        // day to night transition
        long distanceToDayStart = TimeUtils.DAYTIME_START - currentTimeOfDay;
        if (sleepStatus.allAwake() && currentTimeOfDay < TimeUtils.DAYTIME_START && timeToAdd > distanceToDayStart) {
            double newMultiplier = getMultiplier(oldTime + timeToAdd);
            double percentagePassedBoundary = (timeToAdd + timeDecimalAccumulator - distanceToDayStart) / multiplier;

            double timeToAddAfterBoundary = newMultiplier * percentagePassedBoundary;
            timeDecimalAccumulator = timeToAddAfterBoundary - (int) timeToAddAfterBoundary;
            return distanceToDayStart + (int) timeToAddAfterBoundary;
        }

        // morning transition
        long distanceToMorning = TimeUtils.DAY_LENGTH - currentTimeOfDay;
        if (!sleepStatus.allAwake() && timeToAdd > distanceToMorning) {
            double newMultiplier = SERVER_CONFIG.daySpeed.get();
            double percentagePassedBoundary = (timeToAdd + timeDecimalAccumulator - distanceToMorning) / multiplier;

            double timeToAddAfterBoundary = newMultiplier * percentagePassedBoundary;
            timeDecimalAccumulator = timeToAddAfterBoundary - (int) timeToAddAfterBoundary;
            return distanceToMorning + (int) timeToAddAfterBoundary;
        }

        return timeToAdd;
    }

    /**
     * Accelerate the weather cycle in {@link #level}.
     *
     * @param timeDelta  the amount of time to progress the weather cycle
     */
    private void progressWeather(long timeDelta) {
        ServerLevelData levelData = (ServerLevelData) level.getLevelData();
        if (sleepStatus.allAwake()
                || !level.dimensionType().hasSkyLight()
                || !level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)
                || BooleanUtils.isFalse(SERVER_CONFIG.accelerateWeather.get())
                || BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get())) {
            return;
        }

        int clearWeatherTime = levelData.getClearWeatherTime();
        int thunderTime = levelData.getThunderTime();
        int rainTime = levelData.getRainTime();

        // Subtract 1 from weather speed to account for vanilla's weather progression of 1 per tick.
        timeDelta--;

        if (clearWeatherTime <= 0) {
            if (thunderTime > 0) {
                levelData.setThunderTime(Math.max(1, (int) (thunderTime - timeDelta)));
            }
            if (rainTime > 0) {
                levelData.setRainTime(Math.max(1, (int) (rainTime - timeDelta)));
            }
        }
    }

    /**
     * Updates the random tick speed based on configuration values if sleep.accelerateRandomTickSpeed
     * config is enabled.
     *
     * @param elapsedTime the amount of time that has elapsed during this tick
     */
    private void updateRandomTickSpeed(long elapsedTime) {
        if (BooleanUtils.isFalse(SERVER_CONFIG.accelerateRandomTickSpeed.get())) {
            return;
        }

        int speed = SERVER_CONFIG.baseRandomTickSpeed.get();
        if (!sleepStatus.allAwake()) {
            speed *= elapsedTime;
        }

        MinecraftServer server = level.getServer();
        server.getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(speed, server);
    }

    /**
     * Calculates the current time speed multiplier based on the time-of-day and number of sleeping
     * players. Allows manual input of time to allow calculation based on times other then current.
     * A return value of 1 is equivalent to vanilla time speed.
     *
     * @param time  the time of day to calculate the time speed for
     * @return  the time speed multiplier
     */
    public double getMultiplier(long time) {
        if (BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get()) || sleepStatus.allAwake()) {
            if (TimeUtils.isSunUp(time)) {
                return SERVER_CONFIG.daySpeed.get();
            } else {
                return SERVER_CONFIG.nightSpeed.get();
            }
        }

        if (sleepStatus.allAsleep() && SERVER_CONFIG.sleepSpeedAll.get() >= 0) {
            return SERVER_CONFIG.sleepSpeedAll.get();
        }

        double percentageSleeping = sleepStatus.getRatio();
        double sleepSpeedMin = SERVER_CONFIG.sleepSpeedMin.get();
        double sleepSpeedMax = SERVER_CONFIG.sleepSpeedMax.get();
        double multiplier = Mth.lerp(percentageSleeping, sleepSpeedMin, sleepSpeedMax);

        return multiplier;
    }

    /**
     * Broadcasts the current time to all players in {@link TimeService#level}.
     */
    public void broadcastTime() {
        long gameTime = level.getGameTime();
        long dayTime = level.getDayTime();
        boolean ruleDaylight = level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
        ClientboundSetTimePacket timePacket = new ClientboundSetTimePacket(gameTime, dayTime, ruleDaylight);

        if (level.dimension().equals(Level.OVERWORLD)) {
            // broadcast to overworld and derived worlds
            List<ServerPlayer> playerList = level.getServer().getPlayerList().getPlayers();

            for(int i = 0; i < playerList.size(); ++i) {
                ServerPlayer player = playerList.get(i);
                if (player.level == level || player.level.getLevelData() instanceof DerivedLevelData) {
                    player.connection.send(timePacket);
                }
            }
        } else {
            // broadcast to this level
            level.getServer().getPlayerList().broadcastAll(timePacket, level.dimension());
        }
    }

}

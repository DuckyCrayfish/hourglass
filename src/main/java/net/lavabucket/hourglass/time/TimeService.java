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

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.lavabucket.hourglass.HourglassMod;
import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.time.effects.TimeEffect;
import net.lavabucket.hourglass.utils.TimeUtils;
import net.lavabucket.hourglass.utils.VanillaAccessHelper;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
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
    public final ServerLevelData levelData;
    public final SleepStatus sleepStatus;

    private double timeDecimalAccumulator = 0;

    /**
     * Creates a new instance.
     *
     * @param level  the ServerLevel whose time this object should manage
     */
    public TimeService(ServerLevel level) {
        this.level = level;
        this.levelData = (ServerLevelData) level.getLevelData();
        this.sleepStatus = new SleepStatus(() -> SERVER_CONFIG.enableSleepFeature.get());

        VanillaAccessHelper.setSleepStatus(level, this.sleepStatus);
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

        TimeContext context = new TimeContext(this, elapsedTime);
        getActiveTimeEffects().forEach(effect -> effect.onTimeTick(context));

        if (SERVER_CONFIG.enableSleepFeature.get()) {
            if (!sleepStatus.allAwake() && TimeUtils.crossedMorning(oldTime, time)) {
                LOGGER.debug(HourglassMod.MARKER, "Sleep cycle complete on dimension: {}.", level.dimension().location());
                net.minecraftforge.event.ForgeEventFactory.onSleepFinished(level, time, time);
                VanillaAccessHelper.wakeUpAllPlayers(level);

                if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)
                        && SERVER_CONFIG.clearWeatherOnWake.get()) {
                    VanillaAccessHelper.stopWeather(level);
                }
            }
        }

        vanillaTimeCompensation();
    }

    /**
     * This method compensates for time changes made by the vanilla server every tick.
     *
     * The vanilla server increments time at a rate of 1 every tick. Since this functionality
     * conflicts with this mod's time changes, and this functionality cannot be prevented, this
     * method should be called at the end of the {@code START} phase of every world tick to undo
     * this vanilla progression.
     */
    private void vanillaTimeCompensation() {
        level.setDayTime(level.getDayTime() - 1);
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
     * @return the new day time
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
        level.setDayTime(newTime);
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
     * @return the corrected time to elapse
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
     * Calculates the current time speed multiplier based on the time-of-day and number of sleeping
     * players. Allows manual input of time to allow calculation based on times other then current.
     * A return value of 1 is equivalent to vanilla time speed.
     *
     * @param time  the time of day to calculate the time speed for
     * @return the time speed multiplier
     */
    public double getMultiplier(long time) {
        if (!SERVER_CONFIG.enableSleepFeature.get() || sleepStatus.allAwake()) {
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

    private Collection<TimeEffect> getActiveTimeEffects() {
        return HourglassRegistry.TIME_EFFECT.getValues();
    }

}

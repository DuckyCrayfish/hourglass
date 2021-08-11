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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.LogicalSide;

public class ServerTimeHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    // The largest number of lunar cycles that can be stored in an int
    private static final int overflowThreshold = 11184 * TimeUtils.LUNAR_CYCLE_LENGTH;

    public static ServerTimeHandler instance;

    public ServerWorld world;
    public SleepState sleepState;
    public double timeDecimalAccumulator;

    /**
     * Called from the Forge EventBus during a SleepingTimeCheckEvent, a forge event that is
     * called once per tick for every player who is currently sleeping.
     *
     * @param event the event provided by forge from the EventBus
     */
    @SubscribeEvent
    public static void onSleepingCheckEvent(SleepingTimeCheckEvent event) {
        long time = event.getPlayer().level.getDayTime() % 24000;
        if (time >= 23460 || time == 0) {
            event.setResult(Result.ALLOW);
        }
    }

    /**
     * Event listener that is called when a new world is loaded.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            if (world.dimension().equals(World.OVERWORLD)) {
                instance = new ServerTimeHandler((ServerWorld) event.getWorld());
            }
        }
    }

    /**
     * Event listener that is called when a world is unloaded.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) event.getWorld();
            if (world.dimension().equals(World.OVERWORLD)) {
                instance = null;
            }
        }
    }

    /**
     * Event listener that is called every tick per world.
     *
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side == LogicalSide.SERVER
                && event.world instanceof ServerWorld
                && ServerTimeHandler.instance != null
                && event.world.dimension().equals(World.OVERWORLD)) {

            if (event.phase == TickEvent.Phase.START) {
                instance.tick();
            } else {
                instance.undoVanillaTimeTicks();
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param world  the ServerWorld whose time this object should manage
     */
    public ServerTimeHandler(ServerWorld world) {
        this.world = world;
        this.timeDecimalAccumulator = 0;
        this.sleepState = new SleepState();
    }

    /**
     * The vanilla server increments time every tick. This mod conflicts with vanilla time. Call
     * this method at the end of every tick to undo vanilla time increment.
     */
    private void undoVanillaTimeTicks() {
        if (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            world.setDayTime(world.getDayTime() - 1);
        }
    }

    /**
     * Performs all time, sleep, and weather calculations. Should run once per tick.
     */
    public void tick() {
        if (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) == false) {
            return;
        }

        sleepState = calculateSleepState();
        long oldTime = world.getDayTime();
        long time = elapseTime();
        preventTimeOverflow();
        broadcastTime();

        progressWeather((int) (time - oldTime));
        if (BooleanUtils.isTrue(SERVER_CONFIG.enableSleepFeature.get())) {
            VanillaTimeHelper.preventVanillaSleep(world);
            if (!sleepState.allAwake() && TimeUtils.crossedMorning(oldTime, time)) {
                LOGGER.debug(HourglassMod.MARKER, "Sleep cycle complete on dimension: {}.", world.dimension().location());
                net.minecraftforge.event.ForgeEventFactory.onSleepFinished(world, time, time);
                VanillaTimeHelper.wakeUpAllPlayers(world);
                sleepState.sleepingPlayerCount = 0;

                if (world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)
                        && BooleanUtils.isTrue(SERVER_CONFIG.clearWeatherOnWake.get())) {
                    VanillaTimeHelper.stopWeather(world);
                }
            }
        }
    }

    /**
     * Prevents time value from getting too large by essentially keeping it modulo a multiple of the
     * lunar cycle.
     */
    private void preventTimeOverflow() {
        long time = world.getDayTime();
        if (time > overflowThreshold) {
            world.setDayTime(time - overflowThreshold);
        }
    }

    /**
     * Elapse time in world based on the current time multiplier. Should be called during every tick.
     *
     * @return  the new day time
     */
    private long elapseTime() {
        long oldTime = world.getDayTime();

        double multiplier = getMultiplier(oldTime);
        long integralMultiplier = (long) multiplier;
        double fractionalMultiplier = multiplier - integralMultiplier;

        timeDecimalAccumulator += fractionalMultiplier;
        int overflow = (int) timeDecimalAccumulator;
        timeDecimalAccumulator -= overflow;

        long timeToAdd = integralMultiplier + overflow;
        timeToAdd = correctForOvershoot(timeToAdd);

        long newTime = oldTime + timeToAdd;
        world.setDayTime(newTime); // Subtract 1 to compensate for vanilla
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
        long oldTime = world.getDayTime();
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
        if (sleepState.allAwake() && currentTimeOfDay < TimeUtils.DAYTIME_START && timeToAdd > distanceToDayStart) {
            double newMultiplier = getMultiplier(oldTime + timeToAdd);
            double percentagePassedBoundary = (timeToAdd + timeDecimalAccumulator - distanceToDayStart) / multiplier;

            double timeToAddAfterBoundary = newMultiplier * percentagePassedBoundary;
            timeDecimalAccumulator = timeToAddAfterBoundary - (int) timeToAddAfterBoundary;
            return distanceToDayStart + (int) timeToAddAfterBoundary;
        }

        // morning transition
        long distanceToMorning = TimeUtils.DAY_LENGTH - currentTimeOfDay;
        if (!sleepState.allAwake() && timeToAdd > distanceToMorning) {
            double newMultiplier = SERVER_CONFIG.daySpeed.get();
            double percentagePassedBoundary = (timeToAdd + timeDecimalAccumulator - distanceToMorning) / multiplier;

            double timeToAddAfterBoundary = newMultiplier * percentagePassedBoundary;
            timeDecimalAccumulator = timeToAddAfterBoundary - (int) timeToAddAfterBoundary;
            return distanceToMorning + (int) timeToAddAfterBoundary;
        }

        return timeToAdd;
    }

    /**
     * Accelerate the weather cycle in world.
     *
     * @param timeDelta  the amount of time to progress the weather cycle
     */
    private void progressWeather(int timeDelta) {
        IServerWorldInfo levelData = VanillaTimeHelper.getServerLevelData(world);
        if (levelData == null
                || sleepState.allAwake()
                || !world.dimensionType().hasSkyLight()
                || !world.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)
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
                levelData.setThunderTime(Math.max(1, thunderTime - timeDelta));
            }
            if (rainTime > 0) {
                levelData.setRainTime(Math.max(1, rainTime - timeDelta));
            }
        }
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
        if (BooleanUtils.isFalse(SERVER_CONFIG.enableSleepFeature.get()) || sleepState.allAwake()) {
            if (TimeUtils.isSunUp(time)) {
                return SERVER_CONFIG.daySpeed.get();
            } else {
                return SERVER_CONFIG.nightSpeed.get();
            }
        }

        if (sleepState.allAsleep() && SERVER_CONFIG.sleepSpeedAll.get() >= 0) {
            return SERVER_CONFIG.sleepSpeedAll.get();
        }

        double percentageSleeping = sleepState.getRatio();
        double sleepSpeedMin = SERVER_CONFIG.sleepSpeedMin.get();
        double sleepSpeedMax = SERVER_CONFIG.sleepSpeedMax.get();
        double multiplier = MathHelper.lerp(percentageSleeping, sleepSpeedMin, sleepSpeedMax);
        return multiplier;
    }

    /**
     * Calculates the current SleepState.
     *
     * @return  the current SleepState
     */
    public SleepState calculateSleepState() {
        SleepState newSleepState = new SleepState();
        List<ServerPlayerEntity> players = world.getPlayers(player -> !player.isSpectator());
        newSleepState.totalPlayerCount = players.size();
        newSleepState.sleepingPlayerCount =
                (int) players.stream().filter(player -> player.isSleeping()).count();
        return newSleepState;
    }

    /**
     * Broadcasts the current time to all players in {@link ServerTimeHandler#world}.
     */
    public void broadcastTime() {
        long gameTime = world.getGameTime();
        long dayTime = world.getDayTime();
        boolean ruleDaylight = world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
        SUpdateTimePacket timePacket = new SUpdateTimePacket(gameTime, dayTime, ruleDaylight);

        if (world.dimension().equals(World.OVERWORLD)) {
            // broadcast to overworld and derived worlds
            List<ServerPlayerEntity> playerList = world.getServer().getPlayerList().getPlayers();

            for(int i = 0; i < playerList.size(); ++i) {
                ServerPlayerEntity player = playerList.get(i);
                if (player.level == world || player.level.getLevelData() instanceof DerivedWorldInfo) {
                    player.connection.send(timePacket);
                }
            }
        } else {
            // broadcast to this world
            world.getServer().getPlayerList().broadcastAll(timePacket, world.dimension());
        }

    }

}

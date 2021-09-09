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

import static net.lavabucket.hourglass.HourglassMod.MARKER;
import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.time.effects.TimeEffect;
import net.lavabucket.hourglass.utils.MathUtils;
import net.lavabucket.hourglass.utils.TimeUtils;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.lavabucket.hourglass.wrappers.TimePacketWrapper;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Handles the Hourglass time and sleep functionality for a level.
 */
public class TimeService {

    private static final Logger LOGGER = LogManager.getLogger();

    // The largest number of lunar cycles that can be stored in an int
    private static final int overflowThreshold = 11184 * TimeUtils.LUNAR_CYCLE_LENGTH;

    public final ServerLevelWrapper level;
    public final SleepStatus sleepStatus;

    private double timeDecimalAccumulator = 0;

    /**
     * Creates a new instance.
     *
     * @param level  the wrapped level whose time this object should manage
     */
    public TimeService(ServerLevelWrapper level) {
        this.level = level;
        this.sleepStatus = new SleepStatus(() -> SERVER_CONFIG.enableSleepFeature.get());
        this.level.setSleepStatus(this.sleepStatus);
    }

    /**
     * Performs all time, sleep, and weather calculations. Should run once per tick.
     */
    public void tick() {
        if (!level.daylightRuleEnabled()) {
            return;
        }

        long oldTime = level.get().getDayTime();
        long time = elapseTime();
        long elapsedTime = time - oldTime;

        TimeContext context = new TimeContext(this, elapsedTime);
        getActiveTimeEffects().forEach(effect -> effect.onTimeTick(context));

        boolean overrideSleep = SERVER_CONFIG.enableSleepFeature.get();
        if (overrideSleep && !sleepStatus.allAwake() && TimeUtils.crossedMorning(oldTime, time)) {
            handleMorning();
        }

        preventTimeOverflow();
        broadcastTime();
        vanillaTimeCompensation();
    }

    private void handleMorning() {
        long time = level.get().getDayTime();
        ForgeEventFactory.onSleepFinished(level.get(), time, time);
        sleepStatus.removeAllSleepers();
        level.wakeUpAllPlayers();

        if (level.weatherRuleEnabled() && SERVER_CONFIG.clearWeatherOnWake.get()) {
            level.stopWeather();
        }

        LOGGER.debug(MARKER, "Sleep cycle complete on dimension: {}.",
                level.get().dimension().location());
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
        level.get().setDayTime(level.get().getDayTime() - 1);
    }

    /**
     * Prevents time value from getting too large by essentially keeping it modulo a multiple of the
     * lunar cycle.
     */
    private void preventTimeOverflow() {
        long time = level.get().getDayTime();
        if (time > overflowThreshold) {
            level.get().setDayTime(time - overflowThreshold);
        }
    }

    /**
     * Elapse time in this service's {@link #level level} based on the current time
     * multiplier. This method should be called during every tick.
     *
     * @return the new day time
     */
    private long elapseTime() {
        long oldTime = level.get().getDayTime();

        double multiplier = getMultiplier(oldTime);
        long integralMultiplier = (long) multiplier;
        double fractionalMultiplier = multiplier - integralMultiplier;

        timeDecimalAccumulator += fractionalMultiplier;
        int overflow = (int) timeDecimalAccumulator;
        timeDecimalAccumulator -= overflow;

        long timeToAdd = integralMultiplier + overflow;
        timeToAdd = correctForOvershoot(timeToAdd);

        long newTime = oldTime + timeToAdd;
        level.get().setDayTime(newTime);
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
        long oldTime = level.get().getDayTime();
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
        double multiplier = MathUtils.lerp(percentageSleeping, sleepSpeedMin, sleepSpeedMax);

        return multiplier;
    }

    /**
     * Broadcasts the current time to all players who observe it.
     */
    public void broadcastTime() {
        TimePacketWrapper timePacket = TimePacketWrapper.create(level);
        Stream<ServerPlayerWrapper> playerStream = level.get().getServer()
                .getPlayerList().getPlayers().stream()
                .map(player -> new ServerPlayerWrapper(player));

        Predicate<ServerPlayerWrapper> playerPredicate =
                player -> player.get().level.equals(level.get());
        if (level.get().equals(level.get().getServer().overworld())) {
            // If level is overworld, include all derived levels as well.
            playerPredicate = playerPredicate
                    .or(player -> ServerLevelWrapper.isDerived(player.get().level));
        }

        playerStream.filter(playerPredicate)
                .forEach(player -> player.get().connection.send(timePacket.get()));
    }

    private Collection<TimeEffect> getActiveTimeEffects() {
        return HourglassRegistry.TIME_EFFECT.getValues();
    }

}

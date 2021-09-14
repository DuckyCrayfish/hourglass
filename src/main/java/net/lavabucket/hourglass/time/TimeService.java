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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.time.effects.TimeEffect;
import net.lavabucket.hourglass.utils.MathUtils;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.TimePacketWrapper;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * Handles the Hourglass time and sleep functionality for a level.
 */
public class TimeService {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Time of day when the sun rises above the horizon. */
    public static final Time DAY_START = new Time(23500);

    /** Time of day when the sun sets below the horizon. */
    public static final Time NIGHT_START = new Time(12500);

    // The largest number of lunar cycles that can be stored in an int
    private static final int OVERFLOW_THRESHOLD = 11184 * Time.LUNAR_CYCLE_TICKS;

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

        Time oldTime = getDayTime();
        Time deltaTime = tickTime();
        Time time = getDayTime();

        TimeContext context = new TimeContext(this, time, deltaTime);
        getActiveTimeEffects().forEach(effect -> effect.onTimeTick(context));

        boolean overrideSleep = SERVER_CONFIG.enableSleepFeature.get();
        if (overrideSleep && !sleepStatus.allAwake() && Time.crossedMorning(oldTime, time)) {
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
        if (time > OVERFLOW_THRESHOLD) {
            level.get().setDayTime(time - OVERFLOW_THRESHOLD);
        }
    }

    /**
     * Progresses time in this {@link #level} based on the current time-speed.
     * This method should be called every tick.
     *
     * @return the amount of time that elapsed
     */
    private Time tickTime() {
        Time time = getDayTime();

        Time timeDelta = new Time(getTimeSpeed(time));
        timeDelta = correctForOvershoot(time, timeDelta);

        setDayTime(time.add(timeDelta));
        return timeDelta;
    }

    /**
     * Checks to see if the time-speed will change after elapsing time by {@code timeDelta}, and
     * correct for any overshooting (or undershooting) based on the new speed.
     *
     * @param time  the current time
     * @param timeDelta  the proposed amount of time to elapse
     * @return the adjusted amount of time to elapse
     */
    private Time correctForOvershoot(Time time, Time timeDelta) {
        Time nextTime = time.add(timeDelta);
        Time timeOfDay = time.timeOfDay();
        Time nextTimeOfDay = nextTime.timeOfDay();

        // day to night transition
        if (NIGHT_START.betweenMod(timeOfDay, nextTimeOfDay)) {
            double nextTimeSpeed = getTimeSpeed(nextTime);
            Time timeUntilBreakpoint = NIGHT_START.subtract(timeOfDay);
            double breakpointRatio = 1 - timeUntilBreakpoint.divide(timeDelta);

            return timeUntilBreakpoint.add(nextTimeSpeed * breakpointRatio);
        }

        // day to night transition
        if (DAY_START.betweenMod(timeOfDay, nextTimeOfDay)) {
            double nextTimeSpeed = getTimeSpeed(nextTime);
            Time timeUntilBreakpoint = DAY_START.subtract(timeOfDay);
            double breakpointRatio = 1 - timeUntilBreakpoint.divide(timeDelta);

            return timeUntilBreakpoint.add(nextTimeSpeed * breakpointRatio);
        }

        // morning transition
        Time timeUntilMorning = Time.DAY_LENGTH.subtract(timeOfDay);
        if (timeUntilMorning.compareTo(timeDelta) < 0 && !sleepStatus.allAwake()) {
            double nextTimeSpeed = SERVER_CONFIG.daySpeed.get();
            double breakpointRatio = 1 - timeUntilMorning.divide(timeDelta);

            return timeUntilMorning.add(nextTimeSpeed * breakpointRatio);
        }

        return timeDelta;
    }

    /**
     * Calculates the current time-speed multiplier based on the time-of-day and number of sleeping
     * players.
     *
     * Accepts time as a parameter to allow for prediction of other times. Prediction of times other
     * than the current time may not be accurate due to sleeping player changes.
     *
     * A return value of 1 is equivalent to vanilla time speed.
     *
     * @param time  the time at which to calculate the time-speed
     * @return the time-speed
     */
    public double getTimeSpeed(Time time) {
        if (!SERVER_CONFIG.enableSleepFeature.get() || sleepStatus.allAwake()) {
            if (time.equals(DAY_START) || time.timeOfDay().betweenMod(DAY_START, NIGHT_START)) {
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
     * {@return this level's time as an instance of {@link Time}}
     */
    public Time getDayTime() {
        return new Time(level.get().getDayTime(), timeDecimalAccumulator);
    }

    /**
     * Sets this level's 'daytime' to the integer component of {@code time}.
     * @param time  the time to set
     * @return the new time
     */
    public Time setDayTime(Time time) {
        timeDecimalAccumulator = time.fractionalValue();
        level.get().setDayTime(time.longValue());
        return time;
    }

    /**
     * Broadcasts the current time to all players who observe it.
     */
    public void broadcastTime() {
        TimePacketWrapper timePacket = TimePacketWrapper.create(level);
        level.get().getServer().getPlayerList().getPlayers().stream()
                .filter(player -> managesLevel(new ServerLevelWrapper(player.level)))
                .forEach(player -> player.connection.send(timePacket.get()));
    }

    /**
     * Returns true if {@code levelToCheck} has its time managed by this object, or false otherwise.
     * If this object is managing the overworld, this method will return true for all derived
     * levels.
     *
     * @param levelToCheck  the level to check
     * @return true if {@code levelToCheck} has its time managed by this object, or false otherwise.
     */
    public boolean managesLevel(ServerLevelWrapper levelToCheck) {
        if (level.get().equals(levelToCheck.get())) {
            return true;
        } else if (level.get().equals(level.get().getServer().overworld())
                && ServerLevelWrapper.isDerived(levelToCheck.get())) {
            return true;
        } else {
            return false;
        }
    }

    private Collection<TimeEffect> getActiveTimeEffects() {
        return HourglassRegistry.TIME_EFFECT.getValues();
    }

}

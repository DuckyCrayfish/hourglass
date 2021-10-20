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

package net.lavabucket.hourglass.time.providers;

import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import net.lavabucket.hourglass.time.SleepStatus;
import net.lavabucket.hourglass.time.Time;
import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.utils.MathUtils;

/**
 * A time provider that progresses time relative to its current state using a time speed multiplier.
 */
public class StandardTimeProvider implements TimeProvider {

    /** Time of day when the sun rises above the horizon. */
    public static final Time DAY_START = new Time(23500);

    /** Time of day when the sun sets below the horizon. */
    public static final Time NIGHT_START = new Time(12500);

    private final SleepStatus sleepStatus;
    private final TimeService timeService;

    public StandardTimeProvider(TimeService service) {
        this.timeService = service;
        this.sleepStatus = service.sleepStatus;
    }

    /**
     * Progresses time based on the current time-speed.
     * @return the new time
     */
    @Override
    public Time updateTime() {
        Time time = timeService.getDayTime();
        double timeSpeed = getTimeSpeed(time);
        Time timeDelta = new Time(timeSpeed);
        timeDelta = correctForOvershoot(time, timeDelta);

        time = time.add(timeDelta);
        return time;
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

        if (sleepStatus.allAwake()) {
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
        } else {
            // morning transition
            Time timeUntilMorning = Time.DAY_LENGTH.subtract(timeOfDay);
            if (timeUntilMorning.compareTo(timeDelta) < 0) {
                double nextTimeSpeed = SERVER_CONFIG.daySpeed.get();
                double breakpointRatio = 1 - timeUntilMorning.divide(timeDelta);

                return timeUntilMorning.add(nextTimeSpeed * breakpointRatio);
            }
        }

        return timeDelta;
    }

    /**
     * Calculates the current time speed based on the time-of-day and number of sleeping players.
     *
     * A return value of 1 is equivalent to vanilla time speed.
     *
     * <p>Accepts time as a parameter to allow for prediction of other times. Prediction of times
     * other than the current time may not be accurate due to sleeping player changes.
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

        double sleepRatio = sleepStatus.ratio();
        double curve = SERVER_CONFIG.sleepSpeedCurve.get();
        double speedRatio = MathUtils.normalizedTunableSigmoid(sleepRatio, curve);

        double sleepSpeedMin = SERVER_CONFIG.sleepSpeedMin.get();
        double sleepSpeedMax = SERVER_CONFIG.sleepSpeedMax.get();
        double multiplier = MathUtils.lerp(speedRatio, sleepSpeedMin, sleepSpeedMax);

        return multiplier;
    }

}

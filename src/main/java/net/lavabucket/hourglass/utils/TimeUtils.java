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

package net.lavabucket.hourglass.utils;

/**
 * This class contains static utility methods and variables regarding time.
 */
public final class TimeUtils {

    /** Private constructor to disallow instantiation. */
    private TimeUtils() {}

    /** The tick duration of a full Minecraft day. */
    public static final int DAY_LENGTH = 24000;

    /** The amount of time a full lunar cycle takes to complete. */
    public static final int LUNAR_CYCLE_LENGTH = 192000;

    /** Time of day at noon. */
    public static final int NOON = 6000;

    /** Time of day at midnight. */
    public static final int MIDNIGHT = 18000;

    /** Time of day when the sun rises above the horizon. */
    public static final int DAYTIME_START = 23500;

    /** Time of day when the sun sets below the horizon. */
    public static final int DAYTIME_END = 12500;

    /**
     * {@return true if time is between sunrise and sunset}
     *
     * @param time  the time to check
     */
    public static boolean isDay(long time) {
        return time % 24000 > 0 && time % 24000 < 12000;
    }

    /**
     * {@return true if the sun is above the horizon}
     *
     * The sun is defined as being above the horizon between the times {@link #DAYTIME_START} and
     * {@link #DAYTIME_END}.
     *
     * @param time  the time to check
     */
    public static boolean isSunUp(long time) {
        return (0 < time % DAY_LENGTH && time % DAY_LENGTH < DAYTIME_END)
                || time % DAY_LENGTH >= DAYTIME_START;
    }

    /**
     * {@return the number of days that have passed since dayTime 0, starting at 1}
     *
     * @param time  the time to check
     */
    public static long getDay(long time) {
        return time / DAY_LENGTH;
    }

    /**
     * {@return the current time of day, between 0 and {@link #DAY_LENGTH}}
     *
     * @param time  the time to check
     */
    public static long getTimeOfDay(long time) {
        return time % DAY_LENGTH;
    }

    /**
     * {@return true if a new day has started between oldTime and newTime}
     *
     * @param oldTime  the first reference time
     * @param newTime  the second reference
     */
    public static boolean crossedMorning(long oldTime, long newTime) {
        return TimeUtils.getDay(newTime) > TimeUtils.getDay(oldTime);
    }

}

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

public class TimeUtils {

    public static final int DAY_LENGTH = 24000;
    public static final int LUNAR_CYCLE_LENGTH = 192000;
    public static final int NOON = 6000;
    public static final int MIDNIGHT = 18000;
    public static final int DAYTIME_START = 23500;
    public static final int DAYTIME_END = 12500;

    public static boolean isDay(long time) {
        return time % 24000 > 0 && time % 24000 < 12000;
    }

    public static boolean isSunUp(long time) {
        return (0 < time % DAY_LENGTH && time % DAY_LENGTH < DAYTIME_END)
                || time % DAY_LENGTH >= DAYTIME_START;
    }

    public static long getDay(long time) {
        return time / DAY_LENGTH;
    }

    public static long getTimeOfDay(long time) {
        return time % DAY_LENGTH;
    }

    public static boolean crossedMorning(long oldTime, long newTime) {
        return TimeUtils.getDay(newTime) > TimeUtils.getDay(oldTime);
    }

}

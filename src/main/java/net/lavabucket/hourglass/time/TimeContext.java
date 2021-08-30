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

/**
 * Provides contextual time and level data to sleep effects.
 *
 * This class intentionally excludes references to external libraries to minimize changes between
 * Minecraft versions.
 */
public class TimeContext {

    protected TimeService timeService;
    protected long currentTime;
    protected long timeDelta;

    /**
     * Creates a new instance.
     *
     * @param timeService  the time service for this level
     * @param timeDelta  the time that has elapsed during this tick
     */
    public TimeContext(TimeService timeService, long timeDelta) {
        this.timeService = timeService;
        this.timeDelta = timeDelta;
    }

    /** {@return the time service for the level} */
    public TimeService getTimeService() {
        return timeService;
    }

    /** {@return the new time set during this tick} */
    public long getCurrentTime() {
        return currentTime;
    }

    /** {@return the time that has elapsed during this tick} */
    public long getTimeDelta() {
        return timeDelta;
    }

}

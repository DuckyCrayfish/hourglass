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

import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;

/**
 * Provides contextual time and level data to sleep effects.
 *
 * This class intentionally excludes references to external libraries to minimize changes between
 * Minecraft versions.
 */
public class TimeContext {

    protected final TimeService timeService;
    protected final Time currentTime;
    protected final Time timeDelta;

    /**
     * Creates a new instance.
     *
     * @param timeService  the time service for this level
     * @param timeDelta  the time that has elapsed during this tick
     */
    public TimeContext(TimeService timeService, Time currentTime, Time timeDelta) {
        this.timeService = timeService;
        this.currentTime = currentTime;
        this.timeDelta = timeDelta;
    }

    /** {@return the time service for the level} */
    public TimeService getTimeService() {
        return timeService;
    }

    /** {@return the new time set during this tick} */
    public Time getCurrentTime() {
        return currentTime;
    }

    /** {@return the time that has elapsed during this tick} */
    public Time getTimeDelta() {
        return timeDelta;
    }

    /** {@return the level in which this time tick event occurred} */
    public ServerLevelWrapper getLevel() {
        return getTimeService().level;
    }

}

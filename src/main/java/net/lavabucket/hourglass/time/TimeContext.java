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
 * An immutable snapshot of a time progression event.
 * This class is used to pass time information to time effects.
 *
 * <p>This class intentionally excludes references to Minecraft objects (like the level class) to
 * minimize changes between Minecraft versions.
 */
public class TimeContext {

    private final TimeService timeService;
    private final Time oldTime;
    private final Time newTime;
    private final Time timeDelta;

    /**
     * Creates a new instance.
     *
     * @param timeService  the {@code TimeService} for the level
     * @param oldTime  the old time before the change occurred
     * @param newTime  the new time after the change occurred
     */
    public TimeContext(TimeService timeService, Time oldTime, Time newTime) {
        this.timeService = timeService;
        this.oldTime = oldTime;
        this.newTime = newTime;
        this.timeDelta = newTime.subtract(oldTime);
    }

    /** {@return the time service for the level} */
    public final TimeService getTimeService() {
        return timeService;
    }

    /** {@return the old time before this change occurred} */
    public final Time getOldTime() {
        return oldTime;
    }

    /** {@return the new time set during this tick} */
    public final Time getNewTime() {
        return newTime;
    }

    /** {@return the cached value of {@code getNewTime().subtract(getOldTime)}} */
    public Time getTimeDelta() {
        return timeDelta;
    }

    /** {@return the level in which this time tick event occurred} */
    public ServerLevelWrapper getLevel() {
        return getTimeService().level;
    }

}

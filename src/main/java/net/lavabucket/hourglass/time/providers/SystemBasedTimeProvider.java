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

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

import net.lavabucket.hourglass.time.Time;

/**
 * A time provider that is based on system time, allowing Minecraft time to sync with real-life.
 *
 * <p>This class matches Minecraft time 0 with 6:00 a.m. in real-life.
 */
public class SystemBasedTimeProvider implements TimeProvider {

    @Override
    public Time updateTime() {
        ZonedDateTime nowZoned = ZonedDateTime.now();
        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
        Duration duration = Duration.between(midnight, Instant.now());
        long seconds = duration.getSeconds();

        long time = (long) ((double) seconds/3.6D);
        time -= 6000;
        time = Math.floorMod(time, Time.DAY_TICKS);
        return new Time(time);
    }

}

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

public class SleepState {

    public int sleepingPlayerCount;
    public int totalPlayerCount;

    public SleepState() {
        this(0, 0);
    }

    public SleepState(int sleepingPlayerCount, int totalPlayerCount) {
        this.sleepingPlayerCount = sleepingPlayerCount;
        this.totalPlayerCount = totalPlayerCount;
    }

    /**
     * {@return the ratio of sleeping players to total players. Value between 0.0 and 1.0}
     */
    public double getRatio() {
        return (double) sleepingPlayerCount / (double) totalPlayerCount;
    }

    /**
     * {@return true when all players are sleeping, false otherwise}
     */
    public boolean allAsleep() {
        return sleepingPlayerCount == totalPlayerCount;
    }

    /**
     * {@return true when all players are awake, false otherwise}
     */
    public boolean allAwake() {
        return sleepingPlayerCount == 0;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sleepingPlayerCount;
        result = prime * result + totalPlayerCount;
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        SleepState other = (SleepState) obj;
        if (sleepingPlayerCount != other.sleepingPlayerCount)
            return false;
        if (totalPlayerCount != other.totalPlayerCount)
            return false;
        return true;
    }

}


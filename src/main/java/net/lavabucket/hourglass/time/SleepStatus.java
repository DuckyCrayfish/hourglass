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

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * This class mimics a lot of the functionality of {@link net.minecraft.server.players.SleepStatus},
 * but includes the ability to conditionally block vanilla sleep functionality.
 *
 * This class also includes a number of utility methods and getters for use in Hourglass.
 */
public class SleepStatus extends net.minecraft.server.players.SleepStatus {
    protected int activePlayerCount;
    protected int sleepingPlayerCount;
    protected Supplier<Boolean> preventSleepSupplier;

    /**
     * Creates a new instance.
     *
     * @param preventSleepSupplier  a supplier that should return true when vanilla sleep
     * functionality should be blocked, false otherwise.
     */
    public SleepStatus(Supplier<Boolean> preventSleepSupplier) {
        this.preventSleepSupplier = preventSleepSupplier;
    }

    /**
     * Mimics the implementation of {@link SleepStatus#areEnoughSleeping(int)}, except blocks
     * vanilla sleep functionality by returning false if {@link #preventSleepSupplier} returns true.
     */
    @Override
    public boolean areEnoughSleeping(int percentageRequired) {
        if (preventSleepSupplier.get()) {
            return false;
        } else {
            return sleepingPlayerCount >= sleepersNeeded(percentageRequired);
        }
    }

    /**
     * Mimics the implementation of {@link SleepStatus#areEnoughDeepSleeping(int, List)}, except
     * blocks vanilla sleep functionality by returning false if {@link #preventSleepSupplier}
     * returns true.
     */
    @Override
    public boolean areEnoughDeepSleeping(int percentageRequired, List<ServerPlayer> playerList) {
        if (preventSleepSupplier.get()) {
            return false;
        } else {
            long deepSleepers = playerList.stream().filter(Player::isSleepingLongEnough).count();
            return deepSleepers >= sleepersNeeded(percentageRequired);
        }
    }

    /**
     * Mimics the implementation of {@link SleepStatus#sleepersNeeded(int)}.
     */
    @Override
    public int sleepersNeeded(int percentageRequired) {
        return Math.max(1, (int) Math.ceil(activePlayerCount * percentageRequired / 100.0D));
    }

    /**
     * Mimics the implementation of {@link SleepStatus#removeAllSleepers()}.
     */
    @Override
    public void removeAllSleepers() {
        sleepingPlayerCount = 0;
    }

    /**
     * Mimics the implementation of {@link SleepStatus#amountSleeping()}.
     */
    @Override
    public int amountSleeping() {
        return sleepingPlayerCount;
    }

    /**
     * Get the amount of players currently active (not spectating).
     *
     * @return the active player count
     */
    public int amountActive() {
        return activePlayerCount;
    }

    /**
     * {@return true when all players are awake, false otherwise}
     */
    public boolean allAwake() {
        return sleepingPlayerCount == 0;
    }

    /**
     * {@return true when all players are sleeping, false otherwise}
     */
    public boolean allAsleep() {
        return sleepingPlayerCount == activePlayerCount;
    }

    /**
     * {@return the ratio of sleeping players to active players. Value between 0.0 and 1.0}
     */
    public double getRatio() {
        return (double) sleepingPlayerCount / (double) activePlayerCount;
    }

    /**
     * Mimics the implementation of {@link SleepStatus#update(List)}, except blocks vanilla sleep
     * messages by returning false if {@link #preventSleepSupplier} returns true.
     */
    @Override
    public boolean update(List<ServerPlayer> playerList) {
        int oldActiveCount = activePlayerCount;
        int oldSleepingCount = sleepingPlayerCount;
        activePlayerCount = 0;
        sleepingPlayerCount = 0;

        for (ServerPlayer player : playerList) {
            if (!player.isSpectator()) {
                activePlayerCount++;
                if (player.isSleeping()) {
                    sleepingPlayerCount++;
                }
            }
        }

        if (preventSleepSupplier.get()) {
            return false;
        } else {
            boolean noSleepers = oldSleepingCount == 0 && sleepingPlayerCount == 0;
            boolean valueChanged = oldActiveCount != activePlayerCount || oldSleepingCount != sleepingPlayerCount;
            return !noSleepers && valueChanged;
        }
    }
}

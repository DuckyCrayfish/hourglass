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

package net.lavabucket.hourglass.wrappers;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;

/**
 * This class acts as a wrapper for {@link ClientLevel} to increase consistency between Minecraft
 * versions.
 *
 * Since the client-level class changes its name and package between different versions of
 * Minecraft, supporting different Minecraft versions would require modifications to any class that
 * imports or references {@link ClientLevel}. This class consolidates these variations into itself,
 * allowing other classes to depend on it instead.
 */
public class ClientLevelWrapper extends Wrapper<ClientLevel> {

    /**
     * Instantiates a new object.
     * @param level  the client level to wrap
     */
    public ClientLevelWrapper(LevelAccessor level) {
        super((ClientLevel) level);
    }

    /** {@return true if the 'daylight cycle' game rule is enabled in this level} */
    public boolean daylightRuleEnabled() {
        return this.get().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
    }

    /** {@return true if {@code level} is an instance of a client-level} */
    public static boolean isClientLevel(LevelAccessor level) {
        return level instanceof ClientLevel;
    }

}

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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ServerPlayerWrapper extends Wrapper<ServerPlayerEntity> {

    public static Class<ServerPlayerEntity> playerClass = ServerPlayerEntity.class;

    /**
     * Instantiates a new player wrapper.
     * @param player  the player to wrap
     */
    public ServerPlayerWrapper(PlayerEntity player) {
        super(playerClass.cast(player));
    }

    /**
     * Wraps the {@link ServerPlayerEntity#isSleeping()} method to allow for predicates that do not depend
     * on importing the server player class.
     *
     * @return the value of {@link ServerPlayerEntity#isSleeping()}
     */
    public boolean isSleeping() {
        return wrapped.isSleeping();
    }

    /** {@return the wrapped level this player is in} */
    public ServerLevelWrapper getLevel() {
        return new ServerLevelWrapper(get().level);
    }

}

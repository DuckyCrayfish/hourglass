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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerPlayerWrapper extends Wrapper<ServerPlayer> {

    public static Class<ServerPlayer> playerClass = ServerPlayer.class;

    /**
     * Instantiates a new player wrapper.
     * @param player  the player to wrap
     */
    public ServerPlayerWrapper(Player player) {
        super(playerClass.cast(player));
    }

    /**
     * Wraps the {@link ServerPlayer#isSleeping()} method to allow for predicates that do not depend
     * on importing the server player class.
     *
     * @return the value of {@link ServerPlayer#isSleeping()}
     */
    public boolean isSleeping() {
        return wrapped.isSleeping();
    }

    /** {@return the wrapped level this player is in} */
    public ServerLevelWrapper getLevel() {
        return new ServerLevelWrapper(get().level);
    }

}

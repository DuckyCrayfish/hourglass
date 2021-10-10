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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * This class acts as a wrapper for {@link ServerPlayer} to increase the consistency of the
 * Hourglass codebase between Minecraft versions.
 *
 * <p>Since the server player class changes its name and package between different versions of
 * Minecraft, supporting different Minecraft versions would require modifications to any class that
 * imports or references {@link ServerPlayer}. This class consolidates these variations so that
 * other classes may reliably import it instead.
 */
public class ServerPlayerWrapper extends Wrapper<ServerPlayer> {

    private static Method tickEffectsMethod = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_21217_");
    static { tickEffectsMethod.setAccessible(true); }

    /** The class that this {@code Wrapper} wraps. */
    public static Class<ServerPlayer> playerClass = ServerPlayer.class;

    /**
     * Instantiates a new player wrapper.
     * @param player  the player to wrap
     */
    public ServerPlayerWrapper(Player player) {
        super(playerClass.cast(player));
    }

    /**
     * Wraps the "sleeping" player predicate method to allow for predicates that do not depend on
     * importing the server player class.
     *
     * @return true if the player is sleeping, false otherwise
     */
    public boolean isSleeping() {
        return wrapped.isSleeping();
    }

    /** {@return the wrapped level this player is in} */
    public ServerLevelWrapper getLevel() {
        return new ServerLevelWrapper(get().level);
    }

    /** Ticks all MobEffects applied to this player. */
    public void tickEffects() {
        try {
            tickEffectsMethod.invoke(get());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            return;
        }
    }

    /** Sends update packets to this player for each of their active mob effects. */
    public void sendMobEffectUpdatePackets() {
        for (MobEffectInstance e : get().getActiveEffects()) {
            int id = get().getId();
            ClientboundUpdateMobEffectPacket packet = new ClientboundUpdateMobEffectPacket(id, e);
            get().connection.send(packet);
        }
    }

}

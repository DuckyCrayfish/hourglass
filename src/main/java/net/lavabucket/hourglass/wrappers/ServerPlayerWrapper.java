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

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * This class acts as a wrapper for {@link ServerPlayer} to increase the consistency of the
 * Hourglass codebase between Minecraft versions.
 *
 * <p>Since the server player class changes its name and package between different versions of
 * Minecraft, supporting different Minecraft versions would require modifications to any class that
 * imports or references {@link ServerPlayer}. This class consolidates these variations so that
 * other classes may reliably import it instead.
 */
public class ServerPlayerWrapper extends Wrapper<ServerPlayerEntity> {

    private static Method tickEffectsMethod = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "func_70679_bo");
    static { tickEffectsMethod.setAccessible(true); }

    /** The class that this {@code Wrapper} wraps. */
    public static Class<ServerPlayerEntity> playerClass = ServerPlayerEntity.class;

    /**
     * Instantiates a new player wrapper.
     * @param player  the player to wrap
     */
    public ServerPlayerWrapper(PlayerEntity player) {
        super(playerClass.cast(player));
    }

    /**
     * Wraps the "isSleeping" player method to allow for predicates that do not depend on importing
     * the server player class.
     *
     * @return true if the player is sleeping, false otherwise
     */
    public boolean isSleeping() {
        return wrapped.isSleeping();
    }

    /**
     * Wraps the "isSleepingLongEnough" player method to allow for predicates that do not depend on
     * importing the server player class.
     *
     * @return true if the player is sleeping long enough to pass night, false otherwise
     */
    public boolean isSleepingLongEnough() {
        return wrapped.isSleepingLongEnough();
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
        for (EffectInstance e : get().getActiveEffects()) {
            int id = get().getId();
            SPlayEntityEffectPacket packet = new SPlayEntityEffectPacket(id, e);
            get().connection.send(packet);
        }
    }

}

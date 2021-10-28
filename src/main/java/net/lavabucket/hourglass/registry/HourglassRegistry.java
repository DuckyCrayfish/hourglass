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

package net.lavabucket.hourglass.registry;

import net.lavabucket.hourglass.Hourglass;
import net.lavabucket.hourglass.notifications.target.NotificationTarget;
import net.lavabucket.hourglass.time.effects.TimeEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * This class holds the custom registries created by Hourglass.
 */
public final class HourglassRegistry {

    /** Registry for time effects. See {@link TimeEffect} for details on time effects. */
    public static IForgeRegistry<TimeEffect> TIME_EFFECT;
    /** Registry of {@code NotificationTarget} objects. */
    public static IForgeRegistry<NotificationTarget> NOTIFICATION_TARGET;

    /** Registry key for the {@link #TIME_EFFECT} registry. */
    public static final ResourceLocation TIME_EFFECT_KEY = new ResourceLocation(Hourglass.MOD_ID, "time_effect");
    /** Registry key for the {@link #NOTIFICATION_TARGET} registry. */
    public static final ResourceLocation NOTIFICATION_TARGET_KEY = new ResourceLocation(Hourglass.MOD_ID, "notification_target");

    /**
     * Creates all new registries in this class.
     * @param event  the event provided by the Forge event bus
     */
    @SubscribeEvent
    public static void newRegistryEvent(RegistryEvent.NewRegistry event) {
        TIME_EFFECT = new RegistryBuilder<TimeEffect>().setName(TIME_EFFECT_KEY).setType(TimeEffect.class).create();
        NOTIFICATION_TARGET = new RegistryBuilder<NotificationTarget>().setName(NOTIFICATION_TARGET_KEY).setType(NotificationTarget.class).create();
    }

}

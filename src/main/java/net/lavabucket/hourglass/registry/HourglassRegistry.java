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

import net.lavabucket.hourglass.HourglassMod;
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

    public static final ResourceLocation TIME_EFFECT_KEY = new ResourceLocation(HourglassMod.ID, "time_effect");

    /** Creates all new registries in this class. */
    @SubscribeEvent
    public static void newRegistryEvent(RegistryEvent.NewRegistry event) {
        TIME_EFFECT = new RegistryBuilder<TimeEffect>().setName(TIME_EFFECT_KEY).setType(TimeEffect.class).create();
    }

}

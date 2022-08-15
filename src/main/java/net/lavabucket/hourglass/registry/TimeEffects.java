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

import java.util.function.Supplier;

import net.lavabucket.hourglass.Hourglass;
import net.lavabucket.hourglass.time.effects.BlockEntityTimeEffect;
import net.lavabucket.hourglass.time.effects.HungerTimeEffect;
import net.lavabucket.hourglass.time.effects.PotionTimeEffect;
import net.lavabucket.hourglass.time.effects.RandomTickSleepEffect;
import net.lavabucket.hourglass.time.effects.TimeEffect;
import net.lavabucket.hourglass.time.effects.WeatherSleepEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class registers all of the first-party time effects that come with Hourglass.
 */
public final class TimeEffects {

    /** The resource key for the {@link #TIME_EFFECT} registry. */
    public static final ResourceLocation KEY = new ResourceLocation(Hourglass.MOD_ID, "time_effect");

    private static final DeferredRegister<TimeEffect> DEFERRED_REGISTRY = DeferredRegister.create(KEY, Hourglass.MOD_ID);

    /** Registry for time effects. See {@link TimeEffect} for details on time effects. */
    public static final Supplier<IForgeRegistry<TimeEffect>> REGISTRY = DEFERRED_REGISTRY.makeRegistry(RegistryBuilder::new);

    /** @see WeatherSleepEffect */
    public static final RegistryObject<TimeEffect> WEATHER_EFFECT = DEFERRED_REGISTRY.register("weather", WeatherSleepEffect::new);
    /** @see RandomTickSleepEffect */
    public static final RegistryObject<TimeEffect> RANDOM_TICK_EFFECT = DEFERRED_REGISTRY.register("random_tick", RandomTickSleepEffect::new);
    /** @see PotionTimeEffect */
    public static final RegistryObject<TimeEffect> POTION_EFFECT = DEFERRED_REGISTRY.register("potion", PotionTimeEffect::new);
    /** @see HungerTimeEffect */
    public static final RegistryObject<TimeEffect> HUNGER_EFFECT = DEFERRED_REGISTRY.register("hunger", HungerTimeEffect::new);
    /** @see BlockEntityTimeEffect */
    public static final RegistryObject<TimeEffect> BLOCK_ENTITY_EFFECT = DEFERRED_REGISTRY.register("block_entity", BlockEntityTimeEffect::new);

    /**
     * Registers {@link #DEFERRED_REGISTRY} to the mod bus for creation and entry registration.
     * @param event  the event, provided by the mod event bus
     */
    @SubscribeEvent
    public static void onConstructModEvent(FMLConstructModEvent event) {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DEFERRED_REGISTRY.register(modBus);
    }

    // Private constructor to prohibit instantiation.
    private TimeEffects() {}

}

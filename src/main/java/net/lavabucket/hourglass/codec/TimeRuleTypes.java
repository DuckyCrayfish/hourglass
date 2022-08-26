/*
 * Copyright (C) 2022 Nick Iacullo
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

package net.lavabucket.hourglass.codec;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.lavabucket.hourglass.Hourglass;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public final class TimeRuleTypes {

    public static final ResourceLocation KEY = new ResourceLocation(Hourglass.MOD_ID, "time_rule_types");

    public static final DeferredRegister<TimeRuleType<?>> DEFERRED_REGISTRY = DeferredRegister.create(KEY, Hourglass.MOD_ID);

    public static final RegistryBuilder<TimeRuleType<?>> BUILDER = (new RegistryBuilder<TimeRuleType<?>>()).disableSaving().disableSync();
    public static final Supplier<IForgeRegistry<TimeRuleType<?>>> REGISTRY = DEFERRED_REGISTRY.makeRegistry(() -> BUILDER);

    public static final RegistryObject<TimeRuleType<DefaultTimeRule>> DEFAULT = DEFERRED_REGISTRY.register("default", () -> () -> DefaultTimeRule.CODEC);
    public static final RegistryObject<TimeRuleType<SpeedTimeRule>> SPEED = DEFERRED_REGISTRY.register("speed", () -> () -> SpeedTimeRule.CODEC);
    public static final RegistryObject<TimeRuleType<AbsoluteTimeRule>> ABSOLUTE = DEFERRED_REGISTRY.register("absolute", () -> () -> AbsoluteTimeRule.CODEC);
    public static final RegistryObject<TimeRuleType<TimeRuleGroup>> GROUP = DEFERRED_REGISTRY.register("group", () -> () -> TimeRuleGroup.CODEC);
    public static final RegistryObject<TimeRuleType<SystemTimeRule>> SYSTEM = DEFERRED_REGISTRY.register("system", () -> () -> SystemTimeRule.CODEC);

    /**
     * Registers {@link #DEFERRED_REGISTRY} to the mod bus for creation and entry registration.
     * @param event  the event, provided by the mod event bus
     */
    @SubscribeEvent
    public static void onConstructModEvent(FMLConstructModEvent event) {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DEFERRED_REGISTRY.register(modBus);
    }

    public static interface TimeRuleType<T extends TimeRule> {

        Codec<T> codec();

    }

    // Private constructor to prohibit instantiation.
    private TimeRuleTypes() {}

}

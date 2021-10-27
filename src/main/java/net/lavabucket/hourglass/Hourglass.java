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

package net.lavabucket.hourglass;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import net.lavabucket.hourglass.command.HourglassCommand;
import net.lavabucket.hourglass.config.ConfigSynchronizer;
import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.network.NetworkHandler;
import net.lavabucket.hourglass.notifications.HourglassMessages;
import net.lavabucket.hourglass.registry.HourglassRegistry;
import net.lavabucket.hourglass.registry.NotificationTargets;
import net.lavabucket.hourglass.registry.TimeEffects;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** This class contains the mod entry point, as well as some constants related to the mod itself. */
@Mod(Hourglass.MOD_ID)
public final class Hourglass {

    /** Mod identifier. The value here should match an entry in the META-INF/mods.toml file. */
    public static final String MOD_ID = "hourglass";
    /** Log4j marker for Hourglass logs. */
    public static final Marker MARKER = MarkerManager.getMarker(MOD_ID);

    /** Mod entry point. */
    public Hourglass() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modBus.register(HourglassRegistry.class);
        modBus.register(NetworkHandler.class);
        modBus.register(HourglassConfig.class);
        modBus.register(ConfigSynchronizer.class);
        modBus.register(TimeEffects.class);
        modBus.register(NotificationTargets.class);

        forgeBus.register(TimeServiceManager.class);
        forgeBus.register(HourglassMessages.class);
        forgeBus.register(HourglassCommand.class);

        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> HourglassClient::new);
    }

}

/*
 * Copyright (C) 2021-2026 Nick Iacullo
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

import net.lavabucket.hourglass.command.HourglassCommand;
import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.message.HourglassMessages;
import net.lavabucket.hourglass.registry.TimeEffects;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

/** This class contains the mod entry point, as well as some constants related to the mod itself. */
@Mod(Hourglass.MOD_ID)
public class Hourglass {

    /** Mod identifier. The value here should match an entry in the META-INF/mods.toml file. */
    public static final String MOD_ID = "hourglass";

    /** Mod entry point. */
    public Hourglass(IEventBus modBus, ModContainer modContainer) {
        final IEventBus gameBus = NeoForge.EVENT_BUS;

        modContainer.registerConfig(ModConfig.Type.SERVER, HourglassConfig.SERVER_CONFIG.spec);
        modContainer.registerConfig(ModConfig.Type.CLIENT, HourglassConfig.CLIENT_CONFIG.spec);

        TimeEffects.register(modBus);

        gameBus.register(TimeServiceManager.class);
        gameBus.register(HourglassMessages.class);
        gameBus.register(HourglassCommand.class);
    }

}

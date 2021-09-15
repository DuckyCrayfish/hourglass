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

import net.lavabucket.hourglass.client.TimeInterpolator;
import net.lavabucket.hourglass.client.gui.ConfigScreen;
import net.lavabucket.hourglass.client.gui.SleepGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Entry point for client-specific features.
 *
 * <p>Placing client-specific initialization in a dedicated class is necessary to provide sufficient
 * client/server distribution separation. Failure to do this could crash a Minecraft server
 * distribution on startup as Java tries to load classes that do not exist.
 */
public class HourglassClient {

    /** Client-specific entry point. */
    public HourglassClient() {
        final ModLoadingContext context = ModLoadingContext.get();
        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        ConfigScreen.register(context);

        forgeBus.register(SleepGui.class);
        forgeBus.register(TimeInterpolator.class);
    }

}

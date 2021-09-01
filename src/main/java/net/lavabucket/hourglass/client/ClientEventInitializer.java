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

package net.lavabucket.hourglass.client;

import net.lavabucket.hourglass.client.gui.ConfigScreen;
import net.lavabucket.hourglass.client.gui.SleepGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;

/**
 * Initializes all client-only event listeners.
 *
 * Performing this in a dedicated class provides sufficient client/server distribution separation.
 */
public class ClientEventInitializer {

    public static void register() {
        final ModLoadingContext modLoadingContext = ModLoadingContext.get();
        final IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        ConfigScreen.register(modLoadingContext);

        forgeEventBus.register(SleepGui.class);
        forgeEventBus.register(TimeInterpolator.class);
    }

}

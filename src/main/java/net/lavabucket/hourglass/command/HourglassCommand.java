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

package net.lavabucket.hourglass.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This class is used to create and register all commands used in this mod with Forge.
 */
public final class HourglassCommand {

    /**
     * Register all commands. Called by Forge during a RegisterCommandsEvent.
     *
     * @param event  the RegisterCommandsEvent supplied by the event bus.
     */
    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> hourglassCommand = create();
        event.getDispatcher().register(hourglassCommand);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("hourglass")
                .requires(source -> source.hasPermission(2))
                .then(ConfigCommand.create())
                .then(QueryCommand.create());
    }

    // Private constructor to prohibit instantiation.
    private HourglassCommand() {}

}

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

import static net.minecraft.command.Commands.literal;

import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.lavabucket.hourglass.command.config.ConfigCommand;
import net.lavabucket.hourglass.command.config.ConfigCommandEntry;
import net.lavabucket.hourglass.config.ConfigSynchronizer;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This class is used to create and register all commands used in this mod with Forge.
 */
public class HourglassCommand {

    /**
     * Register all commands. Called by Forge during a RegisterCommandsEvent.
     *
     * @param event the RegisterCommandsEvent supplied by the event bus.
     */
    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event) {
        ConfigCommand configCommand = new ConfigCommand()
                .setQuerySuccessHandler(HourglassCommand::onQuerySuccess)
                .setModifySuccessHandler(HourglassCommand::onModifySuccess)
                .setModifyFailureHandler(HourglassCommand::onModifyFailure)
                .register(SERVER_CONFIG.daySpeed, DoubleArgumentType.doubleArg(0, 24000), Double.class)
                .register(SERVER_CONFIG.nightSpeed, DoubleArgumentType.doubleArg(0, 24000), Double.class)
                .register(SERVER_CONFIG.enableSleepFeature, BoolArgumentType.bool(), Boolean.class)
                .register(SERVER_CONFIG.sleepSpeedMin, DoubleArgumentType.doubleArg(0, 24000), Double.class)
                .register(SERVER_CONFIG.sleepSpeedMax, DoubleArgumentType.doubleArg(0, 24000), Double.class)
                .register(SERVER_CONFIG.sleepSpeedAll, DoubleArgumentType.doubleArg(-1, 24000), Double.class)
                .register(SERVER_CONFIG.accelerateWeather, BoolArgumentType.bool(), Boolean.class)
                .register(SERVER_CONFIG.clearWeatherOnWake, BoolArgumentType.bool(), Boolean.class)
                .register(SERVER_CONFIG.displayBedClock, BoolArgumentType.bool(), Boolean.class);

        event.getDispatcher().register(literal("hourglass")
                .requires(source -> source.hasPermission(2))
                .then(configCommand.build(literal("config"))));
    }

    /**
     * Handles a successful ConfigCommand query. Informs the user of the config value.
     *
     * @param <T>  the ConfigCommandEntry type
     * @param context  the context from the query command
     * @param entry  the entry that was queried by the user
     */
    public static <T> void onQuerySuccess(CommandContext<CommandSource> context, ConfigCommandEntry<T> entry) {

        ITextComponent response = new TranslationTextComponent("commands.hourglass.config.query",
                entry.getIdentifier(),
                entry.getConfigValue().get().toString());

        context.getSource().sendSuccess(response, false);
    }

    /**
     * Handles a successful ConfigCommand config modification. Informs the user of the modification.
     *
     * @param <T>  the ConfigCommandEntry type
     * @param context  the context from the modify command
     * @param entry  the entry that was modified by the user
     */
    public static <T> void onModifySuccess(CommandContext<CommandSource> context, ConfigCommandEntry<T> entry) {
        // Force a config sync, as the file watcher does not always catch the change. This may
        // cause the config update to send twice.
        ConfigSynchronizer.syncConfigWithClients();

        ITextComponent response = new TranslationTextComponent("commands.hourglass.config.set",
                entry.getIdentifier(),
                entry.getConfigValue().get());

        context.getSource().sendSuccess(response, true);
    }

    /**
     * Handles a failed ConfigCommand config modification. Informs the user of the failure.
     *
     * @param <T>  the ConfigCommandEntry type
     * @param context  the context from the modify command
     * @param entry  the entry that the user tried to modify
     */
    public static <T> void onModifyFailure(CommandContext<CommandSource> context, ConfigCommandEntry<T> entry) {
        ITextComponent response = new TranslationTextComponent("commands.hourglass.config.failure",
                entry.getIdentifier(),
                entry.getConfigValue().get());

        context.getSource().sendFailure(response);
    }

}

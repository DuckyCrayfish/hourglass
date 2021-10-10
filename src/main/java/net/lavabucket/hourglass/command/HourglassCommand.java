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

import static net.lavabucket.hourglass.config.HourglassConfig.SERVER_CONFIG;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.lavabucket.hourglass.command.config.ConfigCommand;
import net.lavabucket.hourglass.command.config.ConfigCommandEntry;
import net.lavabucket.hourglass.config.ConfigSynchronizer;
import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.lavabucket.hourglass.time.effects.EffectCondition;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * This class is used to create and register all commands used in this mod with Forge.
 */
public class HourglassCommand {

    // A default ArgumentType used for time speed arguments.
    private static final DoubleArgumentType TIME_SPEED_ARGUMENT =
            DoubleArgumentType.doubleArg(0, 24000);

    /**
     * Register all commands. Called by Forge during a RegisterCommandsEvent.
     *
     * @param event  the RegisterCommandsEvent supplied by the event bus.
     */
    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event) {
        ConfigCommand configCommand = new ConfigCommand()
                .setQuerySuccessHandler(HourglassCommand::onQuerySuccess)
                .setModifySuccessHandler(HourglassCommand::onModifySuccess)
                .setModifyFailureHandler(HourglassCommand::onModifyFailure)
                .register(SERVER_CONFIG.daySpeed, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.nightSpeed, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.enableSleepFeature)
                .register(SERVER_CONFIG.sleepSpeedMin, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.sleepSpeedMax, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.sleepSpeedAll, DoubleArgumentType.doubleArg(-1, 24000))
                .register(SERVER_CONFIG.sleepSpeedCurve, DoubleArgumentType.doubleArg(0, 1))
                .register(SERVER_CONFIG.clearWeatherOnWake)
                .register(SERVER_CONFIG.displayBedClock)
                .register(SERVER_CONFIG.allowDaySleep)
                .register(SERVER_CONFIG.weatherEffect, EffectCondition.class)
                .register(SERVER_CONFIG.randomTickEffect, EffectCondition.class)
                .register(SERVER_CONFIG.baseRandomTickSpeed, IntegerArgumentType.integer(0))
                .register(SERVER_CONFIG.potionEffect, EffectCondition.class)
                .register(SERVER_CONFIG.hungerEffect, EffectCondition.class)
                .register(SERVER_CONFIG.blockEntityEffect, EffectCondition.class);

        event.getDispatcher().register(
                Commands.literal("hourglass").requires(source -> source.hasPermission(2))

                    .then(configCommand.build(Commands.literal("config")))

                    .then(Commands.literal("query")
                        .then(Commands.literal("timeSpeed")
                            .executes(HourglassCommand::onTimeSpeedQuery))
                        .then(Commands.literal("sleeperCount")
                            .executes(HourglassCommand::onSleeperCountQuery))
                    )
                );
    }

    /**
     * Handles a successful ConfigCommand query. Informs the user of the config value.
     *
     * @param <T>  the ConfigCommandEntry type
     * @param context  the context from the query command
     * @param entry  the entry that was queried by the user
     */
    public static <T> void onQuerySuccess(CommandContext<CommandSourceStack> context, ConfigCommandEntry<T> entry) {

        TextWrapper response = TextWrapper.translation("commands.hourglass.config.query",
                entry.getIdentifier(),
                entry.getConfigValue().get().toString());

        context.getSource().sendSuccess(response.get(), false);
    }

    /**
     * Handles a successful ConfigCommand config modification. Informs the user of the modification.
     *
     * @param <T>  the ConfigCommandEntry type
     * @param context  the context from the modify command
     * @param entry  the entry that was modified by the user
     */
    public static <T> void onModifySuccess(CommandContext<CommandSourceStack> context, ConfigCommandEntry<T> entry) {
        // Force a config sync, as the file watcher does not always catch the change. This may
        // cause the config update to send twice.
        ConfigSynchronizer.syncConfigWithClients();

        TextWrapper response = TextWrapper.translation("commands.hourglass.config.set",
                entry.getIdentifier(),
                entry.getConfigValue().get());

        context.getSource().sendSuccess(response.get(), true);
    }

    /**
     * Handles a failed ConfigCommand config modification. Informs the user of the failure.
     *
     * @param <T>  the ConfigCommandEntry type
     * @param context  the context from the modify command
     * @param entry  the entry that the user tried to modify
     */
    public static <T> void onModifyFailure(CommandContext<CommandSourceStack> context, ConfigCommandEntry<T> entry) {
        TextWrapper response = TextWrapper.translation("commands.hourglass.config.failure",
                entry.getIdentifier(),
                entry.getConfigValue().get());

        context.getSource().sendFailure(response.get());
    }

    /**
     * Handles a time speed query command.
     * @param context  the command context
     */
    public static int onTimeSpeedQuery(CommandContext<CommandSourceStack> context) {
        ServerLevelWrapper wrapper = new ServerLevelWrapper(context.getSource().getLevel());
        TimeService service = TimeServiceManager.service;

        if (service == null || !service.managesLevel(wrapper)) {
            TextWrapper response = TextWrapper.translation(
                    "commands.hourglass.query.levelNotApplicable");
            context.getSource().sendFailure(response.get());
            return 0;
        }

        TextWrapper response = TextWrapper.translation(
                "commands.hourglass.query.timeSpeed.success",
                service.getTimeSpeed(service.getDayTime()));
        context.getSource().sendSuccess(response.get(), false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles a sleeper count query command.
     * @param context  the command context
     */
    public static int onSleeperCountQuery(CommandContext<CommandSourceStack> context) {
        ServerLevelWrapper wrapper = new ServerLevelWrapper(context.getSource().getLevel());
        TimeService service = TimeServiceManager.service;

        if (service == null || !service.managesLevel(wrapper)) {
            TextWrapper response = TextWrapper.translation(
                    "commands.hourglass.query.levelNotApplicable");
            context.getSource().sendFailure(response.get());
            return 0;
        }

        TextWrapper response = TextWrapper.translation(
                "commands.hourglass.query.sleeperCount.success",
                service.sleepStatus.percentage(),
                service.sleepStatus.amountSleeping(),
                service.sleepStatus.amountActive());
        context.getSource().sendSuccess(response.get(), false);
        return Command.SINGLE_SUCCESS;
    }

}

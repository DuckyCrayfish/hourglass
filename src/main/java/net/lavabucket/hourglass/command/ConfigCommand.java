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

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.lavabucket.hourglass.Hourglass;
import net.lavabucket.hourglass.command.config.ConfigCommandBuilder;
import net.lavabucket.hourglass.command.config.ConfigCommandEntry;
import net.lavabucket.hourglass.config.HourglassConfig;
import net.lavabucket.hourglass.time.effects.EffectCondition;
import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** A "config" command used to modify and query values of the Hourglass configuration. */
public final class ConfigCommand {

    // A default ArgumentType used for time speed arguments.
    private static final DoubleArgumentType TIME_SPEED_ARGUMENT =
            DoubleArgumentType.doubleArg(0, 24000);

    /**
     * Creates and returns a command tree builder for the Hourglass "config" command.
     * @return a command tree builder for the Hourglass "config" command
     */
    public static ArgumentBuilder<CommandSourceStack, ?> create() {
        return new ConfigCommandBuilder()
                .setQuerySuccessHandler(ConfigCommand::onQuerySuccess)
                .setModifySuccessHandler(ConfigCommand::onModifySuccess)
                .setModifyFailureHandler(ConfigCommand::onModifyFailure)
                .register(SERVER_CONFIG.timeRule, StringArgumentType.string(), String.class)
                .register(SERVER_CONFIG.daySpeed, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.nightSpeed, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.enableSleepFeature)
                .register(SERVER_CONFIG.sleepSpeedMin, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.sleepSpeedMax, TIME_SPEED_ARGUMENT)
                .register(SERVER_CONFIG.sleepSpeedAll, DoubleArgumentType.doubleArg(-1, 24000))
                .register(SERVER_CONFIG.sleepSpeedCurve, DoubleArgumentType.doubleArg(0, 1))
                .register(SERVER_CONFIG.clearWeatherOnWake)
                .register(SERVER_CONFIG.allowBedClock)
                .register(SERVER_CONFIG.allowDaySleep)
                .register(SERVER_CONFIG.weatherEffect, EffectCondition.class)
                .register(SERVER_CONFIG.randomTickEffect, EffectCondition.class)
                .register(SERVER_CONFIG.baseRandomTickSpeed, IntegerArgumentType.integer(0))
                .register(SERVER_CONFIG.potionEffect, EffectCondition.class)
                .register(SERVER_CONFIG.hungerEffect, EffectCondition.class)
                .register(SERVER_CONFIG.blockEntityEffect, EffectCondition.class)
                .build(Commands.literal("config"));
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
        HourglassConfig.SYNCHRONIZER.sync(Hourglass.MOD_ID);

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

    // Private constructor to prohibit instantiation.
    private ConfigCommand() {}

}

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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.time.TimeServiceManager;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.TextWrapper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** A "query" command used to query the current state of Hourglass-related properties. */
public final class QueryCommand {

    /**
     * Creates and returns a command tree builder for the Hourglass "query" command.
     * @return a command tree builder for the Hourglass "query" command
     */
    public static ArgumentBuilder<CommandSourceStack, ?> create() {
        return Commands.literal("query")
                .then(Commands.literal("timeSpeed").executes(QueryCommand::onTimeSpeedQuery))
                .then(Commands.literal("sleeperCount").executes(QueryCommand::onSleeperCountQuery));
    }

    /**
     * Handles a time speed query command.
     * @param context  the command context
     * @return 1 for success, 0 for failure
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
                service.getTimeDelta());
        context.getSource().sendSuccess(response.get(), false);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Handles a sleeper count query command.
     * @param context  the command context
     * @return 1 for success, 0 for failure
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

    // Private constructor to prohibit instantiation.
    private QueryCommand() {}

}

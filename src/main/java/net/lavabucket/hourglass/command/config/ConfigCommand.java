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

package net.lavabucket.hourglass.command.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.server.command.EnumArgument;

/**
 * This class is used to create a command tree for modifying forge configuration values. It was
 * written intentionally vague with the hope of eventually making it more extensible and turning
 * it into a library.
 */
public class ConfigCommand {

    private static final Logger LOGGER = LogManager.getLogger();

    protected final Map<String, ConfigCommandEntry<?>> entries;
    protected BiConsumer<CommandContext<CommandSourceStack>, ConfigCommandEntry<?>> querySuccessHandler;
    protected BiConsumer<CommandContext<CommandSourceStack>, ConfigCommandEntry<?>> modifySuccessHandler;
    protected BiConsumer<CommandContext<CommandSourceStack>, ConfigCommandEntry<?>> modifyFailureHandler;

    /** Creates a new instance. */
    public ConfigCommand() {
        entries = new HashMap<>();
    }

    /**
     * Registers an {@code IntValue} to the command using a default {@link IntegerArgumentType} as
     * the value parser and {@code Integer} as the underlying data type.
     *
     * @param intConfigValue the {@code IntValue} config value to register
     * @return this, for chaining
     */
    public ConfigCommand register(IntValue intConfigValue) {
        return register(intConfigValue, IntegerArgumentType.integer(), Integer.class);
    }

    /**
     * Registers an {@code IntValue} to the command using {@code argumentType} as the value parser
     * and {@code Integer} as the underlying data type.
     *
     * @param intConfigValue the {@code IntValue} config value to register
     * @param argumentType  the {@code ArgumentType} used to parse the value in the 'modify' command
     * @return this, for chaining
     */
    public ConfigCommand register(IntValue intConfigValue, ArgumentType<Integer> argumentType) {
        return register(intConfigValue, argumentType, Integer.class);
    }

    /**
     * Registers a {@code DoubleValue} to the command using a default {@link DoubleArgumentType} as
     * the value parser and {@code Double} as the underlying data type.
     *
     * @param doubleConfigValue the {@code DoubleValue} config value to register
     * @return this, for chaining
     */
    public ConfigCommand register(DoubleValue doubleConfigValue) {
        return register(doubleConfigValue, DoubleArgumentType.doubleArg(), Double.class);
    }

    /**
     * Registers a {@code DoubleValue} to the command using {@code argumentType} as the value parser
     * and {@code Double} as the underlying data type.
     *
     * @param doubleConfigValue the {@code DoubleValue} config value to register
     * @param argumentType  the {@code ArgumentType} used to parse the value in the 'modify' command
     * @return this, for chaining
     */
    public ConfigCommand register(DoubleValue doubleConfigValue, ArgumentType<Double> argumentType) {
        return register(doubleConfigValue, argumentType, Double.class);
    }

    /**
     * Registers a {@code BooleanValue} to the command using a default {@link BoolArgumentType} as
     * the value parser and {@code Boolean} as the underlying data type.
     *
     * @param booleanConfigValue  the {@code BooleanValue} config value to register
     * @return this, for chaining
     */
    public ConfigCommand register(BooleanValue booleanConfigValue) {
        return register(booleanConfigValue, BoolArgumentType.bool(), Boolean.class);
    }

    /**
     * Registers an {@code EnumValue} to the command using a default {@link EnumArgument} as the
     * value parser and {@code valueClass} as the underlying data type.
     *
     * @param enumConfigValue  the {@code EnumValue} config value to register
     * @param valueClass  the underlying enum class of the config value
     * @return this, for chaining
     */
    public <T extends Enum<T>> ConfigCommand register(EnumValue<T> enumConfigValue, Class<T> valueClass) {
        return register(enumConfigValue, EnumArgument.enumArgument(valueClass), valueClass);
    }

    /**
     * Registers a new {@code ConfigValue} to the command whose value is parsed by
     * {@code argumentType}.
     *
     * @param <T>  the underlying data type of the config value and argument type
     * @param configValue  the {@code ConfigValue} to register
     * @param argumentType  the {@code ArgumentType} used to parse the value in the 'modify' command
     * @param valueClass  the underlying data class of the config value and argument type
     * @return this, for chaining
     */
    public <T> ConfigCommand register(ConfigValue<T> configValue, ArgumentType<T> argumentType, Class<T> valueClass) {
        ConfigCommandEntry<T> entry = new ConfigCommandEntry<>(configValue, argumentType, valueClass);
        return register(entry);
    }

    /**
     * Registers a new {@link ConfigCommandEntry} in the command.
     *
     * @param <T>  the data type of the underlying config data
     * @param entry  the entry to register
     * @return this, for chaining
     */
    public <T> ConfigCommand register(ConfigCommandEntry<T> entry) {
        entries.put(entry.getIdentifier(), entry);
        return this;
    }

    /**
     * Sets the consumer that is called after the query command is successfully called.
     * This consumer should inform the user of the current config value.
     *
     * @param listener  this handler to set
     * @return this, for chaining
     */
    public ConfigCommand setQuerySuccessHandler(
            BiConsumer<CommandContext<CommandSourceStack>, ConfigCommandEntry<?>> listener) {
        this.querySuccessHandler = listener;
        return this;
    }

    /**
     * Sets the consumer that is called after the modify command is successfully called.
     * This consumer should inform the user of the new config value.
     *
     * @param listener  this handler to set
     * @return this, for chaining
     */
    public ConfigCommand setModifySuccessHandler(
            BiConsumer<CommandContext<CommandSourceStack>, ConfigCommandEntry<?>> listener) {
        this.modifySuccessHandler = listener;
        return this;
    }

    /**
     * Sets the consumer that is called after the modify command failed.
     * This consumer should inform the user of the failure.
     *
     * @param listener  this handler to set
     * @return this, for chaining
     */
    public ConfigCommand setModifyFailureHandler(
            BiConsumer<CommandContext<CommandSourceStack>, ConfigCommandEntry<?>> listener) {
        this.modifyFailureHandler = listener;
        return this;
    }

    /**
     * Builds the Config Command off of the specified parent builder node. Command handlers should
     * be defined before this is called.
     *
     * @param parent  the parent to build config commands off of
     * @return the parent, for additional chaining
     */
    public ArgumentBuilder<CommandSourceStack, ?> build(ArgumentBuilder<CommandSourceStack, ?> parent) {

        for (ConfigCommandEntry<?> entry : entries.values()) {
            parent.then(Commands.literal(entry.getIdentifier())
                    .then(entry.createArgument()
                        .executes(context -> this.modifyConfigCommand(context, entry)))
                    .executes(context -> this.queryConfigCommand(context, entry)));
        }

        return parent;
    }

    /**
     * Command handler that is executed during a 'config query' command.
     *
     * Fetches the current value of the {@link ConfigValue} stored in {@code entry} and prints it to
     * the chat.
     *
     * @param <T>  the underlying data class of the config and argument
     * @param context  the command context from the executing command
     * @param entry  the entry holding the config value to be retrieved
     * @return 1 if a success handler was successfully called, 0 otherwise
     */
    protected <T> int queryConfigCommand(CommandContext<CommandSourceStack> context,
            ConfigCommandEntry<T> entry) {
        if (this.querySuccessHandler != null) {
            this.querySuccessHandler.accept(context, entry);
            return Command.SINGLE_SUCCESS;
        } else {
            return 0;
        }
    }

    /**
     * Command handler that is executed during a 'config setter' command.
     *
     * Retrieves the new config value as an argument from the command context, sets the
     * {@link ConfigValue} stored in {@code entry}, and then sends a config update message to all connected
     * clients.
     *
     * @param <T>  the underlying data class of the config and argument
     * @param context  the command context from the executing command
     * @param entry  the entry holding the config value to be changed
     * @return 1 if successful, 0 otherwise
     */
    protected <T> int modifyConfigCommand(CommandContext<CommandSourceStack> context,
            ConfigCommandEntry<T> entry) {

        T argument;
        try {
            argument = entry.getArgument(context);
        } catch (IllegalArgumentException e) {
            if (this.modifyFailureHandler != null) {
                LOGGER.error("Command failed to fetch config argument.", e);
                this.modifyFailureHandler.accept(context, entry);
                return 0;
            }
            throw e;
        }

        try {
            entry.getConfigValue().set(argument);
        } catch(Exception e) {
            if (this.modifyFailureHandler != null) {
                LOGGER.error("Command failed to set config to value: " + argument, e);
                this.modifyFailureHandler.accept(context, entry);
                return 0;
            }
            throw e;
        }

        if (this.modifySuccessHandler != null) {
            this.modifySuccessHandler.accept(context, entry);
        }
        return Command.SINGLE_SUCCESS;
    }

}

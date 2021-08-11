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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

/**
 * This is a holder class stores a ConfigValue and the ArgumentType used to control it via
 * commands. The underlying data class needs to be provided to retrieve the argument from the
 * command context, as this generic information is not stored at runtime.
 *
 * This class holds an identifier which is used by the config command to differentiate between
 * configs.
 */
public class ConfigCommandEntry<T> {

    protected ConfigValue<T> configValue;
    protected ArgumentType<T> argumentType;
    protected Class<T> valueClass;
    private String identifier;

    /**
     * Creates a new instance that will use the last node of the config path as an identifier.
     *
     * @param configValue  the ConfigValue that this command entry is based on
     * @param argumentType  the ArgumentType used to parse entered data for config modification
     * @param valueClass  the underlying data class for both configValue and argumentType
     */
    public ConfigCommandEntry(ConfigValue<T> configValue, ArgumentType<T> argumentType, Class<T> valueClass) {
        this(configValue, argumentType, valueClass, false);
    }

    /**
     * Creates a new instance that will use full paths delimited by '.' as an identifier if
     * useFullPath is true, or uses the last node of config paths if false.
     *
     * @param configValue  the ConfigValue that this command entry is based on
     * @param argumentType  the ArgumentType used to parse entered data for config modification
     * @param valueClass  the underlying data class for both configValue and argumentType
     * @param useFullPath  If true, this object's identifier will be set to the full path of
     * configValue, joined by a period. If false, this object's identifier will be set to the
     * last node of the path of configValue.
     */
    public ConfigCommandEntry(ConfigValue<T> configValue, ArgumentType<T> argumentType, Class<T> valueClass, boolean useFullPath) {
        this.configValue = configValue;
        this.argumentType = argumentType;
        this.valueClass = valueClass;
        if (useFullPath) {
            this.identifier = String.join(".", configValue.getPath());
        } else {
            this.identifier = configValue.getPath().get(configValue.getPath().size() - 1);
        }
    }

    /**
     * Creates a new instance.
     *
     * @param configValue  the ConfigValue that this command entry is based on
     * @param argumentType  the ArgumentType used to parse entered data for config modification
     * @param valueClass  the underlying data class for both configValue and argumentType
     * @param identifier  the string used to identify and refer to this entry in the config
     * command. The value used here will be the value that users will type to set or query this
     * config.
     */
    public ConfigCommandEntry(ConfigValue<T> configValue, ArgumentType<T> argumentType, Class<T> valueClass, String identifier) {
        this.configValue = configValue;
        this.argumentType = argumentType;
        this.valueClass = valueClass;
        this.identifier = identifier;
    }

    /**
     * @return the configValue
     */
    public ConfigValue<T> getConfigValue() {
        return configValue;
    }

    /**
     * @return the argumentType
     */
    public ArgumentType<T> getArgumentType() {
        return argumentType;
    }

    /**
     * @return the valueClass
     */
    public Class<T> getValueClass() {
        return valueClass;
    }

    /**
     * Creates an argument to be appended to an ArgumentBuilder.
     *
     * @return an ArgumentBuilder, for chaining
     */
    public RequiredArgumentBuilder<CommandSource, T> createArgument() {
        return Commands.argument(this.getArgumentName(), this.getArgumentType());
    }

    /**
     * Fetches the argument value from a command execution context.
     * @param context the command context from a running command
     * @return the argument value
     */
    public T getArgument(CommandContext<?> context){
        return context.getArgument(this.getArgumentName(), this.getValueClass());
    }

    /**
     * Returns this entry's identifier used to refer to this config value in commands.
     *
     * @return this entry's identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Create an argument name using the ConfigValue path.
     * @return
     */
    public String getArgumentName() {
        return "value";
    }
}

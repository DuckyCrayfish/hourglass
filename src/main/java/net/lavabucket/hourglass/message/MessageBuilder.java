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

package net.lavabucket.hourglass.message;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import net.lavabucket.hourglass.wrappers.TextWrapper;

/**
 * Message builder for Hourglass notifications, which allow for customizable targets and variable
 * substitution.
 */
public class MessageBuilder {

    private final LinkedHashMap<String, Object> variables = new LinkedHashMap<>();

    /** {@return the substitution variable map} */
    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Sets a variable to be substituted in the template message.
     *
     * <p>All text components values are properly converted to strings.
     *
     * <p>When this message is built using {@link #buildFromTemplate(String)}, {@code key} is used
     * as the variable lookup key. When using this method, variables may be inserted into the
     * message by using the format {@code ${<key>}}.
     *
     * <p>When this message is built using {@link #buildFromTranslation(String)}, {@code index}
     * is is used as the variable lookup key, where the {@code index} is a number starting at 0 that
     * increments every time a new variable is added. For example, the first variable has an index
     * of 0, the second one has an index of 1, etc. When using this method, variables may be
     * inserted into the message using the format {@code {<index>}}. Alternatively, Minecraft's
     * built-in variable substitution syntax may be used, for example "%s".
     *
     * @param key  the variable name to search for in the template
     * @param value  the value to inject into the message
     * @return this, for chaining
     */
    public MessageBuilder setVariable(String key, Object value) {
        if (TextWrapper.isText(value)){
            value = TextWrapper.from(value).get().getString();
        }
        this.variables.put(key, value);
        return this;
    }

    /**
     * Bakes the variables a new message by directly injecting them into {@code template}.
     *
     * <p>Variable substitution takes the form of {@code ${key}} when using this method. See
     * {@link #setVariable(String, String)} for more info.
     *
     * @param template  the message string to inject
     * @return the wrapped text component of the message
     */
    public TextWrapper buildFromTemplate(String template) {
        StrSubstitutor substitutor = new StrSubstitutor(new VariableLookup());
        String message = substitutor.replace(template);

        return TextWrapper.literal(message);
    }

    /**
     * Bakes the variables into a new translatable message using {@code key} as the translation key.
     *
     * <p>Variable substitution takes the form of {@code {index}} when using this method, where
     * {@code index} refers to the order in which variables were added to this builder.
     *
     * @param key  the key to use for translation lookup
     * @return the wrapped text component of the message
     */
    public TextWrapper buildFromTranslation(String key) {
        return TextWrapper.translation(key, variables.values().toArray());
    }

    // Maps variables to their String values
    private class VariableLookup extends AbstractLookup {

        @Override
        public String lookup(LogEvent event, String key) {
            Object value = MessageBuilder.this.getVariables().get(key);
            return value == null ? null : value.toString();
        }

    }

}

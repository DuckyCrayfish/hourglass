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

package net.lavabucket.hourglass.notifications.textbuilder;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import net.lavabucket.hourglass.wrappers.TextWrapper;

/**
 * A {@code MessageBuilder} class that uses a template string literal as the basis for
 * baking variables into a final message.
 *
 * <p>When baking the message, variables are substituted into the template string using the
 * key provided in {@link #setVariable(String, Object)} as the lookup key. Variables can be
 * referenced for substitution in the template using the following format: {@code ${<key>}}.
 */
public class TemplateTextBuilder extends TextBuilder {

    private String template;

    /**
     * Creates a new instance.
     * @param template  the message template literal used for baking variables
     */
    public TemplateTextBuilder(String template) {
        this.template = template;
    }

    /**
     * Bakes the variables a new message by directly injecting them into the template provided
     * during object construction.
     *
     * <p>Variable substitution takes the form of {@code ${key}}.
     *
     * @return the wrapped text component of the message
     * @see TemplateTextBuilder
     */
    @Override
    public TextWrapper build() {
        StrSubstitutor substitutor = new StrSubstitutor(new VariableLookup());
        String message = substitutor.replace(template);

        return TextWrapper.literal(message);
    }

    // Maps variables to their String values
    private class VariableLookup extends AbstractLookup {

        @Override
        public String lookup(LogEvent event, String key) {
            Object value = TemplateTextBuilder.this.getVariables().get(key);
            return value == null ? null : value.toString();
        }

    }

}

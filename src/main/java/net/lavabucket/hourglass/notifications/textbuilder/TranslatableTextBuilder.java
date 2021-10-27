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

import net.lavabucket.hourglass.wrappers.TextWrapper;

/**
 * A {@code MessageBuilder} that uses a translation key to look up the base message from the
 * local language file. The message is then formatted using Forge and Minecraft's built-in
 * variable substitution mechanism. When using Forge's {@code {index}} format to specify a
 * variable, the index is the zero-based index of the order in which the variable was added to
 * the builder. For example, if the builder contains two variables, {@code {0}} will be
 * substituted with the first variable that was added to the builder, and {@code {1}} will be
 * substituted with the second. Alternatively, Minecraft's built-in variable substitution syntax
 * may be used, for example "%s".
 */
public class TranslatableTextBuilder extends TextBuilder {

    /** The translation key used to look up the translated message content. */
    protected String translationKey;

    /**
     * Instantiates a new builder.
     * @param translationKey  the translation key used to look up the translated message content
     */
    public TranslatableTextBuilder(String translationKey) {
        this.translationKey = translationKey;
    }

    /**
     * Bakes the variables into a new translatable message using the translation key provided
     * during object construction.
     *
     * <p>Variable substitution takes the form of {@code {index}} when using this method, where
     * {@code index} refers to the order in which variables were added to this builder.
     *
     * @return the wrapped text component of the message
     */
    @Override
    public TextWrapper build() {
        return TextWrapper.translation(translationKey, variables.values().toArray());
    }

}

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

import java.util.LinkedHashMap;

import net.lavabucket.hourglass.wrappers.TextWrapper;

/** Message builder for Hourglass notifications that allow for variable substitution. */
public abstract class TextBuilder {

    /** The substitution variable map. */
    protected final LinkedHashMap<String, Object> variables = new LinkedHashMap<>();

    /** {@return the substitution variable map} */
    public LinkedHashMap<String, Object> getVariables() {
        return variables;
    }

    /**
     * Adds a variable to the substitution list for this builder.
     *
     * <p>All text components values are properly converted to strings.
     *
     * @param key  the variable name to search for in the template
     * @param value  the value to inject into the message
     * @return this, for chaining
     */
    public TextBuilder setVariable(String key, Object value) {
        if (TextWrapper.isText(value)){
            value = TextWrapper.from(value).get().getString();
        }
        this.variables.put(key, value);
        return this;
    }

    public abstract TextWrapper build();

}

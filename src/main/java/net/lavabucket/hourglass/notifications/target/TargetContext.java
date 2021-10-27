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

package net.lavabucket.hourglass.notifications.target;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * This class is used for storing contextual data used by {@code NotificationTarget}s retrieve
 * the correct matching players for a notification.
*/
public class TargetContext {

    /** Message context parameters. */
    protected final ImmutableMap<TargetParam<?>, Object> params;

    /**
     * Instantiates a new object.
     * @param params  context parameters
     */
    public TargetContext(Map<TargetParam<?>, Object> params) {
        this.params = ImmutableMap.copyOf(params);
    }

    /** {@return true if this context has a parameter identified by {@code key}, false otherwise} */
    public boolean hasParam(TargetParam<?> key) {
        return this.params.containsKey(key);
    }

    /**
     * Returns the context parameter identified by {@code key}.
     *
     * @param key  the parameter key
     * @return the parameter value, or null if the parameter does not exist
     */
    @SuppressWarnings("unchecked")
    public <T> T getParam(TargetParam<T> key) {
        return (T) this.params.get(key);
    }

    public ImmutableMap<TargetParam<?>, Object> getParams() {
        return this.params;
    }

    /** A {@code NotificationContext} builder for cleaner object construction. */
    public static class Builder {

        private final Map<TargetParam<?>, Object> params = new HashMap<>();

        /**
         * Adds a parameter to include in the {@code NotificationContext} object.
         * @param key  the parameter key
         * @param value  the value of the parameter
         * @return this, for chaining
         */
        public <T> TargetContext.Builder addParameter(TargetParam<T> key, T value) {
            this.params.put(key, value);
            return this;
        }

        /**
         * Creates a new {@code NotificationContext} object using the provided parameters.
         * @return the new {@code NotificationContext} object
         */
        public TargetContext create() {
            return new TargetContext(params);
        }

    }

}

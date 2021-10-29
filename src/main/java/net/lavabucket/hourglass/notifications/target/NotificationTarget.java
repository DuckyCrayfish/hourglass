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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Objects of this class represent an algorithm for retrieving a target subset of players using some
 * provided context.
 */
public class NotificationTarget extends ForgeRegistryEntry<NotificationTarget> {

    /** The context parameters required by this object to find matches. */
    protected final ImmutableSet<TargetParam<?>> requiredParams;
    /** The player retrieving algorithm. */
    protected final Function<TargetContext, Stream<ServerPlayerWrapper>> function;

    /**
     * Creates a new instance.
     * @param requiredParams  the required context parameters for this target
     * @param function  the function used to fetch players using a given {@code NotificationContext}
     */
    public NotificationTarget(Set<TargetParam<?>> requiredParams,
            Function<TargetContext, Stream<ServerPlayerWrapper>> function) {
        this.requiredParams = ImmutableSet.copyOf(requiredParams);
        this.function = function;
    }

    /**
     * {@return true if {@code params} contains all required parameters, false otherwise}
     * @param params  the params to check
     */
    public boolean hasRequiredParams(Collection<TargetParam<?>> params) {
        return params.containsAll(requiredParams);
    }

    /**
     * {@return the set of required parameters not contained in {@code context}}
     * @param context  the context whose parameters are checked
     */
    public Set<TargetParam<?>> getMissingParams(TargetContext context) {
        return Sets.difference(requiredParams, context.getParams().keySet());
    }

    /** {@return the parameters required by this target} */
    public Set<TargetParam<?>> getRequiredParams() {
        return requiredParams;
    }

    /**
     * Returns players that match this target type with the given context.
     * @param context  the {@code NotificationContext} to use for matching
     * @return a {@code Stream} of players that match this target type
     */
    public Stream<ServerPlayerWrapper> findMatches(TargetContext context) {
        if (!hasRequiredParams(context.getParams().keySet())) {
            Set<TargetParam<?>> missing = getMissingParams(context);
            throw new IllegalArgumentException("Notification target context is missing the following params: " + missing);
        }
        return function.apply(context);
    }

    /** Builder class for {@code NotificationTarget} objects. */
    public static class Builder {

        private Set<TargetParam<?>> requiredParams = new HashSet<>();
        private Function<TargetContext, Stream<ServerPlayerWrapper>> function;

        /**
         * Adds {@code param} to the set of context parameters required by this target.
         *
         * @param param  the {@code TargetParam} to add
         * @return this {@code Builder} object
         */
        public Builder requires(TargetParam<?> param) {
            requiredParams.add(param);
            return this;
        }

        /**
         * Sets the {@code Function} used to fetch targeted players using some given
         * {@code NotificationContext} object.
         *
         * @param function  the {@code Function} to use
         * @return this {@code Builder} object
         */
        public Builder function(Function<TargetContext, Stream<ServerPlayerWrapper>> function) {
            this.function = function;
            return this;
        }

        /** {@return a new {@code NotificationTarget} object using the provided parameters} */
        public NotificationTarget create() {
            return new NotificationTarget(requiredParams, function);
        }

    }

}

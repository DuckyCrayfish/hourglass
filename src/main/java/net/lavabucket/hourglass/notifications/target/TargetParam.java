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

import net.lavabucket.hourglass.time.TimeService;
import net.lavabucket.hourglass.wrappers.ServerLevelWrapper;
import net.lavabucket.hourglass.wrappers.ServerPlayerWrapper;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is used as a key to identify notification context parameters.
 *
 * <p>A collection of default parameter types are provided in this class.
 */
public class TargetParam<T> {

    /** The level associated with the notification. */
    public static final TargetParam<ServerLevelWrapper> LEVEL = create("level");
    /** The player associated with the notification. */
    public static final TargetParam<ServerPlayerWrapper> PLAYER = create("player");
    /** The time service for the level associated with the notification. */
    public static final TargetParam<TimeService> TIME_SERVICE = create("time_service");

    /** Creates a new {@code TargetPara} with the given name. */
    private static <T> TargetParam<T> create(String name) {
        return new TargetParam<T>(new ResourceLocation("hourglass", name));
    }

    private final ResourceLocation name;

    /**
     * Creates a new instance.
     * @param name  the identifier of the {@code TargetParam}
     */
    public TargetParam(ResourceLocation name) {
        this.name = name;
    }

    /** {@return the identifier of this {@code TargetParam}} */
    public ResourceLocation getName() {
        return this.name;
    }

    /** {@return a string representation of this parameter} */
    @Override
    public String toString() {
        return "<parameter " + this.name + ">";
    }

}

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

package net.lavabucket.hourglass.time.effects;

/**
 * Time effect setting that controls when an effect is applied.
 *
 * This enum allows time time effect conditions to be configured by users.
 */
public enum EffectCondition {

    /** Do not apply the effect. */
    NEVER,
    /** Always apply the effect. */
    ALWAYS,
    /** Only apply the effect while players are sleeping. */
    SLEEPING

}

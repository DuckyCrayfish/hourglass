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

package net.lavabucket.hourglass.utils;

/**
 * Math utility class.
 *
 * One purpose of this class is to prevent dependance on Minecraft's math class, as its package
 * name changes between versions.
 */
public final class MathUtils {

    // Private constructor to prevent instantiation.
    private MathUtils() {}

    /**
     * Returns a value from the range of numbers between {@code d0} and {@code d1}. The value's
     * position in this range is proportional to the argument {@code percent} as a fraction.
     *
     * @param percent  the fraction of the distance between {@code d0} and {@code d1} to sample
     * @param d0  the first edge of the range
     * @param d1  the second edge of the range
     * @return a value from the range of numbers between {@code d0} and {@code d1}
     */
    public static double lerp(double percent, double d0, double d1) {
        return d0 + percent * (d1 - d0);
    }

    /**
     * Maps a number {@code x} in the unit interval [0,1] to a number on a normalized sigmoid
     * function with a variable curvature defined by {@code c}.
     *
     * @param x  the number to map
     * @param c  the curvature variable
     * @return the number corresponding to {@code x} in the sigmoid function
     */
    public static double normalizedTunableSigmoid(double x, double c) {
        return c*x / (2*c*x - c - x + 1);
    }

}

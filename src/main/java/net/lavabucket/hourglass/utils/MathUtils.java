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
public class MathUtils {

    /**
     * Returns a value {@code percent} from the linear interpolation between {@code d0} and
     * {@code d1}.
     */
   public static double lerp(double percent, double d0, double d1) {
      return d0 + percent * (d1 - d0);
   }

}

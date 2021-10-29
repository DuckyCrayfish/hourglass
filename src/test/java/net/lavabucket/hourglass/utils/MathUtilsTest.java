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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MathUtilsTest {

    @Test
    void testLerp_Positive_Beginning() {
        assertEquals(0, MathUtils.lerp(0, 0, 1));
    }

    @Test
    void testLerp_Positive_Middle() {
        assertEquals(0.5, MathUtils.lerp(0.5, 0, 1));
    }

    @Test
    void testLerp_Positive_End() {
        assertEquals(1, MathUtils.lerp(1, 0, 1));
    }

    @Test
    void testLerp_Negative_Beginning() {
        assertEquals(0, MathUtils.lerp(0, 0, -1));
    }
    @Test
    void testLerp_Negative_Middle() {
        assertEquals(-0.5, MathUtils.lerp(0.5, 0, -1));
    }
    @Test
    void testLerp_Negative_End() {
        assertEquals(-1, MathUtils.lerp(1, 0, -1));
    }

    @Test
    void testLerp_OverZero_Middle() {
        assertEquals(0, MathUtils.lerp(0.5, -1, 1));
    }

    @Test
    void testNormalizedTunableSigmoid_Linear_Beginning() {
        assertEquals(0, MathUtils.normalizedTunableSigmoid(0, 0.5));
    }

    @Test
    void testNormalizedTunableSigmoid_Linear_Middle() {
        assertEquals(0.5, MathUtils.normalizedTunableSigmoid(0.5, 0.5));
    }

    @Test
    void testNormalizedTunableSigmoid_Linear_End() {
        assertEquals(1, MathUtils.normalizedTunableSigmoid(1, 0.5));
    }

    @Test
    void testNormalizedTunableSigmoid_Horizontal_Middle() {
        assertEquals(0, MathUtils.normalizedTunableSigmoid(0.5, 0));
    }

    @Test
    void testNormalizedTunableSigmoid_Vertical_Middle() {
        assertEquals(1, MathUtils.normalizedTunableSigmoid(0.5, 1));
    }

}

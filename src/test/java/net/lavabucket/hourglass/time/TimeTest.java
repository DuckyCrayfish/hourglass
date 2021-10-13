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

package net.lavabucket.hourglass.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TimeTest {

    private final Time zero = new Time(0);

    private final Time morning = new Time(0);
    private final Time morningTomorrow = new Time(24000);

    private final Time noon = new Time(6000);
    private final Time noonTomorrow = new Time(30000);
    private final Time noonNegative = new Time(-6000);

    @Test
    void testAbs_Zero() {
        assertEquals(new Time(0), zero.abs());
    }

    @Test
    void testAbs_Positive() {
        assertEquals(noon, noon.abs());
    }

    @Test
    void testAbs_Negative() {
        assertEquals(noon, noonNegative.abs());
    }

    @Test
    void testAdd() {
        Time expected = new Time(100, .5);
        Time result = zero.add(new Time(100)).add(new Time(.5));
        assertEquals(expected, result);
    }

    @Test
    void testAdd_Integral() {
        Time expected = new Time(100);
        Time result = zero.add(new Time(100));
        assertEquals(expected, result);
    }

    @Test
    void testAdd_Decimal() {
        Time expected = new Time(.5);
        Time result = zero.add(new Time(.5));
        assertEquals(expected, result);
    }

    @Test
    void testBetweenMod_Between() {
        Time start = new Time(6000);
        Time end = new Time(12000);
        Time between = new Time(8000);
        assertTrue(between.betweenMod(start, end));
    }

    @Test
    void testBetweenMod_NotBetween() {
        Time start = new Time(6000);
        Time end = new Time(12000);
        Time notBetween = new Time(2000);
        assertFalse(notBetween.betweenMod(start, end));
    }

    @Test
    void testCompareTo_Positive() {
        assertTrue(noon.compareTo(morning) > 0);
    }

    @Test
    void testCompareTo_Negative() {
        assertTrue(noonNegative.compareTo(morningTomorrow) < 0);
    }

    @Test
    void testCompareTo_Equal() {
        assertTrue(noon.compareTo(noon) == 0);
    }

    @Test
    void testCrossedMorning_True() {
        assertTrue(Time.crossedMorning(noon, noonTomorrow));
    }

    @Test
    void testCrossedMorning_False() {
        assertFalse(Time.crossedMorning(morning, noon));
    }

    @Test
    void testDivide_Time() {
        Time numerator = noon;
        Time denominator = new Time(10);

        double expected = 600;
        double result = numerator.divide(denominator);
        assertEquals(expected, result);
    }

    @Test
    void testDivide_Long() {
        Time numerator = noon;
        long denominator = 2;

        double expected = 3000;
        double result = numerator.divide(denominator);
        assertEquals(expected, result);
    }

    @Test
    void testDivide_Double() {
        Time numerator = noon;
        double denominator = 0.5;

        double expected = 12000;
        double result = numerator.divide(denominator);
        assertEquals(expected, result);
    }

    @Test
    void testFractionalValue_Positive() {
        assertEquals(0.75D, new Time(3451, 0.75).fractionalValue());
    }

    @Test
    void testFractionalValue_Negative() {
        assertEquals(-0.75D, new Time(0, -0.75).fractionalValue());
    }

    @Test
    void testFractionalValue_Zero() {
        assertEquals(0.0D, new Time(0, 0.0).fractionalValue());
    }

    @Test
    void testGetDay_0() {
        assertEquals(0, noon.getDay());
    }

    @Test
    void testGetDay_1() {
        assertEquals(1, morningTomorrow.getDay());
    }

    @Test
    void testMod_Positive_Inside() {
        assertEquals(noon, noon.mod(24000));
    }

    @Test
    void testMod_Positive_Outside() {
        assertEquals(noon, noonTomorrow.mod(24000));
    }

    @Test
    void testMod_Negative() {
        assertEquals(noonNegative, noonNegative.mod(24000));
    }

    @Test
    void testNegate_Positive() {
        assertEquals(new Time(-100, -0.75D), new Time(100, 0.75D).negate());
    }

    @Test
    void testNegate_Negative() {
        assertEquals(new Time(2000, 0.5D), new Time(-2000, -0.5D).negate());
    }

    @Test
    void testTimeOfDay() {
        assertEquals(noon, noonTomorrow.timeOfDay());
    }

}

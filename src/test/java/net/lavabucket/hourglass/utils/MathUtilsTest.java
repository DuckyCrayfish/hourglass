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

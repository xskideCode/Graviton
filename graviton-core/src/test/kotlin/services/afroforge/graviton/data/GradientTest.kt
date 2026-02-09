package services.afroforge.graviton.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GradientTest {

    @Test
    fun `test Gradient linear interpolation`() {
        val gradient = Gradient.between(Color.BLACK, Color.WHITE)
        
        val mid = gradient.evaluate(0.5)
        assertEquals(0.5, mid.r, 1e-9)
        assertEquals(0.5, mid.g, 1e-9)
        assertEquals(0.5, mid.b, 1e-9)
        assertEquals(1.0, mid.a, 1e-9)
    }

    @Test
    fun `test Gradient clamp time`() {
        val gradient = Gradient.between(Color.RED, Color.BLUE)
        
        val before = gradient.evaluate(-1.0)
        assertEquals(1.0, before.r, 1e-9) // Red
        assertEquals(0.0, before.b, 1e-9)

        val after = gradient.evaluate(2.0)
        assertEquals(0.0, after.r, 1e-9)
        assertEquals(1.0, after.b, 1e-9) // Blue
    }

    @Test
    fun `test ValueGradient multi keyframe`() {
        // 0.0 -> 0.0
        // 0.5 -> 1.0
        // 1.0 -> 0.0
        val gradient = ValueGradient(
            Keyframe(0.0, 0.0),
            Keyframe(0.5, 1.0),
            Keyframe(1.0, 0.0)
        )

        assertEquals(0.0, gradient.evaluate(0.0), 1e-9)
        assertEquals(1.0, gradient.evaluate(0.5), 1e-9)
        assertEquals(0.0, gradient.evaluate(1.0), 1e-9)
        assertEquals(0.5, gradient.evaluate(0.25), 1e-9)
        assertEquals(0.5, gradient.evaluate(0.75), 1e-9)
    }
}

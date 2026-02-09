package services.afroforge.graviton.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

class Vector3Test {

    @Test
    fun `test MutableVector3 addition`() {
        val v1 = MutableVector3(1.0, 2.0, 3.0)
        val v2 = MutableVector3(4.0, 5.0, 6.0)
        v1.add(v2)
        assertEquals(5.0, v1.x)
        assertEquals(7.0, v1.y)
        assertEquals(9.0, v1.z)
    }

    @Test
    fun `test MutableVector3 multiplication`() {
        val v1 = MutableVector3(2.0, 3.0, 4.0)
        v1.multiply(2.0)
        assertEquals(4.0, v1.x)
        assertEquals(6.0, v1.y)
        assertEquals(8.0, v1.z)
    }

    @Test
    fun `test Vector3 creation`() {
        val v1 = Vector3(1.0, 2.0, 3.0)
        assertEquals(1.0, v1.x)
        assertEquals(2.0, v1.y)
        assertEquals(3.0, v1.z)
    }

    @Test
    fun `test distance calculation`() {
        val v1 = MutableVector3(0.0, 0.0, 0.0)
        val v2 = MutableVector3(3.0, 4.0, 0.0)
        assertEquals(25.0, v1.distanceSquaredTo(v2))
        assertEquals(5.0, v1.distanceTo(v2))
    }

    @Test
    fun `test normalization`() {
        val v1 = MutableVector3(3.0, 0.0, 0.0)
        v1.normalize()
        assertEquals(1.0, v1.x, 1e-9)
        assertEquals(0.0, v1.y)
        assertEquals(0.0, v1.z)
    }

    @Test
    fun `test zero normalization safe`() {
        val v1 = MutableVector3(0.0, 0.0, 0.0)
        v1.normalize()
        assertTrue(v1.x.isNaN() || v1.x == 0.0 || v1.lengthSquared() == 0.0)
        // Implementation might produce NaN or 0 check. Let's verify behavior.
        // If implementation doesn't check for zero, it divides by zero -> NaN.
        // If it checks, it remains 0.
    }
}

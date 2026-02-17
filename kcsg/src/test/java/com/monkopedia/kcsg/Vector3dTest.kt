package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Vector3dTest {
    @Test
    fun factoryMethodsAndConstants() {
        assertVectorEquals(Vector3d(3.0, 0.0, 0.0), Vector3d.x(3.0))
        assertVectorEquals(Vector3d(0.0, 4.0, 0.0), Vector3d.y(4.0))
        assertVectorEquals(Vector3d(0.0, 0.0, 5.0), Vector3d.z(5.0))
        assertVectorEquals(Vector3d(6.0, 7.0, 0.0), Vector3d.xy(6.0, 7.0))
        assertVectorEquals(Vector3d(8.0, 9.0, 10.0), Vector3d.xyz(8.0, 9.0, 10.0))
        assertVectorEquals(Vector3d(0.0, 11.0, 12.0), Vector3d.yz(11.0, 12.0))
        assertVectorEquals(Vector3d(13.0, 0.0, 14.0), Vector3d.xz(13.0, 14.0))

        assertVectorEquals(Vector3d(0.0, 0.0, 0.0), Vector3d.zero())
        assertVectorEquals(Vector3d(1.0, 1.0, 1.0), Vector3d.unity())
        assertVectorEquals(Vector3d(1.0, 1.0, 1.0), Vector3d.UNITY)
        assertVectorEquals(Vector3d(1.0, 0.0, 0.0), Vector3d.X_ONE)
        assertVectorEquals(Vector3d(0.0, 1.0, 0.0), Vector3d.Y_ONE)
        assertVectorEquals(Vector3d(0.0, 0.0, 0.0), Vector3d.ZERO)
        assertVectorEquals(Vector3d(0.0, 0.0, 1.0), Vector3d.Z_ONE)
    }

    @Test
    fun componentAccessAndArithmetic() {
        val v = Vector3d.xyz(1.0, 2.0, 3.0)
        assertEquals(1.0, v[0], EPS)
        assertEquals(2.0, v[1], EPS)
        assertEquals(3.0, v[2], EPS)

        assertVectorEquals(Vector3d(5.0, 7.0, 9.0), v + Vector3d.xyz(4.0, 5.0, 6.0))
        assertVectorEquals(Vector3d(4.0, 6.0, 8.0), v.plus(3.0, 4.0, 5.0))
        assertVectorEquals(Vector3d(-3.0, -3.0, -3.0), v - Vector3d.xyz(4.0, 5.0, 6.0))
        assertVectorEquals(Vector3d(0.0, -1.0, -2.0), v.minus(1.0, 3.0, 5.0))

        assertVectorEquals(Vector3d(2.0, 4.0, 6.0), v * 2.0)
        assertVectorEquals(Vector3d(2.0, 3.0, 12.0), v * Vector3d.xyz(2.0, 1.5, 4.0))
        assertVectorEquals(Vector3d(2.0, 6.0, 12.0), v.times(2.0, 3.0, 4.0))
        assertVectorEquals(Vector3d(0.5, 1.0, 1.5), v.divided(2.0))
    }

    @Test
    fun vectorMathOps() {
        val x = Vector3d.X_ONE
        val y = Vector3d.Y_ONE
        val z = Vector3d.Z_ONE

        assertEquals(0.0, x.dot(y), EPS)
        assertEquals(1.0, x.dot(x), EPS)
        assertVectorEquals(z, x.crossed(y))
        assertEquals(5.0, Vector3d(3.0, 4.0, 0.0).magnitude(), EPS)
        assertEquals(25.0, Vector3d(3.0, 4.0, 0.0).magnitudeSq(), EPS)
        assertEquals(90.0, x.angle(y), 1e-8)
        assertEquals(5.0, Vector3d(1.0, 2.0, 3.0).distance(Vector3d(4.0, 6.0, 3.0)), EPS)
    }

    @Test
    fun projectionNormalizationAndInterpolation() {
        val base = Vector3d.xyz(3.0, 0.0, 0.0)
        val projected = base.project(Vector3d.xyz(1.0, 1.0, 0.0))
        assertVectorEquals(Vector3d.xyz(1.0, 0.0, 0.0), projected)

        val orthogonal = Vector3d.xyz(0.0, 2.0, 3.0).orthogonal()
        assertEquals(0.0, Vector3d.xyz(0.0, 2.0, 3.0).dot(orthogonal), 1e-8)

        assertVectorEquals(Vector3d.xyz(0.0, 0.6, 0.8), Vector3d.xyz(0.0, 3.0, 4.0).normalized())
        assertVectorEquals(Vector3d.xyz(-1.0, 2.0, -3.0), Vector3d.xyz(1.0, -2.0, 3.0).negated())

        val start = Vector3d.xyz(0.0, 0.0, 0.0)
        val end = Vector3d.xyz(10.0, 20.0, 30.0)
        assertVectorEquals(Vector3d.xyz(5.0, 10.0, 15.0), start.lerp(end, 0.5))
    }

    @Test
    fun collinearAndTransformOverloads() {
        val p1 = Vector3d.xyz(0.0, 0.0, 0.0)
        assertTrue(p1.collinear(Vector3d.xyz(1.0, 1.0, 1.0), Vector3d.xyz(2.0, 2.0, 2.0)))
        assertFalse(p1.collinear(Vector3d.xyz(1.0, 0.0, 0.0), Vector3d.xyz(0.0, 1.0, 0.0)))

        val translation = Transform.unity().translate(10.0, 0.0, -5.0)
        assertVectorEquals(Vector3d.xyz(11.0, 2.0, -2.0), Vector3d.xyz(1.0, 2.0, 3.0).transformed(translation))
        assertVectorEquals(
            Vector3d.xyz(6.0, 2.0, 0.5),
            Vector3d.xyz(1.0, 2.0, 3.0).transformed(translation, 0.5),
        )
    }

    @Test
    fun collinearHandlesEqualLengthBranch() {
        val p = Vector3d.ZERO
        assertTrue(p.collinear(Vector3d.ZERO, Vector3d.ZERO))

        val sqrt3Over2 = kotlin.math.sqrt(3.0) / 2.0
        assertFalse(
            p.collinear(
                Vector3d.xyz(1.0, 0.0, 0.0),
                Vector3d.xyz(0.5, sqrt3Over2, 0.0),
            ),
        )
    }

    @Test
    fun stlObjAndObjectMethods() {
        val v = Vector3d.xyz(1.25, -2.5, 3.75)
        val stlString = v.toStlString()
        val stlBuilder = v.toStlString(StringBuilder()).toString()
        assertEquals(stlString, stlBuilder)
        assertEquals("1.25 -2.5 3.75", stlString)

        val objString = v.toObjString()
        val objBuilder = v.toObjString(StringBuilder()).toString()
        assertEquals(objString, objBuilder)
        assertEquals("1.25 -2.5 3.75", objString)

        assertEquals(v, v.copy())
        assertEquals(v.hashCode(), v.copy().hashCode())
        assertNotEquals(v, Vector3d.xyz(1.25, -2.5, 4.75))
        assertTrue(v.toString().contains("1.25"))
    }

    @Test(expected = RuntimeException::class)
    fun invalidIndexThrows() {
        Vector3d.xyz(1.0, 2.0, 3.0)[3]
    }

    private fun assertVectorEquals(expected: Vector3d, actual: Vector3d, tolerance: Double = EPS) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
        assertEquals(expected.z, actual.z, tolerance)
    }

    companion object {
        private const val EPS = 1e-9
    }
}

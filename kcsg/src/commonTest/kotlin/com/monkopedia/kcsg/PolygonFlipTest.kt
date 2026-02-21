package com.monkopedia.kcsg

import com.monkopedia.kcsg.Polygon.Companion.fromPoints
import com.monkopedia.kcsg.Vector3d
import kotlin.test.Test
import kotlin.test.assertEquals

class PolygonFlipTest {
    @Test
    fun flipPolygonTest() {
        val polygon = fromPoints(
            Vector3d.xy(1.0, 1.0),
            Vector3d.xy(2.0, 1.0),
            Vector3d.xy(1.0, 2.0)
        )
        assertVectorEquals(Vector3d.z(1.0), polygon.plane.normal)
        polygon.flip()
        assertVectorEquals(Vector3d.z(-1.0), polygon.plane.normal)
    }

    private fun assertVectorEquals(expected: Vector3d, actual: Vector3d) {
        assertEquals(expected.x, actual.x, EPSILON)
        assertEquals(expected.y, actual.y, EPSILON)
        assertEquals(expected.z, actual.z, EPSILON)
    }

    companion object {
        private const val EPSILON = 1e-8
    }
}

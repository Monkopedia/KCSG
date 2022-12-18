package com.monkopedia.kcsg

import com.monkopedia.kcsg.Polygon.Companion.fromPoints
import com.monkopedia.kcsg.Vector3d
import org.junit.Assert
import org.junit.Test

class PolygonFlipTest {
    @Test
    fun flipPolygonTest() {
        val polygon = fromPoints(
            Vector3d.xy(1.0, 1.0),
            Vector3d.xy(2.0, 1.0),
            Vector3d.xy(1.0, 2.0)
        )
        assertEquals(Vector3d.z(1.0), polygon.plane.normal)
        polygon.flip()
        assertEquals(Vector3d.z(-1.0), polygon.plane.normal)
    }

    private fun assertEquals(expected: Vector3d, actual: Vector3d) {
        Assert.assertEquals(expected.x, actual.x, EPSILON)
        Assert.assertEquals(expected.y, actual.y, EPSILON)
        Assert.assertEquals(expected.z, actual.z, EPSILON)
    }

    companion object {
        private const val EPSILON = 1e-8
    }
}
package com.monkopedia.kcsg

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class PolygonErrorPathTest {
    @Test
    fun creatingPolygonWithFewerThanThreeVerticesFails() {
        val v1 = Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE)
        val v2 = Vertex(Vector3d.xyz(1.0, 0.0, 0.0), Vector3d.Z_ONE)

        assertFailsWith<RuntimeException> {
            Polygon(listOf(v1, v2))
        }
    }

    @Test
    fun creatingPolygonFromPointsWithFewerThanThreeVerticesFails() {
        assertFailsWith<RuntimeException> {
            Polygon.fromPoints(
                listOf(
                    Vector3d.xyz(0.0, 0.0, 0.0),
                    Vector3d.xyz(1.0, 0.0, 0.0),
                )
            )
        }
    }

    @Test
    fun deprecatedIntersectsThrowsForOverlappingBounds() {
        val a = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 0.0),
        )
        val b = Polygon.fromPoints(
            Vector3d.xyz(1.0, 1.0, 0.0),
            Vector3d.xyz(3.0, 1.0, 0.0),
            Vector3d.xyz(3.0, 3.0, 0.0),
            Vector3d.xyz(1.0, 3.0, 0.0),
        )

        assertFailsWith<UnsupportedOperationException> {
            a.intersects(b)
        }

        val farAway = Polygon.fromPoints(
            Vector3d.xyz(10.0, 10.0, 0.0),
            Vector3d.xyz(11.0, 10.0, 0.0),
            Vector3d.xyz(11.0, 11.0, 0.0),
            Vector3d.xyz(10.0, 11.0, 0.0),
        )
        assertFalse(a.intersects(farAway))
    }
}

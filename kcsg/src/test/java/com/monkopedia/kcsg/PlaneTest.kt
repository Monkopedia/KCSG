package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class PlaneTest {
    @Test
    fun copyAndFlip() {
        val plane = Plane(Vector3d.xyz(0.0, 0.0, 5.0), 3.0)
        val copy = plane.copy()

        assertNotSame(plane, copy)
        assertVectorClose(Vector3d.Z_ONE, copy.normal, EPS)
        assertEquals(3.0, copy.dist, EPS)

        plane.flip()
        assertVectorClose(Vector3d.xyz(0.0, 0.0, -1.0), plane.normal, EPS)
        assertEquals(-3.0, plane.dist, EPS)
        assertVectorClose(Vector3d.Z_ONE, copy.normal, EPS)
        assertEquals(3.0, copy.dist, EPS)
    }

    @Test
    fun createFromPoints() {
        val plane = Plane.createFromPoints(
            Vector3d.xyz(0.0, 0.0, 1.0),
            Vector3d.xyz(2.0, 0.0, 1.0),
            Vector3d.xyz(0.0, 4.0, 1.0),
        )

        assertVectorClose(Vector3d.Z_ONE, plane.normal, EPS)
        assertEquals(1.0, plane.dist, EPS)
    }

    @Test
    fun splitPolygonClassifiesAllCases() {
        val splitPlane = Plane(Vector3d.Z_ONE, 0.0)

        val coplanarFront = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
        )
        val frontResult = splitCounts(splitPlane, coplanarFront)
        assertEquals(1, frontResult.coplanarFront)
        assertEquals(0, frontResult.coplanarBack)
        assertEquals(0, frontResult.front)
        assertEquals(0, frontResult.back)

        val coplanarBack = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
        )
        val backResult = splitCounts(splitPlane, coplanarBack)
        assertEquals(0, backResult.coplanarFront)
        assertEquals(1, backResult.coplanarBack)
        assertEquals(0, backResult.front)
        assertEquals(0, backResult.back)

        val frontPolygon = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 1.0),
            Vector3d.xyz(1.0, 0.0, 1.0),
            Vector3d.xyz(0.0, 1.0, 1.0),
        )
        val pureFront = splitCounts(splitPlane, frontPolygon)
        assertEquals(0, pureFront.coplanarFront)
        assertEquals(0, pureFront.coplanarBack)
        assertEquals(1, pureFront.front)
        assertEquals(0, pureFront.back)

        val backPolygon = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, -1.0),
            Vector3d.xyz(0.0, 1.0, -1.0),
            Vector3d.xyz(1.0, 0.0, -1.0),
        )
        val pureBack = splitCounts(splitPlane, backPolygon)
        assertEquals(0, pureBack.coplanarFront)
        assertEquals(0, pureBack.coplanarBack)
        assertEquals(0, pureBack.front)
        assertEquals(1, pureBack.back)

        val spanningPolygon = Polygon.fromPoints(
            Vector3d.xyz(-1.0, 0.0, -1.0),
            Vector3d.xyz(1.0, 0.0, 1.0),
            Vector3d.xyz(0.0, 1.0, 1.0),
        )
        val spanningResult = splitCounts(splitPlane, spanningPolygon)
        assertEquals(0, spanningResult.coplanarFront)
        assertEquals(0, spanningResult.coplanarBack)
        assertEquals(1, spanningResult.front)
        assertEquals(1, spanningResult.back)
    }

    @Test
    fun splitPolygonWithAndWithoutStackOverflowWorkaround() {
        val originalValue = Plane.USE_STACKOVERFLOW_WORKAROUND
        val splitPlane = Plane(Vector3d.Z_ONE, 0.0)
        val spanningPolygon = Polygon.fromPoints(
            Vector3d.xyz(-1.0, 0.0, -1.0),
            Vector3d.xyz(1.0, 0.0, 1.0),
            Vector3d.xyz(0.0, 1.0, 1.0),
        )

        try {
            Plane.USE_STACKOVERFLOW_WORKAROUND = false
            val withoutWorkaround = splitCounts(splitPlane, spanningPolygon)

            Plane.USE_STACKOVERFLOW_WORKAROUND = true
            val withWorkaround = splitCounts(splitPlane, spanningPolygon)

            assertEquals(withoutWorkaround, withWorkaround)
        } finally {
            Plane.USE_STACKOVERFLOW_WORKAROUND = originalValue
        }
    }

    private fun splitCounts(plane: Plane, polygon: Polygon): SplitCounts {
        val coplanarFront = mutableListOf<Polygon>()
        val coplanarBack = mutableListOf<Polygon>()
        val front = mutableListOf<Polygon>()
        val back = mutableListOf<Polygon>()
        plane.splitPolygon(polygon, coplanarFront, coplanarBack, front, back)
        return SplitCounts(
            coplanarFront = coplanarFront.size,
            coplanarBack = coplanarBack.size,
            front = front.size,
            back = back.size,
        )
    }

    private data class SplitCounts(
        val coplanarFront: Int,
        val coplanarBack: Int,
        val front: Int,
        val back: Int,
    )

    companion object {
        private const val EPS = 1e-9
    }
}

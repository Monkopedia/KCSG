package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.Test

class BoundsTest {
    @Test
    fun centerAndBoundsAreDerivedFromMinAndMax() {
        val bounds = Bounds(
            min = Vector3d.xyz(-1.0, -2.0, -3.0),
            max = Vector3d.xyz(3.0, 4.0, 5.0),
        )

        assertVectorClose(Vector3d.xyz(1.0, 1.0, 1.0), bounds.center, EPS)
        assertVectorClose(Vector3d.xyz(4.0, 6.0, 8.0), bounds.bounds, EPS)
    }

    @Test
    fun toCubeAndToCsg() {
        val bounds = Bounds(
            min = Vector3d.xyz(0.0, 0.0, 0.0),
            max = Vector3d.xyz(2.0, 4.0, 6.0),
        )

        val cube = bounds.toCube()
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 3.0), cube.center, EPS)
        assertVectorClose(Vector3d.xyz(2.0, 4.0, 6.0), cube.dimensions, EPS)

        val csgFirst = bounds.toCSG()
        val csgSecond = bounds.toCSG()
        assertSame(csgFirst, csgSecond)
        assertBoundsClose(bounds, csgFirst.bounds, 1e-6)
        assertVolumeClose(expected = 48.0, actual = csgFirst.computeVolume(), absoluteTolerance = 1e-6)
    }

    @Test
    fun containsOverloadsAreBoundaryInclusive() {
        val bounds = Bounds(
            min = Vector3d.xyz(0.0, 0.0, 0.0),
            max = Vector3d.xyz(2.0, 2.0, 2.0),
        )

        assertTrue(Vector3d.xyz(0.0, 0.0, 0.0) in bounds)
        assertTrue(Vector3d.xyz(2.0, 2.0, 2.0) in bounds)
        assertFalse(Vector3d.xyz(2.01, 1.0, 1.0) in bounds)

        val insideVertex = Vertex(Vector3d.xyz(1.0, 1.0, 1.0), Vector3d.Z_ONE)
        val outsideVertex = Vertex(Vector3d.xyz(3.0, 1.0, 1.0), Vector3d.Z_ONE)
        assertTrue(insideVertex in bounds)
        assertFalse(outsideVertex in bounds)

        val insidePolygon = Polygon.fromPoints(
            Vector3d.xyz(0.5, 0.5, 0.5),
            Vector3d.xyz(1.5, 0.5, 0.5),
            Vector3d.xyz(0.5, 1.5, 0.5),
        )
        val outsidePolygon = Polygon.fromPoints(
            Vector3d.xyz(0.5, 0.5, 0.5),
            Vector3d.xyz(3.0, 0.5, 0.5),
            Vector3d.xyz(0.5, 1.5, 0.5),
        )
        assertTrue(insidePolygon in bounds)
        assertFalse(outsidePolygon in bounds)
    }

    @Test
    fun intersectsOverloadsAndStringOutput() {
        val bounds = Bounds(
            min = Vector3d.xyz(0.0, 0.0, 0.0),
            max = Vector3d.xyz(2.0, 2.0, 2.0),
        )

        val intersectingPolygon = Polygon.fromPoints(
            Vector3d.xyz(1.0, 1.0, 1.0),
            Vector3d.xyz(3.0, 1.0, 1.0),
            Vector3d.xyz(1.0, 3.0, 1.0),
        )
        val nonIntersectingPolygon = Polygon.fromPoints(
            Vector3d.xyz(3.0, 3.0, 3.0),
            Vector3d.xyz(4.0, 3.0, 3.0),
            Vector3d.xyz(3.0, 4.0, 3.0),
        )
        assertTrue(bounds.intersects(intersectingPolygon))
        assertFalse(bounds.intersects(nonIntersectingPolygon))

        val overlappingBounds = Bounds(Vector3d.xyz(1.0, 1.0, 1.0), Vector3d.xyz(3.0, 3.0, 3.0))
        val touchingBounds = Bounds(Vector3d.xyz(2.0, 2.0, 2.0), Vector3d.xyz(4.0, 4.0, 4.0))
        val disjointBounds = Bounds(Vector3d.xyz(2.1, 2.1, 2.1), Vector3d.xyz(4.0, 4.0, 4.0))
        assertTrue(bounds.intersects(overlappingBounds))
        assertTrue(bounds.intersects(touchingBounds))
        assertFalse(bounds.intersects(disjointBounds))

        val rendered = bounds.toString()
        assertTrue(rendered.contains("center"))
        assertTrue(rendered.contains("bounds"))
    }

    companion object {
        private const val EPS = 1e-9
    }
}

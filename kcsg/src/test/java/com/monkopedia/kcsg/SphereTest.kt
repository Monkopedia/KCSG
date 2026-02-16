package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs

class SphereTest {
    @Test
    fun constructorsPopulateProperties() {
        val primary = Sphere(2.0, 24, 12, Vector3d.xyz(1.0, 2.0, 3.0))
        assertEquals(2.0, primary.radius, EPS)
        assertEquals(24, primary.numSlices)
        assertEquals(12, primary.numStacks)
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 3.0), primary.center, EPS)

        val secondary = Sphere(Vector3d.xyz(-1.0, -2.0, -3.0), 4.0, 10, 5)
        assertEquals(4.0, secondary.radius, EPS)
        assertEquals(10, secondary.numSlices)
        assertEquals(5, secondary.numStacks)
        assertVectorClose(Vector3d.xyz(-1.0, -2.0, -3.0), secondary.center, EPS)
    }

    @Test
    fun toPolygonsAndResolutionEffects() {
        val coarse = Sphere(1.0, 8, 4, Vector3d.ZERO)
        val fine = Sphere(1.0, 32, 16, Vector3d.ZERO)

        val coarsePolygons = coarse.toPolygons()
        val finePolygons = fine.toPolygons()
        assertEquals(8 * 4, coarsePolygons.size)
        assertEquals(32 * 16, finePolygons.size)
        assertTrue(finePolygons.size > coarsePolygons.size)

        coarsePolygons.forEach { polygon ->
            assertTrue(polygon.vertices.size == 3 || polygon.vertices.size == 4)
        }
        finePolygons.forEach { polygon ->
            assertTrue(polygon.vertices.size == 3 || polygon.vertices.size == 4)
        }
    }

    @Test
    fun toCsgBoundsAndFiniteMesh() {
        val sphere = Sphere(2.0, 32, 16, Vector3d.xyz(1.0, -1.0, 0.5))
        val csg = sphere.toCSG()

        assertFiniteMesh(csg)
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -3.0, -1.5),
                max = Vector3d.xyz(3.0, 1.0, 2.5),
            ),
            actual = csg.bounds,
            tolerance = 1e-6,
        )
    }

    @Test
    fun volumeConvergesWithHigherResolution() {
        val radius = 2.0
        val expectedVolume = (4.0 / 3.0) * PI * radius * radius * radius

        val coarseVolume = Sphere(radius, 8, 4, Vector3d.ZERO).toCSG().computeVolume()
        val fineVolume = Sphere(radius, 64, 32, Vector3d.ZERO).toCSG().computeVolume()

        val coarseError = abs(coarseVolume - expectedVolume) / expectedVolume
        val fineError = abs(fineVolume - expectedVolume) / expectedVolume

        assertTrue("Expected fine tessellation to reduce error", fineError < coarseError)
        assertTrue("Expected fine tessellation error under 10%", fineError < 0.10)
    }

    companion object {
        private const val EPS = 1e-9
    }
}

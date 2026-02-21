package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.math.PI
import kotlin.math.abs

class CylinderTest {
    @Test
    fun constructorsPopulateGeometryFields() {
        val primary = Cylinder(
            start = Vector3d.xyz(1.0, 2.0, 3.0),
            end = Vector3d.xyz(1.0, 2.0, 8.0),
            startRadius = 2.0,
            endRadius = 1.0,
            numSlices = 24,
        )
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 3.0), primary.start, EPS)
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 8.0), primary.end, EPS)
        assertEquals(2.0, primary.startRadius, EPS)
        assertEquals(1.0, primary.endRadius, EPS)
        assertEquals(24, primary.numSlices)

        val startEndRadius = Cylinder(
            start = Vector3d.xyz(0.0, 0.0, 0.0),
            end = Vector3d.xyz(0.0, 0.0, 5.0),
            radius = 3.0,
            numSlices = 16,
        )
        assertEquals(3.0, startEndRadius.startRadius, EPS)
        assertEquals(3.0, startEndRadius.endRadius, EPS)
        assertEquals(16, startEndRadius.numSlices)

        val radiusHeight = Cylinder(2.5, 7.0, 12)
        assertVectorClose(Vector3d.ZERO, radiusHeight.start, EPS)
        assertVectorClose(Vector3d.xyz(0.0, 0.0, 7.0), radiusHeight.end, EPS)
        assertEquals(2.5, radiusHeight.startRadius, EPS)
        assertEquals(2.5, radiusHeight.endRadius, EPS)
        assertEquals(12, radiusHeight.numSlices)

        val frustumCtor = Cylinder(2.0, 1.0, 4.0, 20)
        assertVectorClose(Vector3d.ZERO, frustumCtor.start, EPS)
        assertVectorClose(Vector3d.xyz(0.0, 0.0, 4.0), frustumCtor.end, EPS)
        assertEquals(2.0, frustumCtor.startRadius, EPS)
        assertEquals(1.0, frustumCtor.endRadius, EPS)
        assertEquals(20, frustumCtor.numSlices)
    }

    @Test
    fun toPolygonsProducesCapsAndSideFacesPerSlice() {
        val slices = 12
        val cylinder = Cylinder(1.5, 4.0, slices)

        val polygons = cylinder.toPolygons()
        assertEquals(slices * 3, polygons.size)
        assertEquals(slices * 2, polygons.count { it.vertices.size == 3 })
        assertEquals(slices, polygons.count { it.vertices.size == 4 })
    }

    @Test
    fun toCsgAndFrustumVsCylinderVolume() {
        val slices = 64
        val height = 5.0
        val cylinderRadius = 2.0

        val cylinder = Cylinder(cylinderRadius, height, slices).toCSG()
        val frustum = Cylinder(2.0, 1.0, height, slices).toCSG()

        assertFiniteMesh(cylinder)
        assertFiniteMesh(frustum)

        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-2.0, -2.0, 0.0),
                max = Vector3d.xyz(2.0, 2.0, 5.0),
            ),
            actual = cylinder.bounds,
            tolerance = 1e-6,
        )
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-2.0, -2.0, 0.0),
                max = Vector3d.xyz(2.0, 2.0, 5.0),
            ),
            actual = frustum.bounds,
            tolerance = 1e-6,
        )

        val cylinderExpected = PI * cylinderRadius * cylinderRadius * height
        val frustumExpected = PI * height * (2.0 * 2.0 + 2.0 * 1.0 + 1.0 * 1.0) / 3.0
        assertVolumeClose(cylinderExpected, cylinder.computeVolume(), absoluteTolerance = 1e-6, relativeTolerance = 0.05)
        assertVolumeClose(frustumExpected, frustum.computeVolume(), absoluteTolerance = 1e-6, relativeTolerance = 0.05)
        assertTrue(frustum.computeVolume() < cylinder.computeVolume())
    }

    @Test
    fun sliceCountImprovesCylinderVolumeApproximation() {
        val radius = 1.0
        val height = 3.0
        val expectedVolume = PI * radius * radius * height

        val coarse = Cylinder(radius, height, 6).toCSG().computeVolume()
        val fine = Cylinder(radius, height, 64).toCSG().computeVolume()
        val coarseError = abs(coarse - expectedVolume) / expectedVolume
        val fineError = abs(fine - expectedVolume) / expectedVolume

        assertTrue(fineError < coarseError, "Expected fine tessellation to reduce error")
        assertTrue(fineError < 0.10, "Expected fine tessellation error under 10%")
    }

    companion object {
        private const val EPS = 1e-9
    }
}

package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundedCubeTest {
    @Test
    fun constructorsPopulateFields() {
        val primary = RoundedCube(
            center = Vector3d.xyz(1.0, 2.0, 3.0),
            dimensions = Vector3d.xyz(4.0, 5.0, 6.0),
            cornerRadius = 0.4,
            resolution = 3,
            centered = true,
        )
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 3.0), primary.center, EPS)
        assertVectorClose(Vector3d.xyz(4.0, 5.0, 6.0), primary.dimensions, EPS)
        assertEquals(0.4, primary.cornerRadius, EPS)
        assertEquals(3, primary.resolution)
        assertTrue(primary.centered)

        val sizeCtor = RoundedCube(2.5)
        assertVectorClose(Vector3d.ZERO, sizeCtor.center, EPS)
        assertVectorClose(Vector3d.xyz(2.5, 2.5, 2.5), sizeCtor.dimensions, EPS)

        val whdCtor = RoundedCube(3.0, 4.0, 5.0)
        assertVectorClose(Vector3d.ZERO, whdCtor.center, EPS)
        assertVectorClose(Vector3d.xyz(3.0, 4.0, 5.0), whdCtor.dimensions, EPS)
    }

    @Test
    fun toPolygonsAndToCsgFollowBoundsInvariants() {
        val rounded = RoundedCube(
            center = Vector3d.xyz(1.0, 2.0, 3.0),
            dimensions = Vector3d.xyz(4.0, 6.0, 8.0),
            cornerRadius = 0.5,
            resolution = 3,
        )

        val polygons = rounded.toPolygons()
        assertTrue(polygons.isNotEmpty())

        val csg = rounded.toCSG()
        assertFiniteMesh(csg)
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -1.0, -1.0),
                max = Vector3d.xyz(3.0, 5.0, 7.0),
            ),
            actual = csg.bounds,
            tolerance = 0.15,
        )
        assertTrue(csg.computeVolume() > 0.0)
    }

    @Test
    fun noCenterShiftsBoundsWithoutChangingVolume() {
        val centered = RoundedCube(
            center = Vector3d.ZERO,
            dimensions = Vector3d.xyz(2.0, 4.0, 6.0),
            cornerRadius = 0.5,
            resolution = 3,
        ).toCSG()
        val noCenter = RoundedCube(
            center = Vector3d.ZERO,
            dimensions = Vector3d.xyz(2.0, 4.0, 6.0),
            cornerRadius = 0.5,
            resolution = 3,
        ).noCenter().toCSG()

        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -2.0, -3.0),
                max = Vector3d.xyz(1.0, 2.0, 3.0),
            ),
            actual = centered.bounds,
            tolerance = 0.15,
        )
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(0.0, 0.0, 0.0),
                max = Vector3d.xyz(2.0, 4.0, 6.0),
            ),
            actual = noCenter.bounds,
            tolerance = 0.15,
        )
        assertVolumeClose(
            expected = centered.computeVolume(),
            actual = noCenter.computeVolume(),
            absoluteTolerance = 1e-6,
            relativeTolerance = 1e-4,
        )
    }

    @Test
    fun cornerRadiusAndResolutionAffectGeneratedMesh() {
        val dimensions = Vector3d.xyz(4.0, 4.0, 4.0)

        val lowResolution = RoundedCube(
            center = Vector3d.ZERO,
            dimensions = dimensions,
            cornerRadius = 0.75,
            resolution = 2,
        ).toPolygons().size
        val highResolution = RoundedCube(
            center = Vector3d.ZERO,
            dimensions = dimensions,
            cornerRadius = 0.75,
            resolution = 5,
        ).toPolygons().size
        assertTrue(highResolution > lowResolution)

        val smallRadiusVolume = RoundedCube(
            center = Vector3d.ZERO,
            dimensions = dimensions,
            cornerRadius = 0.25,
            resolution = 3,
        ).toCSG().computeVolume()
        val largeRadiusVolume = RoundedCube(
            center = Vector3d.ZERO,
            dimensions = dimensions,
            cornerRadius = 1.5,
            resolution = 3,
        ).toCSG().computeVolume()
        assertTrue(smallRadiusVolume > largeRadiusVolume)
    }

    companion object {
        private const val EPS = 1e-9
    }
}

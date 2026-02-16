package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import org.junit.Test

class CSGHullTest {
    @Test
    fun hullWithoutArgumentsUsesThisCsg() {
        val cube = cubeAtX(0.0)
        val hull = cube.hull()

        assertFiniteMesh(hull)
        assertBoundsClose(cube.bounds, hull.bounds, tolerance = 1e-6)
        assertVolumeClose(cube.computeVolume(), hull.computeVolume(), relativeTolerance = 1e-4)
    }

    @Test
    fun hullListOverloadBuildsConvexEnvelope() {
        val a = cubeAtX(0.0)
        val b = cubeAtX(3.0)
        val hull = a.hull(listOf(b))

        assertFiniteMesh(hull)
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -1.0, -1.0),
                max = Vector3d.xyz(4.0, 1.0, 1.0),
            ),
            actual = hull.bounds,
            tolerance = 1e-6,
        )
        assertVolumeClose(20.0, hull.computeVolume(), absoluteTolerance = 1e-6, relativeTolerance = 1e-4)
    }

    @Test
    fun hullVarargMatchesListOverload() {
        val a = cubeAtX(0.0)
        val b = cubeAtX(3.0)
        val c = cubeAtX(6.0)

        val listHull = a.hull(listOf(b, c))
        val varargHull = a.hull(b, c)

        assertBoundsClose(listHull.bounds, varargHull.bounds, tolerance = 1e-6)
        assertVolumeClose(listHull.computeVolume(), varargHull.computeVolume(), relativeTolerance = 1e-4)
    }

    private fun cubeAtX(centerX: Double): CSG {
        return Cube(
            center = Vector3d.xyz(centerX, 0.0, 0.0),
            dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
        ).toCSG()
    }
}

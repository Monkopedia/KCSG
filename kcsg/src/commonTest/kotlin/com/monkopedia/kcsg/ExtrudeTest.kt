package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExtrudeTest {
    @Test
    fun pointsOverloadsProduceEquivalentExtrusions() {
        val dir = Vector3d.xyz(0.0, 0.0, 2.0)
        val profile = squareProfile()

        val fromVararg = Extrude.points(dir, *profile.toTypedArray())
        val fromList = Extrude.points(dir, profile)

        assertFiniteMesh(fromVararg)
        assertFiniteMesh(fromList)
        assertVolumeClose(fromVararg.computeVolume(), fromList.computeVolume(), relativeTolerance = 1e-4)
        assertVolumeClose(2.0, fromVararg.computeVolume(), relativeTolerance = 1e-4)
    }

    @Test
    fun combineAndTopBottomToggles() {
        val dir = Vector3d.xyz(0.0, 0.0, 2.0)
        val profile = squareProfile()
        val bottom = Polygon.fromPoints(profile)
        val top = Polygon.fromPoints(profile.map { it.plus(dir) })

        val combined = Extrude.combine(bottom.copy(), top.copy())
        assertFiniteMesh(combined)
        assertEquals(10, combined.polygons.size)
        assertVolumeClose(2.0, combined.computeVolume(), relativeTolerance = 1e-4)

        val both = Extrude.points(dir, top = true, bottom = true, *profile.toTypedArray())
        val bottomOnly = Extrude.points(dir, top = false, bottom = true, *profile.toTypedArray())
        val topOnly = Extrude.points(dir, top = true, bottom = false, *profile.toTypedArray())
        val sideOnly = Extrude.points(dir, top = false, bottom = false, *profile.toTypedArray())
        val bothList = Extrude.points(dir, top = true, bottom = true, points1 = profile)

        assertEquals(8, both.size)
        assertEquals(6, bottomOnly.size)
        assertEquals(6, topOnly.size)
        assertEquals(4, sideOnly.size)
        assertEquals(both.size, bothList.size)
    }

    @Test
    fun toCwAndIsCcwAndNegativeZRejection() {
        val ccwProfile = squareProfile()
        val ccwPolygon = Polygon.fromPoints(ccwProfile)
        assertTrue(Extrude.isCCW(ccwPolygon))

        val cwProfile = Extrude.toCW(ccwProfile)
        val cwPolygon = Polygon.fromPoints(cwProfile)
        assertFalse(Extrude.isCCW(cwPolygon))

        assertFailsWith<IllegalArgumentException> {
            Extrude.points(Vector3d.xyz(0.0, 0.0, -1.0), *ccwProfile.toTypedArray())
        }
    }

    private fun squareProfile(): List<Vector3d> {
        return listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 1.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
        )
    }
}

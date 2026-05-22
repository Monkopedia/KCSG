package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.assertEquals
import kotlin.test.Test

class CubeTest {
    @Test
    fun constructorsInitializeCenterAndDimensions() {
        val primary = Cube(
            center = Vector3d.xyz(1.0, 2.0, 3.0),
            dimensions = Vector3d.xyz(4.0, 5.0, 6.0),
        )
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 3.0), primary.center, EPS)
        assertVectorClose(Vector3d.xyz(4.0, 5.0, 6.0), primary.dimensions, EPS)

        val sizeCtor = Cube(2.5)
        assertVectorClose(Vector3d.ZERO, sizeCtor.center, EPS)
        assertVectorClose(Vector3d.xyz(2.5, 2.5, 2.5), sizeCtor.dimensions, EPS)

        val whdCtor = Cube(3.0, 4.0, 5.0)
        assertVectorClose(Vector3d.ZERO, whdCtor.center, EPS)
        assertVectorClose(Vector3d.xyz(3.0, 4.0, 5.0), whdCtor.dimensions, EPS)
    }

    @Test
    fun toPolygonsProducesSixQuadFaces() {
        val cube = Cube(
            center = Vector3d.xyz(1.0, 2.0, 3.0),
            dimensions = Vector3d.xyz(4.0, 6.0, 8.0),
        )

        val polygons = cube.toPolygons()
        assertEquals(6, polygons.size)
        polygons.forEach { polygon ->
            assertEquals(4, polygon.vertices.size)
        }

        val csg = cube.toCSG()
        assertFiniteMesh(csg)
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -1.0, -1.0),
                max = Vector3d.xyz(3.0, 5.0, 7.0),
            ),
            actual = csg.bounds,
            tolerance = 1e-6,
        )
    }

    @Test
    fun toCsgRespectsBoundsAndVolumeInvariants() {
        val cube = Cube(
            center = Vector3d.xyz(-2.0, 3.0, 5.0),
            dimensions = Vector3d.xyz(2.0, 4.0, 6.0),
        )
        val csg = cube.toCSG()

        assertFiniteMesh(csg)
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-3.0, 1.0, 2.0),
                max = Vector3d.xyz(-1.0, 5.0, 8.0),
            ),
            actual = csg.bounds,
            tolerance = 1e-6,
        )
        assertVolumeClose(expected = 48.0, actual = csg.computeVolume(), absoluteTolerance = 1e-6)
    }

    @Test
    fun centeredFlagDefaultsTrueAndTogglesWithNoCenter() {
        val cube = Cube(2.0)
        assertEquals(true, cube.centered)
        assertEquals(cube, cube.noCenter())
        assertEquals(false, cube.centered)
        cube.centered = true
        assertEquals(true, cube.centered)
    }

    @Test
    fun noCenterShiftsCubeToPositiveOctantFromOrigin() {
        val centered = Cube(
            center = Vector3d.ZERO,
            dimensions = Vector3d.xyz(2.0, 4.0, 6.0),
        ).toCSG()
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -2.0, -3.0),
                max = Vector3d.xyz(1.0, 2.0, 3.0),
            ),
            actual = centered.bounds,
            tolerance = 1e-6,
        )

        val noCenter = Cube(
            center = Vector3d.ZERO,
            dimensions = Vector3d.xyz(2.0, 4.0, 6.0),
        ).noCenter().toCSG()
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(0.0, 0.0, 0.0),
                max = Vector3d.xyz(2.0, 4.0, 6.0),
            ),
            actual = noCenter.bounds,
            tolerance = 1e-6,
        )
        assertVolumeClose(
            expected = centered.computeVolume(),
            actual = noCenter.computeVolume(),
            absoluteTolerance = 1e-6,
        )
    }

    companion object {
        private const val EPS = 1e-9
    }
}

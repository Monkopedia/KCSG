package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.test.Test

class CSGCoreTest {
    @Test
    fun copyCreatesIndependentPolygonGraph() {
        val original = Cube(2.0).toCSG()
        val copied = original.copy()

        assertNotSame(original, copied)
        assertEquals(original.polygons.size, copied.polygons.size)
        assertNotSame(original.polygons[0], copied.polygons[0])

        val originalPos = original.polygons[0].vertices[0].pos
        copied.polygons[0].vertices[0].pos = Vector3d.xyz(99.0, 99.0, 99.0)
        assertEquals(originalPos, original.polygons[0].vertices[0].pos)
    }

    @Test
    fun optimizationAndDumbUnion() {
        val a = cubeAtX(0.0)
        val b = cubeAtX(3.0)

        val dumbUnion = a.dumbUnion(b)
        assertEquals(a.polygons.size + b.polygons.size, dumbUnion.polygons.size)
        assertVolumeClose(16.0, dumbUnion.computeVolume(), relativeTolerance = 1e-4)

        CSG.OptType.entries.forEach { optType ->
            val union = cubeAtX(0.0).optimization(optType).union(cubeAtX(3.0))
            assertVolumeClose(16.0, union.computeVolume(), relativeTolerance = 1e-4)
        }
    }

    @Test
    fun boundsVolumeAndTransformed() {
        val base = Cube(
            center = Vector3d.ZERO,
            dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
        ).toCSG()
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -1.0, -1.0),
                max = Vector3d.xyz(1.0, 1.0, 1.0),
            ),
            actual = base.bounds,
            tolerance = 1e-6,
        )
        assertVolumeClose(8.0, base.computeVolume(), relativeTolerance = 1e-4)

        val transformed = base.transformed(Transform.unity().translate(5.0, 0.0, 0.0))
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(4.0, -1.0, -1.0),
                max = Vector3d.xyz(6.0, 1.0, 1.0),
            ),
            actual = transformed.bounds,
            tolerance = 1e-6,
        )
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(-1.0, -1.0, -1.0),
                max = Vector3d.xyz(1.0, 1.0, 1.0),
            ),
            actual = base.bounds,
            tolerance = 1e-6,
        )
        assertVolumeClose(base.computeVolume(), transformed.computeVolume(), relativeTolerance = 1e-4)
    }

    @Test
    fun weightedAppliesFunctionToCopy() {
        val base = Cube(2.0).toCSG()
        val weighted = base.weighted { v, _ -> v.x * 2.0 + 1.0 }

        assertNotSame(base, weighted)
        base.polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                assertEquals(1.0, vertex.weight, 1e-9)
            }
        }
        weighted.polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                assertEquals(vertex.pos.x * 2.0 + 1.0, vertex.weight, 1e-9)
            }
        }
    }

    private fun cubeAtX(centerX: Double): CSG {
        return Cube(
            center = Vector3d.xyz(centerX, 0.0, 0.0),
            dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
        ).toCSG()
    }
}

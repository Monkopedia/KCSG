package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CSGRemeshTest {
    @Test
    fun remeshTriangulatesPolygonsAndPreservesVolumeAndBounds() {
        val original = Cube(2.0).toCSG()

        val remeshed = original.remesh()

        assertNotSame(original, remeshed)
        assertEquals(6, original.polygons.size)
        assertEquals(12, remeshed.polygons.size)
        assertTrue(remeshed.polygons.all { it.vertices.size == 3 })

        assertVolumeClose(
            expected = abs(original.computeVolume()),
            actual = abs(remeshed.computeVolume()),
            relativeTolerance = 1e-9,
            absoluteTolerance = 1e-9
        )
        assertBoundsClose(
            expected = original.bounds,
            actual = remeshed.bounds,
            tolerance = 1e-9
        )
        assertFiniteMesh(remeshed, minNonDegenerateTriangles = 12)
        assertEquals(4, original.polygons[0].vertices.size)
    }

    @Test
    fun remeshPreservesPolygonStorageAcrossTriangulation() {
        val original = Cube(2.0).toCSG()
        val taggedFaceStorage = PropertyStorage()
        taggedFaceStorage["test-tag"] = "remesh"
        original.polygons[0].storage = taggedFaceStorage

        val remeshed = original.remesh()
        val taggedTriangles = remeshed.polygons.filter { it.storage === taggedFaceStorage }

        assertEquals(2, taggedTriangles.size)
        assertTrue(taggedTriangles.all { it.vertices.size == 3 })
    }

    @Test
    fun remeshPreservesVertexWeights() {
        val weighted = Cube(2.0).toCSG().weighted { _, _ -> 0.25 }

        val remeshed = weighted.remesh()

        remeshed.polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                assertEquals(0.25, vertex.weight, 1e-9)
            }
        }
    }

    @Test
    fun remeshHandlesEmptyCsg() {
        val remeshed = CSG.fromPolygons().remesh()
        assertTrue(remeshed.polygons.isEmpty())
    }
}

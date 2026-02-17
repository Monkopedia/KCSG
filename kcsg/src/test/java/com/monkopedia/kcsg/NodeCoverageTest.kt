package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NodeCoverageTest {
    @Test
    fun copyCoversEmptyAndLargePolygonBranches() {
        val empty = Node()
        val emptyCopy = empty.copy()
        assertTrue(emptyCopy.allPolygons().isEmpty())

        val manyCoplanarPolygons = List(205) { index ->
            val x = index.toDouble()
            Polygon.fromPoints(
                Vector3d.xyz(x, 0.0, 0.0),
                Vector3d.xyz(x + 0.5, 0.0, 0.0),
                Vector3d.xyz(x, 0.5, 0.0),
            )
        }
        val populated = Node(manyCoplanarPolygons)
        val populatedCopy = populated.copy()

        val originalPolygons = populated.allPolygons()
        val copiedPolygons = populatedCopy.allPolygons()
        assertEquals(originalPolygons.size, copiedPolygons.size)
        assertNotSame(originalPolygons.first(), copiedPolygons.first())
    }

    @Test
    fun invertHandlesEmptyAndPopulatedNodes() {
        val empty = Node()
        empty.invert()
        assertTrue(empty.allPolygons().isEmpty())

        val polygonNode = Node(
            listOf(
                Polygon.fromPoints(
                    Vector3d.xyz(0.0, 0.0, 0.0),
                    Vector3d.xyz(1.0, 0.0, 0.0),
                    Vector3d.xyz(0.0, 1.0, 0.0),
                )
            )
        )

        polygonNode.invert()
        assertEquals(1, polygonNode.allPolygons().size)
    }
}

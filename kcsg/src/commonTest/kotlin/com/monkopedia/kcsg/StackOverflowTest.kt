package com.monkopedia.kcsg

import com.monkopedia.kcsg.CSG.Companion.fromPolygons
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression coverage for a BSP build that used to recurse until the stack blew up on
 * this pair of nearly-coplanar polygons.
 *
 * Surviving the build is only half the contract: [Node.allPolygons] must also hand the
 * input geometry back. Splitting a polygon against a partition plane may increase the
 * polygon count, but it may never drop geometry, so the total surface area is invariant.
 */
class StackOverflowTest {

    @Test
    fun testOverflow() {
        val polys = listOf(
            Polygon(
                listOf(
                    Vertex(Vector3d(1.23079, 0.35525, -0.39928), Vector3d(0.0, 0.0, 0.0)),
                    Vertex(Vector3d(1.26563, 0.28906, -0.40625), Vector3d(0.0, 0.0, 0.0)),
                    Vertex(Vector3d(1.24346, 0.44424, -0.41733), Vector3d(0.0, 0.0, 0.0))
                )
            ),
            Polygon(
                listOf(
                    Vertex(Vector3d(1.26216, 0.46369, -0.43822), Vector3d(0.0, 0.0, 0.0)),
                    Vertex(Vector3d(1.36719, 0.29688, -0.50000), Vector3d(0.0, 0.0, 0.0)),
                    Vertex(Vector3d(1.35204, 0.32133, -0.34461), Vector3d(0.0, 0.0, 0.0)),
                    Vertex(Vector3d(1.25679, 0.47373, -0.34461), Vector3d(0.0, 0.0, 0.0))
                )
            )
        )
        val res = fromPolygons(polys)

        val node = Node(res.polygons)
        val result = node.allPolygons()

        assertTrue(
            result.size >= polys.size,
            "BSP build dropped polygons: expected at least ${polys.size}, got ${result.size}"
        )
        result.forEachIndexed { index, polygon ->
            assertTrue(
                polygon.vertices.size >= 3,
                "polygon $index degenerated to ${polygon.vertices.size} vertices"
            )
            polygon.vertices.forEachIndexed { vertexIndex, vertex ->
                val pos = vertex.pos
                assertTrue(
                    pos.x.isFinite() && pos.y.isFinite() && pos.z.isFinite(),
                    "polygon $index vertex $vertexIndex is non-finite: $pos"
                )
            }
        }

        val inputArea = surfaceArea(polys)
        assertTrue(inputArea > 0.0, "fixture has no surface area")
        // Splitting these slivers costs about 2e-6 of relative precision, so the bound is
        // loose in absolute terms but still four orders of magnitude tighter than losing
        // either of the two polygons would be.
        assertEquals(
            inputArea,
            surfaceArea(result),
            inputArea * 1e-4,
            "BSP partitioning must conserve surface area"
        )
    }

    private fun surfaceArea(polygons: List<Polygon>): Double {
        var area = 0.0
        for (polygon in polygons) {
            for (triangle in polygon.toTriangles()) {
                val p1 = triangle.vertices[0].pos
                val p2 = triangle.vertices[1].pos
                val p3 = triangle.vertices[2].pos
                area += p2.minus(p1).crossed(p3.minus(p1)).magnitude() / 2.0
            }
        }
        return area
    }
}

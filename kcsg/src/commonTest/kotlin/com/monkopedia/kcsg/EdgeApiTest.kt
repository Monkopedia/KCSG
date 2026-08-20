package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EdgeApiTest {
    @Test
    fun containsEqualsAndHashCode() {
        val v1 = Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE)
        val v2 = Vertex(Vector3d.xyz(2.0, 0.0, 0.0), Vector3d.Z_ONE)
        val edge = Edge(v1, v2)

        assertTrue(edge.contains(Vector3d.xyz(1.0, 0.0, 0.0)))
        assertFalse(edge.contains(Vector3d.xyz(3.0, 0.0, 0.0)))

        val reversed = Edge(v2, v1)
        val sameDirection = Edge(v1.copy(), v2.copy())
        assertEquals(edge, reversed)
        assertEquals(edge, sameDirection)
        assertEquals(edge.hashCode(), sameDirection.hashCode())
    }

    /**
     * Regression test for #42, defect 2: `hashCode` was direction-dependent
     * (`71 * (71 * 7 + p1.hash) + p2.hash`) while `equals` is direction-independent, so
     * `Edge(a, b)` and `Edge(b, a)` compared equal but hashed differently. That violates
     * the `hashCode` contract and makes any hash container behave by insertion order.
     */
    @Test
    fun reversedEdgesHashAlikeAndCollapseInHashContainers() {
        val a = Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE)
        val b = Vertex(Vector3d.xyz(2.0, 0.0, 0.0), Vector3d.Z_ONE)
        val forward = Edge(a, b)
        val reversed = Edge(b, a)

        assertEquals(forward, reversed)
        assertEquals(forward.hashCode(), reversed.hashCode())

        assertTrue(hashSetOf(forward).contains(reversed))
        assertTrue(hashSetOf(reversed).contains(forward))
        assertEquals(1, hashSetOf(forward, reversed).size)
        assertEquals(1, hashSetOf(reversed, forward).size)
    }

    /**
     * Regression test for #42, defect 1: `equals` checked that each of the other edge's
     * endpoints matched *some* endpoint of this edge without requiring the two matches to
     * use *different* endpoints. A degenerate edge `Edge(a, a)` therefore compared equal to
     * every edge sharing an endpoint with it, breaking transitivity.
     */
    @Test
    fun equalsIsTransitiveAroundDegenerateEdges() {
        val a = Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE)
        val b = Vertex(Vector3d.xyz(1.0, 0.0, 0.0), Vector3d.Z_ONE)
        val c = Vertex(Vector3d.xyz(0.0, 1.0, 0.0), Vector3d.Z_ONE)
        val ab = Edge(a, b)
        val ac = Edge(a, c)
        val degenerate = Edge(a, a)

        // ab != ac is the anchor: it holds before and after the fix.
        assertNotEquals(ab, ac)
        // ...so transitivity requires the degenerate edge to differ from both.
        assertNotEquals(ab, degenerate)
        assertNotEquals(degenerate, ab)
        assertNotEquals(ac, degenerate)
        assertNotEquals(degenerate, ac)

        // A shared endpoint alone must not make two distinct edges equal either.
        assertNotEquals(ab, Edge(b, c))
        assertEquals(degenerate, Edge(a, a))
    }

    @Test
    fun equalsRejectsNullAndDifferentClass() {
        val edge = Edge(
            Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(1.0, 0.0, 0.0), Vector3d.Z_ONE),
        )

        assertFalse(edge.equals(null))
        assertFalse(edge.equals("not-an-edge"))
    }

    @Test
    fun closestPointAndIntersection() {
        val e1 = Edge(
            Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(2.0, 2.0, 0.0), Vector3d.Z_ONE),
        )
        val e2 = Edge(
            Vertex(Vector3d.xyz(0.0, 2.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(2.0, 0.0, 0.0), Vector3d.Z_ONE),
        )

        val closest = assertNotNull(e1.getClosestPointOrNull(e2))
        assertVectorClose(Vector3d.xyz(1.0, 1.0, 0.0), closest, 1e-9)

        val intersection = assertNotNull(e1.getIntersectionOrNull(e2))
        assertVectorClose(Vector3d.xyz(1.0, 1.0, 0.0), intersection, 1e-9)
    }

    @Test
    fun closestPointFallsBackToNearestEndpoint() {
        val base = Edge(
            Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(1.0, 0.0, 0.0), Vector3d.Z_ONE),
        )
        val other = Edge(
            Vertex(Vector3d.xyz(-2.0, 1.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(-2.0, -1.0, 1.0), Vector3d.Z_ONE),
        )

        val closest = assertNotNull(base.getClosestPointOrNull(other))
        assertVectorClose(Vector3d.xyz(0.0, 0.0, 0.0), closest, 1e-9)
    }

    @Test
    fun toVerticesToPointsAndToPolygonsHelpers() {
        val boundaryEdges = squareBoundaryEdges()
        val plane = Plane.createFromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
        )

        val vertices = Edge.toVertices(boundaryEdges)
        val points = Edge.toPoints(boundaryEdges)
        assertEquals(4, vertices.size)
        assertEquals(4, points.size)
        assertVectorClose(Vector3d.xyz(0.0, 0.0, 0.0), points[0], 1e-9)

        val polygons = Edge.toPolygons(boundaryEdges, plane)
        assertEquals(1, polygons.size)
        assertEquals(4, polygons[0].vertices.size)
    }

    @Test
    fun toPolygonsRejectsEmptyBoundaryEdgeLists() {
        val plane = Plane.createFromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
        )

        assertFailsWith<IllegalArgumentException> {
            Edge.toPolygons(emptyList(), plane)
        }
    }

    @Test
    fun toPolygonsRejectsOpenBoundaryChains() {
        val plane = Plane.createFromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
        )
        val start = Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE)
        val end = Vertex(Vector3d.xyz(1.0, 0.0, 0.0), Vector3d.Z_ONE)
        val openBoundary = listOf(Edge(start, end))

        assertFailsWith<IllegalArgumentException> {
            Edge.toPolygons(openBoundary, plane)
        }
    }

    @Test
    fun boundaryPathsWithHolesAndBoundaryPolygons() {
        val outer = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(4.0, 0.0, 0.0),
            Vector3d.xyz(4.0, 4.0, 0.0),
            Vector3d.xyz(0.0, 4.0, 0.0),
        )
        val hole = Polygon.fromPoints(
            Vector3d.xyz(1.0, 1.0, 0.0),
            Vector3d.xyz(3.0, 1.0, 0.0),
            Vector3d.xyz(3.0, 3.0, 0.0),
            Vector3d.xyz(1.0, 3.0, 0.0),
        )
        val withHoles = Edge.boundaryPathsWithHoles(listOf(outer, hole))
        val parent = withHoles.first { candidate -> hole.centroid() in candidate }
        val holes = assertNotNull(parent.storage.getValue<List<Polygon>>(Edge.KEY_POLYGON_HOLES))
        assertEquals(1, holes.size)

        val triA = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
        )
        val triB = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 0.0),
        )
        val boundaryPolygons = Edge.boundaryPolygons(CSG.fromPolygons(listOf(triA, triB)))
        assertTrue(boundaryPolygons.isNotEmpty())
        assertTrue(Vector3d.xyz(1.0, 1.0, 0.0) in boundaryPolygons.first())
    }

    @Test
    fun boundaryPolygonsLargeCoplanarSetExercisesParallelStreamPaths() {
        val triangles = buildList {
            repeat(205) { index ->
                val x = index * 10.0
                add(
                    Polygon.fromPoints(
                        Vector3d.xyz(x, 0.0, 0.0),
                        Vector3d.xyz(x + 1.0, 0.0, 0.0),
                        Vector3d.xyz(x, 1.0, 0.0),
                    ),
                )
            }
        }
        val boundaries = Edge.boundaryPolygons(CSG.fromPolygons(triangles))

        assertTrue(boundaries.isNotEmpty())
        boundaries.forEach { polygon ->
            assertEquals(3, polygon.vertices.size)
        }
    }

    @Test
    fun boundaryPolygonsHandlesTjunctionPlaneGroup() {
        val large = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(4.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 4.0, 0.0),
        )
        val tJunction = Polygon.fromPoints(
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 1.0, 0.0),
        )

        val result = Edge.boundaryPolygons(CSG.fromPolygons(listOf(large, tJunction)))
        result.forEach { polygon ->
            assertTrue(polygon.vertices.size >= 3)
        }
    }

    private fun squareBoundaryEdges(): List<Edge> {
        val v1 = Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE)
        val v2 = Vertex(Vector3d.xyz(2.0, 0.0, 0.0), Vector3d.Z_ONE)
        val v3 = Vertex(Vector3d.xyz(2.0, 2.0, 0.0), Vector3d.Z_ONE)
        val v4 = Vertex(Vector3d.xyz(0.0, 2.0, 0.0), Vector3d.Z_ONE)
        return listOf(
            Edge(v1, v2),
            Edge(v2, v3),
            Edge(v3, v4),
            Edge(v4, v1),
        )
    }
}

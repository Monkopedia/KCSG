package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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

        val closest = e1.getClosestPoint(e2)
        assertTrue(closest.isPresent)
        assertVectorClose(Vector3d.xyz(1.0, 1.0, 0.0), closest.get(), 1e-9)

        val intersection = e1.getIntersection(e2)
        assertTrue(intersection.isPresent)
        assertVectorClose(Vector3d.xyz(1.0, 1.0, 0.0), intersection.get(), 1e-9)
    }

    @Test
    fun toVerticesToPointsToPolygonsAndPolygonsHelpers() {
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

        val polygonsFromToPolygons = Edge.toPolygons(boundaryEdges, plane)
        val polygonsFromPolygons = Edge.polygons(boundaryEdges, plane)
        assertEquals(1, polygonsFromToPolygons.size)
        assertEquals(1, polygonsFromPolygons.size)
        assertEquals(4, polygonsFromToPolygons[0].vertices.size)
        assertEquals(4, polygonsFromPolygons[0].vertices.size)
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
        val holes = parent.storage.getValue<List<Polygon>>(Edge.KEY_POLYGON_HOLES)
        assertNotNull(holes)
        assertEquals(1, holes!!.size)

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

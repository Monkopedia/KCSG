package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.Test

class PolyhedronTest {
    @Test
    fun listAndArrayConstructorsPopulatePointsAndFaces() {
        val points = tetraPoints()
        val faces = tetraFaces()

        val listCtor = Polyhedron(points, faces)
        assertEquals(points, listCtor.points)
        assertEquals(faces, listCtor.faces)

        val arrayCtor = Polyhedron(
            points = points.toTypedArray(),
            faces = faces.map { it.toTypedArray() }.toTypedArray(),
        )
        assertEquals(points, arrayCtor.points)
        assertEquals(faces, arrayCtor.faces)
    }

    @Test
    fun toPolygonsBuildsPolygonsFromReferencedFaceIndices() {
        val polyhedron = Polyhedron(tetraPoints(), tetraFaces())
        val polygons = polyhedron.toPolygons()

        assertEquals(polyhedron.faces.size, polygons.size)

        polyhedron.faces.forEachIndexed { index, face ->
            val polygon = polygons[index]
            assertEquals(face.size, polygon.vertices.size)
            face.forEachIndexed { vertexIndex, pointIndex ->
                assertVectorClose(
                    expected = polyhedron.points[pointIndex],
                    actual = polygon.vertices[vertexIndex].pos,
                    tolerance = EPS,
                )
            }
        }
    }

    @Test
    fun getPropertiesIsSharedByGeneratedPolygons() {
        val polyhedron = Polyhedron(tetraPoints(), tetraFaces())
        val properties = polyhedron.getProperties()
        val polygons = polyhedron.toPolygons()

        polygons.forEach { polygon ->
            assertSame(properties, polygon.storage)
        }
    }

    private fun tetraPoints(): List<Vector3d> {
        return listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
            Vector3d.xyz(0.0, 0.0, 1.0),
        )
    }

    private fun tetraFaces(): List<List<Int>> {
        return listOf(
            listOf(0, 2, 1),
            listOf(0, 1, 3),
            listOf(1, 2, 3),
            listOf(2, 0, 3),
        )
    }

    companion object {
        private const val EPS = 1e-9
    }
}

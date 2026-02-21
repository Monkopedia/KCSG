package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class PolygonComprehensiveTest {
    @Test
    fun constructorsAndIsValid() {
        val shared = PropertyStorage()
        val vertices = triangleVertices()

        val withShared = Polygon(vertices, shared)
        assertTrue(withShared.isValid)
        assertSame(shared, withShared.storage)

        val listCtor = Polygon(vertices.map { it.copy() })
        assertTrue(listCtor.isValid)

        val varargCtor = Polygon(*vertices.map { it.copy() }.toTypedArray())
        assertTrue(varargCtor.isValid)
    }

    @Test
    fun copyFlipFlippedAndStlConversion() {
        val polygon = square()
        val copied = polygon.copy()

        assertNotSame(polygon, copied)
        assertEquals(polygon.vertices.map { it.pos }, copied.vertices.map { it.pos })

        val originalNormal = polygon.csgPlane.normal
        polygon.flip()
        assertVectorClose(originalNormal.negated(), polygon.csgPlane.normal, 1e-9)

        val source = square()
        val flippedCopy = source.flipped()
        assertNotSame(source, flippedCopy)
        assertVectorClose(source.csgPlane.normal.negated(), flippedCopy.csgPlane.normal, 1e-9)
        assertVectorClose(Vector3d.Z_ONE, source.csgPlane.normal, 1e-9)

        val stlDirect = source.toStlString()
        val stlBuilder = source.toStlString(StringBuilder()).toString()
        assertEquals(stlDirect, stlBuilder)
        assertTrue(stlDirect.contains("facet normal"))
    }

    @Test
    fun trianglesTranslateTransformAndDerivedGeometry() {
        val polygon = square()
        val triangles = polygon.toTriangles()
        assertEquals(2, triangles.size)

        val translated = polygon.translated(Vector3d.xyz(2.0, 3.0, 4.0))
        assertVectorClose(Vector3d.xyz(3.0, 4.0, 4.0), translated.centroid(), 1e-9)
        assertVectorClose(Vector3d.xyz(1.0, 1.0, 0.0), polygon.centroid(), 1e-9)

        polygon.translate(Vector3d.xyz(1.0, 0.0, 0.0))
        assertVectorClose(Vector3d.xyz(2.0, 1.0, 0.0), polygon.centroid(), 1e-9)

        val transform = Transform.unity().translate(0.0, 0.0, 5.0)
        val transformed = polygon.transformed(transform)
        assertVectorClose(Vector3d.xyz(2.0, 1.0, 5.0), transformed.centroid(), 1e-9)
        assertVectorClose(Vector3d.xyz(2.0, 1.0, 0.0), polygon.centroid(), 1e-9)

        polygon.transform(transform)
        assertVectorClose(Vector3d.xyz(2.0, 1.0, 5.0), polygon.centroid(), 1e-9)
        assertBoundsClose(
            expected = Bounds(
                min = Vector3d.xyz(1.0, 0.0, 5.0),
                max = Vector3d.xyz(3.0, 2.0, 5.0),
            ),
            actual = polygon.bounds,
            tolerance = 1e-9,
        )
    }

    @Test
    fun containsPointContainsPolygonAndStorage() {
        val polygon = square()
        assertTrue(Vector3d.xyz(1.0, 1.0, 0.0) in polygon)
        assertTrue(Vector3d.xyz(1.0, 0.0, 0.0) in polygon)
        assertTrue(Vector3d.xyz(1.0, 1.0, 1.0) !in polygon)

        val inside = Polygon.fromPoints(
            Vector3d.xyz(0.5, 0.5, 0.0),
            Vector3d.xyz(1.5, 0.5, 0.0),
            Vector3d.xyz(0.5, 1.5, 0.0),
        )
        val outside = Polygon.fromPoints(
            Vector3d.xyz(0.5, 0.5, 0.0),
            Vector3d.xyz(2.5, 0.5, 0.0),
            Vector3d.xyz(0.5, 1.5, 0.0),
        )
        assertTrue(inside in polygon)
        assertTrue(outside !in polygon)

        val customStorage = PropertyStorage()
        polygon.storage = customStorage
        assertSame(customStorage, polygon.storage)
    }

    @Test
    fun containsUsesVertexAndEdgeShortcuts() {
        val polygon = square()

        assertTrue(Vector3d.xyz(0.0, 0.0, 0.0) in polygon)
        assertTrue(Vector3d.xyz(1.0, 0.0, 0.0) in polygon)
    }

    @Test
    fun containsSupportsProjectionAcrossPrimaryPlanes() {
        val xy = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 0.0),
        )
        val xz = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 2.0),
            Vector3d.xyz(0.0, 0.0, 2.0),
        )
        val yz = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 2.0),
            Vector3d.xyz(0.0, 0.0, 2.0),
        )

        assertTrue(Vector3d.xyz(1.0, 1.0, 0.0) in xy)
        assertTrue(Vector3d.xyz(1.0, 0.0, 1.0) in xz)
        assertTrue(Vector3d.xyz(0.0, 1.0, 1.0) in yz)

        assertFalse(Vector3d.xyz(3.0, 1.0, 0.0) in xy)
        assertFalse(Vector3d.xyz(3.0, 0.0, 1.0) in xz)
        assertFalse(Vector3d.xyz(0.0, 3.0, 1.0) in yz)
    }

    @Test
    fun fromPointsAndFromConcavePointsOverloads() {
        val points = listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 0.0),
        )
        val shared = PropertyStorage()

        val fromList = Polygon.fromPoints(points)
        val fromVararg = Polygon.fromPoints(*points.toTypedArray())
        val fromListWithShared = Polygon.fromPoints(points, shared)
        assertEquals(4, fromList.vertices.size)
        assertEquals(4, fromVararg.vertices.size)
        assertSame(shared, fromListWithShared.storage)

        val concavePoints = listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
            Vector3d.xyz(1.0, 1.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 0.0),
        )
        val concaveFromList = Polygon.fromConcavePoints(concavePoints)
        val concaveFromVararg = Polygon.fromConcavePoints(*concavePoints.toTypedArray())
        assertTrue(concaveFromList.size >= 2)
        assertEquals(concaveFromList.size, concaveFromVararg.size)
    }

    private fun square(): Polygon {
        return Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 2.0, 0.0),
            Vector3d.xyz(0.0, 2.0, 0.0),
        )
    }

    private fun triangleVertices(): List<Vertex> {
        return listOf(
            Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(1.0, 0.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(0.0, 1.0, 0.0), Vector3d.Z_ONE),
        )
    }
}

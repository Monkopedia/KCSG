package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimitiveDefaultBehaviorTest {
    @Test
    fun toCsgUsesToPolygonsResult() {
        val primitive = RecordingPrimitive(
            polygons = listOf(
                Polygon.fromPoints(
                    Vector3d.xyz(0.0, 0.0, 0.0),
                    Vector3d.xyz(1.0, 0.0, 0.0),
                    Vector3d.xyz(0.0, 1.0, 0.0),
                )
            ),
        )

        val csg = withNoOverride { primitive.toCSG() }

        assertEquals(1, primitive.toPolygonsCalls)
        assertEquals(1, csg.polygons.size)
        assertEquals(primitive.polygons[0].vertices.map { it.pos }, csg.polygons[0].vertices.map { it.pos })
    }

    @Test
    fun toCsgPropagatesPrimitiveStorageToAllPolygons() {
        val primitive = RecordingPrimitive(
            polygons = listOf(
                Polygon.fromPoints(
                    Vector3d.xyz(0.0, 0.0, 0.0),
                    Vector3d.xyz(1.0, 0.0, 0.0),
                    Vector3d.xyz(0.0, 1.0, 0.0),
                ),
                Polygon.fromPoints(
                    Vector3d.xyz(0.0, 0.0, 1.0),
                    Vector3d.xyz(1.0, 0.0, 1.0),
                    Vector3d.xyz(0.0, 1.0, 1.0),
                ),
            ),
        )

        primitive.polygons.forEach { polygon ->
            assertTrue(polygon.storage !== primitive.propertyStorage)
        }

        val csg = withNoOverride { primitive.toCSG() }
        csg.polygons.forEach { polygon ->
            assertSame(primitive.propertyStorage, polygon.storage)
        }
        primitive.polygons.forEach { polygon ->
            assertSame(primitive.propertyStorage, polygon.storage)
        }
    }

    private fun <T> withNoOverride(block: () -> T): T {
        val previous = CSG.opOverride
        return try {
            CSG.opOverride = null
            block()
        } finally {
            CSG.opOverride = previous
        }
    }

    private class RecordingPrimitive(
        val polygons: List<Polygon>,
    ) : Primitive {
        val propertyStorage = PropertyStorage()
        var toPolygonsCalls = 0

        override fun toPolygons(): List<Polygon> {
            toPolygonsCalls++
            return polygons
        }

        override fun getProperties(): PropertyStorage {
            return propertyStorage
        }
    }
}

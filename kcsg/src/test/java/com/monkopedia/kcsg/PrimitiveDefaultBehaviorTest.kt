package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStream
import java.nio.file.Path

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

    @Test
    fun toCsgUsesOverrideShortCircuitWhenPresent() {
        val primitive = RecordingPrimitive(
            polygons = listOf(
                Polygon.fromPoints(
                    Vector3d.xyz(0.0, 0.0, 0.0),
                    Vector3d.xyz(1.0, 0.0, 0.0),
                    Vector3d.xyz(0.0, 1.0, 0.0),
                )
            ),
        )
        val overridden = CSG.fromPolygons(
            Polygon.fromPoints(
                Vector3d.xyz(0.0, 0.0, 0.0),
                Vector3d.xyz(2.0, 0.0, 0.0),
                Vector3d.xyz(0.0, 2.0, 0.0),
            )
        )
        val override = object : OpOverride {
            override fun operation(s: String, vararg csg: Any?): CSG? {
                return if (s == "toCSG") overridden else null
            }

            override fun bounds(s: String, vararg csg: Any?): Bounds? = null

            override fun double(s: String, vararg csg: Any?): Double? = null

            override fun file(path: Path): CSG? = null

            override fun inputStream(inputStreamFactory: () -> InputStream): CSG? = null
        }

        val csg = CSG.withOverride(override) { primitive.toCSG() }

        assertSame(overridden, csg)
        assertEquals(0, primitive.toPolygonsCalls)
        assertNotSame(primitive.propertyStorage, csg.polygons[0].storage)
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

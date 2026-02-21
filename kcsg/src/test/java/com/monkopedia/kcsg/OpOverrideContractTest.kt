package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.RecordingOpOverride
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.file.Paths

class OpOverrideContractTest {
    @Test
    fun overrideDispatchesAcrossCsgStlBoundsAndPrimitiveEntrypoints() {
        val base = Cube(1.0).toCSG()
        val other = Cube(center = Vector3d.xyz(2.0, 0.0, 0.0), dimensions = Vector3d.xyz(1.0, 1.0, 1.0)).toCSG()
        val primitive = object : Primitive {
            override fun toPolygons(): List<Polygon> = listOf(
                Polygon.fromPoints(
                    Vector3d.xyz(0.0, 0.0, 0.0),
                    Vector3d.xyz(1.0, 0.0, 0.0),
                    Vector3d.xyz(0.0, 1.0, 0.0),
                ),
            )

            override fun getProperties(): PropertyStorage = PropertyStorage()
        }

        val unionSentinel = emptyCsg()
        val remeshSentinel = emptyCsg()
        val boundsToCsgSentinel = emptyCsg()
        val primitiveToCsgSentinel = emptyCsg()
        val stlFileSentinel = emptyCsg()
        val stlStreamSentinel = emptyCsg()
        val expectedBounds = Bounds(
            min = Vector3d.xyz(-10.0, -10.0, -10.0),
            max = Vector3d.xyz(10.0, 10.0, 10.0),
        )
        val expectedVolume = 123.0

        val override = RecordingOpOverride(
            operationResult = { name, _ ->
                when (name) {
                    "union" -> unionSentinel
                    "remesh" -> remeshSentinel
                    "boundsToCSG" -> boundsToCsgSentinel
                    "toCSG" -> primitiveToCsgSentinel
                    else -> null
                }
            },
            boundsResult = { name, _ ->
                if (name == "bounds") expectedBounds else null
            },
            doubleResult = { name, _ ->
                if (name == "volume") expectedVolume else null
            },
            fileResult = { stlFileSentinel },
            streamResult = { stlStreamSentinel },
        )

        val previous = CSG.opOverride
        try {
            CSG.opOverride = override

            assertSame(unionSentinel, base.union(other))
            assertSame(remeshSentinel, base.remesh())
            assertEquals(expectedBounds, base.bounds)
            assertEquals(expectedVolume, base.computeVolume(), 0.0)
            assertSame(boundsToCsgSentinel, Bounds(Vector3d.ZERO, Vector3d.UNITY).toCSG())
            assertSame(primitiveToCsgSentinel, primitive.toCSG())
            assertSame(stlFileSentinel, STL.file(Paths.get("unused.stl")))
            assertSame(
                stlStreamSentinel,
                STL.from(
                    inputStreamFactory = { ByteArrayInputStream("abc".toByteArray()) },
                    length = { 3L },
                ),
            )

            assertTrue(override.operationCalls.any { it.first == "union" })
            assertTrue(override.operationCalls.any { it.first == "remesh" })
            assertTrue(override.operationCalls.any { it.first == "boundsToCSG" })
            assertTrue(override.operationCalls.any { it.first == "toCSG" })
            assertTrue(override.boundsCalls.any { it.first == "bounds" })
            assertTrue(override.doubleCalls.any { it.first == "volume" })
            assertEquals(1, override.fileCalls.size)
            assertEquals(1, override.inputStreamCalls.size)
        } finally {
            CSG.opOverride = previous
        }
    }

    private fun emptyCsg(): CSG {
        return CSG.withOverride(null) { CSG.fromPolygons() }
    }
}

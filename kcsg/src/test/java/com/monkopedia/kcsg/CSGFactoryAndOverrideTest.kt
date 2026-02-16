package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.RecordingOpOverride
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class CSGFactoryAndOverrideTest {
    @Test
    fun fromPolygonsOverloadsCreateEquivalentCsgs() {
        val polygon = triangle(0.0)
        val fromList = CSG.fromPolygons(listOf(polygon))
        val fromVararg = CSG.fromPolygons(polygon)

        assertEquals(1, fromList.polygons.size)
        assertEquals(1, fromVararg.polygons.size)
        assertEquals(
            fromList.polygons[0].vertices.map { it.pos },
            fromVararg.polygons[0].vertices.map { it.pos },
        )

        val storage = PropertyStorage()
        val storageListPolygon = triangle(1.0)
        val storageVarargPolygon = triangle(2.0)

        val withStorageList = CSG.fromPolygons(storage, listOf(storageListPolygon))
        val withStorageVararg = CSG.fromPolygons(storage, storageVarargPolygon)

        assertSame(storage, withStorageList.polygons[0].storage)
        assertSame(storage, withStorageVararg.polygons[0].storage)
    }

    @Test
    fun setDefaultOptTypeInfluencesOperationsWithoutExplicitOptimization() {
        try {
            CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND)
            val polygonBoundUnion = Cube(2.0).toCSG().union(
                Cube(center = Vector3d.xyz(3.0, 0.0, 0.0), dimensions = Vector3d.xyz(2.0, 2.0, 2.0)).toCSG(),
            )
            assertVolumeClose(16.0, polygonBoundUnion.computeVolume(), relativeTolerance = 1e-4)

            CSG.setDefaultOptType(CSG.OptType.CSG_BOUND)
            val csgBoundUnion = Cube(2.0).toCSG().union(
                Cube(center = Vector3d.xyz(3.0, 0.0, 0.0), dimensions = Vector3d.xyz(2.0, 2.0, 2.0)).toCSG(),
            )
            assertVolumeClose(16.0, csgBoundUnion.computeVolume(), relativeTolerance = 1e-4)
        } finally {
            CSG.setDefaultOptType(CSG.OptType.NONE)
        }
    }

    @Test
    fun opOverrideCanInterceptFactoryCalls() {
        val previousOverride = CSG.opOverride
        val sentinel = Cube(1.0).toCSG()
        val override = RecordingOpOverride(
            operationResult = { name, _ ->
                if (name == "fromPolygons") sentinel else null
            },
        )

        try {
            CSG.opOverride = override
            val result = CSG.fromPolygons(listOf(triangle(0.0)))
            assertSame(sentinel, result)
            assertEquals("fromPolygons", override.operationCalls.single().first)
        } finally {
            CSG.opOverride = previousOverride
        }
    }

    @Test
    fun withOverrideRestoresPreviousOverrideOnSuccessAndFailure() {
        val previousOverride = CSG.opOverride
        val sentinel = Cube(1.0).toCSG()
        val override = RecordingOpOverride(
            operationResult = { name, _ ->
                if (name == "fromPolygons") sentinel else null
            },
        )

        val successResult = CSG.withOverride(override) {
            CSG.fromPolygons(listOf(triangle(0.0)))
        }
        assertSame(sentinel, successResult)
        assertSame(previousOverride, CSG.opOverride)

        assertThrows(IllegalStateException::class.java) {
            CSG.withOverride(override) {
                throw IllegalStateException("boom")
            }
        }
        assertSame(previousOverride, CSG.opOverride)
    }

    private fun triangle(z: Double): Polygon {
        return Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, z),
            Vector3d.xyz(1.0, 0.0, z),
            Vector3d.xyz(0.0, 1.0, z),
        )
    }
}

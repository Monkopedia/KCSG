package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlinx.io.Source
import kotlinx.io.files.Path

class CSGCoverageBranchTest {
    @Test
    fun differenceAndIntersectEmptyListReturnCopies() {
        val base = Cube(2.0).toCSG()

        val diffEmpty = base.difference(emptyList())
        val intersectEmpty = base.intersect(emptyList())

        assertNotSame(base, diffEmpty)
        assertNotSame(base, intersectEmpty)
        assertVolumeClose(base.computeVolume(), diffEmpty.computeVolume(), relativeTolerance = 1e-4)
        assertVolumeClose(base.computeVolume(), intersectEmpty.computeVolume(), relativeTolerance = 1e-4)
    }

    @Test
    fun differenceWithEmptyRightOperandReturnsReceiver() {
        val base = Cube(2.0).toCSG()
        val empty = CSG.fromPolygons()

        val result = base.difference(empty)

        assertSame(base, result)
    }

    @Test
    fun differenceFallbackCatchPathRecoversAfterOneShotBoundsFailure() {
        val left = Cube(2.0).toCSG().optimization(CSG.OptType.CSG_BOUND)
        val right = Cube(
            center = Vector3d.xyz(0.5, 0.0, 0.0),
            dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
        ).toCSG()
        val baseline = left.difference(right)

        val forcedFallback = CSG.withOverride(ThrowOnceBoundsOverride()) {
            left.difference(right)
        }

        assertFiniteMesh(forcedFallback)
        assertVolumeClose(
            baseline.computeVolume(),
            forcedFallback.computeVolume(),
            relativeTolerance = 1e-4,
        )
    }

    @Test
    fun transformedReturnsCopyWhenPolygonListIsEmpty() {
        val empty = CSG.fromPolygons()

        val transformed = empty.transformed(Transform.unity().translate(1.0, 2.0, 3.0))

        assertNotSame(empty, transformed)
        assertTrue(transformed.polygons.isEmpty())
    }

    @Test
    fun toObjStringEmitsFacesForSolid() {
        val obj = Cube(1.0).toCSG().toObjString()

        assertTrue(obj.contains("# Faces"))
        assertTrue(obj.contains("\nf "))

        // Every face line must reference three valid 1-based vertex indices
        // (a vertex line 'v ' must exist for each referenced index).
        val vertexCount = obj.lineSequence().count { it.startsWith("v ") }
        assertTrue(vertexCount > 0)
        val faceLines = obj.lineSequence().filter { it.startsWith("f ") }.toList()
        assertTrue(faceLines.isNotEmpty())
        for (face in faceLines) {
            val refs = face.removePrefix("f ").trim().split(" ").map { it.toInt() }
            assertEquals(3, refs.size)
            for (ref in refs) {
                assertTrue(ref in 1..vertexCount)
            }
        }
    }

    private class ThrowOnceBoundsOverride : OpOverride {
        private var hasThrown = false

        override fun operation(s: String, vararg csg: Any?): CSG? = null

        override fun bounds(s: String, vararg csg: Any?): Bounds? {
            if (!hasThrown) {
                hasThrown = true
                throw IllegalStateException("forced bounds failure")
            }
            return null
        }

        override fun double(s: String, vararg csg: Any?): Double? = null

        override fun file(path: Path): CSG? = null

        override fun source(sourceFactory: () -> Source): CSG? = null
    }
}

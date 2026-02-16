package com.monkopedia.kcsg.testutil

import com.monkopedia.kcsg.Bounds
import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Vector3d
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import kotlin.math.abs
import kotlin.math.max

object GeometryAssertions {
    @JvmStatic
    @JvmOverloads
    fun assertVectorClose(
        expected: Vector3d,
        actual: Vector3d,
        tolerance: Double = 1e-6,
        message: String? = null,
    ) {
        val prefix = if (message.isNullOrBlank()) "" else "$message: "
        assertEquals("${prefix}x mismatch", expected.x, actual.x, tolerance)
        assertEquals("${prefix}y mismatch", expected.y, actual.y, tolerance)
        assertEquals("${prefix}z mismatch", expected.z, actual.z, tolerance)
    }

    @JvmStatic
    @JvmOverloads
    fun assertBoundsClose(
        expected: Bounds,
        actual: Bounds,
        tolerance: Double = 1e-6,
        message: String? = null,
    ) {
        assertVectorClose(expected.min, actual.min, tolerance, withPrefix(message, "min"))
        assertVectorClose(expected.max, actual.max, tolerance, withPrefix(message, "max"))
    }

    @JvmStatic
    @JvmOverloads
    fun assertFiniteMesh(
        csg: CSG,
        minNonDegenerateTriangles: Int = 1,
        areaEpsilon: Double = 1e-12,
    ) {
        var nonDegenerateTriangles = 0
        csg.polygons.forEachIndexed { polyIndex, polygon ->
            polygon.vertices.forEachIndexed { vertexIndex, vertex ->
                val pos = vertex.pos
                if (!pos.x.isFinite() || !pos.y.isFinite() || !pos.z.isFinite()) {
                    fail("Non-finite vertex at polygon=$polyIndex vertex=$vertexIndex value=$pos")
                }
            }

            polygon.toTriangles().forEachIndexed { triIndex, tri ->
                val p1 = tri.vertices[0].pos
                val p2 = tri.vertices[1].pos
                val p3 = tri.vertices[2].pos
                val twiceArea = p2.minus(p1).crossed(p3.minus(p1)).magnitude()
                if (twiceArea > areaEpsilon) {
                    nonDegenerateTriangles++
                }
            }
        }

        if (nonDegenerateTriangles < minNonDegenerateTriangles) {
            fail(
                "Expected at least $minNonDegenerateTriangles non-degenerate triangles " +
                    "but found $nonDegenerateTriangles"
            )
        }
    }

    @JvmStatic
    @JvmOverloads
    fun assertVolumeClose(
        expected: Double,
        actual: Double,
        absoluteTolerance: Double = 1e-6,
        relativeTolerance: Double = 1e-4,
        message: String? = null,
    ) {
        val delta = abs(expected - actual)
        val allowed = max(absoluteTolerance, abs(expected) * relativeTolerance)
        if (delta > allowed) {
            fail(
                "${withPrefix(message, "volume mismatch")}" +
                    "expected=$expected actual=$actual delta=$delta allowed=$allowed"
            )
        }
    }

    @JvmStatic
    @JvmOverloads
    fun assertVolumeClose(
        expected: Double,
        csg: CSG,
        absoluteTolerance: Double = 1e-6,
        relativeTolerance: Double = 1e-4,
        message: String? = null,
    ) {
        assertVolumeClose(
            expected = expected,
            actual = csg.computeVolume(),
            absoluteTolerance = absoluteTolerance,
            relativeTolerance = relativeTolerance,
            message = message,
        )
    }

    private fun withPrefix(message: String?, suffix: String): String {
        return if (message.isNullOrBlank()) suffix else "$message $suffix"
    }
}

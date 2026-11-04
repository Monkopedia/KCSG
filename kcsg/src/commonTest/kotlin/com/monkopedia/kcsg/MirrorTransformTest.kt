package com.monkopedia.kcsg

import com.monkopedia.kcsg.ext.vvecmath.Plane
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression tests for #89: [Transform.mirror] must reflect across the plane itself
 * (p' = p - 2(n.p - w)n with w = n.anchor), including planes that do not pass through the
 * origin, and must compose with other transforms without shearing.
 */
class MirrorTransformTest {

    private val probes = listOf(
        Vector3d.ZERO,
        Vector3d.xyz(1.0, 2.0, 3.0),
        Vector3d.xyz(-4.5, 0.25, 7.0),
        Vector3d.xyz(10.0, -3.0, -2.0),
    )

    private val planes = mapOf(
        "x=5" to Plane.fromPointAndNormal(Vector3d.xyz(5.0, 0.0, 0.0), Vector3d.X_ONE),
        // Negative side of the origin: dist is |anchor| = 3, but the plane's offset is -3.
        "y=-3" to Plane.fromPointAndNormal(Vector3d.xyz(0.0, -3.0, 0.0), Vector3d.Y_ONE),
        "z=5" to Plane.fromPointAndNormal(Vector3d.xyz(0.0, 0.0, 5.0), Vector3d.Z_ONE),
        // Same plane as z=5 with the normal flipped: n.anchor = -5 while dist = 5.
        "z=5 flipped" to Plane.fromPointAndNormal(Vector3d.xyz(0.0, 0.0, 5.0), Vector3d.Z_ONE.negated()),
        // Oblique, anchor not the closest point to the origin.
        "oblique" to Plane.fromPointAndNormal(Vector3d.xyz(1.0, 2.0, 3.0), Vector3d.xyz(1.0, 1.0, 0.0)),
        // fromPoints anchors at the triangle centroid (5, 1/3, 1/3): |anchor| != 5.
        "fromPoints x=5" to Plane.fromPoints(
            Vector3d.xyz(5.0, 0.0, 0.0),
            Vector3d.xyz(5.0, 1.0, 0.0),
            Vector3d.xyz(5.0, 0.0, 1.0),
        ),
    )

    private fun reflect(p: Vector3d, plane: Plane): Vector3d {
        val n = plane.normal
        val w = n.dot(plane.anchor)
        return p - n * (2.0 * (n.dot(p) - w))
    }

    @Test
    fun issueReproPointsReflectAcrossOffsetPlanes() {
        val px5 = planes.getValue("x=5")
        val pz5 = planes.getValue("z=5")
        assertVectorClose(Vector3d.xyz(10.0, 0.0, 0.0), Transform.unity().mirror(px5).transform(Vector3d.ZERO), EPS)
        assertVectorClose(Vector3d.xyz(0.0, 0.0, 10.0), Transform.unity().mirror(pz5).transform(Vector3d.ZERO), EPS)
        assertVectorClose(
            Vector3d.xyz(0.0, -6.0, 0.0),
            Transform.unity().mirror(planes.getValue("y=-3")).transform(Vector3d.ZERO),
            EPS,
        )
        assertVectorClose(
            Vector3d.xyz(10.0, 2.0, 3.0),
            Transform.unity().mirror(planes.getValue("fromPoints x=5")).transform(Vector3d.xyz(0.0, 2.0, 3.0)),
            EPS,
        )
    }

    @Test
    fun mirrorMatchesAnalyticReflectionForEveryPlane() {
        for ((name, plane) in planes) {
            val t = Transform.unity().mirror(plane)
            assertTrue(t.isMirror, "$name: isMirror")
            for (p in probes) {
                assertVectorClose(reflect(p, plane), t.transform(p), EPS, "$name p=$p")
            }
            // A point on the plane is fixed.
            assertVectorClose(plane.anchor, t.transform(plane.anchor), EPS, "$name anchor fixed")
        }
    }

    @Test
    fun mirrorMatrixHasAffineBottomRow() {
        for ((name, plane) in planes) {
            val values = Transform.unity().mirror(plane).to(DoubleArray(16))
            assertEquals(0.0, values[12], EPS, "$name m30")
            assertEquals(0.0, values[13], EPS, "$name m31")
            assertEquals(0.0, values[14], EPS, "$name m32")
            assertEquals(1.0, values[15], EPS, "$name m33")
        }
    }

    @Test
    fun mirrorTwiceIsIdentity() {
        val identity = Transform.unity().to(DoubleArray(16))
        for ((name, plane) in planes) {
            val values = Transform.unity().mirror(plane).mirror(plane).to(DoubleArray(16))
            for (i in 0 until 16) {
                assertEquals(identity[i], values[i], EPS, "$name element $i")
            }
        }
    }

    @Test
    fun mirrorComposesWithTranslateAndRotateInBothOrders() {
        val offset = Vector3d.xyz(1.0, -2.0, 4.0)
        val rot = Transform.unity().rotZ(30.0)
        for ((name, plane) in planes) {
            for (p in probes) {
                // unity().a().b() applies b first, then a.
                assertVectorClose(
                    reflect(p, plane) + offset,
                    Transform.unity().translate(offset).mirror(plane).transform(p),
                    EPS,
                    "$name translate.mirror p=$p",
                )
                assertVectorClose(
                    reflect(p + offset, plane),
                    Transform.unity().mirror(plane).translate(offset).transform(p),
                    EPS,
                    "$name mirror.translate p=$p",
                )
                assertVectorClose(
                    rot.transform(reflect(p, plane)),
                    Transform.unity().rotZ(30.0).mirror(plane).transform(p),
                    EPS,
                    "$name rotZ.mirror p=$p",
                )
                assertVectorClose(
                    reflect(rot.transform(p), plane),
                    Transform.unity().mirror(plane).rotZ(30.0).transform(p),
                    EPS,
                    "$name mirror.rotZ p=$p",
                )
            }
        }
    }

    @Test
    fun mirroredCubeLandsReflectedAndStaysOutwardFacing() {
        val pz5 = planes.getValue("z=5")
        val cube = Cube(2.0).toCSG()

        val mirrored = cube.transformed(Transform.unity().mirror(pz5))
        assertBoundsClose(
            Bounds(Vector3d.xyz(-1.0, -1.0, 9.0), Vector3d.xyz(1.0, 1.0, 11.0)),
            mirrored.bounds,
            EPS,
        )
        assertVolumeClose(8.0, mirrored)
        assertOutwardFacing(mirrored, Vector3d.xyz(0.0, 0.0, 10.0))

        // Issue repro: a prior translate previously sheared this into a 22x2x2 box.
        val shifted = cube.transformed(Transform.unity().translate(1.0, 0.0, 0.0).mirror(pz5))
        assertBoundsClose(
            Bounds(Vector3d.xyz(0.0, -1.0, 9.0), Vector3d.xyz(2.0, 1.0, 11.0)),
            shifted.bounds,
            EPS,
        )
        assertVolumeClose(8.0, shifted)
        assertOutwardFacing(shifted, Vector3d.xyz(1.0, 0.0, 10.0))
    }

    /**
     * computeVolume() is an absolute value, so it cannot see an inside-out solid. Check the
     * signed volume and that every face normal of this convex solid points away from its centre.
     */
    private fun assertOutwardFacing(csg: CSG, center: Vector3d) {
        var signed = 0.0
        for (poly in csg.polygons) {
            for (tri in poly.toTriangles()) {
                val p1 = tri.vertices[0].pos
                val p2 = tri.vertices[1].pos
                val p3 = tri.vertices[2].pos
                signed += p1.dot(p2.crossed(p3)) / 6.0
            }
            val centroid = poly.vertices
                .fold(Vector3d.ZERO) { acc, v -> acc + v.pos } * (1.0 / poly.vertices.size)
            assertTrue(
                poly.csgPlane.normal.dot(centroid - center) > 0.0,
                "face normal ${poly.csgPlane.normal} at $centroid points inward",
            )
        }
        assertTrue(signed > 0.0, "signed volume $signed should be positive")
    }

    private companion object {
        const val EPS = 1e-9
    }
}

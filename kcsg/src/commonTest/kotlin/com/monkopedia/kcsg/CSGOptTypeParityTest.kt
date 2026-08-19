package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import kotlin.test.Test

class CSGOptTypeParityTest {
    @Test
    fun overlappingOperationsAreParityStableAcrossOptTypes() {
        val left = cubeAtX(0.0)
        val right = cubeAtX(0.8)

        assertParity(
            operation = { optType -> left.copy().optimization(optType).union(right.copy()) },
        )
        assertParity(
            operation = { optType -> left.copy().optimization(optType).difference(right.copy()) },
        )
        assertParity(
            operation = { optType -> left.copy().optimization(optType).intersect(right.copy()) },
        )
    }

    @Test
    fun disjointOperationsAreParityStableAcrossOptTypes() {
        val left = cubeAtX(0.0)
        val right = cubeAtX(4.0)

        assertParity(
            operation = { optType -> left.copy().optimization(optType).union(right.copy()) },
        )
        assertParity(
            operation = { optType -> left.copy().optimization(optType).difference(right.copy()) },
        )
        // intersect is deliberately absent: a disjoint intersection is the empty solid,
        // which assertParity's assertFiniteMesh check cannot describe.
    }

    /**
     * Regression for issue #58.
     *
     * `differenceNoOpt` used to hand back an inside-out copy of the subtrahend when its
     * own polygon list was empty, and both bounds-optimized difference paths feed it an
     * empty left operand whenever no part of `this` falls inside the right operand's
     * bounding box. The result was `left` plus a negative-volume ghost of `right`, so
     * `difference` under `CSG_BOUND`/`POLYGON_BOUND` reported `volume(left) - volume(right)`
     * and grew bounds to cover a solid it was supposed to leave untouched.
     *
     * Every case below is a pair with zero shared volume, so `a.difference(b)` must return
     * `a` unchanged under every optimization type. The "tangent" labels describe the intended
     * placement of the analytic solids; the tessellated sphere and cylinder actually fall a
     * fraction short of contact, which is incidental — the trigger is an empty overlap, not
     * an exactly-shared plane.
     */
    @Test
    fun differenceWithNonOverlappingOperandLeavesLeftUnchanged() {
        val cases = listOf(
            Triple("disjoint cubes", cubeAtX(0.0), cubeAtX(4.0)),
            // Exactly face-coplanar, no tessellation gap. Under CSG_BOUND this used to
            // come out with the *right* volume and the *wrong* bounds -- the right
            // operand's -- so only the bounds assertion below catches it.
            Triple("face-tangent cubes (exact)", cubeAtX(0.0), cubeAtX(2.0)),
            // A receiver much larger than the subtrahend, face-tangent to it. Under
            // CSG_BOUND this used to return the subtrahend outright: the 72.0 slab
            // vanished and the result was the 8.0 cube, bounds and all.
            Triple("face-tangent slab/cube", slabEndingAtX(1.0), cubeAtX(2.0)),
            Triple("face-tangent cube/sphere", cubeAtX(0.0), sphereAt(2.0, 0.0, 0.0)),
            Triple("face-tangent sphere/cylinder", sphereAt(0.0, 0.0, 0.0), cylinderAt(2.0, 0.0, 0.0)),
            Triple("edge-tangent cube/cylinder", cubeAtX(0.0), cylinderAt(2.0, 2.0, 0.0)),
            Triple("vertex-tangent sphere/cylinder", sphereAt(0.0, 0.0, 0.0), cylinderAt(2.0, 2.0, 2.0)),
        )

        cases.forEach { (name, left, right) ->
            val expectedVolume = left.computeVolume()
            val expectedBounds = left.bounds

            CSG.OptType.entries.forEach { optType ->
                val result = left.copy().optimization(optType).difference(right.copy())
                assertFiniteMesh(result)
                assertVolumeClose(
                    expected = expectedVolume,
                    actual = result.computeVolume(),
                    absoluteTolerance = 1e-6,
                    relativeTolerance = 1e-6,
                    message = "$name opt=$optType difference volume",
                )
                assertBoundsClose(
                    expected = expectedBounds,
                    actual = result.bounds,
                    tolerance = 1e-6,
                    message = "$name opt=$optType difference bounds",
                )
            }
        }
    }

    /**
     * Companion regression to [differenceWithNonOverlappingOperandLeavesLeftUnchanged],
     * guarding the *other* reading of an empty intermediate.
     *
     * `differencePolygonBoundsOpt` splits the receiver on `csg.bounds.intersects(polygon.bounds)`.
     * That is a proximity test, not a containment test, so the "inner" side is empty both when
     * `csg` is disjoint from the receiver *and* when `csg` sits strictly inside it, clear of
     * every face. The two demand opposite answers — the receiver untouched in the first case,
     * the receiver with an internal void in the second — so neither an unconditional early-out
     * nor an empty-receiver guard further down can serve both. A fix for the disjoint half that
     * silently deletes the cavity in the containment half is a regression of the same size,
     * pointed the other way.
     *
     * `OptType.NONE` is the reference implementation, so it is the oracle here rather than an
     * analytic volume; because every variant tessellates the same primitives, the comparison can
     * be exact-ish instead of tolerance-padded. The cavity in `cube-in-cube` is 1.728 of 125, so
     * anything looser than roughly 1e-2 relative stops being able to see it — see the note on
     * `PrimitiveInteractionMatrixTest.scenarioS3FullContainment`, whose 3e-2 budget is why this
     * gap survived a green suite.
     */
    @Test
    fun differenceWithStrictlyInteriorOperandKeepsCavityUnderEveryOptType() {
        val cases = listOf(
            Triple("cube-in-cube", cubeOfSize(5.0), cubeOfSize(1.2)),
            Triple("sphere-in-cube", cubeOfSize(5.0), sphereOfRadius(0.6)),
            Triple("sphere-in-sphere", sphereOfRadius(2.5), sphereOfRadius(0.6)),
            Triple("cylinder-in-cube", cubeOfSize(5.0), cylinderOfRadius(0.6)),
        )

        cases.forEach { (name, left, right) ->
            val leftVolume = left.computeVolume()
            val rightVolume = right.computeVolume()
            val baseline = left.copy().optimization(CSG.OptType.NONE).difference(right.copy())
            assertFiniteMesh(baseline)

            // Guard the fixture itself: if the subtrahend ever stopped being strictly
            // interior, or the reference difference stopped cutting a void, the parity
            // assertions below would still pass while testing nothing.
            assertVolumeClose(
                expected = leftVolume - rightVolume,
                actual = baseline.computeVolume(),
                relativeTolerance = 1e-3,
                message = "$name fixture: OptType.NONE must actually remove the subtrahend",
            )

            listOf(CSG.OptType.CSG_BOUND, CSG.OptType.POLYGON_BOUND).forEach { optType ->
                val result = left.copy().optimization(optType).difference(right.copy())
                assertFiniteMesh(result)
                assertVolumeClose(
                    expected = baseline.computeVolume(),
                    actual = result.computeVolume(),
                    absoluteTolerance = 1e-9,
                    relativeTolerance = 1e-9,
                    message = "$name opt=$optType difference must match OptType.NONE",
                )
                assertBoundsClose(
                    expected = baseline.bounds,
                    actual = result.bounds,
                    tolerance = 1e-9,
                    message = "$name opt=$optType difference bounds must match OptType.NONE",
                )
            }
        }
    }

    private fun assertParity(operation: (CSG.OptType) -> CSG) {
        val baseline = operation(CSG.OptType.NONE)
        assertFiniteMesh(baseline)

        listOf(CSG.OptType.CSG_BOUND, CSG.OptType.POLYGON_BOUND).forEach { optType ->
            val candidate = operation(optType)
            assertFiniteMesh(candidate)
            assertBoundsClose(baseline.bounds, candidate.bounds, tolerance = 1e-6)
            assertVolumeClose(
                expected = baseline.computeVolume(),
                actual = candidate.computeVolume(),
                absoluteTolerance = 1e-6,
                relativeTolerance = 1e-4,
                message = "optType=$optType",
            )
        }
    }

    private fun cubeAtX(centerX: Double): CSG {
        return Cube(
            center = Vector3d.xyz(centerX, 0.0, 0.0),
            dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
        ).toCSG()
    }

    /**
     * An 18x2x2 slab spanning x in [-17, 1], so that its +x face is exactly coplanar with
     * the -x face of `cubeAtX(2.0)`. Volume 72, nine times the cube it is tangent to.
     */
    private fun slabEndingAtX(maxX: Double): CSG {
        return Cube(
            center = Vector3d.xyz(maxX - 9.0, 0.0, 0.0),
            dimensions = Vector3d.xyz(18.0, 2.0, 2.0),
        ).toCSG()
    }

    private fun cubeOfSize(size: Double): CSG {
        return Cube(
            center = Vector3d.xyz(0.0, 0.0, 0.0),
            dimensions = Vector3d.xyz(size, size, size),
        ).toCSG()
    }

    private fun sphereOfRadius(radius: Double): CSG {
        return Sphere(radius, 18, 9, Vector3d.xyz(0.0, 0.0, 0.0)).toCSG()
    }

    private fun cylinderOfRadius(radius: Double): CSG {
        return Cylinder(
            start = Vector3d.xyz(0.0, 0.0, -radius),
            end = Vector3d.xyz(0.0, 0.0, radius),
            startRadius = radius,
            endRadius = radius,
            numSlices = 18,
        ).toCSG()
    }

    private fun sphereAt(x: Double, y: Double, z: Double): CSG {
        return Sphere(1.0, 18, 9, Vector3d.xyz(x, y, z)).toCSG()
    }

    private fun cylinderAt(x: Double, y: Double, z: Double): CSG {
        return Cylinder(
            start = Vector3d.xyz(x, y, z - 1.0),
            end = Vector3d.xyz(x, y, z + 1.0),
            startRadius = 1.0,
            endRadius = 1.0,
            numSlices = 18,
        ).toCSG()
    }
}

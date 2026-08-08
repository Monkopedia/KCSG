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

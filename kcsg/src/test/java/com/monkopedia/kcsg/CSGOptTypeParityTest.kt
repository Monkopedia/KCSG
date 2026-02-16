package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import org.junit.Test

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
    fun disjointUnionIsParityStableAcrossOptTypes() {
        val left = cubeAtX(0.0)
        val right = cubeAtX(4.0)

        assertParity(
            operation = { optType -> left.copy().optimization(optType).union(right.copy()) },
        )
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
}

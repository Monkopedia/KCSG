package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import kotlin.test.Test
import kotlin.test.assertTrue

class EdgeJvmLegacyApiTest {
    @Test
    @Suppress("DEPRECATION")
    fun legacyJvmOptionalAccessorsRemainAvailable() {
        val e1 = Edge(
            Vertex(Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(2.0, 2.0, 0.0), Vector3d.Z_ONE),
        )
        val e2 = Edge(
            Vertex(Vector3d.xyz(0.0, 2.0, 0.0), Vector3d.Z_ONE),
            Vertex(Vector3d.xyz(2.0, 0.0, 0.0), Vector3d.Z_ONE),
        )

        val closest: java.util.Optional<Vector3d> = e1.getClosestPoint(e2)
        assertTrue(closest.isPresent)
        assertVectorClose(Vector3d.xyz(1.0, 1.0, 0.0), closest.get(), 1e-9)

        val intersection: java.util.Optional<Vector3d> = e1.getIntersection(e2)
        assertTrue(intersection.isPresent)
        assertVectorClose(Vector3d.xyz(1.0, 1.0, 0.0), intersection.get(), 1e-9)
    }
}

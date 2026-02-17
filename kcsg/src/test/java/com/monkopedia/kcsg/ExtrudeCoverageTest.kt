package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtrudeCoverageTest {
    @Test
    fun nonRotationBranchUsesTranslatedTopVertices() {
        val profile = squareProfile()
        val dir = Vector3d.xyz(0.0, 0.0, 2.0)

        val sidePolygons = Extrude.points(dir, top = false, bottom = false, *profile.toTypedArray())
        val actualVertices = uniqueVertices(sidePolygons)
        val expectedVertices = profile + profile.map { it.plus(dir) }

        assertEquals(4, sidePolygons.size)
        assertEquals(8, actualVertices.size)
        expectedVertices.forEach { expected ->
            assertTrue("Missing expected translated vertex $expected", containsApprox(actualVertices, expected))
        }
    }

    @Test
    fun rotationBranchTiltsTopVerticesForNonParallelDirection() {
        val profile = squareProfile()
        val dir = Vector3d.xyz(1.0, 0.0, 1.0)

        val sidePolygons = Extrude.points(dir, top = false, bottom = false, *profile.toTypedArray())
        val actualVertices = uniqueVertices(sidePolygons)
        val simpleTranslation = profile + profile.map { it.plus(dir) }

        assertEquals(4, sidePolygons.size)
        assertEquals(8, actualVertices.size)
        assertTrue(
            "Expected at least one rotated top vertex that is not a simple translation",
            actualVertices.any { vertex -> !containsApprox(simpleTranslation, vertex) },
        )
    }

    private fun squareProfile(): List<Vector3d> {
        return listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 1.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
        )
    }

    private fun uniqueVertices(polygons: List<Polygon>, epsilon: Double = 1e-9): List<Vector3d> {
        val unique = mutableListOf<Vector3d>()
        polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                if (!containsApprox(unique, vertex.pos, epsilon)) {
                    unique += vertex.pos
                }
            }
        }
        return unique
    }

    private fun containsApprox(
        values: List<Vector3d>,
        target: Vector3d,
        epsilon: Double = 1e-9,
    ): Boolean {
        return values.any { value ->
            kotlin.math.abs(value.x - target.x) <= epsilon &&
                kotlin.math.abs(value.y - target.y) <= epsilon &&
                kotlin.math.abs(value.z - target.z) <= epsilon
        }
    }
}

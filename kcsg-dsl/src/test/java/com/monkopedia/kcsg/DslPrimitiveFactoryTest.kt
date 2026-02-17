package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Path

class DslPrimitiveFactoryTest {
    @Test
    fun primitiveFactoriesAndWeightedExtension() {
        val builder = NoopBuilder()
        assertEquals(Vector3d.ZERO, CSGBuilder.ZERO)

        val cube by builder.primitive { cube(2.0) }
        assertEquals(8.0, cube.computeVolume(), 1e-4)

        val rounded by builder.primitive {
            roundedCube(2.0) {
                cornerRadius = 0.25
                resolution = 3
            }
        }
        assertTrue(rounded.computeVolume() > 0.0)

        val explicitCylinder by builder.primitive {
            cylinder(
                start = xyz(0.0, 0.0, 0.0),
                end = xyz(0.0, 0.0, 3.0),
                radius = 1.0,
                endRadius = 0.5,
                numSlices = 24,
            )
        }
        val expectedFrustumVolume = Math.PI * 3.0 * (1.0 * 1.0 + 1.0 * 0.5 + 0.5 * 0.5) / 3.0
        assertEquals(expectedFrustumVolume, explicitCylinder.computeVolume(), 0.5)

        val heightCylinder by builder.primitive {
            cylinder(radius = 1.0, height = 3.0, numSlices = 24)
        }
        val expectedCylinderVolume = Math.PI * 1.0 * 1.0 * 3.0
        assertEquals(expectedCylinderVolume, heightCylinder.computeVolume(), 0.5)

        val weighted by builder.csg {
            cube(2.0).weighted(WeightFunction { _, _ -> 0.25 })
        }
        weighted.polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                assertEquals(0.25, vertex.weight, 1e-9)
            }
        }

        val transformedByContext by builder.csg {
            cube(1.0).toCSG().transformed(transform { translate(2.0, 0.0, 0.0) })
        }
        assertEquals(
            Bounds(
                min = Vector3d.xyz(1.5, -0.5, -0.5),
                max = Vector3d.xyz(2.5, 0.5, 0.5),
            ),
            transformedByContext.bounds,
        )
    }

    @Test
    fun primitiveFactoryDefaultsWithoutExplicitBuilders() {
        val builder = NoopBuilder()

        val roundedDefault by builder.primitive {
            roundedCube()
        }
        assertTrue(roundedDefault.computeVolume() > 0.0)

        val cylinderDefault by builder.primitive {
            cylinder()
        }
        assertTrue(cylinderDefault.computeVolume() > 0.0)
    }

    private class NoopBuilder : KcsgBuilder() {
        override fun exportProperty(propertyName: String) = Unit
        override fun track(propertyName: String, lazy: Lazy<CSG>) = Unit
        override fun findStl(stlName: String): Path = error("unused")
        override fun findScript(csgsName: String): ImportedScript = error("unused")
    }
}

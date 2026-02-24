package com.monkopedia.kcsg

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.Test
import kotlinx.io.files.Path

class DslCollectionExtensionsTest {
    @Test
    fun collectionTransformTimesTranslateScaleAndRot() {
        val base = listOf(
            Cube(center = Vector3d.xyz(0.0, 0.0, 0.0), dimensions = Vector3d.xyz(1.0, 1.0, 1.0)).toCSG(),
            Cube(center = Vector3d.xyz(2.0, 0.0, 0.0), dimensions = Vector3d.xyz(1.0, 1.0, 1.0)).toCSG(),
        )

        val viaTransform = base.transform { translate(1.0, 0.0, 0.0) }
        val viaTimes = base * Transform.unity().translate(1.0, 0.0, 0.0)
        assertEquals(viaTransform.map { it.bounds }, viaTimes.map { it.bounds })

        assertEquals(2, base.translate(1.0, 2.0, 3.0).size)
        assertEquals(2, base.scale(2.0).size)
        assertEquals(2, base.scale(2.0, 3.0, 4.0).size)
        assertEquals(2, base.rot(0.0, 0.0, 90.0).size)
    }

    @Test
    fun flattenAndMergeProduceEquivalentResults() {
        val base = listOf(
            Cube(center = Vector3d.xyz(0.0, 0.0, 0.0), dimensions = Vector3d.xyz(1.0, 1.0, 1.0)).toCSG(),
            Cube(center = Vector3d.xyz(2.0, 0.0, 0.0), dimensions = Vector3d.xyz(1.0, 1.0, 1.0)).toCSG(),
        )

        val flattened = base.flatten()
        val merged = base.merge()
        assertEquals(flattened.computeVolume(), merged.computeVolume(), 1e-6)
    }

    @Test
    fun arrayedAndPrimitivesBuilderHelpers() {
        val builder = NoopBuilder()
        val arrayed by builder.csg {
            arrayed(3) { index ->
                Cube(
                    center = xyz(index * 2.0, 0.0, 0.0),
                    dimensions = Vector3d.xyz(1.0, 1.0, 1.0),
                ).toCSG()
            }
        }
        val primitives by builder.csg {
            primitives(3) { index ->
                Cube(
                    center = xyz(index * 2.0, 0.0, 0.0),
                    dimensions = Vector3d.xyz(1.0, 1.0, 1.0),
                )
            }
        }
        assertEquals(3.0, arrayed.computeVolume(), 1e-4)
        assertEquals(arrayed.computeVolume(), primitives.computeVolume(), 1e-4)
    }

    @Test
    fun topLevelCollectionWrappersAndDefaultArguments() {
        val base = listOf(
            Cube(center = Vector3d.ZERO, dimensions = Vector3d.xyz(2.0, 2.0, 2.0)).toCSG(),
        )

        val translated = base.translate()
        assertEquals(base.map { it.bounds }, translated.map { it.bounds })
        assertEquals(1, base.translate(0.0, 0.0, 2.0).size)

        assertFailsWith<IllegalArgumentException> {
            base.scale()
        }

        assertEquals(1, base.scale(1.0).size)

        assertFailsWith<IllegalArgumentException> {
            base.scale(x = 0.0, y = 0.0, z = 0.0)
        }

        assertFailsWith<IllegalArgumentException> {
            base.scale(y = 1.0)
        }
        assertEquals(1, base.rot().size)
    }

    private class NoopBuilder : KcsgBuilder() {
        override fun exportProperty(propertyName: String) = Unit
        override fun track(propertyName: String, lazy: Lazy<CSG>) = Unit
        override fun findStl(stlName: String): Path = error("unused")
        override fun findScript(csgsName: String): ImportedScript = error("unused")
    }
}

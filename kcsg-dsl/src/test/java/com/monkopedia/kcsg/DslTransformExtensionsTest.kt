package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Test

class DslTransformExtensionsTest {
    @Test
    fun transformBuilderAndTransformExtensionOverloads() {
        val t = TransformBuilder.unity
            .translate(x = 1.0, y = 2.0, z = 3.0)
            .scale(scale = 2.0)
        val transformedPoint = t.transform(Vector3d.xyz(1.0, 1.0, 1.0))
        assertEquals(Vector3d.xyz(3.0, 4.0, 5.0), transformedPoint)

        val t2 = TransformBuilder.unity
            .translate(1.0, 2.0, 3.0)
            .scale(x = 2.0, y = 3.0, z = 4.0)
        val transformedPoint2 = t2.transform(Vector3d.xyz(1.0, 1.0, 1.0))
        assertEquals(Vector3d.xyz(3.0, 5.0, 7.0), transformedPoint2)
    }

    @Test
    fun csgAndPrimitiveTransformExtensions() {
        val base = Cube(2.0).toCSG()
        val tx = Transform.unity().translate(3.0, 0.0, 0.0)

        val viaLambda = base.transform { translate(3.0, 0.0, 0.0) }
        val viaOperator = base * tx
        assertEquals(viaLambda.bounds, viaOperator.bounds)

        val primitive = Cube(2.0)
        val primitiveOperator = primitive * tx
        val primitiveLambda = primitive.transform { translate(3.0, 0.0, 0.0) }
        assertEquals(primitiveOperator.bounds, primitiveLambda.bounds)
    }

    @Test
    fun csgTranslateScaleAndRotExtensions() {
        val base = Cube(center = Vector3d.ZERO, dimensions = Vector3d.xyz(2.0, 4.0, 2.0)).toCSG()

        val translated = base.translate(x = 1.0, y = 2.0, z = 3.0)
        assertEquals(
            Bounds(
                min = Vector3d.xyz(0.0, 0.0, 2.0),
                max = Vector3d.xyz(2.0, 4.0, 4.0),
            ),
            translated.bounds,
        )

        val uniformScaled = base.scale(scale = 2.0)
        assertEquals(base.computeVolume() * 8.0, uniformScaled.computeVolume(), 1e-4)

        val axisScaled = base.scale(x = 2.0, y = 3.0, z = 4.0)
        assertEquals(base.computeVolume() * 24.0, axisScaled.computeVolume(), 1e-4)

        val rotated = base.rot(z = 90.0)
        assertEquals(4.0, rotated.bounds.bounds.x, 1e-6)
        assertEquals(2.0, rotated.bounds.bounds.y, 1e-6)
        assertEquals(2.0, rotated.bounds.bounds.z, 1e-6)
    }
}

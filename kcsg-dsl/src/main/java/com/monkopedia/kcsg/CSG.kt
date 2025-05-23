@file:Suppress("unused")

package com.monkopedia.kcsg

import com.monkopedia.kcsg.KcsgBuilder.BuilderContext

@CsgDsl
object CSGBuilder {
    val ZERO: Vector3d
        get() = Vector3d.ZERO
}

@CsgDsl
fun BuilderContext.xyz(x: Double, y: Double, z: Double): Vector3d = Vector3d.xyz(x, y, z)

@CsgDsl
inline fun Primitive.weighted(weightFunction: WeightFunction): CSG {
    return toCSG().weighted(weightFunction)
}

@CsgDsl
inline fun BuilderContext.roundedCube(size: Double = 1.0, builder: RoundedCube.() -> Unit = {}): RoundedCube {
    return RoundedCube(size).also(builder)
}

@CsgDsl
inline fun BuilderContext.cube(size: Double = 1.0, builder: Cube.() -> Unit = {}): Cube {
    return Cube(size).also(builder)
}

@CsgDsl
inline fun BuilderContext.cylinder(
    start: Vector3d = xyz(0.0, -0.5, 0.0),
    end: Vector3d = xyz(0.0, 0.5, 0.0),
    radius: Double = 1.0,
    endRadius: Double = radius,
    numSlices: Int = 16,
    builder: Cylinder.() -> Unit = {}
): Cylinder {
    return Cylinder(start, end, radius, endRadius, numSlices).also(builder)
}

@CsgDsl
inline fun BuilderContext.cylinder(
    radius: Double,
    height: Double,
    numSlices: Int,
    builder: Cylinder.() -> Unit = {}
): Cylinder {
    return Cylinder(radius, height, numSlices).also(builder)
}

@CsgDsl
inline fun BuilderContext.transform(transform: Transform.() -> Transform): Transform {
    return TransformBuilder.unity.transform()
}

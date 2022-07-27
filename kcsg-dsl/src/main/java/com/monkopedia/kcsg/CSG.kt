@file:Suppress("unused")

package com.monkopedia.kcsg

import com.monkopedia.kcsg.KcsgScript.BuilderContext
import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cube
import eu.mihosoft.jcsg.Cylinder
import eu.mihosoft.jcsg.Primitive
import eu.mihosoft.jcsg.RoundedCube
import eu.mihosoft.jcsg.WeightFunction
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d

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
inline operator fun CSG.plus(other: CSG): CSG {
    return union(other)
}

@CsgDsl
inline operator fun CSG.minus(other: CSG): CSG {
    return difference(other)
}

@CsgDsl
inline operator fun CSG.times(other: CSG): CSG {
    return intersect(other)
}

@CsgDsl
inline operator fun CSG.times(other: Transform): CSG {
    return clone().transformed(other)
}
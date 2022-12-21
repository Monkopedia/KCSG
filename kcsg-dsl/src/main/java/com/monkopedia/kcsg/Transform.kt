package com.monkopedia.kcsg

object TransformBuilder {
    val unity: Transform
        get() = Transform.unity()
}

inline fun Transform.translate(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Transform {
    return translate(x, y, z)
}

inline fun Transform.scale(scale: Double = 0.0): Transform {
    return scale(scale)
}

inline fun Transform.scale(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Transform {
    return scale(x, y, z)
}

@CsgDsl
inline fun CSG.transform(transform: Transform.() -> Transform): CSG {
    return transformed(TransformBuilder.unity.transform())
}

@CsgDsl
operator fun CSG.times(other: Transform): CSG {
    return transformed(other)
}

@CsgDsl
inline operator fun Primitive.times(transform: Transform): CSG {
    return toCSG().transformed(transform)
}

@CsgDsl
inline fun Primitive.transform(transform: Transform.() -> Transform): CSG {
    return toCSG().transformed(TransformBuilder.unity.transform())
}

@CsgDsl
inline fun CSG.translate(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): CSG {
    return transform { translate(x, y, z) }
}

@CsgDsl
inline fun CSG.scale(scale: Double = 0.0): CSG {
    return transform { scale(scale) }
}

@CsgDsl
inline fun CSG.scale(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): CSG {
    return transform { scale(x, y, z) }
}

@CsgDsl
inline fun CSG.rot(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): CSG {
    return transform { rot(x, y, z) }
}
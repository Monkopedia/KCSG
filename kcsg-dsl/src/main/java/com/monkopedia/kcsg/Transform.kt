package com.monkopedia.kcsg

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Primitive
import eu.mihosoft.vvecmath.Transform

object TransformBuilder {
    val unity: Transform
        get() = Transform.unity()

    inline fun translate(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Transform {
        return unity.translate(x, y, z)
    }

    fun rotZ(d: Double): Transform {
        return unity.rotZ(d)
    }

    fun rotX(d: Double): Transform {
        return unity.rotZ(d)
    }

    fun rotY(d: Double): Transform {
        return unity.rotZ(d)
    }
}

inline fun Transform.translate(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Transform {
    return translate(x, y, z)
}

@CsgDsl
inline fun CSG.transform(transform: TransformBuilder.() -> Transform): CSG {
    return clone().transformed(TransformBuilder.transform())
}

@CsgDsl
inline operator fun Primitive.times(transform: Transform): CSG {
    return toCSG().transformed(transform)
}

@CsgDsl
inline fun Primitive.transform(transform: TransformBuilder.() -> Transform): CSG {
    return toCSG().transformed(TransformBuilder.transform())
}

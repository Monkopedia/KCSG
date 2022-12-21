package com.monkopedia.kcsg

import com.monkopedia.kcsg.KcsgBuilder.BuilderContext

@CsgDsl
inline fun Collection<CSG>.transform(transform: Transform.() -> Transform): Collection<CSG> {
    return map { it.transformed(TransformBuilder.unity.transform()) }
}

@CsgDsl
operator fun Collection<CSG>.times(other: Transform): Collection<CSG> {
    return map { it.transformed(other) }
}

@CsgDsl
inline fun Collection<CSG>.translate(
    x: Double = 0.0,
    y: Double = 0.0,
    z: Double = 0.0
): Collection<CSG> {
    return map { it.translate(x, y, z) }
}

@CsgDsl
inline fun Collection<CSG>.scale(scale: Double = 0.0): Collection<CSG> {
    return map { it.scale(scale) }
}

@CsgDsl
inline fun Collection<CSG>.scale(
    x: Double = 0.0,
    y: Double = 0.0,
    z: Double = 0.0
): Collection<CSG> {
    return map { it.scale(x, y, z) }
}

@CsgDsl
inline fun Collection<CSG>.rot(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Collection<CSG> {
    return map { it.rot(x, y, z) }
}

@CsgDsl
inline fun Collection<CSG>.flatten(): CSG {
    return reduce { a, b -> a + b }
}

@CsgDsl
inline fun Collection<CSG>.merge(): CSG {
    return reduce { a, b -> a + b }
}

@CsgDsl
inline fun BuilderContext.arrayed(size: Int, factory: (Int) -> CSG): CSG {
    return List(size, factory).flatten()
}

@CsgDsl
inline fun BuilderContext.primitives(size: Int, factory: (Int) -> Primitive): CSG {
    return List(size) { factory(it).toCSG() }.flatten()
}

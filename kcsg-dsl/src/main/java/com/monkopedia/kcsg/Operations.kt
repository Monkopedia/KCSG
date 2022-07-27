package com.monkopedia.kcsg

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Primitive

@CsgDsl
infix fun CSG.and(other: CSG): CSG = intersect(other)

@CsgDsl
infix fun Primitive.and(other: CSG): CSG = toCSG().intersect(other)

@CsgDsl
infix fun CSG.and(other: Primitive): CSG = intersect(other.toCSG())

@CsgDsl
infix fun Primitive.and(other: Primitive): CSG = toCSG().intersect(other.toCSG())

@CsgDsl
infix fun CSG.or(other: CSG): CSG = union(other)

@CsgDsl
infix fun Primitive.or(other: CSG): CSG = toCSG().union(other)

@CsgDsl
infix fun CSG.or(other: Primitive): CSG = union(other.toCSG())

@CsgDsl
infix fun Primitive.or(other: Primitive): CSG = toCSG().union(other.toCSG())

@CsgDsl
infix fun CSG.xor(other: CSG): CSG = difference(other)

@CsgDsl
infix fun Primitive.xor(other: CSG): CSG = toCSG().difference(other)

@CsgDsl
infix fun CSG.xor(other: Primitive): CSG = difference(other.toCSG())

@CsgDsl
infix fun Primitive.xor(other: Primitive): CSG = toCSG().difference(other.toCSG())

@CsgDsl
operator fun CSG.plus(other: CSG): CSG = union(other)

@CsgDsl
operator fun Primitive.plus(other: CSG): CSG = toCSG().union(other)

@CsgDsl
operator fun CSG.plus(other: Primitive): CSG = union(other.toCSG())

@CsgDsl
operator fun Primitive.plus(other: Primitive): CSG = toCSG().union(other.toCSG())

@CsgDsl
operator fun CSG.minus(other: CSG): CSG = difference(other)

@CsgDsl
operator fun Primitive.minus(other: CSG): CSG = toCSG().difference(other)

@CsgDsl
operator fun CSG.minus(other: Primitive): CSG = difference(other.toCSG())

@CsgDsl
operator fun Primitive.minus(other: Primitive): CSG = toCSG().difference(other.toCSG())

@CsgDsl
operator fun CSG.times(other: CSG): CSG = intersect(other)

@CsgDsl
operator fun Primitive.times(other: CSG): CSG = toCSG().intersect(other)

@CsgDsl
operator fun CSG.times(other: Primitive): CSG = intersect(other.toCSG())

@CsgDsl
operator fun Primitive.times(other: Primitive): CSG = toCSG().intersect(other.toCSG())

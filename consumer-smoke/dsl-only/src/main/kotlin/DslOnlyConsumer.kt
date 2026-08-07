package com.monkopedia.kcsg.consumersmoke

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.KcsgBuilder
import com.monkopedia.kcsg.KcsgHost
import com.monkopedia.kcsg.Primitive
import com.monkopedia.kcsg.WeightFunction
import com.monkopedia.kcsg.cube
import com.monkopedia.kcsg.merge
import com.monkopedia.kcsg.weighted
import kotlinx.io.files.Path

/**
 * Compiles against `com.monkopedia:kcsg-dsl` and nothing else.
 *
 * Every declaration below names a type that kcsg-dsl's public API hands back but does not
 * itself declare: `CSG`, `Primitive`, `WeightFunction` and `Cube` come from `kcsg`, and
 * `Path` comes from `kotlinx-io-core`. If kcsg-dsl declares either dependency as
 * `implementation`, Gradle leaves it out of the published `jvmApiElements` variant and this
 * file does not compile -- which is the whole assertion. See issue #51.
 */
object DslOnlyConsumer {

    /** `merge()` is kcsg-dsl's, but both its element type and its return type are kcsg's. */
    fun mergeAll(parts: Collection<CSG>): CSG = parts.merge()

    /** `weighted()` is kcsg-dsl's; `Primitive`, `WeightFunction` and `CSG` are kcsg's. */
    fun weight(primitive: Primitive, weight: WeightFunction): CSG = primitive.weighted(weight)

    /** The builder DSL: `cube()` is kcsg-dsl's and returns kcsg's `Cube`. */
    fun twoCubes(context: KcsgBuilder.BuilderContext): CSG =
        listOf(context.cube(1.0).toCSG(), context.cube(2.0).toCSG()).merge()

    /** kcsg-dsl also exposes kotlinx-io's `Path` from `KcsgHost`. */
    fun stlPath(host: KcsgHost, name: String): Path = host.findStl(name)
}

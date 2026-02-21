package com.monkopedia.kcsg

import kotlinx.io.Source
import kotlinx.io.files.Path

interface OpOverride {
    fun operation(s: String, vararg csg: Any?): CSG?
    fun bounds(s: String, vararg csg: Any?): Bounds?
    fun double(s: String, vararg csg: Any?): Double?
    fun file(path: Path): CSG?
    fun source(sourceFactory: () -> Source): CSG?
}

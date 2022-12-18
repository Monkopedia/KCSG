package com.monkopedia.kcsg

import java.io.InputStream
import java.nio.file.Path

interface OpOverride {
    fun operation(s: String, vararg csg: Any?): CSG?
    fun bounds(s: String, vararg csg: Any?): Bounds?
    fun double(s: String, vararg csg: Any?): Double?
    fun file(path: Path): CSG?
    fun inputStream(inputStreamFactory: () -> InputStream): CSG?
}
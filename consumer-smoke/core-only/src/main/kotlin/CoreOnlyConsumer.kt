package com.monkopedia.kcsg.consumersmoke

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cube
import com.monkopedia.kcsg.FileUtil
import kotlinx.io.files.Path

/**
 * Compiles against `com.monkopedia:kcsg` and nothing else.
 *
 * `kotlinx.io.files.Path` is not declared here; it has to arrive transitively, because it is
 * the parameter type of kcsg's primary file-I/O entry points. If kcsg declares kotlinx-io as
 * `implementation`, Gradle leaves it out of the published `jvmApiElements` variant and this
 * file does not compile. See issue #51.
 */
object CoreOnlyConsumer {

    fun writeCube(path: Path) {
        val csg: CSG = Cube(1.0).toCSG()
        FileUtil.toStlFile(path, csg)
    }

    fun readBack(path: Path): String = FileUtil.read(path)
}

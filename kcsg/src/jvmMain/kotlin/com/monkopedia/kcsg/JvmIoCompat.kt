@file:JvmName("KcsgJvmIoCompat")

package com.monkopedia.kcsg

import java.io.InputStream
import java.util.Optional
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.Path

/**
 * JVM-only compatibility helpers for the pre-kotlinx I/O API surface.
 */
@Deprecated(
    message = "Use source(sourceFactory) with kotlinx.io.Source",
    replaceWith = ReplaceWith("source(sourceFactory)")
)
fun OpOverride.inputStream(inputStreamFactory: () -> InputStream): CSG? {
    return source {
        inputStreamFactory().asSource().buffered()
    }
}

@Deprecated(
    message = "Use STL.file(kotlinx.io.files.Path)",
    replaceWith = ReplaceWith("file(kotlinx.io.files.Path(path.toString()))")
)
fun STL.file(path: java.nio.file.Path): CSG {
    return file(Path(path.toString()))
}

@Deprecated(
    message = "Use STL.from(sourceFactory, length) with kotlinx.io.Source",
    replaceWith = ReplaceWith("from(sourceFactory, length)")
)
fun STL.from(inputStreamFactory: () -> InputStream, length: () -> Long): CSG {
    return from(
        sourceFactory = { inputStreamFactory().asSource().buffered() },
        length = length,
    )
}

@Deprecated(
    message = "Use FileUtil.write(kotlinx.io.files.Path, String)",
    replaceWith = ReplaceWith("write(kotlinx.io.files.Path(path.toString()), text)")
)
fun FileUtil.write(path: java.nio.file.Path, text: String) {
    write(Path(path.toString()), text)
}

@Deprecated(
    message = "Use FileUtil.read(kotlinx.io.files.Path)",
    replaceWith = ReplaceWith("read(kotlinx.io.files.Path(path.toString()))")
)
fun FileUtil.read(path: java.nio.file.Path): String {
    return read(Path(path.toString()))
}

@Deprecated(
    message = "Use FileUtil.toStlFile(kotlinx.io.files.Path, CSG)",
    replaceWith = ReplaceWith("toStlFile(kotlinx.io.files.Path(path.toString()), csg)")
)
fun FileUtil.toStlFile(path: java.nio.file.Path, csg: CSG) {
    toStlFile(Path(path.toString()), csg)
}

@Deprecated(
    message = "Use ObjFile.toFiles(kotlinx.io.files.Path)",
    replaceWith = ReplaceWith("toFiles(kotlinx.io.files.Path(path.toString()))")
)
fun ObjFile.toFiles(path: java.nio.file.Path) {
    toFiles(Path(path.toString()))
}

@Deprecated(
    message = "Use objSource (kotlinx.io.Source)",
    replaceWith = ReplaceWith("objSource")
)
val ObjFile.objStream: InputStream
    get() = objSource.asInputStream()

@Deprecated(
    message = "Use mtlSource (kotlinx.io.Source)",
    replaceWith = ReplaceWith("mtlSource")
)
val ObjFile.mtlStream: InputStream
    get() = mtlSource.asInputStream()

@Deprecated(
    message = "Use getClosestPointOrNull",
    replaceWith = ReplaceWith("getClosestPointOrNull(e)")
)
fun Edge.getClosestPoint(e: Edge): Optional<Vector3d> {
    return Optional.ofNullable(getClosestPointOrNull(e))
}

@Deprecated(
    message = "Use getIntersectionOrNull",
    replaceWith = ReplaceWith("getIntersectionOrNull(e)")
)
fun Edge.getIntersection(e: Edge): Optional<Vector3d> {
    return Optional.ofNullable(getIntersectionOrNull(e))
}

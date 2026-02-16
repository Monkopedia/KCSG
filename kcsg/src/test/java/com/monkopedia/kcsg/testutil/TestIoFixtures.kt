package com.monkopedia.kcsg.testutil

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

object TestIoFixtures {
    @JvmStatic
    fun withTempDirectory(prefix: String = "kcsg-test", block: (Path) -> Unit) {
        val tempDir = Files.createTempDirectory(prefix)
        try {
            block(tempDir)
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @JvmStatic
    fun writeTempTextFile(
        directory: Path,
        fileName: String,
        content: String,
    ): Path {
        val file = directory.resolve(fileName)
        file.writeText(content)
        return file
    }

    @JvmStatic
    fun readTextFile(path: Path): String {
        return path.readText()
    }

    @JvmStatic
    fun streamFactory(content: String): () -> InputStream {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        return { ByteArrayInputStream(bytes) }
    }

    @JvmStatic
    fun byteLength(content: String): () -> Long {
        val length = content.toByteArray(StandardCharsets.UTF_8).size.toLong()
        return { length }
    }
}

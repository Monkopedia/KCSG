package com.monkopedia.kcsg.testutil

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.files.Path

object TestIoFixtures {
    @JvmStatic
    fun withTempDirectory(prefix: String = "kcsg-test", block: (Path) -> Unit) {
        val tempDir = Files.createTempDirectory(prefix).toString()
        try {
            block(Path(tempDir))
        } finally {
            java.io.File(tempDir).deleteRecursively()
        }
    }

    @JvmStatic
    fun writeTempTextFile(
        directory: Path,
        fileName: String,
        content: String,
    ): Path {
        val file = Path(directory, fileName)
        com.monkopedia.kcsg.FileUtil.write(file, content)
        return file
    }

    @JvmStatic
    fun readTextFile(path: Path): String {
        return com.monkopedia.kcsg.FileUtil.read(path)
    }

    @JvmStatic
    fun sourceFactory(content: String): () -> Source {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        return {
            Buffer().apply {
                write(bytes, 0, bytes.size)
            }
        }
    }

    @JvmStatic
    fun byteLength(content: String): () -> Long {
        val length = content.toByteArray(StandardCharsets.UTF_8).size.toLong()
        return { length }
    }

    @JvmStatic
    fun sourceFactory(bytes: ByteArray): () -> Source = {
        Buffer().apply {
            write(bytes, 0, bytes.size)
        }
    }

    @JvmStatic
    fun byteLength(bytes: ByteArray): () -> Long {
        val length = bytes.size.toLong()
        return { length }
    }
}

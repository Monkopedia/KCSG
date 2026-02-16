package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileUtilTest {
    @Test
    fun writeAndReadRoundTrip() {
        withTempDirectory("kcsg-fileutil-readwrite") { tempDir ->
            val target = tempDir.resolve("sample.txt")
            val content = "hello\nkcsg\n"

            FileUtil.write(target, content)
            val loaded = FileUtil.read(target)

            assertEquals(content, loaded)
        }
    }

    @Test
    fun toStlFileWritesAsciiStl() {
        withTempDirectory("kcsg-fileutil-stl") { tempDir ->
            val target = tempDir.resolve("shape.stl")
            val csg = Cube(1.0).toCSG()

            FileUtil.toStlFile(target, csg)
            val text = FileUtil.read(target)

            assertTrue(text.startsWith("solid v3d.csg"))
            assertTrue(text.contains("facet normal"))
            assertTrue(text.endsWith("endsolid v3d.csg\n"))
        }
    }
}

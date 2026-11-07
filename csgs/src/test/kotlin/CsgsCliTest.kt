package com.monkopedia.csgs

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.parse
import com.monkopedia.kcsg.Logger
import com.monkopedia.kcsg.STL
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlinx.io.files.Path
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Runs the CLI over fixture scripts and asserts on the files it produces, one test (or pair)
 * per option: `-t`, the default caching mode, `-c`, `-I` and `-e ?` / `-e <unknown>`.
 */
class CsgsCliTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val originalOut = System.out
    private val capturedOut = ByteArrayOutputStream()

    @After
    fun tearDown() {
        System.setOut(originalOut)
        Logger.setLogger(null)
    }

    private fun cubeScript(): File = temporaryFolder.newFile("model.csgs").apply {
        writeText(
            """
                val cube by csg(exported = true) {
                    cube {
                        dimensions = xyz(1.0, 1.0, 1.0)
                    }.toCSG()
                }
                val hidden by csg {
                    cube {
                        dimensions = xyz(3.0, 3.0, 3.0)
                    }.toCSG()
                }
            """.trimIndent()
        )
    }

    private fun run(vararg args: String): String {
        System.setOut(PrintStream(capturedOut, true))
        try {
            Csgs().parse(arrayOf(*args))
        } finally {
            System.setOut(originalOut)
        }
        return capturedOut.toString()
    }

    private fun sizeOf(stl: File) = STL.file(Path(stl.path)).bounds.bounds

    @Test
    fun outputTypeObjWritesObjContent() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "out")

        run(script.path, "-o", out.path, "-d", "-t", "obj")

        val obj = File(out, "cube.obj")
        assertTrue("expected ${obj.path}", obj.isFile)
        assertFalse(File(out, "cube.stl").exists())
        val text = obj.readText()
        assertTrue(text, text.startsWith("# Group\n"))
        assertTrue(text, text.contains("\nv "))
        assertTrue(text, text.contains("\nf "))
        assertFalse(text, text.contains("solid"))
    }

    @Test
    fun outputTypeStlWritesStlContent() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "out")

        run(script.path, "-o", out.path, "-d", "-t", "stl")

        val stl = File(out, "cube.stl")
        assertTrue("expected ${stl.path}", stl.isFile)
        assertFalse(File(out, "cube.obj").exists())
        val text = stl.readText()
        assertTrue(text, text.startsWith("solid "))
        assertTrue(text, text.contains("facet normal"))
        assertEquals(1.0, sizeOf(stl).x, 1e-9)
    }

    @Test
    fun defaultCachingStoresStlAndReproducesOutput() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "out")

        run(script.path, "-o", out.path)

        val cached = File(out, "cache").listFiles().orEmpty().toList()
        assertEquals(cached.toString(), 1, cached.size)
        assertTrue(cached.single().name, cached.single().name.matches(Regex("[0-9a-f]+\\.stl")))
        val first = File(out, "cube.stl").readBytes()
        assertTrue(first.isNotEmpty())

        run(script.path, "-o", out.path)

        assertArrayEquals(first, File(out, "cube.stl").readBytes())
        assertEquals(cached, File(out, "cache").listFiles().orEmpty().toList())
    }

    @Test
    fun defaultCachingServesSecondRunFromCacheFile() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "out")
        run(script.path, "-o", out.path)
        val cacheFile = File(out, "cache").listFiles()!!.single()

        // Replace the cached cube with a single triangle: a second run only reproduces it if
        // the cache file is actually read instead of recomputing the cube.
        cacheFile.writeText(
            """
                solid tampered
                facet normal 0 0 1
                outer loop
                vertex 0 0 0
                vertex 5 0 0
                vertex 0 5 0
                endloop
                endfacet
                endsolid tampered
            """.trimIndent() + "\n"
        )
        run(script.path, "-o", out.path)

        val text = File(out, "cube.stl").readText()
        assertEquals(text, 1, Regex("facet normal").findAll(text).count())
    }

    @Test
    fun cleanCreatesMissingOutputDirectory() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "missing/out")
        assertFalse(out.exists())

        run(script.path, "-o", out.path, "-d", "-c")

        assertTrue(File(out, "cube.stl").isFile)
    }

    @Test
    fun cleanRemovesStaleContents() {
        val script = cubeScript()
        val out = temporaryFolder.newFolder("out")
        val stale = File(out, "stale.stl").apply { writeText("stale") }
        val staleNested = File(out, "cache/deadbeef.stl").apply {
            parentFile.mkdirs()
            writeText("stale")
        }

        val stdout = run(script.path, "-o", out.path, "-c")

        assertFalse(stale.exists())
        assertFalse(staleNested.exists())
        assertTrue(File(out, "cube.stl").isFile)
        assertTrue(
            stdout,
            stdout.contains("Warning: caching enabled, but output directory was cleared")
        )
    }

    @Test
    fun withoutCleanExistingContentsAreKept() {
        val script = cubeScript()
        val out = temporaryFolder.newFolder("out")
        val keep = File(out, "keep.txt").apply { writeText("keep") }

        run(script.path, "-o", out.path, "-d")

        assertEquals("keep", keep.readText())
    }

    @Test
    fun importsResolveScriptFromIncludedDirectory() {
        val libDir = temporaryFolder.newFolder("lib")
        File(libDir, "parts.csgs").writeText(
            """
                val part by csg {
                    cube {
                        dimensions = xyz(2.0, 2.0, 2.0)
                    }.toCSG()
                }
            """.trimIndent()
        )
        val scriptDir = temporaryFolder.newFolder("src")
        val script = File(scriptDir, "main.csgs").apply {
            writeText(
                """
                    val parts by import("parts")
                    val fromLib by csg(exported = true) {
                        parts["part"]
                    }
                """.trimIndent()
            )
        }
        val out = File(temporaryFolder.root, "out")

        run(script.path, "-o", out.path, "-d", "-I", libDir.path)

        val stl = File(out, "fromLib.stl")
        assertTrue("expected ${stl.path}", stl.isFile)
        assertEquals(2.0, sizeOf(stl).x, 1e-9)
    }

    @Test
    fun exportQuestionMarkListsAllTargets() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "out")

        val stdout = run(script.path, "-o", out.path, "-d", "-e", "?")

        val lines = stdout.lines()
        assertTrue(stdout, lines.contains("Listing targets:"))
        assertTrue(stdout, lines.contains("  cube"))
        assertTrue(stdout, lines.contains("  hidden"))
        assertFalse(File(out, "?.stl").exists())
    }

    @Test
    fun exportNamedTargetExportsNonDefaultTarget() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "out")

        run(script.path, "-o", out.path, "-d", "-e", "hidden")

        assertEquals(3.0, sizeOf(File(out, "hidden.stl")).x, 1e-9)
    }

    @Test
    fun exportUnknownTargetIsAUsageError() {
        val script = cubeScript()
        val out = File(temporaryFolder.root, "out")

        try {
            run(script.path, "-o", out.path, "-d", "-e", "nope")
            fail("expected an error for an unknown export")
        } catch (e: BadParameterValue) {
            assertTrue(e.message, e.message!!.contains("Unknown target nope"))
        }
        assertFalse(File(out, "nope.stl").exists())
    }
}

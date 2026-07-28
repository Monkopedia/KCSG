package com.monkopedia.csgs

import com.github.ajalt.clikt.core.parse
import com.monkopedia.kcsg.Logger
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Verifies that running the CLI installs a [Logger] delegate, so diagnostics emitted by the
 * core actually reach the user. Without the delegate every diagnostic is silently dropped and
 * a script producing a degenerate mesh exits cleanly with an empty stderr.
 */
class CsgsLoggingTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val originalErr = System.err
    private val captured = ByteArrayOutputStream()

    @After
    fun tearDown() {
        System.setErr(originalErr)
        Logger.setLogger(null)
    }

    /**
     * Runs the CLI over a script that builds a polygon from duplicate points, which makes
     * `Polygon` emit an ERROR diagnostic, and returns everything written to stderr.
     */
    private fun runDegenerateScript(vararg extraArgs: String): String {
        val script = temporaryFolder.newFile("degenerate.csgs")
        script.writeText(
            """
                val degenerate by csg {
                    Polygon.fromPoints(
                        listOf(
                            Vector3d.xyz(0.0, 0.0, 0.0),
                            Vector3d.xyz(1.0, 0.0, 0.0),
                            Vector3d.xyz(1.0, 0.0, 0.0)
                        )
                    )
                    cube {
                        dimensions = xyz(1.0, 1.0, 1.0)
                    }.toCSG()
                }
            """.trimIndent()
        )
        val output = temporaryFolder.newFolder("out")
        System.setErr(PrintStream(captured, true))
        Csgs().parse(
            arrayOf(
                script.path,
                "-o",
                output.path,
                "-d",
                "-e",
                "degenerate",
                *extraArgs
            )
        )
        return captured.toString()
    }

    @Test
    fun errorDiagnosticsReachStderrWithoutVerbose() {
        val stderr = runDegenerateScript()

        assertTrue(stderr, stderr.contains("[ERROR] KCSG.Polygon: "))
        assertTrue(stderr, stderr.contains("Normal is zero!"))
        assertFalse(stderr, stderr.contains("[INFO] "))
    }

    @Test
    fun verboseAddsInformationalDiagnostics() {
        val stderr = runDegenerateScript("-v")

        assertTrue(stderr, stderr.contains("[ERROR] KCSG.Polygon: "))
        assertTrue(stderr, stderr.contains("[INFO] "))
    }
}

package com.monkopedia.csgs

import com.monkopedia.kcsg.Logger
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StderrLoggerTest {
    private val captured = ByteArrayOutputStream()
    private val stream = PrintStream(captured, true)

    private fun logAllLevels(verbose: Boolean): String {
        val logger = StderrLogger(verbose, stream)
        Logger.Level.entries.forEach { logger.log(it, "Tag", "message for $it") }
        return captured.toString()
    }

    @Test
    fun quietEmitsOnlyWarnAndError() {
        val output = logAllLevels(verbose = false)

        assertEquals(
            listOf("[WARN] Tag: message for WARN", "[ERROR] Tag: message for ERROR"),
            output.trim().lines()
        )
    }

    @Test
    fun verboseEmitsEveryLevel() {
        val output = logAllLevels(verbose = true)

        assertEquals(Logger.Level.entries.size, output.trim().lines().size)
        assertTrue(output.contains("[TRACE] Tag: message for TRACE"))
        assertTrue(output.contains("[INFO] Tag: message for INFO"))
    }

    @Test
    fun throwableIsPrinted() {
        StderrLogger(verbose = false, stream = stream)
            .error("Tag", "boom", IllegalStateException("cause text"))

        val output = captured.toString()
        assertTrue(output.contains("[ERROR] Tag: boom"))
        assertTrue(output.contains("IllegalStateException: cause text"))
    }
}

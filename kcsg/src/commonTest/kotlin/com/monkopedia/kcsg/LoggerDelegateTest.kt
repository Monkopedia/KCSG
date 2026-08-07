package com.monkopedia.kcsg

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class LoggerDelegateTest {
    private data class LogEntry(
        val level: Logger.Level,
        val tag: String,
        val message: String,
        val throwable: Throwable?
    )

    private class RecordingLogger : Logger {
        val entries = mutableListOf<LogEntry>()

        override fun log(level: Logger.Level, tag: String, message: String, throwable: Throwable?) {
            entries.add(LogEntry(level, tag, message, throwable))
        }
    }

    @Test
    fun defaultLoggerDropsCalls() {
        val recorder = RecordingLogger()
        try {
            // Sanity: the recorder is wired up, so an empty recorder below means the
            // calls were dropped rather than that the recorder never worked.
            Logger.setLogger(recorder)
            Logger.info("KCSG.Test", "observed")
            assertEquals(1, recorder.entries.size, "installed delegate should receive calls")

            Logger.setLogger(null)
            Logger.trace("KCSG.Test", "ignored")
            Logger.debug("KCSG.Test", "ignored")
            Logger.info("KCSG.Test", "ignored")
            Logger.warn("KCSG.Test", "ignored")
            Logger.error("KCSG.Test", "ignored", IllegalStateException("boom"))
            Logger.tagged("KCSG.Test").info("ignored")

            assertEquals(
                listOf("observed"),
                recorder.entries.map { it.message },
                "with no delegate installed every call must be dropped"
            )
        } finally {
            Logger.setLogger(null)
        }
    }

    @Test
    fun installingTheCompanionAsItsOwnDelegateDropsCalls() {
        val recorder = RecordingLogger()
        try {
            Logger.setLogger(recorder)
            // Would otherwise recurse forever through Logger.Companion.log.
            Logger.setLogger(Logger)
            Logger.info("KCSG.Test", "ignored")

            assertEquals(
                emptyList(),
                recorder.entries.map { it.message },
                "installing the companion as its own delegate must clear the delegate"
            )
        } finally {
            Logger.setLogger(null)
        }
    }

    @Test
    fun companionDispatchesAllLevelsWithTagAndThrowable() {
        val recorder = RecordingLogger()
        val throwable = IllegalStateException("boom")
        try {
            Logger.setLogger(recorder)
            Logger.trace("KCSG.Test", "trace")
            Logger.debug("KCSG.Test", "debug")
            Logger.info("KCSG.Test", "info")
            Logger.warn("KCSG.Test", "warn")
            Logger.error("KCSG.Test", "error", throwable)
        } finally {
            Logger.setLogger(null)
        }

        assertEquals(5, recorder.entries.size)
        assertEquals(Logger.Level.TRACE, recorder.entries[0].level)
        assertEquals(Logger.Level.DEBUG, recorder.entries[1].level)
        assertEquals(Logger.Level.INFO, recorder.entries[2].level)
        assertEquals(Logger.Level.WARN, recorder.entries[3].level)
        assertEquals(Logger.Level.ERROR, recorder.entries[4].level)
        assertEquals("KCSG.Test", recorder.entries[4].tag)
        assertEquals("error", recorder.entries[4].message)
        assertEquals(throwable, recorder.entries[4].throwable)
    }

    @Test
    fun taggedLoggerUsesConfiguredTag() {
        val recorder = RecordingLogger()
        try {
            Logger.setLogger(recorder)
            val tagged = Logger.tagged("KCSG.Tagged")
            tagged.debug("msg")
            tagged.warn("warn")
            tagged.error("error")
        } finally {
            Logger.setLogger(null)
        }

        assertEquals(3, recorder.entries.size)
        recorder.entries.forEach { entry ->
            assertEquals("KCSG.Tagged", entry.tag)
            assertNull(entry.throwable)
        }
        assertEquals(Logger.Level.DEBUG, recorder.entries[0].level)
        assertEquals(Logger.Level.WARN, recorder.entries[1].level)
        assertEquals(Logger.Level.ERROR, recorder.entries[2].level)
    }
}

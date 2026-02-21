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
        Logger.setLogger(null)
        Logger.info("KCSG.Test", "ignored")
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

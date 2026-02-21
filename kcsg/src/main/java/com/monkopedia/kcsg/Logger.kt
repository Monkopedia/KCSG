package com.monkopedia.kcsg

/**
 * Logging abstraction for KCSG that supports delegate installation.
 *
 * By default no logger is installed and all log calls are dropped.
 */
interface Logger {
    enum class Level {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    fun log(level: Level, tag: String, message: String, throwable: Throwable? = null)

    fun trace(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.TRACE, tag, message, throwable)
    }

    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.DEBUG, tag, message, throwable)
    }

    fun info(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.INFO, tag, message, throwable)
    }

    fun warn(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.WARN, tag, message, throwable)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message, throwable)
    }

    companion object : Logger {
        private var delegate: Logger? = null

        fun setLogger(logger: Logger?) {
            delegate = if (logger === this) null else logger
        }

        fun tagged(tag: String): TaggedLogger = TaggedLogger(tag)

        override fun log(level: Level, tag: String, message: String, throwable: Throwable?) {
            delegate?.log(level, tag, message, throwable)
        }
    }
}

class TaggedLogger internal constructor(private val tag: String) {
    fun trace(message: String, throwable: Throwable? = null) {
        Logger.trace(tag, message, throwable)
    }

    fun debug(message: String, throwable: Throwable? = null) {
        Logger.debug(tag, message, throwable)
    }

    fun info(message: String, throwable: Throwable? = null) {
        Logger.info(tag, message, throwable)
    }

    fun warn(message: String, throwable: Throwable? = null) {
        Logger.warn(tag, message, throwable)
    }

    fun error(message: String, throwable: Throwable? = null) {
        Logger.error(tag, message, throwable)
    }
}

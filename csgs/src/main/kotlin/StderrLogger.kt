package com.monkopedia.csgs

import com.monkopedia.kcsg.Logger
import java.io.PrintStream

/**
 * [Logger] delegate that writes KCSG diagnostics to [stream].
 *
 * Warnings and errors are always emitted, since a silently discarded parse failure or
 * degenerate mesh looks like a successful run. Lower levels are only emitted when
 * [verbose] is set.
 *
 * Diagnostics never go to stdout, which carries the CLI's own output.
 */
internal class StderrLogger(
    private val verbose: Boolean,
    private val stream: PrintStream = System.err
) : Logger {
    override fun log(
        level: Logger.Level,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        if (!verbose && level < Logger.Level.WARN) return
        stream.println("[$level] $tag: $message")
        throwable?.printStackTrace(stream)
    }
}

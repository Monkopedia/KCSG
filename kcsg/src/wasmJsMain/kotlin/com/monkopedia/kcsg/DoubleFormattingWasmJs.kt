package com.monkopedia.kcsg

// This target's Double.toString digits are not always the shortest or closest (#41).
internal actual fun platformJavaString(value: Double): String = shortestJavaString(value)

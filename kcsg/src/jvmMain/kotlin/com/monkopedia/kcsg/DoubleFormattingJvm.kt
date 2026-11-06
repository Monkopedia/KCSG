package com.monkopedia.kcsg

// java.lang.Double.toString is the reference rendering, so the JVM uses it unchanged.
internal actual fun platformJavaString(value: Double): String = value.toString()

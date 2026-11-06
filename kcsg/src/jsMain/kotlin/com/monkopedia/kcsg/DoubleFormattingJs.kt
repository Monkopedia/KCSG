package com.monkopedia.kcsg

// Number::toString already yields the shortest, closest digits; only the layout differs.
internal actual fun platformJavaString(value: Double): String =
    formatJavaFromEcmaScript(value, value.toString())

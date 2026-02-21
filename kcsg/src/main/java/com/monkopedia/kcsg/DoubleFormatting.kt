package com.monkopedia.kcsg

internal fun Double.toJvmString(): String {
    val rendered = toString()
    if (rendered.contains('.') || rendered.contains('e') || rendered.contains('E')) {
        return rendered
    }
    return "$rendered.0"
}

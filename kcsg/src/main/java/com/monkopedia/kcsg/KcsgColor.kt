package com.monkopedia.kcsg

data class KcsgColor(
    val red: Double,
    val green: Double,
    val blue: Double,
) {
    companion object {
        val RED = KcsgColor(1.0, 0.0, 0.0)
        val YELLOW = KcsgColor(1.0, 1.0, 0.0)
        val GREEN = KcsgColor(0.0, 1.0, 0.0)
        val BLUE = KcsgColor(0.0, 0.0, 1.0)
        val MAGENTA = KcsgColor(1.0, 0.0, 1.0)
        val WHITE = KcsgColor(1.0, 1.0, 1.0)
        val BLACK = KcsgColor(0.0, 0.0, 0.0)
        val GRAY = KcsgColor(0.5, 0.5, 0.5)
        val ORANGE = KcsgColor(1.0, 0.6470588235294118, 0.0)
    }
}

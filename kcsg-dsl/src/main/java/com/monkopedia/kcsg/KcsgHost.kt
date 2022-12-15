package com.monkopedia.kcsg

import java.nio.file.Path

interface KcsgHost {
    fun findStl(stlName: String): Path
}

object EmptyHost : KcsgHost {
    override fun findStl(stlName: String): Path = error("Not implemented")
}
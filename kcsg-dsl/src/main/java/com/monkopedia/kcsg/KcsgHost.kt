package com.monkopedia.kcsg

import java.nio.file.Path

interface KcsgHost {
    fun findStl(stlName: String): Path
    fun findScript(csgsName: String): ImportedScript
}

object EmptyHost : KcsgHost {
    override fun findStl(stlName: String): Path = error("Not implemented")
    override fun findScript(csgsName: String): ImportedScript = error("Not implemented")
}
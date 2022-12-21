package com.monkopedia.kcsg

import java.nio.file.Path

interface KcsgHost {
    fun findStl(stlName: String): Path
    fun findScript(csgsName: String): ImportedScript

    val supportsCaching: Boolean

    fun checkCached(hash: String): CSG?
    fun storeCached(hash: String, csg: CSG)
}

object EmptyHost : KcsgHost {
    override fun findStl(stlName: String): Path = error("Not implemented")
    override fun findScript(csgsName: String): ImportedScript = error("Not implemented")

    override val supportsCaching: Boolean
        get() = false
    override fun checkCached(hash: String): CSG? = null
    override fun storeCached(hash: String, csg: CSG) = error("Not implemented")
}
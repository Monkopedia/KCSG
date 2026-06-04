package com.monkopedia.kcsg

import kotlinx.io.files.Path

interface KcsgHost {
    fun findStl(stlName: String): Path
    fun findScript(csgsName: String): ImportedScript

    /**
     * Returns an opaque version token for the named STL that changes whenever the STL's content
     * changes (e.g. the source file's modification time, or a content version). It is folded into
     * the cache key of an `stl()` property so that re-editing a source STL under the same name
     * invalidates cached results that consumed it. The default `""` means "no versioning" — the
     * cache key is the STL name only, matching prior behavior.
     */
    fun stlVersion(stlName: String): String = ""

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

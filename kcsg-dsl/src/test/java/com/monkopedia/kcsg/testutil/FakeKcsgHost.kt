package com.monkopedia.kcsg.testutil

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.ImportedScript
import com.monkopedia.kcsg.KcsgHost
import java.nio.file.Path
import kotlin.io.path.Path

class FakeKcsgHost(
    override val supportsCaching: Boolean = true,
) : KcsgHost {
    val stlPaths = mutableMapOf<String, Path>()
    val scripts = mutableMapOf<String, ImportedScript>()
    val cache = mutableMapOf<String, CSG>()

    val stlRequests = mutableListOf<String>()
    val scriptRequests = mutableListOf<String>()
    val cacheChecks = mutableListOf<String>()
    val cacheStores = mutableListOf<String>()

    override fun findStl(stlName: String): Path {
        stlRequests.add(stlName)
        return stlPaths[stlName] ?: error("No STL registered for '$stlName'")
    }

    override fun findScript(csgsName: String): ImportedScript {
        scriptRequests.add(csgsName)
        return scripts[csgsName] ?: error("No script registered for '$csgsName'")
    }

    override fun checkCached(hash: String): CSG? {
        cacheChecks.add(hash)
        return cache[hash]
    }

    override fun storeCached(hash: String, csg: CSG) {
        cacheStores.add(hash)
        cache[hash] = csg
    }

    fun registerStl(name: String, path: String) {
        stlPaths[name] = Path(path)
    }

    fun registerScript(name: String, script: ImportedScript) {
        scripts[name] = script
    }
}

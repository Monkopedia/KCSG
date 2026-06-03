package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.FakeKcsgHost
import java.nio.file.Files
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KcsgScriptJvmIoTest {

    @Test
    fun stlVersionTokenChangesDependentCacheHash() {
        // A csg{} property importing an STL by name must change its cache key when the host
        // reports a new version token for that STL — so re-editing the source STL invalidates
        // cached results that consumed it.
        val host = FakeKcsgHost(supportsCaching = true)
        val stlPath = Files.createTempFile("kcsg-stl-version", ".stl")
        FileUtil.toStlFile(Path(stlPath.toString()), Cube(1.0).toCSG())
        host.registerStl("widget", stlPath.toString())

        fun cacheHashWithVersion(version: String): String {
            host.stlVersions["widget"] = version
            val script = KcsgScript(host)
            val widget by script.stl("widget")
            val part by script.csg { widget }
            part.polygons // force the real build → getCached → storeCached(hash)
            return host.cacheStores.last()
        }

        val v1 = cacheHashWithVersion("2026-01-01T00:00:00Z")
        val v2 = cacheHashWithVersion("2026-05-23T12:00:00Z")
        assertNotEquals(v1, v2)

        stlPath.toFile().delete()
    }
    @Test
    fun hostDelegationAndScriptEnvelopeConstants() {
        val host = FakeKcsgHost(supportsCaching = false)
        val fixturePath = Files.createTempFile("kcsg-script-host-delegation", ".stl")
        FileUtil.toStlFile(Path(fixturePath.toString()), Cube(1.0).toCSG())
        host.registerStl("fixture", fixturePath.toString())
        val importedScript = object : ImportedScript {
            override val exports: Collection<String> = listOf("x")
            override val targets: Collection<String> = listOf("x")
            override fun get(name: String): CSG = Cube(1.0).toCSG()
        }
        host.registerScript("dep", importedScript)

        val script = KcsgScript(host)
        val stl by script.stl("fixture")
        assertTrue(stl.polygons.isNotEmpty())
        assertSame(importedScript, script.import("dep").value)
        assertEquals(listOf("fixture"), host.stlRequests)
        assertEquals(listOf("dep"), host.scriptRequests)

        assertTrue(KcsgScript.HEADER.contains("KcsgScript().apply"))
        assertNotNull(KcsgScript.FOOTER)
        assertTrue(KcsgScript.FOOTER.trim().endsWith("}"))

        fixturePath.toFile().delete()
    }
}

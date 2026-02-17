package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.FakeKcsgHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class KcsgScriptTest {
    @Test
    fun overrideExportGenerateExportsTargetsAndCacheDelegation() {
        val host = FakeKcsgHost(supportsCaching = true)
        val script = KcsgScript(host)
        val alpha by script.csg(exported = true, allowCaching = true) {
            Cube(1.0).toCSG()
        }
        val beta by script.csg(exported = false, allowCaching = true) {
            Cube(
                center = Vector3d.xyz(2.0, 0.0, 0.0),
                dimensions = Vector3d.xyz(1.0, 1.0, 1.0),
            ).toCSG()
        }

        assertTrue(script.targets().containsAll(listOf("alpha", "beta")))
        assertTrue(script.exports().contains("alpha"))
        assertFalse(script.exports().contains("beta"))

        val defaultExports = script.generateExports()
        assertEquals(setOf("alpha"), defaultExports.keys)
        assertEquals(alpha.computeVolume(), defaultExports["alpha"]!!.computeVolume(), 1e-4)

        val betaTarget = script.generateTarget("beta")
        assertEquals(beta.computeVolume(), betaTarget.computeVolume(), 1e-4)
        assertThrows(IllegalStateException::class.java) {
            script.generateTarget("missing")
        }

        script.overrideExport("beta", true)
        assertTrue(script.exports().contains("beta"))
        script.overrideExport("alpha", false)
        assertFalse(script.exports().contains("alpha"))

        val checksAfterFirstRead = host.cacheChecks.size
        val storesAfterFirstRead = host.cacheStores.size
        assertTrue(checksAfterFirstRead > 0)
        assertTrue(storesAfterFirstRead > 0)

        alpha
        assertEquals(checksAfterFirstRead, host.cacheChecks.size)
        assertEquals(storesAfterFirstRead, host.cacheStores.size)
    }

    @Test
    fun hostDelegationAndScriptEnvelopeConstants() {
        val host = FakeKcsgHost(supportsCaching = false)
        val fixturePath = Files.createTempFile("kcsg-script-host-delegation", ".stl")
        FileUtil.toStlFile(fixturePath, Cube(1.0).toCSG())
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

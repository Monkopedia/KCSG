package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.FakeKcsgHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

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
}

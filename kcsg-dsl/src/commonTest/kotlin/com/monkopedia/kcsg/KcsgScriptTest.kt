package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.FakeKcsgHost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        assertFailsWith<IllegalStateException> {
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
    fun allowCachingFalsePropertyCanBeReadThroughCacheableProperties() {
        // #82: a custom WeightFunction cannot be hashed, so its property opts out of caching. A
        // cacheable property that reads it must not try to hash it either; instead it inherits
        // the opt-out and is built uncached.
        val host = FakeKcsgHost(supportsCaching = true)
        val script = KcsgScript(host)
        val shaped by script.csg(allowCaching = false) {
            Cube(2.0).toCSG().weighted(WeightFunction { _, _ -> 0.5 })
        }
        val part by script.csg { shaped }
        val outer by script.csg {
            part.union(
                Cube(
                    center = Vector3d.xyz(5.0, 0.0, 0.0),
                    dimensions = Vector3d.xyz(1.0, 1.0, 1.0),
                ).toCSG()
            )
        }
        val independent by script.csg { Cube(3.0).toCSG() }

        assertEquals(8.0, shaped.computeVolume(), 1e-4)
        assertEquals(8.0, part.computeVolume(), 1e-4)
        assertEquals(9.0, outer.computeVolume(), 1e-4)
        assertTrue(host.cacheStores.isEmpty(), "no dependent of shaped may be cached")

        assertEquals(27.0, independent.computeVolume(), 1e-4)
        assertEquals(1, host.cacheStores.size, "unrelated properties still cache")
    }
}

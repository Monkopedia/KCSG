package com.monkopedia.kcsg

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlin.test.Test

class ImportedScriptTest {
    @Test
    fun importedKcsgScriptSurfacesExportsTargetsAndGet() {
        val script = KcsgScript()
        val alpha by script.csg(exported = true) { Cube(1.0).toCSG() }
        val beta by script.csg(exported = false) { Cube(2.0).toCSG() }
        val imported = ImportedKcsgScript(script)

        assertTrue(imported.exports.contains("alpha"))
        assertFalse(imported.exports.contains("beta"))
        assertTrue(imported.targets.containsAll(listOf("alpha", "beta")))

        assertEquals(alpha.computeVolume(), imported["alpha"].computeVolume(), 1e-4)
        assertEquals(beta.computeVolume(), imported["beta"].computeVolume(), 1e-4)
        assertFailsWith<IllegalStateException> {
            imported["missing"]
        }
    }
}

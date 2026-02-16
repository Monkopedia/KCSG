package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

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
        assertThrows(IllegalStateException::class.java) {
            imported["missing"]
        }
    }
}

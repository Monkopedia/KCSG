package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PropertyStorageTest {
    @Test
    fun setAndTypedGetLookup() {
        val storage = PropertyStorage()
        storage["name"] = "cube"
        storage["count"] = 7

        val name: String? = storage.getValue("name")
        val count: Int? = storage.getValue("count")
        val missing: String? = storage.getValue("missing")

        assertEquals("cube", name)
        assertEquals(7, count)
        assertNull(missing)

        assertThrows(ClassCastException::class.java) {
            val wrongType: Double? = storage.getValue("count")
            // Keep a usage so the assignment isn't optimized away.
            assertNull(wrongType)
        }
    }

    @Test
    fun deleteAndContains() {
        val storage = PropertyStorage()
        storage["key"] = "value"

        assertTrue("key" in storage)
        storage.delete("key")
        assertFalse("key" in storage)

        // Deleting a missing key is a no-op.
        storage.delete("key")
        assertFalse("key" in storage)
    }

    @Test
    fun randomColorInitializationAndCompanionHelper() {
        val storage = PropertyStorage()
        assertTrue("material:color" in storage)

        val initialColor: String? = storage.getValue("material:color")
        assertNotNull(initialColor)
        assertValidRgbString(initialColor!!)

        storage["material:color"] = "not-a-color"
        PropertyStorage.randomColor(storage)

        val randomizedColor: String? = storage.getValue("material:color")
        assertNotNull(randomizedColor)
        assertValidRgbString(randomizedColor!!)
    }

    private fun assertValidRgbString(value: String) {
        val parts = value.split(" ")
        assertEquals(3, parts.size)
        parts.forEach { part ->
            val numeric = part.toDouble()
            assertTrue(numeric in 0.0..1.0)
        }
    }
}

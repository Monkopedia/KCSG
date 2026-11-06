package com.monkopedia.kcsg

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    fun newStorageHasNoMaterialColor() {
        // Regression for #46: the constructor used to assign a random colour.
        val storage = PropertyStorage()
        assertFalse("material:color" in storage)
        assertNull(storage.getValue<String>("material:color"))
    }

    @Test
    fun randomColorCompanionHelperAssignsValidColor() {
        val storage = PropertyStorage()
        storage["material:color"] = "not-a-color"
        PropertyStorage.randomColor(storage)

        val randomizedColor: String? = storage.getValue("material:color")
        assertValidRgbString(assertNotNull(randomizedColor))
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

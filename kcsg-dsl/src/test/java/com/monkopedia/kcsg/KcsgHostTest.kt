package com.monkopedia.kcsg

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class KcsgHostTest {
    @Test
    fun emptyHostDefaultsAndErrorPaths() {
        assertFalse(EmptyHost.supportsCaching)
        assertNull(EmptyHost.checkCached("missing"))

        assertThrows(IllegalStateException::class.java) {
            EmptyHost.findStl("shape.stl")
        }
        assertThrows(IllegalStateException::class.java) {
            EmptyHost.findScript("shape.csgs")
        }
        assertThrows(IllegalStateException::class.java) {
            EmptyHost.storeCached("hash", Cube(1.0).toCSG())
        }
    }
}

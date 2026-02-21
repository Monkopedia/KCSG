package com.monkopedia.kcsg

import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertFailsWith
import kotlin.test.Test

class KcsgHostTest {
    @Test
    fun emptyHostDefaultsAndErrorPaths() {
        assertFalse(EmptyHost.supportsCaching)
        assertNull(EmptyHost.checkCached("missing"))

        assertFailsWith<IllegalStateException> {
            EmptyHost.findStl("shape.stl")
        }
        assertFailsWith<IllegalStateException> {
            EmptyHost.findScript("shape.csgs")
        }
        assertFailsWith<IllegalStateException> {
            EmptyHost.storeCached("hash", Cube(1.0).toCSG())
        }
    }
}

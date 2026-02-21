package com.monkopedia.kcsg

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.Test

class CylinderRadiusPropertyTest {
    @Test
    fun getterAndSetterOnEvenCylinder() {
        val cylinder = Cylinder(radius = 1.5, height = 3.0, numSlices = 12)
        assertEquals(1.5, cylinder.radius, 1e-9)

        cylinder.radius = 2.25
        assertEquals(2.25, cylinder.startRadius, 1e-9)
        assertEquals(2.25, cylinder.endRadius, 1e-9)
        assertEquals(2.25, cylinder.radius, 1e-9)
    }

    @Test
    fun getterFailsOnUnevenCylinder() {
        val uneven = Cylinder(
            start = Vector3d.ZERO,
            end = Vector3d.Z_ONE.times(2.0),
            startRadius = 1.0,
            endRadius = 2.0,
            numSlices = 12,
        )

        assertFailsWith<IllegalArgumentException> {
            @Suppress("UNUSED_VARIABLE")
            val ignored = uneven.radius
        }
    }
}

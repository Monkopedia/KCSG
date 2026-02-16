package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Test

class AxisModifierTest {
    @Test
    fun xModifierSupportsCenteredAndNonCenteredModes() {
        val csg = Cube(2.0).toCSG()

        val nonCentered = XModifier()
        assertEquals(0.0, nonCentered.eval(Vector3d.xyz(-1.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(0.5, nonCentered.eval(Vector3d.xyz(0.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(1.0, nonCentered.eval(Vector3d.xyz(1.0, 0.0, 0.0), csg), 1e-9)

        val centered = XModifier(true)
        assertEquals(1.0, centered.eval(Vector3d.xyz(-1.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(0.0, centered.eval(Vector3d.xyz(0.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(1.0, centered.eval(Vector3d.xyz(1.0, 0.0, 0.0), csg), 1e-9)
    }

    @Test
    fun yModifierSupportsCenteredAndNonCenteredModes() {
        val csg = Cube(2.0).toCSG()

        val nonCentered = YModifier()
        assertEquals(0.0, nonCentered.eval(Vector3d.xyz(0.0, -1.0, 0.0), csg), 1e-9)
        assertEquals(0.5, nonCentered.eval(Vector3d.xyz(0.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(1.0, nonCentered.eval(Vector3d.xyz(0.0, 1.0, 0.0), csg), 1e-9)

        val centered = YModifier(true)
        assertEquals(1.0, centered.eval(Vector3d.xyz(0.0, -1.0, 0.0), csg), 1e-9)
        assertEquals(0.0, centered.eval(Vector3d.xyz(0.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(1.0, centered.eval(Vector3d.xyz(0.0, 1.0, 0.0), csg), 1e-9)
    }

    @Test
    fun zModifierSupportsCenteredAndNonCenteredModes() {
        val csg = Cube(2.0).toCSG()

        val nonCentered = ZModifier()
        assertEquals(0.0, nonCentered.eval(Vector3d.xyz(0.0, 0.0, -1.0), csg), 1e-9)
        assertEquals(0.5, nonCentered.eval(Vector3d.xyz(0.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(1.0, nonCentered.eval(Vector3d.xyz(0.0, 0.0, 1.0), csg), 1e-9)

        val centered = ZModifier(true)
        assertEquals(1.0, centered.eval(Vector3d.xyz(0.0, 0.0, -1.0), csg), 1e-9)
        assertEquals(0.0, centered.eval(Vector3d.xyz(0.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(1.0, centered.eval(Vector3d.xyz(0.0, 0.0, 1.0), csg), 1e-9)
    }
}

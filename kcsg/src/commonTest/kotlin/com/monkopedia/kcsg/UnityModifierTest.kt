package com.monkopedia.kcsg

import kotlin.test.assertEquals
import kotlin.test.Test

class UnityModifierTest {
    @Test
    fun unityModifierAlwaysReturnsOne() {
        val modifier = UnityModifier()
        val csg = Cube(2.0).toCSG()

        assertEquals(1.0, modifier.eval(Vector3d.xyz(-1.0, -1.0, -1.0), csg), 1e-9)
        assertEquals(1.0, modifier.eval(Vector3d.xyz(0.0, 0.0, 0.0), csg), 1e-9)
        assertEquals(1.0, modifier.eval(Vector3d.xyz(1.0, 1.0, 1.0), csg), 1e-9)
    }

    @Test
    fun weightedWithUnityModifierLeavesWeightsAtOne() {
        val base = Cube(2.0).toCSG()
        val weighted = base.weighted(UnityModifier())

        weighted.polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                assertEquals(1.0, vertex.weight, 1e-9)
            }
        }
    }
}

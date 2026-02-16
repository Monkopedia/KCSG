package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WeightFunctionAndModifierTest {
    @Test
    fun weightedInvokesWeightFunctionAndAppliesWeightsToCopy() {
        val base = Cube(2.0).toCSG()
        var calls = 0
        var receivedCsg: CSG? = null

        val weighted = base.weighted(
            WeightFunction { position, csg ->
                calls++
                if (receivedCsg == null) {
                    receivedCsg = csg
                } else {
                    assertSame(receivedCsg, csg)
                }
                position.x + position.y + position.z
            },
        )

        assertNotSame(base, weighted)
        assertTrue(calls > 0)
        weighted.polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                assertEquals(vertex.pos.x + vertex.pos.y + vertex.pos.z, vertex.weight, 1e-9)
            }
        }
        base.polygons.forEach { polygon ->
            polygon.vertices.forEach { vertex ->
                assertEquals(1.0, vertex.weight, 1e-9)
            }
        }
    }
}

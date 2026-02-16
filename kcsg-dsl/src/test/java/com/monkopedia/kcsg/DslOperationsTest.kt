package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Test

class DslOperationsTest {
    @Test
    fun operationOverloadsAcrossCsgAndPrimitivePairs() {
        val p1 = Cube(center = Vector3d.xyz(0.0, 0.0, 0.0), dimensions = Vector3d.xyz(2.0, 2.0, 2.0))
        val p2 = Cube(center = Vector3d.xyz(0.5, 0.0, 0.0), dimensions = Vector3d.xyz(2.0, 2.0, 2.0))
        val c1 = p1.toCSG()
        val c2 = p2.toCSG()

        val unionExpected = c1.union(c2).computeVolume()
        val differenceExpected = c1.difference(c2).computeVolume()
        val intersectExpected = c1.intersect(c2).computeVolume()

        // and / intersect
        assertVolume(intersectExpected, c1 and c2)
        assertVolume(intersectExpected, p1 and c2)
        assertVolume(intersectExpected, c1 and p2)
        assertVolume(intersectExpected, p1 and p2)

        // or / union
        assertVolume(unionExpected, c1 or c2)
        assertVolume(unionExpected, p1 or c2)
        assertVolume(unionExpected, c1 or p2)
        assertVolume(unionExpected, p1 or p2)

        // xor / difference
        assertVolume(differenceExpected, c1 xor c2)
        assertVolume(differenceExpected, p1 xor c2)
        assertVolume(differenceExpected, c1 xor p2)
        assertVolume(differenceExpected, p1 xor p2)

        // plus / union
        assertVolume(unionExpected, c1 + c2)
        assertVolume(unionExpected, p1 + c2)
        assertVolume(unionExpected, c1 + p2)
        assertVolume(unionExpected, p1 + p2)

        // minus / difference
        assertVolume(differenceExpected, c1 - c2)
        assertVolume(differenceExpected, p1 - c2)
        assertVolume(differenceExpected, c1 - p2)
        assertVolume(differenceExpected, p1 - p2)

        // times / intersect
        assertVolume(intersectExpected, c1 * c2)
        assertVolume(intersectExpected, p1 * c2)
        assertVolume(intersectExpected, c1 * p2)
        assertVolume(intersectExpected, p1 * p2)
    }

    private fun assertVolume(expected: Double, actual: CSG) {
        assertEquals(expected, actual.computeVolume(), 1e-4)
    }
}

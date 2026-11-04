package com.monkopedia.kcsg

import kotlin.test.assertEquals
import kotlin.test.Test

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

    /** Regression for issue #88: `and` / `*` must not resurrect geometry from an empty step. */
    @Test
    fun intersectOperatorsWithEmptyOperandAreEmpty() {
        val a = Cube(center = Vector3d.xyz(0.0, 0.0, 0.0), dimensions = Vector3d.xyz(2.0, 2.0, 2.0))
        val far = Cube(center = Vector3d.xyz(10.0, 0.0, 0.0), dimensions = Vector3d.xyz(2.0, 2.0, 2.0))
        val empty = CSG.fromPolygons(emptyList())

        listOf(
            "(a and far) and a" to ((a and far) and a),
            "(a * far) * a" to ((a * far) * a),
            "a and empty" to (a and empty),
            "empty and a" to (empty and a),
            "a * empty" to (a * empty),
            "empty * a" to (empty * a),
        ).forEach { (name, result) ->
            assertEquals(0, result.polygons.size, "$name must have no polygons")
            assertEquals(0.0, result.computeVolume(), "$name must have volume 0")
        }
    }

    private fun assertVolume(expected: Double, actual: CSG) {
        assertEquals(expected, actual.computeVolume(), 1e-4)
    }
}

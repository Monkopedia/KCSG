/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.jcsg.Edge.Companion.boundaryPathsWithHoles
import eu.mihosoft.jcsg.Polygon.Companion.fromPoints
import eu.mihosoft.vvecmath.Vector3d
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class HoleDetectionTest {
    @Test
    fun holeDetectionTest() {

        // one polygon with one hole
        val p1 = fromPoints(
            Vector3d.xy(1.0, 1.0),
            Vector3d.xy(2.0, 3.0),
            Vector3d.xy(4.0, 3.0),
            Vector3d.xy(5.0, 2.0),
            Vector3d.xy(4.0, 1.0),
            Vector3d.xy(3.0, 0.0),
            Vector3d.xy(2.0, 2.0)
        )
        val p1Hole = fromPoints(
            Vector3d.xy(3.0, 1.0),
            Vector3d.xy(3.0, 2.0),
            Vector3d.xy(4.0, 2.0)
        )
        createNumHolesTest(Arrays.asList(p1, p1Hole), 1, 0)

        // one polygon with two holes
        val p2 = fromPoints(
            Vector3d.xy(1.0, 1.0),
            Vector3d.xy(2.0, 2.0),
            Vector3d.xy(1.0, 5.0),
            Vector3d.xy(2.0, 6.0),
            Vector3d.xy(6.0, 6.0),
            Vector3d.xy(3.0, 5.0),
            Vector3d.xy(6.0, 5.0),
            Vector3d.xy(6.0, 1.0),
            Vector3d.xy(3.0, 0.0)
        )
        val p2Hole1 = fromPoints(
            Vector3d.xy(3.0, 2.0),
            Vector3d.xy(3.0, 3.0),
            Vector3d.xy(4.0, 2.0),
            Vector3d.xy(4.0, 1.0)
        )
        val p2Hole2 = fromPoints(
            Vector3d.xy(2.0, 3.0),
            Vector3d.xy(2.0, 4.0),
            Vector3d.xy(3.0, 4.0)
        )
        createNumHolesTest(Arrays.asList(p2, p2Hole1, p2Hole2), 2, 0, 0)

        // one polygon with two holes, one of the holes contains another
        // polygon with one hole
        val p3 = fromPoints(
            Vector3d.xy(1.0, 1.0),
            Vector3d.xy(2.0, 2.0),
            Vector3d.xy(1.0, 5.0),
            Vector3d.xy(2.0, 6.0),
            Vector3d.xy(6.0, 6.0),
            Vector3d.xy(3.0, 5.0),
            Vector3d.xy(6.0, 5.0),
            Vector3d.xy(6.0, 1.0),
            Vector3d.xy(3.0, 0.0)
        )
        val p3Hole1 = fromPoints(
            Vector3d.xy(3.0, 2.0),
            Vector3d.xy(3.0, 3.0),
            Vector3d.xy(4.0, 4.0),
            Vector3d.xy(5.0, 3.0),
            Vector3d.xy(5.0, 2.0),
            Vector3d.xy(4.0, 1.0)
        )
        val p3p1 = fromPoints(
            Vector3d.xy(4.0, 2.0),
            Vector3d.xy(3.5, 2.5),
            Vector3d.xy(4.0, 3.0),
            Vector3d.xy(4.5, 2.5)
        )
        val p3p1Hole = fromPoints(
            Vector3d.xy(4.0, 2.25),
            Vector3d.xy(3.75, 2.5),
            Vector3d.xy(4.0, 2.75),
            Vector3d.xy(4.25, 2.5)
        )
        val p3Hole2 = fromPoints(
            Vector3d.xy(2.0, 3.0),
            Vector3d.xy(2.0, 4.0),
            Vector3d.xy(3.0, 4.0)
        )
        createNumHolesTest(
            Arrays.asList(p3, p3Hole1, p3Hole2, p3p1, p3p1Hole),
            2, 0, 0, 1, 0
        )
    }

    companion object {
        private fun createNumHolesTest(
            polygons: List<Polygon>, vararg numHoles: Int
        ) {
            var polygons = polygons
            require(polygons.size == numHoles.size) {
                ("Number of polygons and number of entries in numHoles-array"
                    + " are not equal!")
            }
            polygons = boundaryPathsWithHoles(polygons)
            for (i in polygons.indices) {
                val holesOfPresult =
                    polygons[i].storage.getValue<List<Polygon>>(Edge.KEY_POLYGON_HOLES)
                var numHolesOfP: Int
                numHolesOfP = if (!holesOfPresult.isPresent) {
                    0
                } else {
                    val holesOfP = holesOfPresult.get()
                    holesOfP.size
                }
                Assert.assertTrue(
                    "Polygon " + i + ": Expected " + numHoles[i]
                        + " holes, got "
                        + numHolesOfP, numHolesOfP == numHoles[i]
                )
            }
        }
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.vvecmath.Vector3d
import org.junit.Assert
import org.junit.Test

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class EdgeIntersectionTest {
    @Test
    fun closestPointTest() {

        // closest point is e1p2
        createClosestPointTest(
            Vector3d.xyz(1.0, 2.0, 3.0),  /*e1p2*/Vector3d.xyz(4.0, 5.0, 6.0),
            Vector3d.xyz(4.0, 5.0, 7.0), Vector3d.xyz(0.0, 1.0, 7.0),
            Vector3d.xyz(4.0, 5.0, 6.0)
        )

        // parallel edges (result=null)
        createClosestPointTest(
            Vector3d.xyz(1.0, 1.0, -1.0), Vector3d.xyz(1.0, 1.0, 1.0),
            Vector3d.xyz(2.0, 2.0, -3.0), Vector3d.xyz(2.0, 2.0, 4.0),
            null
        )
        createClosestPointTest(
            Vector3d.xyz(1.0, 3.0, -1.0),
            Vector3d.xyz(1.0, 4.0, 2.0),
            Vector3d.xyz((1 + 10).toDouble(), 3.0, -1.0),
            Vector3d.xyz((1 + 10).toDouble(), 4.0, 2.0),
            null
        )
        createClosestPointTest(
            Vector3d.xyz(3.0, 6.0, -1.0),
            Vector3d.xyz(10.0, 7.0, 1.0),
            Vector3d.xyz(3.0, 6.0, (-1 + 3).toDouble()),
            Vector3d.xyz(10.0, 7.0, (1 + 3).toDouble()),
            null
        )

        // result is exactly in the middle of e1 and e2
        createClosestPointTest(
            Vector3d.xyz(5.0, 4.0, 2.0),  /*e1p2*/Vector3d.xyz(3.0, 2.0, 11.0),
            Vector3d.xyz(5.0, 2.0, 11.0),  /*e1p2*/Vector3d.xyz(3.0, 4.0, 2.0),
            Vector3d.xyz(4.0, 3.0, 6.5)
        )
    }

    @Test
    fun intersectionTest() {
        // closest point is e1p2 which does not exist on e2. thus, the expected
        // result is null
        createIntersectionTest(
            Vector3d.xyz(1.0, 2.0, 3.0),  /*e1p2*/Vector3d.xyz(4.0, 5.0, 6.0),
            Vector3d.xyz(4.0, 5.0, 7.0), Vector3d.xyz(0.0, 1.0, 7.0),
            null
        )

        // parallel edges (result=null)
        createIntersectionTest(
            Vector3d.xyz(1.0, 1.0, -1.0), Vector3d.xyz(1.0, 1.0, 1.0),
            Vector3d.xyz(2.0, 2.0, -3.0), Vector3d.xyz(2.0, 2.0, 4.0),
            null
        )
        createIntersectionTest(
            Vector3d.xyz(1.0, 3.0, -1.0),
            Vector3d.xyz(1.0, 4.0, 2.0),
            Vector3d.xyz((1 + 10).toDouble(), 3.0, -1.0),
            Vector3d.xyz((1 + 10).toDouble(), 4.0, 2.0),
            null
        )
        createIntersectionTest(
            Vector3d.xyz(3.0, 6.0, -1.0),
            Vector3d.xyz(10.0, 7.0, 1.0),
            Vector3d.xyz(3.0, 6.0, (-1 + 3).toDouble()),
            Vector3d.xyz(10.0, 7.0, (1 + 3).toDouble()),
            null
        )

        // result is exactly in the middle of e1 and e2
        createIntersectionTest(
            Vector3d.xyz(5.0, 4.0, 2.0),  /*e1p2*/Vector3d.xyz(3.0, 2.0, 11.0),
            Vector3d.xyz(5.0, 2.0, 11.0),  /*e1p2*/Vector3d.xyz(3.0, 4.0, 2.0),
            Vector3d.xyz(4.0, 3.0, 6.5)
        )
    }

    companion object {
        private fun createIntersectionTest(
            e1p1: Vector3d, e1p2: Vector3d,
            e2p1: Vector3d, e2p2: Vector3d,
            expectedPoint: Vector3d?
        ) {
            val e1 = Edge(
                Vertex(
                    e1p1, Vector3d.Z_ONE
                ),
                Vertex(
                    e1p2, Vector3d.Z_ONE
                )
            )
            val e2 = Edge(
                Vertex(
                    e2p1, Vector3d.Z_ONE
                ),
                Vertex(
                    e2p2, Vector3d.Z_ONE
                )
            )
            val closestPointResult = e1.getIntersection(e2)
            if (expectedPoint != null) {
                Assert.assertTrue(
                    "Intersection point must exist",
                    closestPointResult.isPresent
                )
                val closestPoint = closestPointResult.get()
                Assert.assertTrue(
                    "Intersection point " + expectedPoint + ", got "
                        + closestPoint, expectedPoint == closestPoint
                )
            } else {
                Assert.assertFalse(
                    "Intersection point must not exist : "
                        + closestPointResult, closestPointResult.isPresent
                )
            }
        }

        private fun createClosestPointTest(
            e1p1: Vector3d, e1p2: Vector3d,
            e2p1: Vector3d, e2p2: Vector3d,
            expectedPoint: Vector3d?
        ) {
            val e1 = Edge(
                Vertex(
                    e1p1, Vector3d.Z_ONE
                ),
                Vertex(
                    e1p2, Vector3d.Z_ONE
                )
            )
            val e2 = Edge(
                Vertex(
                    e2p1, Vector3d.Z_ONE
                ),
                Vertex(
                    e2p2, Vector3d.Z_ONE
                )
            )
            val closestPointResult = e1.getClosestPoint(e2)
            if (expectedPoint != null) {
                Assert.assertTrue(
                    "Closest point must exist",
                    closestPointResult.isPresent
                )
                val closestPoint = closestPointResult.get()
                Assert.assertTrue(
                    "Expected point " + expectedPoint + ", got "
                        + closestPoint, expectedPoint == closestPoint
                )
            } else {
                Assert.assertFalse(
                    "Closest point must not exist : "
                        + closestPointResult, closestPointResult.isPresent
                )
            }
        }
    }
}
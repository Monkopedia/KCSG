/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cube
import eu.mihosoft.vvecmath.Transform
import kotlin.math.min

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class SquareMail {
    internal fun toCSG(numX: Int, numY: Int): CSG {

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        val w = 10.0
        val d = 2.2
        val jointRadius = 1.1
        val coneLength = 1.8
        val hingeHoleScale = 1.15
        val pinLength = 0.8
        val pinThickness = 1.2
        val mainCube = Cube(w, w, d).toCSG()
        val hingePrototype = Hinge().setJointRadius(jointRadius).setJointLength(pinThickness)
            .setConeLength(coneLength)
        hingePrototype.jointConnectionThickness = hingePrototype.jointRadius * 2
        var hinge1 = hingePrototype.toCSG()
        val hingeBounds = hinge1.bounds.bounds
        hinge1 = hinge1.intersect(
            Cube(
                hingeBounds.x,
                min(hingeBounds.y, d), hingeBounds.z
            ).toCSG()
        )
        hinge1 = hinge1.transformed(Transform.unity().rotX(90.0))
        val pin = Cube(
            pinLength + hingePrototype.jointRadius,
            pinThickness, d
        ).toCSG().transformed(Transform.unity().translateX(-(jointRadius + pinLength) * 0.5))
        hinge1 = hinge1.union(pin)
        hinge1 = hinge1.transformed(
            Transform.unity().translateX(
                w * 0.5 + hingePrototype.jointRadius
                    + pinLength
            )
        )
        val hinge2 = hinge1.transformed(Transform.unity().rotZ(90.0))
        var hingeHole1 = hinge1.transformed(
            Transform.unity().translateX(
                -w * 0.5 - hingePrototype.jointRadius
                    - pinLength
            )
        )
        hingeHole1 = hingeHole1.transformed(Transform.unity().scale(hingeHoleScale))
        hingeHole1 = hingeHole1.transformed(
            Transform.unity().translateX(
                -w * 0.5 + jointRadius * hingeHoleScale
            )
        )
        val hingeHole2 = hingeHole1.transformed(Transform.unity().rotZ(90.0))
        val part = mainCube.union(hinge1, hinge2).difference(hingeHole1, hingeHole2)
        val partBounds = part.bounds.bounds
        var result: CSG? = null
        for (y in 0 until numY) {
            for (x in 0 until numX) {
                val translateX =
                    (-partBounds.x + jointRadius + jointRadius * hingeHoleScale) * x
                val translateY =
                    (-partBounds.y + jointRadius + jointRadius * hingeHoleScale) * y
                val part2 =
                    part.transformed(Transform.unity().translate(translateX, translateY, 0.0))
                if (result == null) {
                    result = part2.clone()
                }
                result = result.dumbUnion(part2)
            }
        }
        return result!!
    }
}
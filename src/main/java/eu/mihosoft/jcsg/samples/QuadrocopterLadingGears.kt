/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.*
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class QuadrocopterLadingGears {
    private fun toCSG(): CSG {
        val armThickness = 18.0
        val armShrinkFactor = 0.640
        val gearHeadHeight = 18.0
        val gearWidth = 16.0
        val gearDepth = 20.0
        val armInset = 6.0
        var arm: CSG? = QuadrocopterArm.outerCyl(
            armThickness / 2.0,
            gearDepth,
            0.0,
            armShrinkFactor,
            0.0,
            true
        )
        arm = arm!!.transformed(Transform.unity().translateY(armInset))
        var landingGearHead = Cube(gearWidth, gearHeadHeight, gearDepth).toCSG()
        val lgOrigin = Transform.unity().translate(0.0, gearHeadHeight / 2.0, gearDepth / 2.0)
        landingGearHead = landingGearHead.transformed(lgOrigin)
        landingGearHead = landingGearHead.difference(arm)
        val gearLegHeight = 120.0
        val legResolution = 10
        val legPrototype =
            Cube(gearDepth, gearLegHeight / legResolution, gearWidth).noCenter().toCSG()
                .transformed(Transform.unity().translate(0.0, gearHeadHeight, -gearWidth / 2.0))
        var leg = legPrototype.clone()
        val dH = gearLegHeight / legResolution
        for (i in 1 until legResolution) {
            leg = leg.union(legPrototype.transformed(Transform.unity().translateY(i * dH)))
        }
        val translateWeight = label@ WeightFunction { v: Vector3d?, csg: CSG? ->
            if (v!!.y() < 2 * dH) {
                return@WeightFunction 0.0
            } else {

//                System.out.println("val: " + val + ", " + Math.min(1,val));
                return@WeightFunction 0.9 + v.y() * v.y() / (gearLegHeight * gearLegHeight + gearLegHeight * 10)
            }
        }
        leg = leg.weighted(translateWeight)!!
            .transformed(
                Transform.unity().scale(0.6, 1.0, 0.6)
            ).weighted(UnityModifier())!!.transformed(Transform.unity().rotY(90.0))
        //                weighted(translateWeight).transformed(unity().translateX(-50)).
        return leg.union(landingGearHead)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val moebiusStairs = QuadrocopterLadingGears()
            val csg = moebiusStairs.toCSG()
            FileUtil.write(Paths.get("quadcopter-landing-gear.stl"), csg.toStlString())
            csg.toObj().toFiles(Paths.get("quadcopter-landing-gear.obj"))
        }
    }
}
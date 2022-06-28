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
class WeightedTubeSample {
    fun toCSG(): CSG? {
        val weight = WeightFunction { v: Vector3d?, csg: CSG? ->
            val w = Math.max(1.0, (0.1 + Math.random()) / (v!!.z() * 0.1 + 0.1))
            w
        }

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        val protoOuter = Cylinder(1.0, 1.0, 16).toCSG()
        val protoInner = Cylinder(0.5, 1.0, 16).toCSG()
        var outer = protoOuter
        var inner = protoInner
        for (i in 0..49) {
            outer = outer!!.union(protoOuter!!.transformed(Transform.unity().translateZ(i / 5.0)))
            inner = inner!!.union(protoInner!!.transformed(Transform.unity().translateZ(i / 5.0)))
        }
        val scale = Transform.unity().scale(2.0, 2.0, 1.0)
        val scaleInner = Transform.unity().scale(1.5, 1.5, 1.0)
        inner = inner!!.weighted(weight)!!.transformed(scaleInner).weighted(UnityModifier())!!
        return outer!!.weighted(weight)!!.transformed(scale).difference(inner)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.Companion.write(
                Paths.get("weighted-tube.stl"),
                WeightedTubeSample().toCSG()!!.toStlString()
            )

//        new WeightedTubeSample().toCSG().toObj().toFiles(Paths.get("weighted-tube.obj"));
        }
    }
}
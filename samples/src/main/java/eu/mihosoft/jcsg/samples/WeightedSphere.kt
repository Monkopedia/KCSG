/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.CSG.OptType
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.jcsg.Sphere
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class WeightedSphere {
    fun toCSG(): CSG {
        val prototype = Sphere(3.0, 128, 64).toCSG().optimization(OptType.POLYGON_BOUND)

//        CSG result = new Sphere(3, 128, 64).toCSG();
//        return result.
//                
//                weighted(new ZModifier(true)).transformed(Transform.unity().scale(0.1)).
//                weighted(new YModifier(true)).transformed(Transform.unity().scale(0.1)).
//                weighted(new XModifier(true)).transformed(Transform.unity().scale(0.1)).
//                transformed(Transform.unity().translateX(1)).weighted(new UnityModifier());
        var result = prototype


//        double dt = 0.1;
//        for (int i = 0; i < 10; i++) {
//            final int index = i+1;
//            System.out.println("index: " + index + ", dt: " + (dt*index));
//            CSG morphed = prototype.weighted((v, csg) -> {
//                double w = (1 + Math.sin(v.z * 2)*Math.cos(v.z * 2)) / 2.0;
//
//                w = w * dt* index;
//
//                return w;
//
//            }).transformed(Transform.unity().scale(0.1)).
//                    weighted(new XModifier(true)).
////                    weighted(new UnityModifier()).
//                    transformed(Transform.unity().translateX(8+(i+1) * (8)));
//
//            result = result.union(morphed);
//        }
//        return result;
        val morphed = prototype.weighted { v: Vector3d?, csg: CSG? ->
            val w = (1 + sin(
                v!!.z() * 2
            ) * cos(v.z() * 2)) / 2.0
            w
        }.transformed(
            Transform.unity().scale(0.1)
        ).transformed(Transform.unity().translateX(1.0).rotZ(90.0))
        //                    weighted(new XModifier(true)).
        //                    weighted(new UnityModifier()).
        result = morphed
        return result
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Paths.get("rounded-cube-mod.stl"),
                WeightedSphere().toCSG().toStlString()
            )
            WeightedSphere().toCSG().toObj().toFiles(Paths.get("rounded-cube-mod.obj"))
        }
    }
}
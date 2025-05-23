/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.CSG.OptType
import com.monkopedia.kcsg.Sphere
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import kotlin.math.cos
import kotlin.math.sin

/**
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
                v!!.z * 2
            ) * cos(v.z * 2)) / 2.0
            w
        }.transformed(
            Transform.unity().scale(0.1)
        ).transformed(Transform.unity().translateX(1.0).rotZ(90.0))
        //                    weighted(new XModifier(true)).
        //                    weighted(new UnityModifier()).
        result = morphed
        return result
    }
}

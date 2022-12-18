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

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Spheres {
    fun toCSG(): CSG {
        val maxR = 10.0
        val w = 30.0
        val h = 30.0
        val d = 30.0

        // optimization reduces runtime dramatically
        CSG.setDefaultOptType(OptType.POLYGON_BOUND)
        var spheres: CSG? = null
        val random = java.util.Random(5)
        for (i in 0..69) {
            val s = Sphere(random.nextDouble() * maxR).toCSG().transformed(
                Transform.unity().translate(
                    random.nextDouble() * w,
                    random.nextDouble() * h,
                    random.nextDouble() * d
                )
            )
            spheres = spheres?.union(s) ?: s
        }
        return spheres!!
    }
}
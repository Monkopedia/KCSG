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
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Spheres {
    fun toCSG(): CSG? {
        val maxR = 10.0
        val w = 30.0
        val h = 30.0
        val d = 30.0

        // optimization reduces runtime dramatically
        CSG.setDefaultOptType(OptType.POLYGON_BOUND)
        var spheres: CSG? = null
        for (i in 0..69) {
            val s = Sphere(Math.random() * maxR).toCSG().transformed(
                Transform.unity().translate(
                    Math.random() * w,
                    Math.random() * h,
                    Math.random() * d
                )
            )
            spheres = spheres?.union(s) ?: s
        }
        return spheres
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(Paths.get("spheres.stl"), Spheres().toCSG()!!.toStlString())
        }
    }
}
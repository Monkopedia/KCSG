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
object BraceletGenerator {
    fun toCSG(): CSG {
        val sphereRadius = 10.0
        var sphere = Sphere(Vector3d.ZERO, sphereRadius, 64, 64).toCSG()
        val spaceRadius = 1.0
        val spaceProt = Cylinder(spaceRadius * 1, sphereRadius * 2, 16).toCSG()
            .transformed(Transform.unity().translate(0.0, 0.0, -sphereRadius).rotZ(45.0))
        var spaces: CSG? = null
        val step = 360.0 / 20
        var i = 0.0
        while (i < 360) {
            val sp = spaceProt.transformed(
                Transform.unity().translate(sphereRadius - spaceRadius * 0.98, 0.0, 0.0)
            ).transformed(
                Transform.unity().rotZ(i)
            )
            spaces = spaces?.union(sp) ?: sp
            i += step
        }
        val braceletHeight = 2.0
        val top = Cube(sphereRadius * 2).toCSG()
            .transformed(Transform.unity().translateZ(sphereRadius + braceletHeight / 2.0))
        val bottom = Cube(sphereRadius * 2).toCSG()
            .transformed(Transform.unity().translateZ(-sphereRadius - braceletHeight / 2.0))
        sphere = sphere.transformed(Transform.unity().scaleZ(0.5))
        return sphere.difference(top).difference(bottom).difference(spaces!!)
            .transformed(Transform.unity().scale(3.5))
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        FileUtil.write(Paths.get("sample.stl"), toCSG().toStlString())
    }
}
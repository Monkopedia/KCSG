/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.*
import eu.mihosoft.jcsg.ext.vvecmath.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 * Average Chicken Egg.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Sabine {
    fun toCSG(): CSG {
        val w = 16.0
        val h = 9.0
        val offset = 1.5
        val fractW = 2.0
        val fractL = 10.0
        val fractD = 0.25
        val cube = Cube(w, h, 1.0).toCSG()
        val beam = Cylinder(1.0, 10.0, 32).toCSG()
        val beam1 = beam.weighted(ZModifier())
            .transformed(Transform.unity().scale(0.5, 0.5, 1.0))
            .weighted(UnityModifier())
            .transformed(Transform.unity().translate(w / 2.0 - offset, h / 2.0 - offset, 0.0))
        val beam2 = beam.transformed(
            Transform.unity().translate(-(w / 2.0 - offset), h / 2.0 - offset, 0.0)
        )
        val beam3 = beam.transformed(
            Transform.unity().translate(-(w / 2.0 - offset), -(h / 2.0 - offset), 0.0)
        )
        val beam4 = beam.transformed(
            Transform.unity().translate(w / 2.0 - offset, -(h / 2.0 - offset), 0.0)
        )
        var fractures: CSG? = null
        for (i in 0..49) {
            val angleX = 45 + Math.random() * 90
            val angleZ = 45 + Math.random() * 90
            var fracture1 = Cube(
                0.1 + fractW * Math.random(),
                fractL * Math.random(),
                fractD
            ).noCenter().toCSG().transformed(Transform.unity().rotZ(-angleZ).rotX(-angleX))
            val x = -w / 2.0 + Math.random() * (w - 2)
            val y = -h / 2.0 + Math.random() * (h - 2)
            fracture1 = fracture1.transformed(Transform.unity().translate(x, y, 0.0))
            fractures = fractures?.union(fracture1) ?: fracture1
        }
        val diffCube = Cube(w, h, 11.0).noCenter().toCSG()
            .transformed(Transform.unity().translate(-w / 2.0, -h / 2.0, 0.0))
        fractures = fractures!!.intersect(diffCube)
        return cube.union(beam1, beam2, beam3, beam4, fractures)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(Paths.get("sabine.stl"), Sabine().toCSG().toStlString())
        }
    }
}
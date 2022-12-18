/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cube
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.jcsg.ext.vvecmath.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class MoebiusStairs {
    private var n = 45.0
    private var twists = 1.0
    private val tilt = 0.0
    fun resolution(n: Double): MoebiusStairs {
        this.n = n
        return this
    }

    fun twists(twists: Double): MoebiusStairs {
        this.twists = twists
        return this
    }

    fun toCSG(): CSG {

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        var result: CSG? = null
        var firstCube: CSG? = null
        var prevCube: CSG? = null
        var i = 1
        while (i <= n * 2) {
            val deg = i * 360.0 / n
            val rot1 = Transform.unity().rotZ(deg)
            val translate1 = Transform.unity().translate(
                -20 + 5 * sin(i * 360.0 * (twists + .5) / n), 0.0,
                8 * cos(i * 360 * (twists + .5) / n)
            )
            val rot2 = Transform.unity().rotX(90 - tilt)
            val finalTransform = rot1.apply(translate1).apply(rot2)
            var cube = Cube(
                3 + abs(8.0 * cos(30 + (twists + 0.5) * deg)),
                4.0,
                4.0
            ).toCSG()

//            CSG cube = new Cylinder(
//                    4,
//                    3 + abs(8.0 * cos(30 + i * 360 * (twists + 0.5) / n)),
//                    3).toCSG().transformed(unity().rotY(90).rotZ(60+i*60.0/n));
            cube = cube.transformed(finalTransform)
            if (i == 1) {
                firstCube = cube
            }
            if (result == null) {
                result = cube
            }
            //
            if (prevCube != null) {
                val union = cube.hull(prevCube)
                result = result.union(union)
            }
            if (i.toDouble() == n * 2) {
                val union = firstCube!!.hull(prevCube!!)
                result = result.union(union)
            }
            prevCube = cube
            i++
        }
        return result!!.transformed(Transform.unity().translateZ((8 + 4 / 2).toDouble()))
    }

    private fun abs(value: Double): Double {
        return kotlin.math.abs(value)
    }

    companion object {
        private fun sin(deg: Double): Double {
            return kotlin.math.sin(Math.toRadians(deg))
        }

        private fun cos(deg: Double): Double {
            return kotlin.math.cos(Math.toRadians(deg))
        }

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val moebiusStairs = MoebiusStairs()
            val csg = moebiusStairs.toCSG()
            FileUtil.write(Paths.get("moebius-stairs.stl"), csg.toStlString())
            csg.toObj().toFiles(Paths.get("moebius-stairs.obj"))
        }
    }
}
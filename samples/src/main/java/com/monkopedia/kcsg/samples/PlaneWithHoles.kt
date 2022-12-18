/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.CSG.OptType
import com.monkopedia.kcsg.Cube
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.Sphere
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import java.io.IOException
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class PlaneWithHoles {
    fun toCSG(): CSG {
        var result = Cube(Vector3d.ZERO, Vector3d.xyz(30.0, 30.0, 1.0)).toCSG()

//        CSG result = null;
//        try {
//            result = STL.file(Paths.get("box_refined-01.stl")).transformed(Transform.unity().scale(30, 30, 0.5)).optimization(CSG.OptType.POLYGON_BOUND);
//        } catch (IOException ex) {
//            Logger.getLogger(PlaneWithHoles.class.getName()).log(Level.SEVERE, null, ex);
//        }
        var spheres: CSG? = null
        for (y in 0..10) {
            println("line: $y")
            for (x in 0..10) {
                val radius = 1.2
                val spacing = 0.25

//                CSG sphere = new Cylinder(radius, 1, 24).toCSG().transformed(
//                        Transform.unity().translate((x - 5) * (radius * 1.7 + spacing), (y - 5) * (radius * 1.7 + spacing), -0.5)).optimization(CSG.OptType.CSG_BOUND);
                val sphere = Sphere(radius).toCSG().transformed(
                    Transform.unity().translate(
                        (x - 5) * (radius * 2 + spacing),
                        (y - 5) * (radius * 2 + spacing),
                        -0.0
                    )
                ).optimization(OptType.POLYGON_BOUND)


//                result = result.difference(sphere);
                spheres = spheres?.union(sphere) ?: sphere
            }
        }
        try {
            FileUtil.write(Paths.get("cyl.stl"), spheres!!.toStlString())
        } catch (ex: IOException) {
            Logger.getLogger(PlaneWithHoles::class.java.name).log(Level.SEVERE, null, ex)
        }
        println(">> final diff")
        result = result.difference(spheres!!)
        return result
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            CSG.setDefaultOptType(OptType.CSG_BOUND)
            val planeWithHoles = PlaneWithHoles()

            // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
            FileUtil.write(
                Paths.get("sample.stl"),
                planeWithHoles.toCSG().toStlString()
            )
        }
    }
}
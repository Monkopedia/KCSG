/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.jcsg.RoundedCube
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class RoundedCubeSample {
    fun toCSG(): CSG {
        return RoundedCube(3.0).resolution(8).cornerRadius(0.2).toCSG()
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Paths.get("rounded-cube.stl"),
                RoundedCubeSample().toCSG()!!.toStlString()
            )
            RoundedCubeSample().toCSG()!!.toObj().toFiles(Paths.get("rounded-cube.obj"))
        }
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author miho
 */
class MicroSDCard {
    fun toCSG(): CSG {

        // data taken from
        // https://www.sparkfun.com/datasheets/Prototyping/microSD_Spec.pdf
        // total card width
        val A = 10.9
        // front width
        val A1 = 9.6
        val A8 = 0.6

        // total card length
        val B = 14.9
        val B1 = 6.3

        // slit pos relative to front
        val B10 = 7.8

        // slit thickness
        val B11 = 1.1

        // total card thickness 
        val C1 = 0.6
        val A_ = A - A1
        val B_ = B - B1 + A_
        return Extrude.points(
            Vector3d.xyz(0.0, 0.0, C1),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(A, 0.0),
            Vector3d.xy(A, B),
            Vector3d.xy(A_, B),
            Vector3d.xy(A_, B_),
            Vector3d.xy(0.0, B - B1),
            Vector3d.xy(0.0, B - B10),
            Vector3d.xy(A8, B - B10),
            Vector3d.xy(A8, B - B10 - B11),
            Vector3d.xy(0.0, B - B10 - B11 - A8)
        )
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(Paths.get("mircosd.stl"), MicroSDCard().toCSG().toStlString())
        }
    }
}
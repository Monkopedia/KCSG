/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 */
class ServoMount {
    // mini servo
    //    private double servoWidth = 22.9;
    //    private double servoThickness = 12.0;
    //    
    //standard servo
    private val servoWidth = 40.0
    private val servoThickness = 19.0
    private val borderThickness = 2.0
    private val overlap = 3.0
    private val servoMountHeight = 20.0
    private val boardMountingThickness = 2.0
    private val boardHolderLength = 12.0
    private val boardMountingWidth = 8.1
    private val pegHeight = 1.0
    private val pegToothHeight = 0.6
    private val pegOverlap = 0.5
    private fun toCSGSimple(): CSG {
        return Extrude.points(
            Vector3d.xyz(0.0, 0.0, servoMountHeight),
            Vector3d.xy(0.0, servoThickness),
            Vector3d.xy(overlap, servoThickness),
            Vector3d.xy(-borderThickness, servoThickness + borderThickness),
            Vector3d.xy(-borderThickness, -borderThickness),
            Vector3d.xy(servoWidth + borderThickness, -borderThickness),
            Vector3d.xy(servoWidth + borderThickness, servoThickness + borderThickness),
            Vector3d.xy(servoWidth - overlap, servoThickness),
            Vector3d.xy(servoWidth, servoThickness),
            Vector3d.xy(servoWidth, 0.0),
            Vector3d.xy(0.0, 0.0)
        )
    }

    fun toCSG(): CSG {
        val bm1 = boardMount().transformed(
            Transform.unity().rotY(90.0).rotZ(90.0)
                .translate(borderThickness, borderThickness, -boardHolderLength + borderThickness)
        )
        val bm2 = bm1.transformed(
            Transform.unity().translateX(servoWidth - boardHolderLength + borderThickness * 2)
        )
        val sm = toCSGSimple()
        return sm.union(bm1).union(bm2) //.transformed(Transform.unity().scale(0.08));
    }

    private fun boardMount(): CSG {
        val h = boardMountingWidth
        val points = mutableListOf(
            Vector3d.ZERO,
            Vector3d.xy(0.0, -borderThickness),
            Vector3d.xy(boardMountingThickness + borderThickness, -borderThickness),
            Vector3d.xy(boardMountingThickness + borderThickness, h + pegToothHeight + pegHeight),
            Vector3d.xy(boardMountingThickness - pegOverlap, h + pegToothHeight),
            Vector3d.xy(boardMountingThickness, h),
            Vector3d.xy(boardMountingThickness, 0.0)
        )
        points.reverse()
        return Extrude.points(
            Vector3d.xyz(0.0, 0.0, boardHolderLength),
            points
        )
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val sMount = ServoMount()

            // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
            FileUtil.write(Paths.get("servo-mount.stl"), sMount.toCSG().toStlString())
        }
    }
}

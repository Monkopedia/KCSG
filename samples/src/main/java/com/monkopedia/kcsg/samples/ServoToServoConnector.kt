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
class ServoToServoConnector {
    //standard servo
    private val servoWidth = 40.0
    private val servoThickness = 19.0
    private val borderThickness = 2.0
    private val connectorThickness = 4.0
    private val servoMountHeight = 10.0
    private val servoDistance = 17.0
    private val height = 12.0
    fun toCSG(): CSG {
        val sth = servoThickness
        val sd = servoDistance
        val th = borderThickness
        val th2 = connectorThickness
        val h = height
        val fork: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, servoMountHeight),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(sth, 0.0),
            Vector3d.xy(sth, h),
            Vector3d.xy(sth + th, h),
            Vector3d.xy(sth + th, -th),
            Vector3d.xy(sth / 2 + th2 / 2, -th),
            Vector3d.xy(sth / 2 + th2 / 4, -th - sd / 2),
            Vector3d.xy(sth / 2 - th2 / 4, -th - sd / 2),
            Vector3d.xy(sth / 2 - th2 / 2, -th),
            Vector3d.xy(-th, -th),
            Vector3d.xy(-th, h),
            Vector3d.xy(0.0, h)
        )
        val fork2 =
            fork.transformed(Transform.unity().rotZ(180.0).translateX(-sth).translateY(sd + th * 2))
        return fork.union(fork2)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val servo2ServoConnector = ServoToServoConnector()

            // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
            FileUtil.write(
                Paths.get("sample.stl"),
                servo2ServoConnector.toCSG().toStlString()
            )
        }
    }
}

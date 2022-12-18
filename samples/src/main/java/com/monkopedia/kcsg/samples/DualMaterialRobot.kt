/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.STL
import com.monkopedia.kcsg.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 */
object DualMaterialRobot {
    private var robot: CSG? = null
    @Throws(IOException::class)
    fun middleToCSG(): CSG {
        if (robot == null) {
            robot = STL.file(Paths.get("/home/miho/CuraExamples/UltimakerRobot_support.stl"))
        }

//        return robot.getBounds().toCSG().transformed(Transform.unity().scale(1.1,0.5,1.1).translateY(10).translateZ(-1.5)).difference(robot);
        val robotBounds = robot!!.bounds
        val middle = robotBounds.toCSG()
            .transformed(
                Transform.unity().scaleZ(1 / 2.0).translateZ(
                    robotBounds.bounds.z() / 3.0
                )
            )
        return robot!!.intersect(middle)
    }

    @Throws(IOException::class)
    fun topBottomCSG(): CSG {
        if (robot == null) {
            robot = STL.file(Paths.get("/home/miho/CuraExamples/UltimakerRobot_support.stl"))
        }
        val robotBounds = robot!!.bounds
        val middle = robotBounds.toCSG()
            .transformed(
                Transform.unity().scaleZ(1 / 2.0).translateZ(
                    robotBounds.bounds.z() / 3.0
                )
            )
        return robot!!.difference(middle)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {

//        FileUtil.write(Paths.get("robot-color-1.stl"), DualMaterialRobot.topBottomCSG().toStlString());
//        FileUtil.write(Paths.get("robot-color-2.stl"), DualMaterialRobot.middleToCSG().toStlString());
        robot = STL.file(Paths.get("/home/miho/CuraExamples/UltimakerRobot_support.stl"))
        FileUtil.write(Paths.get("robot-ascii.stl"), robot!!.toStlString())
    }
}

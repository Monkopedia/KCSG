/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class ServoHead {
    private var toothLength = 0.7
    private var toothWidth = 0.1
    private var toothHeight = 0.3
    private var toothCount = 25
    private var headHeight = 4.0
    private var headDiameter = 5.92
    private var headScrewDiameter = 2.5
    private var headThickness = 1.1

    constructor(
        toothLength: Double,
        toothWidth: Double,
        toothHeight: Double,
        toothCount: Int,
        headHeight: Double,
        headDiameter: Double,
        headScrewDiameter: Double,
        headThickness: Double
    ) {
        this.toothLength = toothLength
        this.toothWidth = toothWidth
        this.toothHeight = toothHeight
        this.toothCount = toothCount
        this.headHeight = headHeight
        this.headDiameter = headDiameter
        this.headScrewDiameter = headScrewDiameter
        this.headThickness = headThickness
    }

    constructor()

    private fun servoTooth(): CSG {

        //
        //       |  tw  |
        //       --------    --
        //      /        \   th
        //     /          \  --
        //     
        //     |    tl    |
        //
        return Extrude.points(
            Vector3d.xyz(0.0, 0.0, headHeight),
            Vector3d.xy(-toothLength / 2, 0.0),
            Vector3d.xy(-toothWidth / 2, toothHeight),
            Vector3d.xy(toothWidth / 2, toothHeight),
            Vector3d.xy(toothLength / 2, 0.0)
        )
    }

    fun servoHeadMale(): CSG? {
        val clear = 0.3
        val cylinder = Cylinder(
            Vector3d.xyz(0.0, 0.0, 0.0), Vector3d.xyz(0.0, 0.0, headHeight),
            headDiameter / 2 - toothHeight + clear + 0.03, toothCount * 2
        ).toCSG()
        var result: CSG? = null
        for (i in 0 until toothCount) {
            var tooth: CSG? = servoTooth()
            val translate = Transform.unity().translateY(headDiameter / 2 - toothHeight + clear)
            val rot = Transform.unity().rotZ(i * (360.0 / toothCount))
            tooth = tooth!!.transformed(rot.apply(translate))
            result = result?.union(tooth) ?: tooth
        }
        if (result != null) {
            result = result.union(cylinder)
        }
        return result
    }

    fun servoHeadFemale(): CSG {
        val cyl1 = Cylinder(headDiameter / 2 + headThickness, headHeight + 1, 16).toCSG()
        //        cyl1 = cyl1.transformed(Transform.unity().translateZ(0.1));
        val cyl2 = Cylinder(headScrewDiameter / 2, 10.0, 16).toCSG()
        val head = servoHeadMale()
        val headFinal = cyl1.difference(cyl2).difference(head!!)
        return headFinal.transformed(
            Transform.unity().rotX(180.0).translateZ(-headHeight - headThickness)
        )
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            println("RUNNING")
            FileUtil.write(
                Paths.get("servo-head-female.stl"), ServoHead().servoHeadFemale()
                    .toStlString()
            )
            FileUtil.write(
                Paths.get("servo-head-male.stl"), ServoHead().servoHeadMale()!!
                    .toStlString()
            )
        }
    }
}
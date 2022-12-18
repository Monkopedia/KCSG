/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class ServoWheel {
    private val toothLength = 0.7
    private val toothWidth = 0.1
    private val toothHeight = 0.3
    private val toothCount = 25
    private val headHeight = 4.0
    private val headDiameter = 5.92
    private val headScrewDiameter = 2.5
    private val headThickness = 1.1
    private val servoHead = ServoHead(
        toothLength,
        toothWidth,
        toothHeight,
        toothCount,
        headHeight,
        headDiameter,
        headScrewDiameter,
        headThickness
    )
    private val numberOfArms = 3
    private var innerWidth = 7.0
    private var outerWidth = 3.5
    var thickness = 2.0
    var radius = 40.0
    private var ringThickness = 3.0
    private var wheelThickness = 5.0
    private var minorArmLength = radius * 0.75
    private var minorArmHeight = headHeight
    private var minorArmThickness = 2.5
    private var outerRingThickness = wheelThickness / 3.0 * 2
    private var outerRingDepth = 0.5
    fun toCSG(): CSG {
        val dt = 360.0 / numberOfArms
        var arms: CSG? = null
        for (i in 0 until numberOfArms) {
            val arm = servoArm(
                innerWidth,
                outerWidth,
                thickness,
                radius,
                ringThickness,
                minorArmThickness,
                minorArmLength,
                minorArmHeight
            ).transformed(Transform.unity().rotZ(dt * i))
            arms = arms?.union(arm) ?: arm
        }
        var sHead = servoHead.servoHeadFemale()
        val screwHole = Cylinder(headScrewDiameter / 2.0, ringThickness * 2, 16).toCSG()
        if (arms != null) {
            sHead = sHead.union(arms)
        }
        sHead = sHead.difference(screwHole)
        val outerWheelCylinder = Cylinder(radius, wheelThickness, 64).toCSG()
        val innerWheelCylinder = Cylinder(radius - ringThickness, wheelThickness, 64).toCSG()
        val ring = outerWheelCylinder.difference(innerWheelCylinder)
        var wheel = ring.union(sHead)
        val outerRingOutCylinder = Cylinder(radius, outerRingThickness, 64).toCSG()
        val outerRingInnerCylinder =
            Cylinder(radius - outerRingDepth, outerRingThickness, 64).toCSG()
        val outerRing = outerRingOutCylinder.difference(outerRingInnerCylinder).transformed(
            Transform.unity().translateZ(wheelThickness * 0.5 - outerRingThickness * 0.5)
        )
        wheel = wheel.difference(outerRing)
        return wheel
    }

    private fun servoArm(
        innerWidth: Double,
        outerWidth: Double,
        thickness: Double,
        radius: Double,
        wheelThickness: Double,
        minorArmThickness: Double,
        minorArmLegth: Double,
        minorArmHeight: Double
    ): CSG {
        val mainArm: CSG = Extrude.points(
            Vector3d.z(thickness),
            Vector3d.xy(-innerWidth * 0.5, 0.0),
            Vector3d.xy(innerWidth * 0.5, 0.0),
            Vector3d.xy(outerWidth * 0.5, radius - wheelThickness),
            Vector3d.xy(-outerWidth * 0.5, radius - wheelThickness)
        )
        var minorArm: CSG? = Extrude.points(
            Vector3d.z(minorArmThickness),
            Vector3d.xy(headDiameter * 0.5 + headThickness * 0.5, thickness),
            Vector3d.xy(minorArmLegth - headDiameter * 0.5 - headThickness * 0.5, thickness),
            Vector3d.xy(headDiameter * 0.5 + headThickness * 0.5, minorArmHeight + thickness * 0.5)
        ).transformed(
            Transform.unity().rot(-90.0, 0.0, 0.0).translateZ(-minorArmThickness * 0.5)
        )
        minorArm = minorArm!!.transformed(Transform.unity().rotZ(-90.0))
        return mainArm.union(minorArm)
    }
}

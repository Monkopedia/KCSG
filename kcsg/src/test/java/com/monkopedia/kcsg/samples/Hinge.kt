/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cube
import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.Transform

/**
 */
class Hinge {
    private var jointLength = 10.0
    var jointRadius = 5.0
    private var coneLength = 5.0
    private var jointHoleLength = 5.0
    var jointConnectionThickness = jointRadius * 0.5
    var resolution = 16

    fun toCSG(): CSG {
        val sideConeR: CSG =
            Cylinder(getJointRadius(), 0.0, getConeLength(), getResolution())
                .toCSG().transformed(
                    Transform.unity().translateZ(getJointLength() * 0.5)
                )
        val sideConeL: CSG =
            Cylinder(getJointRadius(), 0.0, getConeLength(), getResolution())
                .toCSG().transformed(
                    Transform.unity().translateZ(-getJointLength() * 0.5)
                        .rotX(180.0)
                )
        val sideCones = sideConeL.union(sideConeR)
        val conesAndCyl = sideCones.hull()
        val cylinderHole =
            Cube(getJointRadius() * 2, getJointHoleLength() * 2, getJointHoleLength()).toCSG()
                .transformed(
                    Transform.unity()
                        .translate(getJointConnectionThickness(), 0.0, -getJointHoleLength() * 0.0)
                )
        return conesAndCyl.difference(cylinderHole)
    }

    fun setJointLength(jointLength: Double): Hinge {
        this.jointLength = jointLength
        return this
    }

    fun setJointRadius(jointRadius: Double): Hinge {
        this.jointRadius = jointRadius
        return this
    }

    fun setConeLength(coneLength: Double): Hinge {
        this.coneLength = coneLength
        return this
    }

    fun setJointHoleLength(jointHoleLength: Double): Hinge {
        this.jointHoleLength = jointHoleLength
        return this
    }

    fun setJointConnectionThickness(jointConnectionThickness: Double): Hinge {
        this.jointConnectionThickness = jointConnectionThickness
        return this
    }

    fun setResolution(resolution: Int): Hinge {
        this.resolution = resolution
        return this
    }

    /**
     * @return the jointLength
     */
    @JvmName("getJointLength1")
    fun getJointLength(): Double {
        return jointLength
    }

    /**
     * @return the jointRadius
     */
    @JvmName("getJointRadius1")
    fun getJointRadius(): Double {
        return jointRadius
    }

    /**
     * @return the coneLength
     */
    @JvmName("getConeLength1")
    fun getConeLength(): Double {
        return coneLength
    }

    /**
     * @return the jointHoleLength
     */
    @JvmName("getJointHoleLength1")
    fun getJointHoleLength(): Double {
        return jointHoleLength
    }

    /**
     * @return the jointConnectionThickness
     */
    @JvmName("getJointConnectionThickness1")
    fun getJointConnectionThickness(): Double {
        return jointConnectionThickness
    }

    /**
     * @return the resolution
     */
    @JvmName("getResolution1")
    fun getResolution(): Int {
        return resolution
    }
}

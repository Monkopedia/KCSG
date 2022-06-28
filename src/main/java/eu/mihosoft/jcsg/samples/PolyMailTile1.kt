/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cube
import eu.mihosoft.jcsg.Cylinder
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Transform
import java.io.IOException
import java.nio.file.Paths
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal enum class TileType {
    MALE, FEMALE, COMBINED
}

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class PolyMailTile {
    private var tileType = TileType.MALE
    var radius = 10.0
        private set
    var thickness = 2.2
        private set
    var jointRadius = 1.1
        private set
    private var coneLength = 1.8
        private set
    var hingeHoleScale = 1.16
        private set
    var pinLength = 1.0
        private set
    private var pinThickness = 2.0
        private set
    private var numEdges = 3
        private set
    fun toCSG(): CSG {

//        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND);
        val step = 360.0 / numEdges
        val initialRot = step * 0.5
        val mainPrism = Cylinder(radius, thickness, numEdges).toCSG()
            .transformed(Transform.unity().translateZ(-thickness * 0.5).rotZ(initialRot))
        val hingePrototype =
            Hinge().setJointRadius(jointRadius).setJointLength(pinThickness)
                .setConeLength(coneLength)
        hingePrototype.jointConnectionThickness = hingePrototype.jointRadius * 2
        var hinge1 = hingePrototype.toCSG()
        val hingeBounds = hinge1!!.bounds.bounds
        hinge1 = hinge1.intersect(
            Cube(
                hingeBounds.x(),
                min(hingeBounds.y(), thickness), hingeBounds.z()
            ).toCSG()
        )
        hinge1 = hinge1.transformed(Transform.unity().rotX(90.0))
        val pin = Cube(
            pinLength + hingePrototype.jointRadius,
            pinThickness,
            thickness
        ).toCSG().transformed(Transform.unity().translateX(-(jointRadius + pinLength) * 0.5))
        hinge1 = hinge1.union(pin)
        val apothem = apothem
        hinge1 = hinge1.transformed(
            Transform.unity().translateX(
                apothem + hingePrototype.jointRadius +
                    pinLength
            )
        )
        val hinges: MutableList<CSG?> = ArrayList()
        hinges.add(hinge1)
        for (i in 1 until numEdges) {
            val hinge = hinge1.transformed(Transform.unity().rotZ(i * step))
            hinges.add(hinge)
        }
        var hingeHole1 = hinge1.transformed(
            Transform.unity().translateX(
                -apothem - hingePrototype.jointRadius -
                    pinLength
            )
        )
        hingeHole1 = hingeHole1.transformed(Transform.unity().scale(hingeHoleScale))
        hingeHole1 = hingeHole1.transformed(
            Transform.unity().translateX(
                -apothem + jointRadius * hingeHoleScale
            )
        )

        // TODO get rid of this
        if (numEdges % 2 != 0) {
            hingeHole1 = hingeHole1.transformed(Transform.unity().rotZ(initialRot))
        }
        val hingeHoles: MutableList<CSG?> = ArrayList()
        hingeHoles.add(hingeHole1)
        for (i in 1 until numEdges) {
            val hole = hingeHole1.transformed(Transform.unity().rotZ(i * step))
            hingeHoles.add(hole)
        }
        val malePart = mainPrism.union(hinges)
        val femalePart = mainPrism.difference(hingeHoles)
        var combinedPart = mainPrism.clone()
        for (i in 0 until numEdges) {
            combinedPart = if (i % 2 == 0) {
                combinedPart.union(hinges[i])
            } else {
                combinedPart.difference(
                    hingeHoles[i]!!.transformed(Transform.unity().rotZ(step))
                )
            }
        }
        require(!(numEdges % 2 != 0 && isCombined())) { "Combined type can only be used for even edge numbers." }
        if (isMale()) {
            return malePart
        } else if (isFemale()) {
            return femalePart
        } else if (isCombined()) {
            return combinedPart
        }
        return mainPrism
    }

    /**
     * @return the male
     */
    private fun isMale(): Boolean {
        return tileType == TileType.MALE
    }

    /**
     * @return the male
     */
    private fun isFemale(): Boolean {
        return tileType == TileType.FEMALE
    }

    private fun isCombined(): Boolean {
        return tileType == TileType.COMBINED
    }

    fun setMale(): PolyMailTile {
        tileType = TileType.MALE
        return this
    }

    fun setFemale(): PolyMailTile {
        tileType = TileType.FEMALE
        return this
    }

    fun setCombined(): PolyMailTile {
        tileType = TileType.COMBINED
        return this
    }

    /**
     * @param radius the radius to set
     */
    fun setRadius(radius: Double): PolyMailTile {
        this.radius = radius
        return this
    }

    /**
     * @param thickness the thickness to set
     */
    fun setThickness(thickness: Double): PolyMailTile {
        this.thickness = thickness
        return this
    }

    /**
     * @param jointRadius the jointRadius to set
     */
    fun setJointRadius(jointRadius: Double): PolyMailTile {
        this.jointRadius = jointRadius
        return this
    }

    /**
     * @param coneLength the coneLength to set
     */
    fun setConeLength(coneLength: Double): PolyMailTile {
        this.coneLength = coneLength
        return this
    }

    /**
     * @param hingeHoleScale the hingeHoleScale to set
     */
    fun setHingeHoleScale(hingeHoleScale: Double): PolyMailTile {
        this.hingeHoleScale = hingeHoleScale
        return this
    }

    /**
     * @param pinLength the pinLength to set
     */
    fun setPinLength(pinLength: Double): PolyMailTile {
        this.pinLength = pinLength
        return this
    }

    /**
     * @param pinThickness the pinThickness to set
     */
    fun setPinThickness(pinThickness: Double): PolyMailTile {
        this.pinThickness = pinThickness
        return this
    }

    fun setNumEdges(numEdges: Int): PolyMailTile {
        this.numEdges = numEdges
        return this
    }

    val sideLength: Double
        get() {
            return 2 * radius * sin(Math.toRadians(180.0 / numEdges))
        }

    val apothem: Double
        get() {
            return radius * cos(Math.toRadians(180.0 / numEdges))
        }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Paths.get("triangularmail.stl"),
                PolyMailTile().setNumEdges(6).setCombined().toCSG()!!
                    .toStlString()
            )
        }
    }
}

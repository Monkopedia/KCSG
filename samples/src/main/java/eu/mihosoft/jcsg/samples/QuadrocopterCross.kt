/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.*
import eu.mihosoft.jcsg.CSG.OptType
import eu.mihosoft.jcsg.ext.vvecmath.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class QuadrocopterCross {
    fun print3d(csg: CSG, n: Int) {
        try {
            FileUtil.write(Paths.get("quadrocopter-cross-$n.stl"), csg.toStlString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun toCSG(
        armHeight: Double,
        armScaleFactor: Double,
        armCubeWidth: Double,
        armCubeThickness: Double,
        holderPlatformRadius: Double,
        holderPlatformThickness: Double
    ): CSG {
        var armCubeThickness = armCubeThickness
        val widthTol = 2.0
        val thicknessTol = 0.1
        val holderWallThickness = 6.0
        val armOverlap = 30.0
        armCubeThickness += thicknessTol
        val holderCubeDepth = armOverlap + armCubeThickness + holderWallThickness
        val armWidth = armHeight * armScaleFactor
        val xTransform = Transform.unity().translateX(-holderWallThickness * 2)
        val yTransform = Transform.unity()
            .translateY(-armCubeThickness / 2.0 - armOverlap / 2.0 + holderWallThickness)
        val armCube = Cube(armCubeWidth + widthTol, armCubeThickness, armHeight).toCSG()
            .transformed(yTransform)
        var arm = Cube(armWidth, holderCubeDepth, armHeight).toCSG()
            .transformed(Transform.unity().translateZ(armHeight / 2.0))
        arm = Cylinder(armHeight / 2.0, holderCubeDepth, 32).toCSG()
            .transformed(
                Transform.unity().rotX(90.0).translate(0.0, 0.0, -holderCubeDepth / 2.0)
                    .scaleX(armScaleFactor)
            ).union(arm)
        val holder = armCube.union(arm).transformed(Transform.unity().rotZ(90.0))
        val sideArmHight = 150 / 2.0
        val sideArmGroundDist = 25.0
        val sideArmRadius = armHeight / 6.0
        val sideArmShrinkFactor = 0.6

//        CSG sideArms = QuadrocopterArm.sideArms(sideArmGroundDist, sideArmHight, sideArmRadius, sideArmShrinkFactor, armCubeThickness,armWidth).transformed(xTransform);

//        return holder.union(sideArms);
        return holder
    }

    fun toCSG2(): CSG {
        val platformRadius = 80.0
        val innerHoleRadius = 50.0
        val platformThickness = 3.0 // deprecated
        val armHeight = 18.0
        val armScaleFactor = 0.65
        val armCubeThickness = 4.0
        val holderPlatformRadius = 20.0
        val distToInnerHole = 5.0
        val armHolderPrototype = toCSG(
            armHeight, armScaleFactor, armHeight,
            armCubeThickness, holderPlatformRadius, platformThickness
        ).transformed(Transform.unity().translateX(68.0).translateZ(14.0))
        var armHolders = armHolderPrototype.copy()
        val quarterPrototype = RoundedCube(platformRadius).apply {
            centered = false
            cornerRadius = 10.0
            resolution = 16
        }.toCSG()
            .transformed(Transform.unity().rotZ(45.0)).transformed(Transform.unity().scaleY(3.0))
            .transformed(
                Transform.unity().translate(
                    innerHoleRadius + distToInnerHole, 0.0, -armHeight / 2.0
                )
            )
            .transformed(Transform.unity().rotZ(-45.0))
        var quarters = quarterPrototype.copy()
        for (i in 1..3) {
            val rotTransform = Transform.unity().rotZ((i * 90).toDouble())
            armHolders = armHolders.union(armHolderPrototype.transformed(rotTransform))
            quarters = quarters.union(quarterPrototype.transformed(rotTransform))
        }
        var platform = Cylinder(platformRadius, armHeight, 64).toCSG()
        val innerHole = Cylinder(innerHoleRadius, armHeight, 64).toCSG()
        platform = platform.difference(armHolders, innerHole, quarters)
        return platform
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            CSG.setDefaultOptType(OptType.NONE)
            val result = QuadrocopterCross().toCSG2()
            FileUtil.write(Paths.get("quadrocopter-cross.stl"), result.toStlString())
            result.toObj().toFiles(Paths.get("quadrocopter-cross.obj"))
        }
    }
}
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

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class QuadrocopterArmHolder {
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
        val holderWallThickness = 3.0
        val armOverlap = 15.0
        val holderTopRailWidth = 2.0
        val holderTopRailSpacing = 2.0
        armCubeThickness += thicknessTol
        val holderCubeWidth = widthTol + holderWallThickness * 2 + armCubeWidth
        val holderCubeHeight = armHeight + holderWallThickness
        val holderCubeDepth = armOverlap + armCubeThickness + holderWallThickness
        val holderCube = Cube(holderCubeWidth, holderCubeDepth, holderCubeHeight).toCSG()
        val armWidth = armHeight * armScaleFactor
        val armCube = Cube(armCubeWidth + widthTol, armCubeThickness, armHeight).toCSG()
            .transformed(
                Transform.unity()
                    .translateY(-armCubeThickness / 2.0 - armOverlap / 2.0 + holderWallThickness)
            )
        var arm = Cube(armWidth, holderCubeDepth, armHeight).toCSG()
            .transformed(Transform.unity().translateZ(armHeight / 2.0))
        arm = Cylinder(armHeight / 2.0, holderCubeDepth, 32).toCSG()
            .transformed(
                Transform.unity().rotX(90.0).translate(0.0, 0.0, -holderCubeDepth / 2.0)
                    .scaleX(armScaleFactor)
            ).union(arm)
        var holder = holderCube.difference(
            armCube.union(arm)
                .transformed(Transform.unity().translate(0.0, 0.0, 0.5 * holderWallThickness))
        ).transformed(
            Transform.unity().translateY(-holderCubeDepth / 2.0)
        )
        var holderTopRail = Cylinder(holderTopRailWidth / 2.0, holderCubeDepth, 6).toCSG()
            .transformed(
                Transform.unity().translate(
                    -holderCubeWidth / 2.0,
                    -holderCubeDepth,
                    -holderTopRailWidth / 2.0 + holderCubeHeight / 2.0 - holderTopRailSpacing
                ).rotX(90.0).rotZ(30.0)
            )
        holderTopRail = holderTopRail.union(
            holderTopRail.transformed(
                Transform.unity().translateX(holderCubeWidth)
            )
        )
        holder = holder.difference(holderTopRail)

//        return holder;
        val holderPlatform = Cylinder(holderPlatformRadius, holderPlatformThickness, 64).toCSG()
            .transformed(Transform.unity().scaleY(1.15).translateY(-holderPlatformRadius * 0.75))
        return holderPlatform.union(
            holder.transformed(
                Transform.unity().translateZ(4 * holderWallThickness)
            )
        ).transformed(
            Transform.unity().rotZ(-90.0)
        )
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val result = QuadrocopterArmHolder().toCSG(18.0, 0.5, 18.0, 4.0, 20.0, 3.0)
            FileUtil.write(
                Paths.get("quadrocopter-arm-holder.stl"),
                result.toStlString()
            )
            result.toObj().toFiles(Paths.get("quadrocopter-arm-holder.obj"))
        }
    }
}
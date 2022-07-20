/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cube
import eu.mihosoft.jcsg.Cylinder
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d

/**
 *
 * @author miho
 */
class BreadBoardMount {
    private val breadBoardWidth = 84.0
    private val breadboardLength = 54.0
    private val breadBoardHeight = 8.5
    private val pinRadius = 5.0
    private val pinHeight = 14.0
    private val pinHoleHeight = 10.0
    private val bottomThickness = 2.0
    private val servoConnectThickness = 7.0
    private fun board(): CSG {
        return Cube(
            Vector3d.ZERO,
            Vector3d.xyz(breadBoardWidth, breadboardLength, breadBoardHeight)
        ).toCSG()
    }

    private fun pins(): CSG {
        val prototype = Cylinder(pinRadius, pinHeight, 16).toCSG()
        val first = prototype.clone().transformed(
            Transform.unity().translate(breadBoardWidth / 2.0, breadBoardHeight / 2.0, 0.0)
        )
        val second = prototype.clone().transformed(
            Transform.unity().translate(breadBoardWidth / 2.0, -breadBoardHeight / 2.0, 0.0)
        )
        val third = prototype.clone()
            .transformed(Transform.unity().translate(-breadBoardWidth / 2.0 - 1, 0.0, 0.0))
        val pins = first.union(second).union(third)
        val board = board().transformed(Transform.unity().translateZ(pinHoleHeight * 2))
        return pins.difference(board)
    }

    private fun pinConnections(): CSG {
        val first = Cube(
            Vector3d.ZERO,
            Vector3d.xyz(breadBoardWidth / 2, 3.0, bottomThickness)
        ).toCSG().transformed(
            Transform.unity().translate(-breadBoardWidth / 4, 0.0, bottomThickness / 2)
        )
        val second = Cube(
            Vector3d.ZERO,
            Vector3d.xyz(breadBoardWidth / 2 + 10, 3.0, bottomThickness)
        ).toCSG().transformed(
            Transform.unity().rotZ(37.8)
                .translate(breadBoardWidth / 4 + 5, 0.0, bottomThickness / 2)
        )
        val third = Cube(
            Vector3d.ZERO,
            Vector3d.xyz(breadBoardWidth / 2 + 10, 3.0, bottomThickness)
        ).toCSG().transformed(
            Transform.unity().rotZ(-37.8)
                .translate(breadBoardWidth / 4 + 5, 0.0, bottomThickness / 2)
        )
        return first.union(second).union(third)
    }

    private fun servoConnect(): CSG {
        val firstA = Cube(
            Vector3d.ZERO,
            Vector3d.xyz(breadBoardWidth, servoConnectThickness, bottomThickness)
        ).toCSG().transformed(
            Transform.unity().translate(0.0, -breadBoardHeight / 2, bottomThickness / 2)
        )
        val firstB = Cube(
            Vector3d.ZERO,
            Vector3d.xyz(
                3.0,
                breadBoardHeight / 2 + servoConnectThickness / 2,
                bottomThickness
            )
        ).toCSG().transformed(
            Transform.unity().translate(
                -breadBoardWidth / 2,
                -breadBoardHeight / 4 - servoConnectThickness / 4,
                bottomThickness / 2
            )
        )
        val first = firstA.union(firstB)
        val second = first.transformed(Transform.unity().rotX(180.0).translateZ(-bottomThickness))
        return first.union(second)
    }

    fun toCSG(): CSG {
        return pins().union(pinConnections()).union(servoConnect())
    }
}

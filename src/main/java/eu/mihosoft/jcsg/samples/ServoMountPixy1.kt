/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cube
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths
import java.util.*

/**
 *
 * @author miho
 */
class ServoMountPixy {
    // mini servo
    //    private double servoWidth = 22.9;
    //    private double servoThickness = 12.0;
    //    
    //standard servo
    private val servoWidth = 40.5
    private val servoThickness = 19.0
    private val borderThickness = 3.0
    private val overlap = 3.0
    private val servoMountHeight = 20.0
    private val boardMountingThickness = 3.0
    private val boardHolder1Length = 12.0
    private val boardHolder2Length = 16.0
    private val boardMountingWidth = 8.1
    private val pegHeight = 1.0
    private val pegToothHeight = 0.6
    private val pegOverlap = 0.5
    fun toCSGSimple(): CSG {
        return Extrude.Companion.points(
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

    fun toCSG(): CSG? {
        val bm1Transform = Transform.unity().rotY(90.0).rotZ(90.0)
            .translate(borderThickness, borderThickness, -boardHolder1Length + borderThickness)
        val bm1 = boardMount1().transformed(bm1Transform)
        val bm2Transform =
            Transform.unity().translateX(servoWidth - boardHolder1Length + borderThickness * 2)
        val bm2 = boardMountWithPixy()!!.transformed(bm1Transform).transformed(bm2Transform)
        val sm = toCSGSimple()
        return sm.union(bm1).union(bm2) //.transformed(Transform.unity().scale(0.08));
    }

    private fun boardMount1(): CSG {
        return boardMount(boardHolder1Length)
    }

    private fun boardMount2(): CSG {
        return boardMount(boardHolder2Length)
    }

    private fun boardMountWithPixy(): CSG? {
        return boardMount2().union(pixyMount())
    }

    private fun pixyMount(): CSG? {
        val pixyBoardThickness = 2.0
        val camHeight = 60.0
        val camHolderHeight = 20.0
        //        
//        double outerThickness = boardHolder2Length*0.5-boardHolder2Length*0.5;
//        double innerThickness = pixyBoardThickness;
//        return pixyMountBase(
//                outerThickness).
//                union(pixyMountBase(innerThickness).transformed(Transform.unity().translateZ(outerThickness)));
        var pixyBoard = Cube(50.0, 50.0, pixyBoardThickness).toCSG()
        pixyBoard = pixyBoard!!.transformed(
            Transform.unity().translate(
                camHeight + camHolderHeight + boardMountingThickness + borderThickness * 4,
                pixyBoard.bounds.bounds.y() * 0.5, boardHolder2Length * 0.5
            )
        )
        return pixyMountBase().difference(pixyBoard)
    }

    private fun pixyMountBase(): CSG {
        val h = boardMountingWidth
        val camHeight = 60.0
        val camHolderHeight = 20.0
        val camWidth = 53.0
        val outerPiMountWidth = 60.0
        val camOverlap = 10.0
        val upperCamOverlap = 3.0
        val camHolderWidth = 10.0
        val breadBoardHeight = 26.0
        val breadBoardThickness = 9.0
        val breadBoardOverlap = 14.5
        val bottomThickness = 3.0
        val points = Arrays.asList(
            Vector3d.xy(boardMountingThickness + borderThickness, -borderThickness),
            Vector3d.xy(
                boardMountingThickness + borderThickness + camHeight + camHolderHeight,
                -borderThickness
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + camHeight + camHolderHeight,
                0 + upperCamOverlap
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + camHeight,
                outerPiMountWidth - camWidth + camOverlap
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + camHeight - borderThickness,
                outerPiMountWidth - camWidth + camOverlap
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + camHeight - borderThickness - camHolderWidth,
                0.0
            ),  // -> (breadboard)
            Vector3d.xy(
                boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness + borderThickness + bottomThickness,
                0.0
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness + borderThickness,
                breadBoardOverlap
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness,
                breadBoardOverlap
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + breadBoardHeight + breadBoardThickness,
                0.0
            ),
            Vector3d.xy(boardMountingThickness + borderThickness + breadBoardHeight, 0.0),
            Vector3d.xy(
                boardMountingThickness + borderThickness + breadBoardHeight,
                breadBoardOverlap
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + breadBoardHeight - borderThickness,
                breadBoardOverlap
            ),
            Vector3d.xy(
                boardMountingThickness + borderThickness + breadBoardHeight - borderThickness - bottomThickness,
                0.0
            ),  // <-
            Vector3d.xy(boardMountingThickness - pegOverlap + borderThickness, 0.0),
            Vector3d.xy(boardMountingThickness, h),
            Vector3d.xy(boardMountingThickness, 0.0)
        )
        Collections.reverse(points)
        return Extrude.Companion.points(
            Vector3d.xyz(0.0, 0.0, boardHolder2Length),
            points
        )
    }

    private fun boardMount(boardHolderLength: Double): CSG {
        val bottomThickness = 3.0
        val h = boardMountingWidth
        val points = Arrays.asList(
            Vector3d.ZERO,
            Vector3d.xy(0.0, -borderThickness),
            Vector3d.xy(
                boardMountingThickness + borderThickness + bottomThickness,
                -borderThickness
            ),
            Vector3d.xy(boardMountingThickness + borderThickness, h + pegToothHeight + pegHeight),
            Vector3d.xy(boardMountingThickness - pegOverlap, h + pegToothHeight + pegHeight * 0.25),
            Vector3d.xy(boardMountingThickness - pegOverlap, h + pegToothHeight),
            Vector3d.xy(boardMountingThickness, h),
            Vector3d.xy(boardMountingThickness, 0.0)
        )
        Collections.reverse(points)
        return Extrude.Companion.points(
            Vector3d.xyz(0.0, 0.0, boardHolderLength),
            points
        )
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val sMount = ServoMountPixy()

            // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
            FileUtil.Companion.write(
                Paths.get("servo-mount-pixy.stl"),
                sMount.toCSG()!!.toStlString()
            )
        }
    }
}
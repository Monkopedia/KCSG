/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author miho
 */
class RaspberryArduinoConnector {
    private val arduinoMountingThickness = 2.0
    private val rspberryMountingThickness = 2.0
    private val boardToBoardSpacing = 30.0
    private val connectorDepth = 8.0
    private val pegHeight = 1.0
    private val pegToothHeight = 0.3
    private val pegOverlap = 0.6
    private val boardMountingWidth = 8.0
    fun toCSG(): CSG {
        val th = 2.0
        val smh = boardMountingWidth
        val ath = arduinoMountingThickness
        val rth = rspberryMountingThickness
        val b2bs = boardToBoardSpacing
        val pth = pegToothHeight
        val ph = pegHeight
        val po = pegOverlap
        return Extrude.Companion.points(
            Vector3d.xyz(0.0, 0.0, connectorDepth),
            Vector3d.xy(-th, -th),
            Vector3d.xy(smh + pth + ph, -th),
            Vector3d.xy(smh + pth + Math.max(ph / 3, 0.4), 0 + po),
            Vector3d.xy(smh + pth, 0 + po),
            Vector3d.xy(smh, 0.0),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(0.0, ath),
            Vector3d.xy(smh, ath),
            Vector3d.xy(smh, ath + th),
            Vector3d.xy(0.0, ath + th),
            Vector3d.xy(0.0, ath + th + b2bs),
            Vector3d.xy(smh, ath + th + b2bs),
            Vector3d.xy(smh, ath + th + b2bs + th),
            Vector3d.xy(0.0, ath + th + b2bs + th),
            Vector3d.xy(0.0, ath + th + b2bs + th + rth),
            Vector3d.xy(smh, ath + th + b2bs + th + rth),
            Vector3d.xy(smh + pth, ath + th + b2bs + th + rth - po),
            Vector3d.xy(smh + pth + Math.max(ph / 3, 0.4), ath + th + b2bs + th + rth - po),
            Vector3d.xy(smh + pth + ph, ath + th + b2bs + th + rth + th),
            Vector3d.xy(-th, ath + th + b2bs + th + rth + th)
        )
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val arConnect = RaspberryArduinoConnector()

            // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
            FileUtil.Companion.write(
                Paths.get("pi-arduino-connector.stl"),
                arConnect.toCSG().toStlString()
            )
        }
    }
}
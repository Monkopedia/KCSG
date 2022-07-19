/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.vvecmath.Vector3d

/**
 *
 * @author miho
 */
class BatteryHolder {
    private val mountingThickness = 3.0
    private val boardToBoardSpacing = 30.0
    private val connectorDepth = 25.0
    private val pegHeight = 1.0
    private val pegToothHeight = 0.3
    private val pegOverlap = 0.6
    private val boardMountingWidth = 8.11
    private val batteryHeight = 22.0
    private val batteryLength = 54.0
    private val footHeight = 25.0
    private val footSize = 10.0
    fun toCSG(): CSG {
        val th = 3.0
        val smh = boardMountingWidth
        val mth = mountingThickness
        val pth = pegToothHeight
        val ph = pegHeight
        val po = pegOverlap
        val o = 13.0
        return Extrude.points(
            Vector3d.xyz(0.0, 0.0, connectorDepth),
            Vector3d.xy(-th, -th),
            Vector3d.xy(smh + pth + ph + o, -th),
            Vector3d.xy(smh + pth + Math.max(ph / 3, 0.4) + o, 0 + po),
            Vector3d.xy(smh + pth + o, 0 + po),
            Vector3d.xy(smh + o, 0.0),
            Vector3d.xy(0 + o, 0.0),
            Vector3d.xy(0 + o, mth),
            Vector3d.xy(smh + o, mth),
            Vector3d.xy(smh + o, mth + th),
            Vector3d.xy(0.0, mth + th),
            Vector3d.xy(0.0, mth + th + batteryHeight),
            Vector3d.xy(batteryLength, mth + th + batteryHeight),
            Vector3d.xy(batteryLength, mth + th + batteryHeight * 0.3),
            Vector3d.xy(batteryLength + th, mth + th + batteryHeight * 0.3),
            Vector3d.xy(batteryLength + th, mth + th + batteryHeight + th),
            Vector3d.xy(0.0, mth + th + batteryHeight + th),
            Vector3d.xy(0.0, mth + th + batteryHeight + th + footHeight - th * 2),
            Vector3d.xy(footSize, mth + th + batteryHeight + th + footHeight - th),
            Vector3d.xy(footSize, mth + th + batteryHeight + th + footHeight),
            Vector3d.xy(-th, mth + th + batteryHeight + th + footHeight)

        )
    }

}
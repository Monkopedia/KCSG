/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.Vector3d
import kotlin.math.max

/**
 */
class BreadBoardConnector {
    private val boardMountingThickness = 2.0
    private val breadBoardThickness = 9.0
    private val connectorDepth = 30.0
    private val pegHeight = 1.0
    private val pegToothHeight = 0.3
    private val pegOverlap = 0.6
    private val boardMountingWidth = 8.1
    private val breadBoardToPiMountDistance = 26.0

    //    private double breadBoardMountLength = 20;
    fun toCSG(): CSG {
        val th = 2.0
        val smh = boardMountingWidth
        val bmth = boardMountingThickness
        val bbpbd = breadBoardToPiMountDistance
        val bbth = breadBoardThickness - th
        val pth = pegToothHeight
        val ph = pegHeight
        val po = pegOverlap
        return Extrude.points(
            Vector3d.xyz(0.0, 0.0, connectorDepth),
            Vector3d.xy(-th, -th),
            Vector3d.xy(smh + pth + ph, -th),
            Vector3d.xy(smh + pth + max(ph / 3, 0.4), 0 + po),
            Vector3d.xy(smh + pth, 0 + po),
            Vector3d.xy(smh, 0.0),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(0.0, bmth),
            Vector3d.xy(smh, bmth),
            Vector3d.xy(smh, bmth + th),
            Vector3d.xy(0.0, bmth + th),
            Vector3d.xy(0.0, bmth + bbpbd - th), // 1
            Vector3d.xy(smh, bmth + bbpbd - th), // 2
            Vector3d.xy(smh, bmth + th + bbpbd - th), // 3
            Vector3d.xy(0.0, bmth + th + bbpbd - th), // 4
            Vector3d.xy(0.0, bmth + th + bbpbd + bbth), // 5
            Vector3d.xy(smh, bmth + th + bbpbd + bbth), // 6
            Vector3d.xy(smh, bmth + th + bbpbd + bbth + th), // 7
            Vector3d.xy(0.0, bmth + th + bbpbd + bbth + th), // 8
            Vector3d.xy(-th, bmth + th + bbpbd + bbth + th) // 9
        )
    }

}

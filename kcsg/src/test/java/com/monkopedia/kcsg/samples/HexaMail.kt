/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Transform

/**
 */
class HexaMail {
    fun toCSG(numEdges: Int, numX: Int, numY: Int): CSG? {
        val tile = PolyMailTile().setNumEdges(numEdges).setHingeHoleScale(1.2)
        val hingeHoleScale = tile.hingeHoleScale
        val malePart = tile.setCombined().toCSG()
        val femalePart = tile.setCombined().toCSG()
        var result: CSG? = null
        for (y in 0 until numY) {
            for (x in 0 until numX) {
                val pinOffset = (
                    tile.pinLength -
                        (
                            tile.jointRadius * hingeHoleScale -
                                tile.jointRadius
                            )
                    )
                var xOffset = 0.0
                val yOffset = pinOffset * 0.9
                if (y % 2 == 0) {
                    xOffset = tile.apothem + pinOffset * 0.5
                }
                val translateX = (-tile.apothem * 2 - pinOffset) * x + xOffset
                val translateY = (-tile.radius * 0.5 - tile.radius) * y - yOffset * y
                var part2: CSG?
                part2 = if (x % 2 == 0) {
                    femalePart.copy()
                } else {
                    malePart.copy()
                }
                if (numEdges % 2 != 0) {
                    part2 = part2.transformed(
                        Transform.unity().rotZ(360.0 / numEdges * 0.5)
                    )
                }
                part2 = part2.transformed(
                    Transform.unity().translate(translateX, translateY, 0.0)
                )
                if (result == null) {
                    result = part2.copy()
                }
                result = result.dumbUnion(part2)
            }
        }
        return result
    }
}

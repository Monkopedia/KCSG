/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
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
                val pinOffset = (tile.pinLength
                    - (tile.jointRadius * hingeHoleScale
                    - tile.jointRadius))
                var xOffset = 0.0
                val yOffset = pinOffset * 0.9
                if (y % 2 == 0) {
                    xOffset = tile.apothem + pinOffset * 0.5
                }
                val translateX = (-tile.apothem * 2 - pinOffset) * x + xOffset
                val translateY = (-tile.radius * 0.5 - tile.radius) * y - yOffset * y
                var part2: CSG?
                part2 = if (x % 2 == 0) {
                    femalePart.clone()
                } else {
                    malePart.clone()
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
                    result = part2.clone()
                }
                result = result.dumbUnion(part2)
            }
        }
        return result
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Paths.get("hexamail.stl"),
                HexaMail().toCSG(6, 3, 3)!!.toStlString()
            )
        }
    }
}
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
class TriMail {
    fun toCSG(numEdges: Int, numX: Int, numY: Int): CSG? {
        val tile = PolyMailTile().setNumEdges(numEdges).setPinThickness(2.1).setHingeHoleScale(1.2)
            .setConeLength(1.8)
        val hingeHoleScale = tile.hingeHoleScale
        val malePart = tile.setMale().toCSG()!!.transformed(
            Transform.unity().rotZ(360.0 / numEdges * 0.75)
        )
        val femalePart = tile.setFemale().toCSG()!!.transformed(
            Transform.unity().rotZ(360.0 / numEdges * 0.25)
        )
        var result: CSG? = null
        for (y in 0 until numY) {
            for (x in 0 until numX) {
                val pinOffset = (tile.pinLength
                    - (tile.jointRadius * hingeHoleScale
                    - tile.jointRadius))
                var xOffset = 0.0
                var yOffset = 0.0
                if (y % 2 == 0) {
                    xOffset = tile.sideLength * 0.5 + pinOffset * 0.5
                    if (y / 2 % 2 == 0) {
                        xOffset -= tile.sideLength * 0.5 + pinOffset * 0.5
                    }
                    yOffset = +tile.pinLength * 3
                }
                val translateX = (-tile.sideLength - pinOffset) * x + xOffset
                val translateY = tile.radius * y - yOffset
                var part2: CSG? = if (y % 2 == 0) {
                    femalePart.clone()
                } else {
                    malePart.clone()
                }
                part2 = part2!!.transformed(
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
                Paths.get("trimail-test.stl"),
                TriMail().toCSG(3, 3, 3)!!.toStlString()
            )
        }
    }
}
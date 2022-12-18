/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class QuadrocopterBottom {
    fun print3d(csg: CSG, n: Int) {
        try {
            FileUtil.write(Paths.get("quadrocopter-bottom-$n.stl"), csg.toStlString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun toCSG(): CSG {
        val outerRadius = 93.0
        val bottomThickness = 3.0
        val numHoneyCombs = 21
        val honeyCombWallThickness = 2.0
        val platformBorderThickness = 5.0
        return basePlatform(
            outerRadius,
            numHoneyCombs,
            bottomThickness,
            platformBorderThickness,
            honeyCombWallThickness
        )
    }

    private fun basePlatform(
        platformRadius: Double,
        numHoneycombs: Int,
        platformThickness: Double,
        platformBorderThickness: Double,
        honeycombWallThickness: Double
    ): CSG {
        val honeycombRadius = platformRadius / numHoneycombs
        var platform = Cylinder(platformRadius, platformThickness, 64).toCSG()
        val innerPlatform =
            Cylinder(platformRadius - platformBorderThickness, platformThickness, 64).toCSG()
        val platformShell = platform.difference(innerPlatform)
        val honeycombPrototype = Cylinder(honeycombRadius, platformThickness, 6).toCSG()
        var numHoneycomb = (platformRadius * 2 / (honeycombRadius * 2)).toInt()
        var hexagons: CSG? = null
        val inradiusOfHexagon = honeycombRadius * kotlin.math.cos(180.0 / 6.0 * Math.PI / 180)
        val sideLength = honeycombRadius * 2 * kotlin.math.sin(180.0 / 6.0 * Math.PI / 180)

        // TODO: change that!
        // inradius makes previus calculation obsolete
        // to be sure we use numHoneyCombs*1.3
        numHoneycomb = (numHoneycomb * 1.4).toInt()
        val centerOffset = 0.0 //+honeycombRadius-inradiusOfHexagon;
        for (y in 0 until numHoneycomb) {
            for (x in 0 until numHoneycomb) {
                val offset = inradiusOfHexagon * (x % 2)
                var dx = -platformRadius + x * sideLength * 1.5
                var dy =
                    -platformRadius + y * inradiusOfHexagon * 2.0 + offset - honeycombWallThickness / 4.0
                dx += honeycombWallThickness * x + centerOffset - honeycombWallThickness / 6.0
                dy += honeycombWallThickness * y + honeycombWallThickness * (x % 2) / 2 + centerOffset * 1.75 - inradiusOfHexagon * 0.5 + honeycombWallThickness / 2.0
                val h = honeycombPrototype.transformed(
                    Transform.unity().translate(
                        dx, dy, 0.0
                    )
                )
                hexagons = hexagons?.union(h) ?: h
            }
        }
        val centerHoleRadius = 15.0
        val centerHoleOuter = Cylinder(
            centerHoleRadius + platformBorderThickness,
            platformThickness,
            16
        ).toCSG()
        val centerHoleInner = Cylinder(centerHoleRadius, platformThickness, 16).toCSG()
        val centerHoleShell = centerHoleOuter.difference(centerHoleInner)
        if (hexagons != null) {
            platform = platform.difference(hexagons)
        }
        return platform.union(platformShell, centerHoleShell).difference(centerHoleInner)
    }
}
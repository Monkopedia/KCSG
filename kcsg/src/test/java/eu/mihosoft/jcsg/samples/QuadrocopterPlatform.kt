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
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class QuadrocopterPlatform {
    internal fun toCSG(): CSG {
        val platformRadius = 84.0
        val platformThickness = 3.0
        val platformBorderThickness = 4.0
        val numHoneycombs = 17
        val honeycombWallThickness = 1.0
        var platform = basePlatform(
            platformRadius,
            numHoneycombs,
            platformThickness,
            platformBorderThickness,
            honeycombWallThickness
        )
        val armHeight = 25.0
        val armScaleFactor = 0.5
        val armCubeThickness = 4.0
        val holderPlatformRadius = 20.0
        val armHolderPrototype = QuadrocopterArmHolder().toCSG(
            armHeight,
            armScaleFactor,
            armHeight,
            armCubeThickness,
            holderPlatformRadius,
            platformThickness
        ).transformed(Transform.unity().translateX(-platformRadius))
        var armHolders = armHolderPrototype.clone()
        for (i in 1..3) {
            armHolders = armHolders.union(
                armHolderPrototype.transformed(
                    Transform.unity().rotZ((i * 90).toDouble())
                )
            )
        }
        var cross = Cube(platformRadius * 2, platformBorderThickness, platformThickness).toCSG()
            .transformed(Transform.unity().translateZ(platformThickness / 2.0))
        cross = cross.union(cross.transformed(Transform.unity().rotZ(90.0)))
        platform = platform.union(armHolders, cross)
        return platform
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
        val inradiusOfHexagon = honeycombRadius * cos(180.0 / 6.0 * Math.PI / 180)
        val sideLength = honeycombRadius * 2 * sin(180.0 / 6.0 * Math.PI / 180)

        // TODO: change that!
        // inradius makes previus calculation obsolete
        // to be sure we use numHoneyCombs*1.3
        numHoneycomb = (numHoneycomb * 1.4).toInt()
        val centerOffset = 0.0 // +honeycombRadius-inradiusOfHexagon;
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
        if (hexagons != null) {
            platform = platform.difference(hexagons)
        }
        return platform.union(
            platformShell,
            honeycombPrototype.transformed(Transform.unity().scale(1.05, 1.05, 1.0))
        )
    }
}

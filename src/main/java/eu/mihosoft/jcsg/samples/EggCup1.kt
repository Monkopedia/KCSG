/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.CSG.OptType
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.jcsg.Sphere
import eu.mihosoft.jcsg.samples.EggCup
import eu.mihosoft.vvecmath.Transform
import java.io.IOException
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class EggCup {
    private fun toCSG(): CSG {
        CSG.setDefaultOptType(OptType.POLYGON_BOUND)
        val egg = Egg().toCSG()
        val eggBounds = egg.bounds.bounds
        val upperNegativeEgg = egg.transformed(
            Transform.unity().translateZ(
                eggBounds.z() * 0.175
            ).scale(0.88, 0.88, 1.0)
        )
        val lowerNegativeEgg =
            egg.transformed(Transform.unity().translateZ(-eggBounds.z() * 0.50))
        println("egg-size: " + upperNegativeEgg.bounds)
        try {
            FileUtil.write(Paths.get("eggcup-neg.stl"), upperNegativeEgg.toStlString())
        } catch (ex: IOException) {
            Logger.getLogger(EggCup::class.java.name).log(Level.SEVERE, null, ex)
        }
        val resolution = 64
        val wallThickness = 5.0
        val radius = eggBounds.x() / 2.0 + wallThickness
        CSG.setDefaultOptType(OptType.NONE)
        var feet = MoebiusStairs().resolution(90.0).twists(2.0).toCSG()
        feet = feet.transformed(Transform.unity().translateZ(-radius).scale(1.2, 1.2, 1.3))
        CSG.setDefaultOptType(OptType.POLYGON_BOUND)
        val shellOuter = Sphere(radius, resolution, resolution / 2).toCSG()
            .transformed(Transform.unity().scaleZ(1.25))
        var shell = shellOuter.difference(lowerNegativeEgg)
        val shellHeight = shell.bounds.bounds.z()
        val shrinkTransformZ = 0.8
        shell = shell.transformed(Transform.unity().scaleZ(shrinkTransformZ))
        var lowerIntersectionHeight = shellOuter.bounds.bounds.z() - shellHeight
        lowerIntersectionHeight *= shrinkTransformZ
        val shellTransform = Transform.unity().translateZ(-lowerIntersectionHeight)
        shell = shell.transformed(shellTransform)
        shell = shell.union(feet)
        shell = shell.difference(upperNegativeEgg.transformed(shellTransform))
        return shell
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(Paths.get("eggcup.stl"), EggCup().toCSG().toStlString())
        }
    }
}
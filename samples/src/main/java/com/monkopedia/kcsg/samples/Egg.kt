/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cube
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.Sphere
import com.monkopedia.kcsg.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 * Average Chicken Egg.
 */
class Egg {
    fun toCSG(): CSG {
        val radius = 22.0
        val stretch = 1.50
        val resolution = 64

        // cube that cuts the spheres
        var cube = Cube(2 * stretch * radius).toCSG()
        cube =
            cube.transformed(Transform.unity().translateZ(stretch * radius))

        // stretched sphere
        var upperHalf = Sphere(radius, resolution, resolution / 2).toCSG()
            .transformed(Transform.unity().scaleZ(stretch))

        // upper half
        upperHalf = upperHalf.intersect(cube)
        var lowerHalf = Sphere(radius, resolution, resolution / 2).toCSG()
        lowerHalf = lowerHalf.difference(cube)

        // stretch lower half
        lowerHalf =
            lowerHalf.transformed(Transform.unity().scaleZ(stretch * 0.72))
        return upperHalf.union(lowerHalf)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(Paths.get("egg.stl"), Egg().toCSG().toStlString())
        }
    }
}

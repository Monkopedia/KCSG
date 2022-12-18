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
 */
class Naze32Holder {
    fun toCSG(): CSG {
        val w = 36.0
        val h = 36.0
        val wInner = 31.0
        val hInner = 31.0
        val r = 1.5
        val screwR = 1.25
        val screwHolderHeight = 4.0
        val thickness = 2.0
        val resolution = 16
        val base = basePlatform(r, thickness, resolution, w, h)
        val screCylPrototype = Cylinder(screwR, screwHolderHeight, resolution).toCSG()
            .transformed(Transform.unity().translateZ(thickness))
        val cyl1 = screCylPrototype.transformed(
            Transform.unity().translateX(-wInner / 2.0).translateY(-hInner / 2.0)
        )
        val cyl2 = screCylPrototype.transformed(
            Transform.unity().translateX(wInner / 2.0).translateY(-hInner / 2.0)
        )
        val cyl3 = screCylPrototype.transformed(
            Transform.unity().translateX(wInner / 2.0).translateY(hInner / 2.0)
        )
        val cyl4 = screCylPrototype.transformed(
            Transform.unity().translateX(-wInner / 2.0).translateY(hInner / 2.0)
        )
        return base.union( /*peg1, peg2, */cyl1, cyl2, cyl3, cyl4)
    }

    private fun basePlatform(
        r: Double,
        thickness: Double,
        resolution: Int,
        w: Double,
        h: Double
    ): CSG {
        val cylPrototype = Cylinder(r, thickness, resolution).toCSG()
        val cyl1 = cylPrototype.transformed(
            Transform.unity().translateX(-w / 2.0 + r)
                .translateY(-h / 2.0 + r)
        )
        val cyl2 = cylPrototype.transformed(
            Transform.unity().translateX(w / 2.0 - r).translateY(-h / 2.0 + r)
        )
        val cyl3 = cylPrototype.transformed(
            Transform.unity().translateX(w / 2.0 - r).translateY(h / 2.0 - r)
        )
        val cyl4 = cylPrototype.transformed(
            Transform.unity().translateX(-w / 2.0 + r).translateY(h / 2.0 - r)
        )
        return cyl1.hull(cyl2, cyl3, cyl4)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Paths.get("naze32-mount.stl"),
                Naze32Holder().toCSG().toStlString()
            )
            Naze32Holder().toCSG().toObj().toFiles(Paths.get("naze32-mount.obj"))
        }
    }
}

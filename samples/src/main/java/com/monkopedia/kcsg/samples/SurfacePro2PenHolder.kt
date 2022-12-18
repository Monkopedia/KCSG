/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 */
class SurfacePro2PenHolder {
    fun toCSG(): CSG {
        val sdCard = MicroSDCard().toCSG()
        val width = sdCard.bounds.bounds.x()
        val height = sdCard.bounds.bounds.z()
        val extensionSize = 11.0
        val extension: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, height * 2),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(width, 0.0),
            Vector3d.xy(width, -extensionSize),
            Vector3d.xy(0.0, -extensionSize)
        ).transformed(Transform.unity().translateZ(-height))
        val extensionHeight = 10.0
        val extensionThickness = 0.8
        val extension2: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, extensionHeight),
            Vector3d.xy(0.0, -extensionSize),
            Vector3d.xy(width, -extensionSize),
            Vector3d.xy(width, -extensionSize - extensionThickness),
            Vector3d.xy(0.0, -extensionSize - extensionThickness)
        ).transformed(Transform.unity().translateZ(-extensionHeight + height))
        return sdCard.union(extension.union(extension2))
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Paths.get("surfac2penholder.stl"), SurfacePro2PenHolder().toCSG()
                    .toStlString()
            )
        }
    }
}

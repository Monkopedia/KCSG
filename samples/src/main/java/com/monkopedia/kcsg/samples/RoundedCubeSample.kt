/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import kotlinx.io.files.Path

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.RoundedCube
import java.io.IOException

/**
 */
class RoundedCubeSample {
    fun toCSG(): CSG {
        return RoundedCube(3.0).apply {
            resolution = 8
            cornerRadius = 0.2
        }.toCSG()
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Path("rounded-cube.stl"),
                RoundedCubeSample().toCSG().toStlString()
            )
            RoundedCubeSample().toCSG().toObj().toFiles(Path("rounded-cube.obj"))
        }
    }
}

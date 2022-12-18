/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.RoundedCube

/**
 */
class RoundedCubeSample {
    fun toCSG(): CSG {
        return RoundedCube(3.0).apply {
            resolution = 8
            cornerRadius = 0.2
        }.toCSG()
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.Vector3d

/**
 *
 * @author miho
 */
class MicroSDCard {
    fun toCSG(): CSG {

        // data taken from
        // https://www.sparkfun.com/datasheets/Prototyping/microSD_Spec.pdf
        // total card width
        val a = 10.9
        // front width
        val a1 = 9.6
        val a8 = 0.6

        // total card length
        val b = 14.9
        val b1 = 6.3

        // slit pos relative to front
        val b10 = 7.8

        // slit thickness
        val b11 = 1.1

        // total card thickness
        val c1 = 0.6
        val a2 = a - a1
        val b2 = b - b1 + a2
        return Extrude.points(
            Vector3d.xyz(0.0, 0.0, c1),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(a, 0.0),
            Vector3d.xy(a, b),
            Vector3d.xy(a2, b),
            Vector3d.xy(a2, b2),
            Vector3d.xy(0.0, b - b1),
            Vector3d.xy(0.0, b - b10),
            Vector3d.xy(a8, b - b10),
            Vector3d.xy(a8, b - b10 - b11),
            Vector3d.xy(0.0, b - b10 - b11 - a8)
        )
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

import kotlin.math.abs

/**
 * Modifies along z axis.
 */
class ZModifier : WeightFunction {
    private var bounds: Bounds? = null
    private val min = 0.0
    private val max = 1.0
    private var sPerUnit = 0.0
    private var centered = false

    /**
     * Constructor.
     */
    constructor()

    /**
     * Constructor.
     *
     * @param centered defines whether to center origin at the csg location
     */
    constructor(centered: Boolean) {
        this.centered = centered
    }

    override fun eval(pos: Vector3d, csg: CSG): Double {
        if (bounds == null) {
            bounds = csg.bounds
            sPerUnit = (max - min) / (bounds!!.max.z - bounds!!.min.z)
        }
        var s = sPerUnit * (pos.z - bounds!!.min.z)
        if (centered) {
            s -= (max - min) / 2.0
            s = abs(s) * 2
        }
        return s
    }
}

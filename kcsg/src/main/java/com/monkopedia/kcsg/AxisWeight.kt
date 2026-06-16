/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

import kotlin.math.abs

/**
 * Shared bounds-caching and weighting math for the per-axis weight functions
 * ([XModifier], [YModifier], [ZModifier]). Each modifier holds one instance and
 * supplies the axis component selector, so the eval math lives in a single place
 * instead of being triplicated across the three classes.
 */
internal class AxisWeight(private val axisOf: (Vector3d) -> Double) {
    private val min = 0.0
    private val max = 1.0
    private var bounds: Bounds? = null
    private var sPerUnit = 0.0

    fun eval(pos: Vector3d, csg: CSG, centered: Boolean): Double {
        val bounds = this.bounds ?: csg.bounds.also {
            this.bounds = it
            sPerUnit = (max - min) / (axisOf(it.max) - axisOf(it.min))
        }
        var s = sPerUnit * (axisOf(pos) - axisOf(bounds.min))
        if (centered) {
            s -= (max - min) / 2.0
            s = abs(s) * 2
        }
        return s
    }
}

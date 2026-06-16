/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

/**
 * Modifies along y axis.
 */
class YModifier : WeightFunction {
    private val weight = AxisWeight { it.y }

    /**
     * Whether the origin is centered at the CSG location. Affects produced weights.
     */
    var centered = false
        private set

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

    override fun eval(pos: Vector3d, csg: CSG): Double = weight.eval(pos, csg, centered)
}

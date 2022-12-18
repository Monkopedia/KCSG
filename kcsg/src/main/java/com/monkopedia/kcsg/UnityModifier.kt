/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

/**
 * Modifies along x axis.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class UnityModifier : WeightFunction {
    override fun eval(pos: Vector3d, csg: CSG): Double {
        return 1.0
    }
}
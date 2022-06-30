/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.vvecmath.Vector3d

/**
 * Weight function.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
fun interface WeightFunction {
    /**
     * Evaluates the function at the specified location.
     * @param v location
     * @param csg csg
     * @return the weight of the specified position
     */
    fun eval(v: Vector3d, csg: CSG): Double
}
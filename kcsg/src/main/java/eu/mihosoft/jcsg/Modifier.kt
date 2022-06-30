/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
internal class Modifier(private val function: WeightFunction) {
    fun modify(csg: CSG) {
        for (p in csg.polygons) {
            for (v in p.vertices) {
                v.weight = function.eval(v.pos, csg)
            }
        }
    }

    fun modified(csg: CSG): CSG {
        val result = csg.clone()
        modify(result)
        return result
    }
}
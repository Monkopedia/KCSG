/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

/**
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
        CSG.opOverride?.operation("modifier", csg, this)?.let { return it }
        val result = csg.copy()
        modify(result)
        return result
    }
}

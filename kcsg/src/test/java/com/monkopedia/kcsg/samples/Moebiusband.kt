/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d

/**
 */
class Moebiusband {
    fun toCSG(): CSG {
        val width = 10.0
        val height = 20.0
        val points = listOf(
            Vector3d.xy(-width / 2, -height / 2),
            Vector3d.xy(width / 2, -height / 2),
            Vector3d.xy(width / 2, height / 2),
            Vector3d.xy(-width / 2, height / 2)
        )
        val originalFacets: MutableList<CSG> = ArrayList()
        val facets: MutableList<CSG> = ArrayList()
        var prev: CSG? = null
        for (i in 0..9) {
            val t = Transform.unity().translateZ(2.0).rotZ(i.toDouble())
            val facet: CSG = Extrude.points(Vector3d.xyz(0.0, 0.0, 1.0), points)
            if (prev != null) {
                facets.add(facet.union(prev).hull())
            }
            originalFacets.add(facet)
            points.stream().forEach { p: Vector3d? -> t.transform(p!!) }
            prev = facet
        }
        var result = facets[0]
        for (i in 1 until facets.size) {
            result = result.union(facets[i])
        }
        var originalResult: CSG? = originalFacets[0]
        for (i in 1 until facets.size) {
            originalResult = originalResult!!.union(originalFacets[i])
        }
        return result.union(originalResult!!.transformed(Transform.unity().translateX(width * 2)))
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths
import java.util.*

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Moebiusband {
    fun toCSG(): CSG? {
        val width = 10.0
        val height = 20.0
        val points = Arrays.asList(
            Vector3d.xy(-width / 2, -height / 2),
            Vector3d.xy(width / 2, -height / 2),
            Vector3d.xy(width / 2, height / 2),
            Vector3d.xy(-width / 2, height / 2)
        )
        val originalFacets: MutableList<CSG> = ArrayList()
        val facets: MutableList<CSG?> = ArrayList()
        var prev: CSG? = null
        for (i in 0..9) {
            val t = Transform.unity().translateZ(2.0).rotZ(i.toDouble())
            val facet: CSG = Extrude.Companion.points(Vector3d.xyz(0.0, 0.0, 1.0), points)
            if (prev != null) {
                facets.add(facet.union(prev).hull())
            }
            originalFacets.add(facet)
            points.stream().forEach { p: Vector3d? -> t.transform(p) }
            prev = facet
        }
        var result = facets[0]
        for (i in 1 until facets.size) {
            result = result!!.union(facets[i])
        }
        var originalResult: CSG? = originalFacets[0]
        for (i in 1 until facets.size) {
            originalResult = originalResult!!.union(originalFacets[i])
        }
        return result!!.union(originalResult!!.transformed(Transform.unity().translateX(width * 2)))
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            println("RUNNING")
            FileUtil.Companion.write(
                Paths.get("möbiusband.stl"),
                Moebiusband().toCSG()!!.toStlString()
            )
        }
    }
}
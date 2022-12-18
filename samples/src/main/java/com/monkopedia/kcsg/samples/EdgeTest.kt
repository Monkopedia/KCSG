/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.Edge
import com.monkopedia.kcsg.FileUtil
import com.monkopedia.kcsg.Polygon
import com.monkopedia.kcsg.Sphere
import com.monkopedia.kcsg.Transform
import java.io.IOException
import java.nio.file.Paths

/**
 * Average Chicken Egg.
 */
class EdgeTest {
    fun toCSG(optimized: Boolean): CSG {
        val radius = 22.0
        val stretch = 1.50
        val resolution = 64
        val cylinder = Cylinder(1.0, 0.3, 8).toCSG()
        val sphere = Sphere(0.1, 8, 4).toCSG()
            .transformed(Transform.unity().translateZ(0.15))
        val cyl = Cylinder(0.08, 0.3, 8).toCSG()

//        CSG csg = cylinder.difference(cyl).union(sphere);
        val csg = cylinder.difference(cyl)
        //        CSG csg = cylinder.union(sphere);
        return if (!optimized) {
            csg
        } else {
            val boundaryPolygons: MutableList<Polygon> =
                Edge.boundaryPolygons(csg)
            println("#groups: " + boundaryPolygons.size)

//        List<Polygon> polys = boundaryPolygons.stream().peek(p->System.out.println("verts: "+p.vertices)).map(p->PolygonUtil.concaveToConvex(p)).flatMap(pList->pList.stream()).collect(Collectors.toList());
            CSG.fromPolygons(boundaryPolygons)
        }

//        return csg;
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.write(
                Paths.get("edge-test.stl"),
                EdgeTest().toCSG(true).toStlString()
            )
            FileUtil.write(
                Paths.get("edge-test-orig.stl"),
                EdgeTest().toCSG(false).toStlString()
            )
        }
    }
}

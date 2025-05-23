/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.ext.quickhull3d

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Polygon
import com.monkopedia.kcsg.PropertyStorage
import com.monkopedia.kcsg.Vertex
import com.monkopedia.kcsg.Vector3d
import java.util.function.Consumer

/**
 */
internal class HullUtil private constructor() {
    companion object {
        fun hull(points: List<Vector3d>, storage: PropertyStorage?): CSG {
            CSG.opOverride?.operation("hull", *points.toTypedArray())?.let { return it }
            val hullPoints =
                points.map { vec: Vector3d -> Point3d(vec.x, vec.y, vec.z) }
                    .toTypedArray()
            val hull = QuickHull3D()
            hull.build(hullPoints)
            hull.triangulate()
            val faces = hull.getFaces()
            val polygons: MutableList<Polygon> = ArrayList()
            val vertices: MutableList<Vector3d> = ArrayList()
            for (verts in faces) {
                for (i in verts!!) {
                    vertices.add(points[hull.getVertexPointIndices()[i]])
                }
                polygons.add(Polygon.fromPoints(vertices, storage))
                vertices.clear()
            }
            return CSG.fromPolygons(polygons)
        }

        fun hull(csg: CSG, storage: PropertyStorage?): CSG {
            CSG.opOverride?.operation("hull", csg)?.let { return it }
            val points: MutableList<Vector3d> = ArrayList(
                csg.polygons.size * 3
            )
            csg.polygons
                .forEach { p: Polygon ->
                    p.vertices.forEach(
                        Consumer { v: Vertex ->
                            points.add(
                                v.pos
                            )
                        })
                }
            return hull(points, storage)
        }
    }

    init {
        throw AssertionError("Don't instantiate me!", null)
    }
}

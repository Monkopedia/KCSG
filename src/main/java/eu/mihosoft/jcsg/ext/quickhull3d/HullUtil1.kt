/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.ext.quickhull3d

import eu.mihosoft.jcsg.*
import eu.mihosoft.jcsg.Vertex
import eu.mihosoft.vvecmath.Vector3d
import java.util.function.Consumer

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class HullUtil private constructor() {
    companion object {
        fun hull(points: List<Vector3d>, storage: PropertyStorage?): CSG {
            val hullPoints =
                points.map { vec: Vector3d -> Point3d(vec.x(), vec.y(), vec.z()) }
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
            val points: MutableList<Vector3d> = ArrayList(
                csg.polygons!!.size * 3
            )
            csg.polygons!!
                .forEach(Consumer { p: Polygon? ->
                    p!!.vertices.forEach(
                        Consumer { v: Vertex? ->
                            points.add(
                                v!!.pos
                            )
                        })
                })
            return hull(points, storage)
        }
    }

    init {
        throw AssertionError("Don't instantiate me!", null)
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.vvecmath.Vector3d
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Polyhedron.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Polyhedron : Primitive {
    private val properties = PropertyStorage()
    private val points: MutableList<Vector3d> = ArrayList()
    private val faces: MutableList<List<Int>> = ArrayList()

    /**
     * Constructor. Creates a polyhedron defined by a list of points and a list
     * of faces.
     *
     * @param points points ([Vector3d] list)
     * @param faces list of faces (list of point index lists)
     */
    constructor(points: List<Vector3d>?, faces: List<List<Int>>?) {
        this.points.addAll(points!!)
        this.faces.addAll(faces!!)
    }

    /**
     * Constructor. Creates a polyhedron defined by a list of points and a list
     * of faces.
     *
     * @param points points ([Vector3d] array)
     * @param faces list of faces (array of point index arrays)
     */
    constructor(points: Array<Vector3d>, faces: Array<Array<Int>>) {
        this.points.addAll(listOf(*points))
        for (list in faces) {
            this.faces.add(listOf(*list))
        }
    }

    override fun toPolygons(): MutableList<Polygon>? {
        val indexToPoint = Function { i: Int? -> points[i!!].clone() }
        val faceListToPolygon = Function<List<Int>, Polygon> { faceList: List<Int> ->
            Polygon.fromPoints(
                faceList.stream().map(indexToPoint).collect(Collectors.toList()),
                properties
            )
        }
        return faces.stream().map(faceListToPolygon).collect(Collectors.toList())
    }

    override fun getProperties(): PropertyStorage {
        return properties
    }
}
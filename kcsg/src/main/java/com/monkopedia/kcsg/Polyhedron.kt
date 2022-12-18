/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

import java.util.*

/**
 * Polyhedron.
 */
data class Polyhedron
/**
 * Constructor. Creates a polyhedron defined by a list of points and a list
 * of faces.
 *
 * @param points points ([Vector3d] list)
 * @param faces list of faces (list of point index lists)
 */(
    val points: List<Vector3d> = ArrayList(),
    val faces: List<List<Int>> = ArrayList()
) : Primitive {
    private val properties = PropertyStorage()

    /**
     * Constructor. Creates a polyhedron defined by a list of points and a list
     * of faces.
     *
     * @param points points ([Vector3d] array)
     * @param faces list of faces (array of point index arrays)
     */
    constructor(points: Array<Vector3d>, faces: Array<Array<Int>>) : this(
        points.toList(),
        faces.map { it.toList() }
    )

    override fun toPolygons(): List<Polygon> {
        return faces.map { faceList ->
            Polygon.fromPoints(
                faceList.map { points[it].copy() },
                properties
            )
        }
    }

    override fun getProperties(): PropertyStorage {
        return properties
    }
}

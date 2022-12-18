package com.monkopedia.kcsg.ext.quickhull3d

import com.monkopedia.kcsg.Vector3d

/**
 * Represents vertices of the hull, as well as the points from
 * which it is formed.
 */
internal class Vertex {
    /**
     * Spatial point associated with this vertex.
     */
    var pnt: Vector3d

    /**
     * Back index into an array.
     */
    var index = 0

    /**
     * List forward link.
     */
    var prev: Vertex? = null

    /**
     * List backward link.
     */
    var next: Vertex? = null

    /**
     * Current face that this vertex is outside of.
     */
    var face: Face? = null

    /**
     * Constructs a vertex and sets its coordinates to 0.
     */
    constructor() {
        pnt = Point3d()
    }

    /**
     * Constructs a vertex with the specified coordinates
     * and index.
     */
    constructor(x: Double, y: Double, z: Double, idx: Int) {
        pnt = Point3d(x, y, z)
        index = idx
    }
}

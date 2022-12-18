/**
 * Plane.java
 *
 * Copyright 2014-2014 Michael Hoffer <info></info>@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info></info>@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info></info>@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info></info>@michaelhoffer.de>.
 */
package com.monkopedia.kcsg

// # class Plane
/**
 * Represents a plane in 3D space.
 */
class Plane(normal: Vector3d, dist: Double) {
    /**
     * Normal vector.
     */
    var normal: Vector3d

    /**
     * Distance to origin.
     */
    var dist: Double
    public fun copy(): Plane {
        return Plane(normal.copy(), dist)
    }

    /**
     * Flips this plane.
     */
    fun flip() {
        normal = normal.negated()
        dist = -dist
    }

    /**
     * Splits a [Polygon] by this plane if needed. After that it puts the
     * polygons or the polygon fragments in the appropriate lists
     * (`front`, `back`). Coplanar polygons go into either
     * `coplanarFront`, `coplanarBack` depending on their
     * orientation with respect to this plane. Polygons in front or back of this
     * plane go into either `front` or `back`.
     *
     * @param polygon polygon to split
     * @param coplanarFront "coplanar front" polygons
     * @param coplanarBack "coplanar back" polygons
     * @param front front polygons
     * @param back back polgons
     */
    fun splitPolygon(
        polygon: Polygon,
        coplanarFront: MutableList<Polygon>,
        coplanarBack: MutableList<Polygon>,
        front: MutableList<Polygon>,
        back: MutableList<Polygon>
    ) {
        // Classify each point as well as the entire polygon into one of the
        // above four classes.
        var polygonType = 0
        val types: MutableList<Int> = ArrayList(polygon.vertices.size)
        for (i in polygon.vertices.indices) {
            val t = normal.dot(polygon.vertices[i].pos) - dist
            val type = if (t < -EPSILON) BACK else if (t > EPSILON) FRONT else COPLANAR
            polygonType = polygonType or type
            types.add(type)
        }
        when (polygonType) {
            COPLANAR -> // System.out.println(" -> coplanar");
                (if (normal.dot(polygon.csgPlane.normal) > 0) coplanarFront else coplanarBack).add(
                    polygon
                )
            FRONT -> // System.out.println(" -> front");
                front.add(polygon)
            BACK -> // System.out.println(" -> back");
                back.add(polygon)
            SPANNING -> {
                // System.out.println(" -> spanning");
                val f: MutableList<Vertex> = ArrayList()
                val b: MutableList<Vertex> = ArrayList()
                var i = 0
                while (i < polygon.vertices.size) {
                    val j = (i + 1) % polygon.vertices.size
                    val ti = types[i]
                    val tj = types[j]
                    val vi = polygon.vertices[i]
                    val vj = polygon.vertices[j]
                    if (ti != BACK) {
                        f.add(vi)
                    }
                    if (ti != FRONT) {
                        b.add(if (ti != BACK) vi.copy() else vi)
                    }
                    if (ti or tj == SPANNING) {
                        val t = (
                            (dist - normal.dot(vi.pos)) /
                                normal.dot(vj.pos.minus(vi.pos))
                            )
                        val v = vi.interpolate(vj, t)
                        f.add(v)
                        b.add(v.copy())
                    }
                    i++
                }
                if (f.size >= 3) {
                    front.add(Polygon(f, polygon.storage))
                }
                if (b.size >= 3) {
                    back.add(Polygon(b, polygon.storage))
                }
            }
        }
    }

    companion object {
        const val COPLANAR = 0
        const val FRONT = 1
        const val BACK = 2
        const val SPANNING = 3 // == some in the FRONT + some in the BACK

        /**
         * EPSILON is the tolerance used by [ ][.splitPolygon] to decide if a point is on the plane.
         */
        var EPSILON = 1e-8

        /**
         * XY plane.
         */
        val XY_PLANE = Plane(Vector3d.Z_ONE, 1.0)

        /**
         * XZ plane.
         */
        val XZ_PLANE = Plane(Vector3d.Y_ONE, 1.0)

        /**
         * YZ plane.
         */
        val YZ_PLANE = Plane(Vector3d.X_ONE, 1.0)

        /**
         * Creates a plane defined by the the specified points.
         *
         * @param a first point
         * @param b second point
         * @param c third point
         * @return a plane
         */
        fun createFromPoints(a: Vector3d, b: Vector3d, c: Vector3d): Plane {
            val n = b.minus(a).crossed(c.minus(a)).normalized()
            return Plane(n, n.dot(a))
        }
    }

    /**
     * Constructor. Creates a new plane defined by its normal vector and the
     * distance to the origin.
     *
     * @param normal plane normal
     * @param dist distance from origin
     */
    init {
        this.normal = normal.normalized()
        this.dist = dist
    }
}

/**
 * Polygon.java
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

import com.monkopedia.kcsg.ext.org.poly2tri.PolygonUtil
import kotlin.math.abs

/**
 * Represents a convex polygon.
 *
 * Each convex polygon has a `shared` property, which is shared between
 * all polygons that are clones of each other or where split from the same
 * polygon. This can be used to define per-polygon properties (such as surface
 * color).
 */
class Polygon {
    private var _vertices: MutableList<Vertex>

    /**
     * Polygon vertices
     */
    val vertices: List<Vertex>
        get() = _vertices

    /**
     * Shared property (can be used for shared color etc.).
     */
    private var shared: PropertyStorage? = null

    /**
     * Plane defined by this polygon.
     *
     * **Note:** uses first three vertices to define the plane.
     */
    val csgPlane: Plane

    /**
     * Returns the plane defined by this triangle.
     *
     * @return plane
     */
    var plane: com.monkopedia.kcsg.ext.vvecmath.Plane
        private set

    /**
     * Indicates whether this polyon is valid, i.e., if it
     *
     * @return
     */
    val isValid: Boolean
        get() {
            return valid
        }

    private var valid = true

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * **Note:** the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     * @param shared shared property
     */
    constructor(vertices: List<Vertex>, shared: PropertyStorage?) {
        requireMinimumVertexCount(vertices)
        this._vertices = vertices.toMutableList()
        this.shared = shared
        csgPlane = Plane.createFromPoints(
            vertices[0].pos,
            vertices[1].pos,
            vertices[2].pos
        )
        plane = com.monkopedia.kcsg.ext.vvecmath.Plane.fromPointAndNormal(centroid(), csgPlane.normal)
        validateAndInit(vertices)
    }

    private fun validateAndInit(vertices1: List<Vertex>) {
        for (v in vertices1) {
            v.normal = csgPlane.normal
        }
        if (Vector3d.ZERO == csgPlane.normal) {
            valid = false
            logger.error(
                """
                    Normal is zero! Probably, duplicate points have been specified!
                    
                    ${toStlString()}
                """.trimIndent()
            )
        }
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * **Note:** the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     */
    constructor(vertices: List<Vertex>) {
        requireMinimumVertexCount(vertices)
        this._vertices = vertices.toMutableList()
        csgPlane = Plane.createFromPoints(
            vertices[0].pos,
            vertices[1].pos,
            vertices[2].pos
        )
        plane = com.monkopedia.kcsg.ext.vvecmath.Plane.fromPointAndNormal(centroid(), csgPlane.normal)
        validateAndInit(vertices)
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * **Note:** the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     */
    constructor(vararg vertices: Vertex) : this(listOf<Vertex>(*vertices))

    private fun requireMinimumVertexCount(vertices: List<Vertex>) {
        if (vertices.size < 3) {
            throw RuntimeException(
                "Invalid polygon: at least 3 vertices expected, got: " +
                    vertices.size
            )
        }
    }

    fun copy(): Polygon {
        val newVertices: MutableList<Vertex> = ArrayList()
        vertices.forEach { vertex: Vertex ->
            newVertices.add(vertex.copy())
        }
        return Polygon(newVertices, storage)
    }

    /**
     * Flips this polygon.
     *
     * @return this polygon
     */
    fun flip(): Polygon {
        vertices.forEach { vertex: Vertex -> vertex.flip() }
        _vertices.reverse()
        csgPlane.flip()
        plane = plane.flipped()
        return this
    }

    /**
     * Returns a flipped copy of this polygon.
     *
     * **Note:** this polygon is not modified.
     *
     * @return a flipped copy of this polygon
     */
    fun flipped(): Polygon {
        return copy().flip()
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @return this polygon in STL string format
     */
    fun toStlString(): String {
        return toStlString(StringBuilder()).toString()
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @param sb string builder
     *
     * @return the specified string builder
     */
    fun toStlString(sb: StringBuilder): StringBuilder {
        if (vertices.size >= 3) {
            // TODO: improve the triangulation?
            //
            // STL requires triangular polygons.
            // If our polygon has more vertices, create
            // multiple triangles:
            val firstVertexStl = vertices[0].toStlString()
            for (i in 0 until vertices.size - 2) {
                sb.append("  facet normal ").append(csgPlane.normal.toStlString()).append("\n")
                    .append("    outer loop\n").append("      ").append(firstVertexStl).append("\n")
                    .append("      ")
                vertices[i + 1].toStlString(sb).append("\n").append("      ")
                vertices[i + 2].toStlString(sb).append("\n").append("    endloop\n")
                    .append("  endfacet\n")
            }
        }
        return sb
    }

    /**
     * Returns a triangulated version of this polygon.
     *
     * @return triangles
     */
    fun toTriangles(): List<Polygon> {
        val result: MutableList<Polygon> = ArrayList()
        if (vertices.size >= 3) {
            // TODO: improve the triangulation?
            //
            // If our polygon has more vertices, create
            // multiple triangles:
            val firstVertexStl = vertices[0]
            for (i in 0 until vertices.size - 2) {
                // create triangle
                val polygon = fromPoints(
                    firstVertexStl.pos,
                    vertices[i + 1].pos,
                    vertices[i + 2].pos
                )
                result.add(polygon)
            }
        }
        return result
    }

    /**
     * Translates this polygon.
     *
     * @param v the vector that defines the translation
     * @return this polygon
     */
    fun translate(v: Vector3d): Polygon {
        vertices.forEach { vertex: Vertex -> vertex.pos = vertex.pos.plus(v) }
        val a = vertices[0].pos
        val b = vertices[1].pos
        val c = vertices[2].pos

        // TODO plane update correct?
        csgPlane.normal = b.minus(a).crossed(c.minus(a))
        plane = com.monkopedia.kcsg.ext.vvecmath.Plane.fromPointAndNormal(centroid(), csgPlane.normal)
        return this
    }

    /**
     * Returns a translated copy of this polygon.
     *
     * **Note:** this polygon is not modified
     *
     * @param v the vector that defines the translation
     *
     * @return a translated copy of this polygon
     */
    fun translated(v: Vector3d): Polygon {
        return copy().translate(v)
    }

    /**
     * Applies the specified transformation to this polygon.
     *
     * **Note:** if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * @param transform the transformation to apply
     *
     * @return this polygon
     */
    fun transform(transform: Transform): Polygon {
        vertices.forEach { v: Vertex -> v.transform(transform) }
        val a = vertices[0].pos
        val b = vertices[1].pos
        val c = vertices[2].pos
        csgPlane.normal = b.minus(a).crossed(c.minus(a)).normalized()
        csgPlane.dist = csgPlane.normal.dot(a)
        plane = com.monkopedia.kcsg.ext.vvecmath.Plane.fromPointAndNormal(centroid(), csgPlane.normal)
        vertices.forEach { vertex: Vertex -> vertex.normal = plane.normal }
        if (transform.isMirror) {
            // the transformation includes mirroring. flip polygon
            flip()
        }
        return this
    }

    /**
     * Returns a transformed copy of this polygon.
     *
     * **Note:** if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * **Note:** this polygon is not modified
     *
     * @param transform the transformation to apply
     * @return a transformed copy of this polygon
     */
    fun transformed(transform: Transform): Polygon {
        return copy().transform(transform)
    }

    /**
     * Returns the bounds of this polygon.
     *
     * @return bouds of this polygon
     */
    val bounds: Bounds
        get() {
            var minX = Double.POSITIVE_INFINITY
            var minY = Double.POSITIVE_INFINITY
            var minZ = Double.POSITIVE_INFINITY
            var maxX = Double.NEGATIVE_INFINITY
            var maxY = Double.NEGATIVE_INFINITY
            var maxZ = Double.NEGATIVE_INFINITY
            for (i in vertices.indices) {
                val vert = vertices[i]
                if (vert.pos.x < minX) {
                    minX = vert.pos.x
                }
                if (vert.pos.y < minY) {
                    minY = vert.pos.y
                }
                if (vert.pos.z < minZ) {
                    minZ = vert.pos.z
                }
                if (vert.pos.x > maxX) {
                    maxX = vert.pos.x
                }
                if (vert.pos.y > maxY) {
                    maxY = vert.pos.y
                }
                if (vert.pos.z > maxZ) {
                    maxZ = vert.pos.z
                }
            } // end for vertices
            return Bounds(
                Vector3d.xyz(minX, minY, minZ),
                Vector3d.xyz(maxX, maxY, maxZ)
            )
        }

    fun centroid(): Vector3d {
        var sum = Vector3d.zero()
        for (v in vertices) {
            sum = sum.plus(v.pos)
        }
        return sum.times(1.0 / vertices.size)
    }

    /**
     * Indicates whether the specified point is contained within this polygon.
     *
     * @param p point
     * @return `true` if the point is inside the polygon or on one of the
     * edges; `false` otherwise
     */
    operator fun contains(p: Vector3d): Boolean {
        // P not on the plane
        if (plane.distance(p) > Plane.EPSILON) {
            return false
        }

        // if P is on one of the vertices, return true
        for (i in 0 until vertices.size - 1) {
            if (p.minus(vertices[i].pos).magnitude() < Plane.EPSILON) {
                return true
            }
        }

        // if P is on the plane, we proceed with projection to XY plane
        //
        // P1--P------P2
        //     ^
        //     |
        // P is on the segment if( dist(P1,P) + dist(P2,P) - dist(P1,P2) < TOL)
        for (i in 0 until vertices.size - 1) {
            val p1 = vertices[i].pos
            val p2 = vertices[i + 1].pos
            val onASegment: Boolean =
                (p1.minus(p).magnitude() + p2.minus(p).magnitude() - p1.minus(p2).magnitude()) <
                    Plane.EPSILON
            if (onASegment) {
                return true
            }
        }

        // find projection plane
        // we start with XY plane
        var coordIndex1 = 0
        var coordIndex2 = 1
        val orthogonalToXY: Boolean = abs(
            com.monkopedia.kcsg.ext.vvecmath.Plane.XY_PLANE.normal
                .dot(plane.normal)
        ) < Plane.EPSILON
        var foundProjectionPlane = false
        if (!orthogonalToXY && !foundProjectionPlane) {
            coordIndex1 = 0
            coordIndex2 = 1
            foundProjectionPlane = true
        }
        val orthogonalToXZ: Boolean = abs(
            com.monkopedia.kcsg.ext.vvecmath.Plane.XZ_PLANE.normal
                .dot(plane.normal)
        ) < Plane.EPSILON
        if (!orthogonalToXZ && !foundProjectionPlane) {
            coordIndex1 = 0
            coordIndex2 = 2
            foundProjectionPlane = true
        }
        val orthogonalToYZ: Boolean = abs(
            com.monkopedia.kcsg.ext.vvecmath.Plane.YZ_PLANE.normal
                .dot(plane.normal)
        ) < Plane.EPSILON
        if (!orthogonalToYZ && !foundProjectionPlane) {
            coordIndex1 = 1
            coordIndex2 = 2
            foundProjectionPlane = true
        }

        // see from http://www.java-gaming.org/index.php?topic=26013.0
        // see http://alienryderflex.com/polygon/
        // see http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        var j = vertices.size - 1
        var oddNodes = false
        val x = p[coordIndex1]
        val y = p[coordIndex2]
        var i: Int = 0
        while (i < vertices.size) {
            val xi = vertices[i].pos[coordIndex1]
            val yi = vertices[i].pos[coordIndex2]
            val xj = vertices[j].pos[coordIndex1]
            val yj = vertices[j].pos[coordIndex2]
            if ((
                yi < y && yj >= y ||
                    yj < y && yi >= y
                ) &&
                (xi <= x || xj <= x)
            ) {
                oddNodes = oddNodes xor (xi + (y - yi) / (yj - yi) * (xj - xi) < x)
            }
            j = i
            i++
        }
        return oddNodes
    }

    @Deprecated("")
    fun intersects(p: Polygon): Boolean {
        if (!bounds.intersects(p.bounds)) {
            return false
        }
        throw UnsupportedOperationException("Not implemented")
    }

    operator fun contains(p: Polygon): Boolean {
        for (v in p.vertices) {
            if (!contains(v.pos)) {
                return false
            }
        }
        return true
    }
    /**
     * @return the shared
     */
    var storage: PropertyStorage
        get() {
            if (shared == null) {
                shared = PropertyStorage()
            }
            return shared!!
        }
        set(storage) {
            shared = storage
        }

    override fun toString(): String {
        return "Poly($vertices)"
    }

    companion object {
        private val logger = Logger.tagged("KCSG.Polygon")
        /**
         * Decomposes the specified concave polygon into convex polygons.
         *
         * @param points the points that define the polygon
         * @return the decomposed concave polygon (list of convex polygons)
         */
        fun fromConcavePoints(vararg points: Vector3d): List<Polygon> {
            val p = fromPoints(*points)
            return PolygonUtil.concaveToConvex(p)
        }

        /**
         * Decomposes the specified concave polygon into convex polygons.
         *
         * @param points the points that define the polygon
         * @return the decomposed concave polygon (list of convex polygons)
         */
        fun fromConcavePoints(points: List<Vector3d>): List<Polygon> {
            val p = fromPoints(points)
            return PolygonUtil.concaveToConvex(p)
        }

        /**
         * Creates a polygon from the specified point list.
         *
         * @param points the points that define the polygon
         * @return a polygon defined by the specified point list
         */
        fun fromPoints(points: List<Vector3d>): Polygon {
            return fromPoints(points, PropertyStorage())
        }

        /**
         * Creates a polygon from the specified points.
         *
         * @param points the points that define the polygon
         * @return a polygon defined by the specified point list
         */
        fun fromPoints(vararg points: Vector3d): Polygon {
            return fromPoints(listOf(*points), PropertyStorage())
        }

        /**
         * Creates a polygon from the specified point list.
         *
         * @param points the points that define the polygon
         * @param shared
         * @param plane may be null
         * @return a polygon defined by the specified point list
         */
        fun fromPoints(
            points: List<Vector3d>,
            shared: PropertyStorage?
        ): Polygon {
            requireMinimumPointCount(points)
            var normal = Plane.createFromPoints(
                points[0],
                points[1],
                points[2]
            ).normal
            val vertices: MutableList<Vertex> = ArrayList()
            for (p in points) {
                val vec = p.copy()
                val vertex = Vertex(vec, normal)
                vertices.add(vertex)
            }
            return Polygon(vertices, shared)
        }

        private fun requireMinimumPointCount(points: List<Vector3d>) {
            if (points.size < 3) {
                throw RuntimeException(
                    "Invalid polygon: at least 3 vertices expected, got: " +
                        points.size
                )
            }
        }
    }
}

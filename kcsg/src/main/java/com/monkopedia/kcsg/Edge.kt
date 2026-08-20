/**
 * Edge.java
 *
 * Copyright 2014-2016 Michael Hoffer <info></info>@michaelhoffer.de>. All rights
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
import kotlin.math.sqrt

/**
 */
data class Edge(val p1: Vertex, val p2: Vertex) {
    private val direction: Vector3d = p2.pos.minus(p1.pos).normalized()

    /**
     * Determines whether the specified point lies on tthis edge.
     *
     * @param p point to check
     * @param TOL tolerance
     * @return `true` if the specified point lies on this line
     * segment; `false` otherwise
     */
    /**
     * Determines whether the specified point lies on tthis edge.
     *
     * @param p point to check
     * @return `true` if the specified point lies on this line
     * segment; `false` otherwise
     */
    fun contains(p: Vector3d, TOL: Double = Plane.EPSILON): Boolean {
        val x = p.x
        val x1 = p1.pos.x
        val x2 = p2.pos.x
        val y = p.y
        val y1 = p1.pos.y
        val y2 = p2.pos.y
        val z = p.z
        val z1 = p1.pos.z
        val z2 = p2.pos.z
        val ab = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1))
        val ap = sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1) + (z - z1) * (z - z1))
        val pb = sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y) + (z2 - z) * (z2 - z))
        return abs(ab - (ap + pb)) < TOL
    }

    /**
     * Edges compare equal regardless of direction (see [equals]), so the hash must be
     * symmetric in [p1] and [p2] — otherwise `Edge(a, b)` and `Edge(b, a)` would be equal
     * with different hash codes and land in different buckets of any hash container.
     * Addition rather than `xor` because `xor` collapses *every* degenerate edge
     * `Edge(a, a)` onto the same constant regardless of `a`.
     *
     * **This does not make [Edge] safe as a hash key.** [Vertex]/[Vector3d] compare with a
     * tolerance (`ext.vvecmath.Plane.TOL`, 1e-12 — not `com.monkopedia.kcsg.Plane.EPSILON`,
     * 1e-8) but hash their exact bit pattern, so vertices that are
     * equal-within-tolerance without being bit-identical have different hash codes, and an
     * [Edge] built from them inherits the inconsistency. `-0.0` vs `0.0` is the guaranteed
     * case; in practice it is trig residue in revolved primitives. See issue #65; until
     * that is fixed, edge frequency must be counted by scanning with [equals], not by a
     * hash-based grouping.
     */
    override fun hashCode(): Int = 497 + (p1.hashCode() + p2.hashCode())

    /**
     * Two edges are equal when they connect the same pair of vertices, in either order.
     * Direction independence is intentional and is relied upon by the boundary-edge
     * detection in `boundaryEdgesOfPlaneGroup`, which counts an edge and its reverse as
     * the same edge. The pairing must be genuine: matching both of `other`'s endpoints
     * against the *same* endpoint of this edge would make every degenerate edge
     * `Edge(a, a)` equal to every edge incident to `a`, which is not transitive.
     */
    override fun equals(other: Any?): Boolean {
        if (other !is Edge) {
            return false
        }
        return (p1 == other.p1 && p2 == other.p2) || (p1 == other.p2 && p2 == other.p1)
    }

    private fun getDirection(): Vector3d {
        return direction
    }

    /**
     * Returns the the point of this edge that is closest to the specified edge.
     *
     * **NOTE:** returns `null` if the edges are parallel
     *
     * @param e the edge to check
     * @return the the point of this edge that is closest to the specified edge
     */
    fun getClosestPointOrNull(e: Edge): Vector3d? {

        // algorithm from:
        // org.apache.commons.math3.geometry.euclidean.threed/Line.java.html
        val ourDir = getDirection()
        val cos = ourDir.dot(e.getDirection())
        val n = 1 - cos * cos
        if (n < Plane.EPSILON) {
            // the lines are parallel
            return null
        }
        val thisDelta = p2.pos.minus(p1.pos)
        val norm2This = thisDelta.magnitudeSq()
        val eDelta = e.p2.pos.minus(e.p1.pos)
        val norm2E = eDelta.magnitudeSq()

        // line points above the origin
        val thisZero = p1.pos.plus(thisDelta.times(-p1.pos.dot(thisDelta) / norm2This))
        val eZero = e.p1.pos.plus(eDelta.times(-e.p1.pos.dot(eDelta) / norm2E))
        val delta0 = eZero.minus(thisZero)
        val a = delta0.dot(direction)
        val b = delta0.dot(e.direction)
        val closestP = thisZero.plus(direction.times((a - b * cos) / n))
        return if (!contains(closestP)) {
            if (closestP.minus(p1.pos).magnitudeSq()
                < closestP.minus(p2.pos).magnitudeSq()
            ) {
                p1.pos
            } else {
                p2.pos
            }
        } else closestP
    }

    /**
     * Returns the intersection point between this edge and the specified edge.
     *
     * **NOTE:** returns `null` if the edges are parallel or if
     * the intersection point is not inside the specified edge segment
     *
     * @param e edge to intersect
     * @return the intersection point between this edge and the specified edge
     */
    fun getIntersectionOrNull(e: Edge): Vector3d? {
        val closestP = getClosestPointOrNull(e) ?: run {
            // edges are parallel
            return null
        }
        return if (e.contains(closestP)) {
            closestP
        } else {
            // intersection point outside of segment
            null
        }
    }

    companion object {
        private val logger = Logger.tagged("KCSG.Edge")

        private fun fromPolygon(poly: Polygon): List<Edge> {
            val result: MutableList<Edge> = ArrayList()
            for (i in poly.vertices.indices) {
                val e = Edge(poly.vertices[i], (poly.vertices[(i + 1) % poly.vertices.size]))
                result.add(e)
            }
            return result
        }

        fun toVertices(edges: List<Edge>): List<Vertex> {
            return edges.map { e: Edge -> e.p1 }
        }

        fun toPoints(edges: List<Edge>): List<Vector3d> {
            return edges.map { e: Edge -> e.p1.pos }
        }

        private fun toPolygon(points: List<Vector3d>, plane: Plane): Polygon {
            val p: Polygon = Polygon.fromPoints(points)
            p.vertices.forEach { vertex: Vertex ->
                vertex.normal = plane.normal.copy()
            }
            return p
        }

        fun toPolygons(boundaryEdges: List<Edge>, plane: Plane): List<Polygon> {
            require(boundaryEdges.isNotEmpty()) { "boundaryEdges must not be empty" }
            val boundaryPath: MutableList<Vector3d> = ArrayList()
            val used = BooleanArray(boundaryEdges.size)
            var edge = boundaryEdges[0]
            used[0] = true
            while (true) {
                val finalEdge = edge
                boundaryPath.add(finalEdge.p1.pos)
                val nextEdgeIndex = boundaryEdges.indexOfFirst { e: Edge ->
                    finalEdge.p2 == e.p1
                }
                require(nextEdgeIndex >= 0) {
                    "Boundary edges do not form a closed path."
                }
                if (used[nextEdgeIndex]) {
                    break
                }
                edge = boundaryEdges[nextEdgeIndex]
                used[nextEdgeIndex] = true
            }
            val result: MutableList<Polygon> = ArrayList()
            logger.info("#bnd-path-length: " + boundaryPath.size)
            result.add(toPolygon(boundaryPath, plane))
            return result
        }

        const val KEY_POLYGON_HOLES = "jcsg:edge:polygon-holes"

        fun boundaryPathsWithHoles(boundaryPaths: List<Polygon>): List<Polygon> {
            val result = boundaryPaths.map { p: Polygon -> p.copy() }
            val parents: MutableList<List<Int>> = ArrayList()
            val isHole = BooleanArray(result.size)
            for (i in result.indices) {
                val p1 = result[i]
                val parentsOfI: MutableList<Int> = ArrayList()
                parents.add(parentsOfI)
                for (j in result.indices) {
                    val p2 = result[j]
                    if (i != j) {
                        if (p2.contains(p1)) {
                            parentsOfI.add(j)
                        }
                    }
                }
                isHole[i] = parentsOfI.size % 2 != 0
            }
            val parent = IntArray(result.size)
            for (i in parent.indices) {
                parent[i] = -1
            }
            for (i in parents.indices) {
                val par = parents[i]
                var max = 0
                var maxIndex = 0
                for (pIndex in par) {
                    val pSize = parents[pIndex].size
                    if (max < pSize) {
                        max = pSize
                        maxIndex = pIndex
                    }
                }
                parent[i] = maxIndex
                if (!isHole[maxIndex] && isHole[i]) {
                    var holes: MutableList<Polygon>
                    val holesOpt = result[maxIndex]
                        .storage.getValue<MutableList<Polygon>>(KEY_POLYGON_HOLES)
                    if (holesOpt != null) {
                        holes = holesOpt
                    } else {
                        holes = ArrayList()
                        result[maxIndex].storage[KEY_POLYGON_HOLES] = holes
                    }
                    holes.add(result[i])
                }
            }
            return result
        }

        /**
         * Returns a list of all boundary paths.
         *
         * @param boundaryEdges boundary edges (all paths must be closed)
         * @return
         */
        private fun boundaryPaths(boundaryEdges: List<Edge>): List<Polygon> {
            val result: MutableList<Polygon> = ArrayList()
            val used = BooleanArray(boundaryEdges.size)
            var startIndex = 0
            var edge = boundaryEdges[startIndex]
            used[startIndex] = true
            startIndex = 1
            while (startIndex > 0) {
                val boundaryPath: MutableList<Vector3d> = ArrayList()
                while (true) {
                    val finalEdge = edge
                    boundaryPath.add(finalEdge.p1.pos)
                    val nextEdgeResult =
                        boundaryEdges.firstOrNull { e: Edge -> finalEdge.p2 == e.p1 }
                    if (nextEdgeResult == null) {
                        logger.error(
                            "ERROR: unclosed path:" +
                                " no edge found with " + finalEdge.p2
                        )
                        break
                    }
                    val nextEdgeIndex = boundaryEdges.indexOf(nextEdgeResult)
                    if (used[nextEdgeIndex]) {
                        break
                    }
                    edge = nextEdgeResult
                    logger.info("-> edge: " + edge.p1.pos)
                    used[nextEdgeIndex] = true
                }
                if (boundaryPath.size < 3) {
                    break
                }
                result.add(Polygon.fromPoints(boundaryPath))
                startIndex = nextUnused(used)
                if (startIndex > 0) {
                    edge = boundaryEdges[startIndex]
                    used[startIndex] = true
                }
            }
            logger.info("paths: " + result.size)
            return result
        }

        /**
         * Returns the next unused index as specified in the given boolean array.
         *
         * @param usage the usage array
         * @return the next unused index or a value &lt; 0 if all indices are used
         */
        private fun nextUnused(usage: BooleanArray): Int {
            for (i in usage.indices) {
                if (!usage[i]) {
                    return i
                }
            }
            return -1
        }

        fun boundaryPolygons(csg: CSG): MutableList<Polygon> {
            val result: MutableList<Polygon> = ArrayList()
            for (polygonGroup in searchPlaneGroups(csg.polygons)) {
                result.addAll(boundaryPolygonsOfPlaneGroup(polygonGroup))
            }
            return result
        }

        private fun boundaryEdgesOfPlaneGroup(planeGroup: List<Polygon>): List<Edge> {
            val edges: MutableList<Edge> =
                ArrayList()

            planeGroup.forEach { p: Polygon ->
                edges.addAll(fromPolygon(p))
            }

            // find potential boundary edges, i.e., edges that occur once (freq=1)
            //
            // This O(n^2) scan must NOT be replaced with a hash-based frequency count
            // (`edges.groupingBy { it }.eachCount()`). Vertex/Vector3d compare with a
            // tolerance (ext.vvecmath.Plane.TOL, 1e-12) but hash exact bits, so vertices
            // that are equal-within-tolerance but not bit-identical hash into different
            // buckets and split one edge in two. `-0.0` vs `0.0` is the guaranteed case;
            // in practice it is trig residue in revolved primitives — a cylinder's seam
            // vertex is emitted once at angle 0, where sin is exactly 0.0, and once at
            // angle 2*PI, where sin is 2.4e-16 in magnitude rather than 0. Measured on
            // Cylinder(1.0, 2.0, 8): zero -0.0 coordinates, but two such pairs
            // ([1.0, 0.0, z] ~ [1.0, 2.4492935982947064E-16, z], one per cap circle),
            // which is why it drops from 10 boundary polygons to 9 under groupingBy.
            // Every primitive swept through a full turn has the same seam. See issue #65.
            val potentialBoundaryEdges: MutableList<Edge> =
                ArrayList()
            edges.forEach { e: Edge ->
                val count = edges.count { it == e }
                if (count == 1) {
                    potentialBoundaryEdges.add(e)
                }
            }

            // now find "false boundary" edges end remove them from the
            // boundary-edge-list
            //
            // thanks to Susanne Höllbacher for the idea :)
            return potentialBoundaryEdges.filter { be: Edge ->
                !edges.any { e: Edge -> falseBoundaryEdgeSharedWithOtherEdge(be, e) }
            }
        }

        private fun boundaryPolygonsOfPlaneGroup(
            planeGroup: List<Polygon>
        ): List<Polygon> {
            val polygons = boundaryPathsWithHoles(
                boundaryPaths(boundaryEdgesOfPlaneGroup(planeGroup))
            )
            val result: MutableList<Polygon> = ArrayList(polygons.size)
            for (p in polygons) {
                val holesOfPresult = p.storage.getValue<List<Polygon>>(KEY_POLYGON_HOLES)
                if (holesOfPresult == null) {
                    result.add(p)
                } else {
                    result.addAll(PolygonUtil.concaveToConvex(p))
                }
            }
            return result
        }

        private fun falseBoundaryEdgeSharedWithOtherEdge(fbe: Edge, e: Edge): Boolean {

            // we don't consider edges with shared end-points since we are only
            // interested in "false-boundary-edge"-cases
            val sharedEndPoints =
                e.p1.pos == fbe.p1.pos || e.p1.pos == fbe.p2.pos || e.p2.pos == fbe.p1.pos || e.p2.pos == fbe.p2.pos
            return if (sharedEndPoints) {
                false
            } else fbe.contains(e.p1.pos) || fbe.contains(e.p2.pos)
        }

        private fun searchPlaneGroups(polygons: List<Polygon>): List<List<Polygon>> {
            val planeGroups: MutableList<List<Polygon>> = ArrayList()
            val used = BooleanArray(polygons.size)
            logger.info("#polys: " + polygons.size)
            for (pOuterI in polygons.indices) {
                if (used[pOuterI]) {
                    continue
                }
                val pOuter = polygons[pOuterI]
                val otherPolysInPlane: MutableList<Polygon> = ArrayList()
                otherPolysInPlane.add(pOuter)
                for (pInnerI in polygons.indices) {
                    val pInner = polygons[pInnerI]
                    if (pOuter == pInner) {
                        continue
                    }
                    val nOuter = pOuter.csgPlane.normal
                    val nInner = pInner.csgPlane.normal

                    // TODO do we need radians or degrees?
                    val angle = nOuter.angle(nInner)

                    if (angle < 0.01 /*&& abs(pOuter.plane.dist - pInner.plane.dist) < 0.1*/) {
                        otherPolysInPlane.add(pInner)
                        used[pInnerI] = true
                        logger.info("used: $pOuterI -> $pInnerI")
                    }
                }
                if (otherPolysInPlane.isNotEmpty()) {
                    planeGroups.add(otherPolysInPlane)
                }
            }
            return planeGroups
        }
    }
}

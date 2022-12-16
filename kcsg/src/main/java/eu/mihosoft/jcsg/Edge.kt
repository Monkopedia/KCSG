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
package eu.mihosoft.jcsg

import eu.mihosoft.jcsg.ext.org.poly2tri.PolygonUtil
import eu.mihosoft.vvecmath.Vector3d
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.sqrt

/**
 *
 * @author miho
 */
class Edge(val p1: Vertex, val p2: Vertex) : Cloneable {
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
    @JvmOverloads
    fun contains(p: Vector3d, TOL: Double = Plane.EPSILON): Boolean {
        val x = p.x()
        val x1 = p1.pos.x()
        val x2 = p2.pos.x()
        val y = p.y()
        val y1 = p1.pos.y()
        val y2 = p2.pos.y()
        val z = p.z()
        val z1 = p1.pos.z()
        val z2 = p2.pos.z()
        val ab = sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1))
        val ap = sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1) + (z - z1) * (z - z1))
        val pb = sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y) + (z2 - z) * (z2 - z))
        return abs(ab - (ap + pb)) < TOL
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 71 * hash + Objects.hashCode(p1)
        hash = 71 * hash + Objects.hashCode(p2)
        return hash
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as Edge
        if (!(p1 == other.p1 || p2 == other.p1)) {
            return false
        }
        return p2 == other.p2 || p1 == other.p2
    }

    private fun getDirection(): Vector3d {
        return direction
    }

    /**
     * Returns the the point of this edge that is closest to the specified edge.
     *
     * **NOTE:** returns an empty optional if the edges are parallel
     *
     * @param e the edge to check
     * @return the the point of this edge that is closest to the specified edge
     */
    fun getClosestPoint(e: Edge): Optional<Vector3d> {

        // algorithm from:
        // org.apache.commons.math3.geometry.euclidean.threed/Line.java.html
        val ourDir = getDirection()
        val cos = ourDir.dot(e.getDirection())
        val n = 1 - cos * cos
        if (n < Plane.EPSILON) {
            // the lines are parallel
            return Optional.empty()
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
                Optional.of(p1.pos)
            } else {
                Optional.of(p2.pos)
            }
        } else Optional.of(closestP)
    }

    /**
     * Returns the intersection point between this edge and the specified edge.
     *
     * **NOTE:** returns an empty optional if the edges are parallel or if
     * the intersection point is not inside the specified edge segment
     *
     * @param e edge to intersect
     * @return the intersection point between this edge and the specified edge
     */
    fun getIntersection(e: Edge): Optional<Vector3d> {
        val closestPOpt = getClosestPoint(e)
        if (!closestPOpt.isPresent) {
            // edges are parallel
            return Optional.empty()
        }
        val closestP = closestPOpt.get()
        return if (e.contains(closestP)) {
            closestPOpt
        } else {
            // intersection point outside of segment
            Optional.empty()
        }
    }

    companion object {
        //    /**
        //     * @param p2 the p2 to set
        //     */
        //    public void setP2(Vertex p2) {
        //        this.p2 = p2;
        //    }
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

//        List<Vector3d> points = edges.stream().().map(e -> e.p1.pos).
//                collect(Collectors.toList());
            val p: Polygon = Polygon.fromPoints(points)
            p.vertices.forEach { vertex: Vertex ->
                vertex.normal = plane.normal.clone()
            }

//        // we try to detect wrong orientation by comparing normals
//        if (p.plane.normal.angle(plane.normal) > 0.1) {
//            p.flip();
//        }
            return p
        }

        fun toPolygons(boundaryEdges: List<Edge>, plane: Plane): List<Polygon> {
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
                if (used[nextEdgeIndex]) {
//                System.out.println("nexIndex: " + nextEdgeIndex);
                    break
                }
                //            System.out.print("edge: " + edge.p2.pos);
                edge = boundaryEdges[nextEdgeIndex]
                //            System.out.println("-> edge: " + edge.p1.pos);
                used[nextEdgeIndex] = true
            }
            val result: MutableList<Polygon> = ArrayList()
            println("#bnd-path-length: " + boundaryPath.size)
            result.add(toPolygon(boundaryPath, plane))
            return result
        }

        const val KEY_POLYGON_HOLES = "jcsg:edge:polygon-holes"

        @kotlin.jvm.JvmStatic
        fun boundaryPathsWithHoles(boundaryPaths: List<Polygon>): List<Polygon> {
            val result = boundaryPaths.map { p: Polygon -> p.clone() }
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
                    print("edge: " + edge.p2.pos)
                    val nextEdgeResult =
                        boundaryEdges.firstOrNull { e: Edge -> finalEdge.p2 == e.p1 }
                    if (nextEdgeResult == null) {
                        println(
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
                    println("-> edge: " + edge.p1.pos)
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
            println("paths: " + result.size)
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

        fun polygons(boundaryEdges: List<Edge>, plane: Plane): List<Polygon> {
            val boundaryPath: MutableList<Vector3d> = ArrayList()
            val used = BooleanArray(boundaryEdges.size)
            var edge = boundaryEdges[0]
            used[0] = true
            while (true) {
                val finalEdge = edge
                boundaryPath.add(finalEdge.p1.pos)
                val nextEdgeIndex = boundaryEdges.indexOfFirst { e: Edge -> finalEdge.p2 == e.p1 }
                if (used[nextEdgeIndex]) {
//                System.out.println("nexIndex: " + nextEdgeIndex);
                    break
                }
                //            System.out.print("edge: " + edge.p2.pos);
                edge = boundaryEdges[nextEdgeIndex]
                //            System.out.println("-> edge: " + edge.p1.pos);
                used[nextEdgeIndex] = true
            }
            val result: MutableList<Polygon> = ArrayList()
            println("#bnd-path-length: " + boundaryPath.size)
            result.add(toPolygon(boundaryPath, plane))
            return result
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

            val pStream: Stream<Polygon> = if (planeGroup.size > 200) {
                planeGroup.parallelStream()
            } else {
                planeGroup.stream()
            }
            pStream.map { p: Polygon ->
                fromPolygon(p)
            }.forEach { pEdges: List<Edge> ->
                edges.addAll(pEdges)
            }

            val edgeStream: Stream<Edge> = if (edges.size > 200) {
                edges.parallelStream()
            } else {
                edges.stream()
            }

            // find potential boundary edges, i.e., edges that occur once (freq=1)
            val potentialBoundaryEdges: MutableList<Edge> =
                ArrayList()
            edgeStream.forEachOrdered { e: Edge ->
                val count = Collections.frequency(edges, e)
                if (count == 1) {
                    potentialBoundaryEdges.add(e)
                }
            }

            // now find "false boundary" edges end remove them from the
            // boundary-edge-list
            //
            // thanks to Susanne Höllbacher for the idea :)
            val bndEdgeStream: Stream<Edge> = if (potentialBoundaryEdges.size > 200) {
                potentialBoundaryEdges.parallelStream()
            } else {
                potentialBoundaryEdges.stream()
            }

            //
//        System.out.println("#bnd-edges: " + realBndEdges.size()
//                + ",#edges: " + edges.size()
//                + ", #del-bnd-edges: " + (boundaryEdges.size() - realBndEdges.size()));
            return bndEdgeStream.filter { be: Edge ->
                !edges.any { e: Edge -> falseBoundaryEdgeSharedWithOtherEdge(be, e) }
            }.collect(Collectors.toList())
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
            println("#polys: " + polygons.size)
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

//                System.out.println("angle: " + angle + " between " + pOuterI+" -> " + pInnerI);
                    if (angle < 0.01 /*&& abs(pOuter.plane.dist - pInner.plane.dist) < 0.1*/) {
                        otherPolysInPlane.add(pInner)
                        used[pInnerI] = true
                        println("used: $pOuterI -> $pInnerI")
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

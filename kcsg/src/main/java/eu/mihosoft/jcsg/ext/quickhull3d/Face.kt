/*
  * Copyright John E. Lloyd, 2003. All rights reserved. Permission
  * to use, copy, and modify, without fee, is granted for non-commercial
  * and research purposes, provided that this copyright notice appears
  * in all copies.
  *
  * This  software is distributed "as is", without any warranty, including
  * any implied warranty of merchantability or fitness for a particular
  * use. The authors assume no responsibility for, and shall not be liable
  * for, any special, indirect, or consequential damages, or any damages
  * whatsoever, arising out of or in connection with the use of this
  * software.
  */
package eu.mihosoft.jcsg.ext.quickhull3d

import eu.mihosoft.jcsg.ext.vvecmath.Vector3d
import eu.mihosoft.jcsg.ext.vvecmath.Vector3d.Companion.ZERO
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Basic triangular face used to form the hull.
 *
 *
 * The information stored for each face consists of a planar
 * normal, a planar offset, and a doubly-linked list of three [HalfEdges](HalfEdge) which surround the face in a
 * counter-clockwise direction.
 *
 * @author John E. Lloyd, Fall 2004
 */
internal class Face {
    var he0: HalfEdge? = null
    var normal: Vector3d = Vector3d.ZERO
        private set
    var area = 0.0
    var centroid: Point3d = Point3d.ZERO
        private set
    private var planeOffset = 0.0
    var index = 0
    private var numVerts = 0
    var next: Face? = null
    var mark = VISIBLE
    var outside: Vertex? = null

    private fun computeCentroid(): Point3d {
        var centroid = Vector3d.ZERO
        var he = he0
        do {
            centroid += he!!.head().pnt
            he = he.next
        } while (he !== he0)
        return centroid * (1 / numVerts.toDouble())
    }

    private fun computeNormal(minArea: Double): Vector3d {
        var normal = computeNormal()
        if (area < minArea) {
            // make the normal more robust by removing
            // components parallel to the longest edge
            var hedgeMax: HalfEdge? = null
            var lenSqrMax = 0.0
            var hedge = he0
            do {
                val lenSqr = hedge!!.lengthSquared()
                if (lenSqr > lenSqrMax) {
                    hedgeMax = hedge
                    lenSqrMax = lenSqr
                }
                hedge = hedge.next
            } while (hedge !== he0)
            val p2 = hedgeMax!!.head().pnt
            val p1 = hedgeMax.tail()!!.pnt
            val lenMax = sqrt(lenSqrMax)
            val ux = (p2.x - p1.x) / lenMax
            val uy = (p2.y - p1.y) / lenMax
            val uz = (p2.z - p1.z) / lenMax
            val dot = normal.x * ux + normal.y * uy + normal.z * uz
            normal = normal.minus(
                dot * ux,
                dot * uy,
                dot * uz
            ).normalized()
        }
        return normal
    }

    private fun computeNormal(): Vector3d {
        var he1 = he0!!.next
        var he2 = he1!!.next
        val p0 = he0!!.head().pnt
        var p2 = he1.head().pnt
        var d2x = p2.x - p0.x
        var d2y = p2.y - p0.y
        var d2z = p2.z - p0.z
        var normal = ZERO
        numVerts = 2
        while (he2 !== he0) {
            val d1x = d2x
            val d1y = d2y
            val d1z = d2z
            p2 = he2!!.head().pnt
            d2x = p2.x - p0.x
            d2y = p2.y - p0.y
            d2z = p2.z - p0.z
            normal += Vector3d.xyz(
                d1y * d2z - d1z * d2y,
                d1z * d2x - d1x * d2z,
                d1x * d2y - d1y * d2x
            )
            he1 = he2
            he2 = he2.next
            numVerts++
        }
        area = normal.magnitude()
        return normal * (1 / area)
    }

    private fun computeNormalAndCentroid() {
        normal = computeNormal()
        centroid = computeCentroid()
        planeOffset = normal.dot(centroid)
        var numv = 0
        var he = he0
        do {
            numv++
            he = he!!.next
        } while (he !== he0)
        if (numv != numVerts) {
            throw InternalErrorException(
                "face $vertexString numVerts=$numVerts should be $numv"
            )
        }
    }

    private fun computeNormalAndCentroid(minArea: Double) {
        normal = computeNormal(minArea)
        centroid = computeCentroid()
        planeOffset = normal.dot(centroid)
    }

    /**
     * Gets the i-th half-edge associated with the face.
     *
     * @param i the half-edge index, in the range 0-2.
     * @return the half-edge
     */
    fun getEdge(i: Int): HalfEdge? {
        var i = i
        var he = he0
        while (i > 0) {
            he = he!!.next
            i--
        }
        while (i < 0) {
            he = he!!.prev
            i++
        }
        return he
    }

    val firstEdge: HalfEdge?
        get() {
            return he0
        }

    /**
     * Finds the half-edge within this face which has
     * tail `vt` and head `vh`.
     *
     * @param vt tail point
     * @param vh head point
     * @return the half-edge, or null if none is found.
     */
    fun findEdge(vt: Vertex, vh: Vertex): HalfEdge? {
        var he = he0
        do {
            if (he!!.head() === vh && he!!.tail() === vt) {
                return he
            }
            he = he!!.next
        } while (he !== he0)
        return null
    }

    /**
     * Computes the distance from a point p to the plane of
     * this face.
     *
     * @param p the point
     * @return distance from the point to the plane
     */
    fun distanceToPlane(p: Point3d?): Double {
        return normal.x * p!!.x + normal.y * p.y + normal.z * p.z - planeOffset
    }

    fun numVertices(): Int {
        return numVerts
    }

    val vertexString: String?
        get() {
            var s: String? = null
            var he = he0
            do {
                if (s == null) {
                    s = "" + he!!.head().index
                } else {
                    s += " " + he!!.head().index
                }
                he = he.next
            } while (he !== he0)
            return s
        }

    fun getVertexIndices(idxs: IntArray) {
        var he = he0
        var i = 0
        do {
            idxs[i++] = he!!.head().index
            he = he.next
        } while (he !== he0)
    }

    private fun connectHalfEdges(
        hedgePrev: HalfEdge?,
        hedge: HalfEdge?
    ): Face? {
        var discardedFace: Face? = null
        if (hedgePrev!!.oppositeFace() === hedge!!.oppositeFace()) { // then there is a redundant edge that we can get rid off
            val oppFace = hedge!!.oppositeFace()
            val hedgeOpp: HalfEdge?
            if (hedgePrev === he0) {
                he0 = hedge
            }
            if (oppFace!!.numVertices() == 3) { // then we can get rid of the opposite face altogether
                hedgeOpp = hedge.opposite!!.prev!!.opposite
                oppFace.mark = DELETED
                discardedFace = oppFace
            } else {
                hedgeOpp = hedge.opposite!!.next
                if (oppFace.he0 === hedgeOpp!!.prev) {
                    oppFace.he0 = hedgeOpp
                }
                hedgeOpp!!.prev = hedgeOpp.prev!!.prev
                hedgeOpp.prev!!.next = hedgeOpp
            }
            hedge.prev = hedgePrev!!.prev
            hedge.prev!!.next = hedge
            hedge.opposite = hedgeOpp
            hedgeOpp!!.opposite = hedge

            // oppFace was modified, so need to recompute
            oppFace.computeNormalAndCentroid()
        } else {
            hedgePrev!!.next = hedge
            hedge!!.prev = hedgePrev
        }
        return discardedFace
    }

    fun checkConsistency() {
        // do a sanity check on the face
        var hedge = he0
        var maxd = 0.0
        var numv = 0
        if (numVerts < 3) {
            throw InternalErrorException(
                "degenerate face: $vertexString"
            )
        }
        do {
            val hedgeOpp = hedge!!.opposite
            if (hedgeOpp == null) {
                throw InternalErrorException(
                    "face " + vertexString + ": " +
                        "unreflected half edge " + hedge.vertexString
                )
            } else if (hedgeOpp.opposite !== hedge) {
                throw InternalErrorException(
                    "face " + vertexString + ": " +
                        "opposite half edge " + hedgeOpp.vertexString +
                        " has opposite " +
                        hedgeOpp.opposite!!.vertexString
                )
            }
            if (hedgeOpp.head() !== hedge.tail() ||
                hedge.head() !== hedgeOpp.tail()
            ) {
                throw InternalErrorException(
                    "face " + vertexString + ": " +
                        "half edge " + hedge.vertexString +
                        " reflected by " + hedgeOpp.vertexString
                )
            }
            val oppFace = hedgeOpp.face
            if (oppFace == null) {
                throw InternalErrorException(
                    "face " + vertexString + ": " +
                        "no face on half edge " + hedgeOpp.vertexString
                )
            } else if (oppFace.mark == DELETED) {
                throw InternalErrorException(
                    "face " + vertexString + ": " +
                        "opposite face " + oppFace.vertexString +
                        " not on hull"
                )
            }
            val d = abs(distanceToPlane(hedge.head().pnt))
            if (d > maxd) {
                maxd = d
            }
            numv++
            hedge = hedge.next
        } while (hedge !== he0)
        if (numv != numVerts) {
            throw InternalErrorException(
                "face $vertexString numVerts=$numVerts should be $numv"
            )
        }
    }

    fun mergeAdjacentFace(
        hedgeAdj: HalfEdge,
        discarded: Array<Face?>
    ): Int {
        val oppFace = hedgeAdj.oppositeFace()
        var numDiscarded = 0
        discarded[numDiscarded++] = oppFace
        oppFace!!.mark = DELETED
        val hedgeOpp = hedgeAdj.opposite
        var hedgeAdjPrev = hedgeAdj.prev
        var hedgeAdjNext = hedgeAdj.next
        var hedgeOppPrev = hedgeOpp!!.prev
        var hedgeOppNext = hedgeOpp.next
        while (hedgeAdjPrev!!.oppositeFace() === oppFace) {
            hedgeAdjPrev = hedgeAdjPrev!!.prev
            hedgeOppNext = hedgeOppNext!!.next
        }
        while (hedgeAdjNext!!.oppositeFace() === oppFace) {
            hedgeOppPrev = hedgeOppPrev!!.prev
            hedgeAdjNext = hedgeAdjNext!!.next
        }
        var hedge: HalfEdge?
        hedge = hedgeOppNext
        while (hedge !== hedgeOppPrev!!.next) {
            hedge!!.face = this
            hedge = hedge.next
        }
        if (hedgeAdj === he0) {
            he0 = hedgeAdjNext
        }

        // handle the half edges at the head
        var discardedFace: Face? = connectHalfEdges(hedgeOppPrev, hedgeAdjNext)
        if (discardedFace != null) {
            discarded[numDiscarded++] = discardedFace
        }

        // handle the half edges at the tail
        discardedFace = connectHalfEdges(hedgeAdjPrev, hedgeOppNext)
        if (discardedFace != null) {
            discarded[numDiscarded++] = discardedFace
        }
        computeNormalAndCentroid()
        checkConsistency()
        return numDiscarded
    }

    private fun areaSquared(hedge0: HalfEdge, hedge1: HalfEdge): Double {
        // return the squared area of the triangle defined
        // by the half edge hedge0 and the point at the
        // head of hedge1.
        val p0 = hedge0.tail()!!.pnt
        val p1 = hedge0.head().pnt
        val p2 = hedge1.head().pnt
        val dx1 = p1.x - p0.x
        val dy1 = p1.y - p0.y
        val dz1 = p1.z - p0.z
        val dx2 = p2.x - p0.x
        val dy2 = p2.y - p0.y
        val dz2 = p2.z - p0.z
        val x = dy1 * dz2 - dz1 * dy2
        val y = dz1 * dx2 - dx1 * dz2
        val z = dx1 * dy2 - dy1 * dx2
        return x * x + y * y + z * z
    }

    fun triangulate(newFaces: FaceList, minArea: Double) {
        var hedge: HalfEdge?
        if (numVertices() < 4) {
            return
        }
        val v0 = he0!!.head()
        val prevFace: Face? = null
        hedge = he0!!.next
        var oppPrev = hedge!!.opposite
        var face0: Face? = null
        hedge = hedge.next
        while (hedge !== he0!!.prev) {
            val face = createTriangle(v0, hedge!!.prev!!.head(), hedge.head(), minArea)
            face.he0!!.next!!.opposite = (oppPrev!!)
            face.he0!!.prev!!.opposite = (hedge.opposite!!)
            oppPrev = face.he0
            newFaces.add(face)
            if (face0 == null) {
                face0 = face
            }
            hedge = hedge.next
        }
        hedge = HalfEdge(he0!!.prev!!.prev!!.head(), this)
        hedge.opposite = (oppPrev!!)
        hedge.prev = he0
        hedge.prev!!.next = hedge
        hedge.next = he0!!.prev
        hedge.next!!.prev = hedge
        computeNormalAndCentroid(minArea)
        checkConsistency()
        var face = face0
        while (face != null) {
            face.checkConsistency()
            face = face.next
        }
    }

    companion object {
        const val VISIBLE = 1
        const val NON_CONVEX = 2
        const val DELETED = 3

        /**
         * Constructs a triangule Face from vertices v0, v1, and v2.
         *
         * @param v0 first vertex
         * @param v1 second vertex
         * @param v2 third vertex
         */
        @JvmOverloads
        fun createTriangle(
            v0: Vertex,
            v1: Vertex,
            v2: Vertex,
            minArea: Double = 0.0
        ): Face {
            val face = Face()
            val he0 = HalfEdge(v0, face)
            val he1 = HalfEdge(v1, face)
            val he2 = HalfEdge(v2, face)
            he0.prev = he2
            he0.next = he1
            he1.prev = he0
            he1.next = he2
            he2.prev = he1
            he2.next = he0
            face.he0 = he0

            // compute the normal and offset
            face.computeNormalAndCentroid(minArea)
            return face
        }

        fun create(vtxArray: Array<Vertex>, indices: IntArray): Face {
            val face = Face()
            var hePrev: HalfEdge? = null
            for (i in indices.indices) {
                val he = HalfEdge(vtxArray[indices[i]], face)
                if (hePrev != null) {
                    he.prev = (hePrev)
                    hePrev.next = (he)
                } else {
                    face.he0 = he
                }
                hePrev = he
            }
            face.he0!!.prev = (hePrev)
            hePrev!!.next = (face.he0)

            // compute the normal and offset
            face.computeNormalAndCentroid()
            return face
        }
    }

    init {
        mark = VISIBLE
    }
}

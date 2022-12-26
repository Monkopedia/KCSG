/**
 * Copyright John E. Lloyd, 2004. All rights reserved. Permission to use,
 * copy, modify and redistribute is granted, provided that this copyright
 * notice is retained and the author is given credit whenever appropriate.
 *
 * This  software is distributed "as is", without any warranty, including
 * any implied warranty of merchantability or fitness for a particular
 * use. The author assumes no responsibility for, and shall not be liable
 * for, any special, indirect, or consequential damages, or any damages
 * whatsoever, arising out of or in connection with the use of this
 * software.
 */
@file:Suppress("ControlFlowWithEmptyBody")

package com.monkopedia.kcsg.ext.quickhull3d

import com.monkopedia.kcsg.Vector3d
import com.monkopedia.kcsg.ext.quickhull3d.Face.Companion.createTriangle
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Computes the convex hull of a set of three dimensional points.
 *
 *
 * The algorithm is a three dimensional implementation of Quickhull, as
 * described in Barber, Dobkin, and Huhdanpaa, [ ``The Quickhull
 * Algorithm for Convex Hulls''](http://citeseer.ist.psu.edu/barber96quickhull.html) (ACM Transactions on Mathematical Software,
 * Vol. 22, No. 4, December 1996), and has a complexity of O(n log(n)) with
 * respect to the number of points. A well-known C implementation of Quickhull
 * that works for arbitrary dimensions is provided by [qhull](http://www.qhull.org).
 *
 *
 * A hull is constructed by providing a set of points
 * to either a constructor or a
 * [build][.build] method. After
 * the hull is built, its vertices and faces can be retrieved
 * using [ getVertices][.getVertices] and [getFaces][.getFaces].
 * A typical usage might look like this:
 * <pre>
 * // x y z coordinates of 6 points
 * Point3d[] points = new Point3d[]
 * { new Point3d (0.0,  0.0,  0.0),
 * new Point3d (1.0,  0.5,  0.0),
 * new Point3d (2.0,  0.0,  0.0),
 * new Point3d (0.5,  0.5,  0.5),
 * new Point3d (0.0,  0.0,  2.0),
 * new Point3d (0.1,  0.2,  0.3),
 * new Point3d (0.0,  2.0,  0.0),
 * };
 *
 * QuickHull3D hull = new QuickHull3D();
 * hull.build (points);
 *
 * System.out.println ("Vertices:");
 * Point3d[] vertices = hull.getVertices();
 * for (int i = 0; i < vertices.length; i++)
 * { Point3d pnt = vertices[i];
 * System.out.println (pnt.x + " " + pnt.y + " " + pnt.z);
 * }
 *
 * System.out.println ("Faces:");
 * int[][] faceIndices = hull.getFaces();
 * for (int i = 0; i < faceIndices.length; i++)
 * { for (int k = 0; k < faceIndices[i].length; k++)
 * { System.out.print (faceIndices[i][k] + " ");
 * }
 * System.out.println ("");
 * }
</pre> *
 * As a convenience, there are also [build][.build]
 * and [getVertex][.getVertices] methods which
 * pass point information using an array of doubles.
 *
 * <h3><a name=distTol>Robustness</a></h3> Because this algorithm uses floating
 * point arithmetic, it is potentially vulnerable to errors arising from
 * numerical imprecision.  We address this problem in the same way as [qhull](http://www.qhull.org), by merging faces whose edges are not
 * clearly convex. A face is convex if its edges are convex, and an edge is
 * convex if the centroid of each adjacent plane is clearly *below* the
 * plane of the other face. The centroid is considered below a plane if its
 * distance to the plane is less than the negative of a [ ][.getDistanceTolerance].  This tolerance represents the
 * smallest distance that can be reliably computed within the available numeric
 * precision. It is normally computed automatically from the point data,
 * although an application may [set this][.setExplicitDistanceTolerance].
 *
 *
 * Numerical problems are more likely to arise in situations where data
 * points lie on or within the faces or edges of the convex hull. We have
 * tested QuickHull3D for such situations by computing the convex hull of a
 * random point set, then adding additional randomly chosen points which lie
 * very close to the hull vertices and edges, and computing the convex
 * hull again. The hull is deemed correct if [check][.check] returns
 * `true`.  These tests have been successful for a large number of
 * trials and so we are confident that QuickHull3D is reasonably robust.
 *
 * <h3>Merged Faces</h3> The merging of faces means that the faces returned by
 * QuickHull3D may be convex polygons instead of triangles. If triangles are
 * desired, the application may [triangulate][.triangulate] the faces, but
 * it should be noted that this may result in triangles which are very small or
 * thin and hence difficult to perform reliable convexity tests on. In other
 * words, triangulating a merged face is likely to restore the numerical
 * problems which the merging process removed. Hence is it
 * possible that, after triangulation, [check][.check] will fail (the same
 * behavior is observed with triangulated output from [qhull](http://www.qhull.org)).
 *
 * <h3>Degenerate Input</h3>It is assumed that the input points
 * are non-degenerate in that they are not coincident, colinear, or
 * colplanar, and thus the convex hull has a non-zero volume.
 * If the input points are detected to be degenerate within
 * the [distance tolerance][.getDistanceTolerance], an
 * IllegalArgumentException will be thrown.
 */
internal class QuickHull3D {
    private var findIndex = -1

    // estimated size of the point set
    private var charLength = 0.0
    private var pointBuffer = emptyArray<Vertex>()
    private var vertexPointIndices = IntArray(0)
    private val discardedFaces = arrayOfNulls<Face>(3)
    private val maxVtxs = arrayOfNulls<Vertex>(3)
    private val minVtxs = arrayOfNulls<Vertex>(3)
    private var faces: Vector<Face> = Vector<Face>(16)
    private var horizon: Vector<HalfEdge> = Vector<HalfEdge>(16)
    private val newFaces = FaceList()
    private val unclaimed = VertexList()
    private val claimed = VertexList()
    private var numVertices = 0
    private var numFaces = 0
    private var numPoints = 0
    private var explicitTolerance = AUTOMATIC_TOLERANCE
    private var tolerance = 0.0

    /**
     * Returns the distance tolerance that was used for the most recently
     * computed hull. The distance tolerance is used to determine when
     * faces are unambiguously convex with respect to each other, and when
     * points are unambiguously above or below a face plane, in the
     * presence of [numerical imprecision](#distTol). Normally,
     * this tolerance is computed automatically for each set of input
     * points, but it can be set explicitly by the application.
     *
     * @return distance tolerance
     * @see QuickHull3D.setExplicitDistanceTolerance
     */
    val distanceTolerance: Double
        get() {
            return tolerance
        }

    /**
     * Sets an explicit distance tolerance for convexity tests.
     * If [AUTOMATIC_TOLERANCE][.AUTOMATIC_TOLERANCE]
     * is specified (the default), then the tolerance will be computed
     * automatically from the point data.
     *
     * @param tol explicit tolerance
     * @see .getDistanceTolerance
     */
    private fun setExplicitDistanceTolerance(tol: Double) {
        explicitTolerance = tol
    }

    private fun addPointToFace(vtx: Vertex, face: Face) {
        vtx.face = face
        if (face.outside == null) {
            claimed.add(vtx)
        } else {
            claimed.insertBefore(vtx, face.outside!!)
        }
        face.outside = vtx
    }

    private fun removePointFromFace(vtx: Vertex, face: Face?) {
        if (vtx === face!!.outside) {
            if (vtx.next != null && vtx.next!!.face === face) {
                face!!.outside = vtx.next
            } else {
                face!!.outside = null
            }
        }
        claimed.delete(vtx)
    }

    private fun removeAllPointsFromFace(face: Face?): Vertex? {
        return if (face!!.outside != null) {
            var end = face.outside
            while (end!!.next != null && end.next!!.face === face) {
                end = end.next
            }
            claimed.delete(face.outside!!, end)
            end.next = null
            face.outside
        } else {
            null
        }
    }

    /**
     * Creates an empty convex hull object.
     */
    constructor()


    /**
     * Constructs the convex hull of a set of points whose
     * coordinates are given by an array of doubles.
     *
     * @param coords x, y, and z coordinates of each input
     * point. The length of this array must be at least three times
     * `nump`.
     * @param nump number of input points
     * @throws IllegalArgumentException the number of input points is less
     * than four or greater than 1/3 the length of `coords`,
     * or the points appear to be coincident, colinear, or
     * coplanar.
     */
    /**
     * Constructs the convex hull of a set of points whose
     * coordinates are given by an array of doubles.
     *
     * @param coords x, y, and z coordinates of each input
     * point. The length of this array will be three times
     * the number of input points.
     * @throws IllegalArgumentException the number of input points is less
     * than four, or the points appear to be coincident, colinear, or
     * coplanar.
     */
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun build(coords: DoubleArray, nump: Int = coords.size / 3) {
        require(nump >= 4) { "Less than four input points specified" }
        require(coords.size / 3 >= nump) { "Coordinate array too small for specified number of points" }
        initBuffers(nump)
        setPoints(coords, nump)
        buildHull()
    }


    /**
     * Constructs the convex hull of a set of points.
     *
     * @param points input points
     * @param nump number of input points
     * @throws IllegalArgumentException the number of input points is less
     * than four or greater then the length of `points`, or the
     * points appear to be coincident, colinear, or coplanar.
     */
    /**
     * Constructs the convex hull of a set of points.
     *
     * @param points input points
     * @throws IllegalArgumentException the number of input points is less
     * than four, or the points appear to be coincident, colinear, or
     * coplanar.
     */
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun build(points: Array<Point3d>, nump: Int = points.size) {
        require(nump >= 4) { "Less than four input points specified" }
        require(points.size >= nump) { "Point array too small for specified number of points" }
        initBuffers(nump)
        setPoints(points, nump)
        buildHull()
    }

    /**
     * Triangulates any non-triangular hull faces. In some cases, due to
     * precision issues, the resulting triangles may be very thin or small,
     * and hence appear to be non-convex (this same limitation is present
     * in [qhull](http://www.qhull.org)).
     */
    fun triangulate() {
        val minArea = 1000 * charLength * DOUBLE_PREC
        newFaces.clear()
        val it: Iterator<*> = faces.iterator()
        while (it.hasNext()) {
            val face = it.next() as Face
            if (face.mark == Face.VISIBLE) {
                face.triangulate(newFaces, minArea)
                // splitFace (face);
            }
        }
        var face = newFaces.first()
        while (face != null) {
            faces.add(face)
            face = face.next
        }
    }

    // 	private void splitFace (Face face)
    // 	 {
    //  	   Face newFace = face.split();
    //  	   if (newFace != null)
    //  	    { newFaces.add (newFace);
    //  	      splitFace (newFace);
    //  	      splitFace (face);
    //  	    }
    // 	 }
    private fun initBuffers(nump: Int) {
        if (pointBuffer.size < nump) {
            vertexPointIndices = IntArray(nump)
            val newBuffer = Array(nump) { i ->
                pointBuffer.getOrNull(i) ?: Vertex()
            }
            pointBuffer = newBuffer
        }
        faces.clear()
        claimed.clear()
        numFaces = 0
        numPoints = nump
    }

    private fun setPoints(coords: DoubleArray, nump: Int) {
        for (i in 0 until nump) {
            val vtx = pointBuffer[i]
            vtx.pnt = Vector3d.xyz(coords[i * 3 + 0], coords[i * 3 + 1], coords[i * 3 + 2])
            vtx.index = i
        }
    }

    private fun setPoints(pnts: Array<Point3d>, nump: Int) {
        for (i in 0 until nump) {
            val vtx = pointBuffer[i]
            vtx.pnt = pnts[i]
            vtx.index = i
        }
    }

    private fun computeMaxAndMin() {
        for (i in 0..2) {
            minVtxs[i] = pointBuffer[0]
            maxVtxs[i] = minVtxs[i]
        }
        var max = pointBuffer[0].pnt
        var min = pointBuffer[0].pnt
        for (i in 1 until numPoints) {
            val pnt = pointBuffer[i].pnt
            if (pnt.x > max.x) {
                max = max.copy(x = pnt.x)
                maxVtxs[0] = pointBuffer[i]
            } else if (pnt.x < min.x) {
                min = min.copy(x = pnt.x)
                minVtxs[0] = pointBuffer[i]
            }
            if (pnt.y > max.y) {
                max = max.copy(y = pnt.y)
                maxVtxs[1] = pointBuffer[i]
            } else if (pnt.y < min.y) {
                min = min.copy(y = pnt.y)
                minVtxs[1] = pointBuffer[i]
            }
            if (pnt.z > max.z) {
                max = max.copy(z = pnt.z)
                maxVtxs[2] = pointBuffer[i]
            } else if (pnt.z < min.z) {
                min = min.copy(z = pnt.z)
                minVtxs[2] = pointBuffer[i]
            }
        }

        // this epsilon formula comes from QuickHull, and I'm
        // not about to quibble.
        charLength = max(max.x - min.x, max.y - min.y)
        charLength = max(max.z - min.z, charLength)
        tolerance = if (explicitTolerance == AUTOMATIC_TOLERANCE) {
            3 * DOUBLE_PREC * (
                max(
                    abs(max.x),
                    abs(min.x)
                ) +
                    max(abs(max.y), abs(min.y)) +
                    max(abs(max.z), abs(min.z))
                )
        } else {
            explicitTolerance
        }
    }

    /**
     * Creates the initial simplex from which the hull will be built.
     */
    @Throws(IllegalArgumentException::class)
    private fun createInitialSimplex() {
        var max = 0.0
        var imax = 0
        for (i in 0..2) {
            val diff = maxVtxs[i]!!.pnt[i] - minVtxs[i]!!.pnt[i]
            if (diff > max) {
                max = diff
                imax = i
            }
        }
        require(max > tolerance) { "Input points appear to be coincident" }
        val vtxInit = arrayOfNulls<Vertex>(4)
        // set first two vertices to be those with the greatest
        // one dimensional separation
        vtxInit[0] = maxVtxs[imax]
        vtxInit[1] = minVtxs[imax]

        // set third vertex to be the vertex farthest from
        // the line between vtx0 and vtx1
        val u01 = (vtxInit[1]!!.pnt - vtxInit[0]!!.pnt).normalized()
        var diff02: Vector3d
        var nrml = Vector3d.ZERO
        var xprod: Vector3d
        var maxSqr = 0.0
        for (i in 0 until numPoints) {
            diff02 = (pointBuffer[i].pnt - vtxInit[0]!!.pnt)
            xprod = u01.crossed(diff02)
            val lenSqr = xprod.magnitudeSq()
            if (lenSqr > maxSqr && pointBuffer[i] !== vtxInit[0] && // paranoid
                pointBuffer[i] !== vtxInit[1]
            ) {
                maxSqr = lenSqr
                vtxInit[2] = pointBuffer[i]
                nrml = xprod
            }
        }
        require(sqrt(maxSqr) > 100 * tolerance) { "Input points appear to be colinear" }
        nrml = nrml.normalized()
        var maxDist = 0.0
        val d0 = vtxInit[2]!!.pnt.dot(nrml)
        for (i in 0 until numPoints) {
            val dist = abs(pointBuffer[i].pnt.dot(nrml) - d0)
            if (dist > maxDist && pointBuffer[i] !== vtxInit[0] && // paranoid
                pointBuffer[i] !== vtxInit[1] && pointBuffer[i] !== vtxInit[2]
            ) {
                maxDist = dist
                vtxInit[3] = pointBuffer[i]
            }
        }
        val vtx = vtxInit.requireNoNulls()
        require(abs(maxDist) > 100 * tolerance) { "Input points appear to be coplanar" }
        logger.debug("initial vertices:")
        logger.debug(vtx[0].index.toString() + ": " + vtx[0].pnt)
        logger.debug(vtx[1].index.toString() + ": " + vtx[1].pnt)
        logger.debug(vtx[2].index.toString() + ": " + vtx[2].pnt)
        logger.debug(vtx[3].index.toString() + ": " + vtx[3].pnt)
        val tris = arrayOfNulls<Face>(4)
        if (vtx[3].pnt.dot(nrml) - d0 < 0) {
            tris[0] = createTriangle(vtx[0], vtx[1], vtx[2])
            tris[1] = createTriangle(vtx[3], vtx[1], vtx[0])
            tris[2] = createTriangle(vtx[3], vtx[2], vtx[1])
            tris[3] = createTriangle(vtx[3], vtx[0], vtx[2])
            for (i in 0..2) {
                val k = (i + 1) % 3
                tris[i + 1]!!.getEdge(1)!!.opposite = (tris[k + 1]!!.getEdge(0))
                tris[i + 1]!!.getEdge(2)!!.opposite = (tris[0]!!.getEdge(k))
            }
        } else {
            tris[0] = createTriangle(vtx[0], vtx[2], vtx[1])
            tris[1] = createTriangle(vtx[3], vtx[0], vtx[1])
            tris[2] = createTriangle(vtx[3], vtx[1], vtx[2])
            tris[3] = createTriangle(vtx[3], vtx[2], vtx[0])
            for (i in 0..2) {
                val k = (i + 1) % 3
                tris[i + 1]!!.getEdge(0)!!.opposite = (tris[k + 1]!!.getEdge(1))
                tris[i + 1]!!.getEdge(2)!!.opposite = (tris[0]!!.getEdge((3 - i) % 3))
            }
        }
        for (i in 0..3) {
            faces.add(tris[i])
        }
        for (i in 0 until numPoints) {
            val v = pointBuffer[i]
            if (v === vtx[0] || v === vtx[1] || v === vtx[2] || v === vtx[3]) {
                continue
            }
            maxDist = tolerance
            var maxFace: Face? = null
            for (k in 0..3) {
                val dist = tris[k]!!.distanceToPlane(v.pnt)
                if (dist > maxDist) {
                    maxFace = tris[k]
                    maxDist = dist
                }
            }
            maxFace?.let { addPointToFace(v, it) }
        }
    }

    /**
     * Returns the vertex points in this hull.
     *
     * @return array of vertex points
     * @see QuickHull3D.getVertices
     * @see QuickHull3D.getFaces
     */
    val vertices: Array<Point3d>
        get() {
            val vtxs = arrayOfNulls<Point3d>(numVertices)
            for (i in 0 until numVertices) {
                vtxs[i] = pointBuffer[vertexPointIndices[i]].pnt
            }
            return vtxs.requireNoNulls()
        }

    /**
     * Returns the coordinates of the vertex points of this hull.
     *
     * @param coords returns the x, y, z coordinates of each vertex.
     * This length of this array must be at least three times
     * the number of vertices.
     * @return the number of vertices
     * @see QuickHull3D.getVertices
     * @see QuickHull3D.getFaces
     */
    private fun getVertices(coords: DoubleArray): Int {
        for (i in 0 until numVertices) {
            val pnt = pointBuffer[vertexPointIndices[i]].pnt
            coords[i * 3 + 0] = pnt.x
            coords[i * 3 + 1] = pnt.y
            coords[i * 3 + 2] = pnt.z
        }
        return numVertices
    }

    /**
     * Returns an array specifing the index of each hull vertex
     * with respect to the original input points.
     *
     * @return vertex indices with respect to the original points
     */
    @JvmName("getVertexPointIndices1")
    fun getVertexPointIndices(): IntArray {
        val indices = IntArray(numVertices)
        for (i in 0 until numVertices) {
            indices[i] = vertexPointIndices[i]
        }
        return indices
    }

    /**
     * Returns the number of faces in this hull.
     *
     * @return number of faces
     */
    @JvmName("getNumFaces1")
    fun getNumFaces(): Int {
        return faces.size
    }

    /**
     * Returns the faces associated with this hull.
     *
     *
     * Each face is represented by an integer array which gives the
     * indices of the vertices. These indices are numbered
     * relative to the
     * hull vertices, are zero-based,
     * and are arranged counter-clockwise. More control
     * over the index format can be obtained using
     * [getFaces(indexFlags)][.getFaces].
     *
     * @return array of integer arrays, giving the vertex
     * indices for each face.
     * @see QuickHull3D.getVertices
     * @see QuickHull3D.getFaces
     */
    fun getFaces(): Array<IntArray?> {
        return getFaces(0)
    }

    /**
     * Returns the faces associated with this hull.
     *
     *
     * Each face is represented by an integer array which gives the
     * indices of the vertices. By default, these indices are numbered with
     * respect to the hull vertices (as opposed to the input points), are
     * zero-based, and are arranged counter-clockwise. However, this
     * can be changed by setting [ POINT_RELATIVE][.POINT_RELATIVE], [INDEXED_FROM_ONE][.INDEXED_FROM_ONE], or
     * [CLOCKWISE][.CLOCKWISE] in the indexFlags parameter.
     *
     * @param indexFlags specifies index characteristics (0 results
     * in the default)
     * @return array of integer arrays, giving the vertex
     * indices for each face.
     * @see QuickHull3D.getVertices
     */
    private fun getFaces(indexFlags: Int): Array<IntArray?> {
        val allFaces = arrayOfNulls<IntArray>(faces.size)
        var k = 0
        val it: Iterator<*> = faces.iterator()
        while (it.hasNext()) {
            val face = it.next() as Face
            allFaces[k] = IntArray(face.numVertices())
            getFaceIndices(allFaces[k], face, indexFlags)
            k++
        }
        return allFaces
    }

    private fun getFaceIndices(indices: IntArray?, face: Face, flags: Int) {
        val ccw = flags and CLOCKWISE == 0
        val indexedFromOne = flags and INDEXED_FROM_ONE != 0
        val pointRelative = flags and POINT_RELATIVE != 0
        var hedge = face.he0
        var k = 0
        do {
            var idx = hedge!!.head().index
            if (pointRelative) {
                idx = vertexPointIndices[idx]
            }
            if (indexedFromOne) {
                idx++
            }
            indices!![k++] = idx
            hedge = if (ccw) hedge.next else hedge.prev
        } while (hedge !== face.he0)
    }

    private fun resolveUnclaimedPoints(newFaces: FaceList) {
        var vtxNext = unclaimed.first()
        var vtx = vtxNext
        while (vtx != null) {
            vtxNext = vtx.next
            var maxDist = tolerance
            var maxFace: Face? = null
            var newFace = newFaces.first()
            while (newFace != null) {
                if (newFace.mark == Face.VISIBLE) {
                    val dist = newFace.distanceToPlane(vtx.pnt)
                    if (dist > maxDist) {
                        maxDist = dist
                        maxFace = newFace
                    }
                    if (maxDist > 1000 * tolerance) {
                        break
                    }
                }
                newFace = newFace.next
            }
            if (maxFace != null) {
                addPointToFace(vtx, maxFace)
                if (vtx.index == findIndex) {
                    logger.info(
                        findIndex.toString() + " CLAIMED BY " +
                            maxFace.vertexString
                    )
                }
            } else {
                if (vtx.index == findIndex) {
                    logger.info("$findIndex DISCARDED")
                }
            }
            vtx = vtxNext
        }
    }

    private fun deleteFacePoints(face: Face?, absorbingFace: Face?) {
        val faceVtxs = removeAllPointsFromFace(face)
        if (faceVtxs != null) {
            if (absorbingFace == null) {
                unclaimed.addAll(faceVtxs)
            } else {
                var vtxNext = faceVtxs
                var vtx = vtxNext
                while (vtx != null) {
                    vtxNext = vtx.next
                    val dist = absorbingFace.distanceToPlane(vtx.pnt)
                    if (dist > tolerance) {
                        addPointToFace(vtx, absorbingFace)
                    } else {
                        unclaimed.add(vtx)
                    }
                    vtx = vtxNext
                }
            }
        }
    }

    private fun oppFaceDistance(he: HalfEdge?): Double {
        return he!!.face.distanceToPlane(he.opposite!!.face.centroid)
    }

    private fun doAdjacentMerge(face: Face, mergeType: Int): Boolean {
        var hedge = face.he0
        var convex = true
        do {
            val oppFace = hedge!!.oppositeFace()
            var merge = false
            var dist1: Double
            var dist2: Double
            if (mergeType == NONCONVEX) { // then merge faces if they are definitively non-convex
                if (oppFaceDistance(hedge) > -tolerance ||
                    oppFaceDistance(hedge.opposite) > -tolerance
                ) {
                    merge = true
                }
            } else // mergeType == NONCONVEX_WRT_LARGER_FACE
                { // merge faces if they are parallel or non-convex
                    // wrt to the larger face; otherwise, just mark
                    // the face non-convex for the second pass.
                    if (face.area > oppFace!!.area) {
                        if (oppFaceDistance(hedge).also { dist1 = it } > -tolerance) {
                            merge = true
                        } else if (oppFaceDistance(hedge.opposite) > -tolerance) {
                            convex = false
                        }
                    } else {
                        if (oppFaceDistance(hedge.opposite) > -tolerance) {
                            merge = true
                        } else if (oppFaceDistance(hedge) > -tolerance) {
                            convex = false
                        }
                    }
                }
            if (merge) {
                logger.info(
                    "  merging " + face.vertexString + "  and  " +
                        oppFace!!.vertexString
                )
                val numd = face.mergeAdjacentFace(hedge, discardedFaces)
                for (i in 0 until numd) {
                    deleteFacePoints(discardedFaces[i], face)
                }
                logger.info(
                    "  result: " + face.vertexString
                )
                return true
            }
            hedge = hedge.next
        } while (hedge !== face.he0)
        if (!convex) {
            face.mark = Face.NON_CONVEX
        }
        return false
    }

    private fun calculateHorizon(
        eyePnt: Point3d?,
        edge0: HalfEdge?,
        face: Face?,
        horizon: Vector<HalfEdge>
    ) {
// 	   oldFaces.add (face);
        var edge0 = edge0
        deleteFacePoints(face, null)
        face!!.mark = Face.DELETED
        logger.info("  visiting face " + face.vertexString)
        var edge: HalfEdge?
        if (edge0 == null) {
            edge0 = face.getEdge(0)
            edge = edge0
        } else {
            edge = edge0.next
        }
        do {
            val oppFace = edge!!.oppositeFace()
            if (oppFace!!.mark == Face.VISIBLE) {
                if (oppFace.distanceToPlane(eyePnt) > tolerance) {
                    calculateHorizon(
                        eyePnt,
                        edge.opposite,
                        oppFace,
                        horizon
                    )
                } else {
                    horizon.add(edge)
                    logger.info(
                        "  adding horizon edge " +
                            edge.vertexString
                    )
                }
            }
            edge = edge.next
        } while (edge !== edge0)
    }

    private fun addAdjoiningFace(
        eyeVtx: Vertex,
        he: HalfEdge
    ): HalfEdge? {
        val face: Face = createTriangle(
            eyeVtx,
            he.tail()!!,
            he.head()
        )
        faces.add(face)
        face.getEdge(-1)!!.opposite = (he.opposite)
        return face.getEdge(0)
    }

    private fun addNewFaces(
        newFaces: FaceList,
        eyeVtx: Vertex,
        horizon: Vector<*>
    ) {
        newFaces.clear()
        var hedgeSidePrev: HalfEdge? = null
        var hedgeSideBegin: HalfEdge? = null
        val it: Iterator<*> = horizon.iterator()
        while (it.hasNext()) {
            val horizonHe = it.next() as HalfEdge
            val hedgeSide = addAdjoiningFace(eyeVtx, horizonHe)
            logger.info(
                "new face: " + hedgeSide!!.face.vertexString
            )
            if (hedgeSidePrev != null) {
                hedgeSide!!.next!!.opposite = (hedgeSidePrev)
            } else {
                hedgeSideBegin = hedgeSide
            }
            newFaces.add(hedgeSide!!.face)
            hedgeSidePrev = hedgeSide
        }
        hedgeSideBegin!!.next!!.opposite = (hedgeSidePrev)
    }

    private fun nextPointToAdd(): Vertex? {
        return if (!claimed.isEmpty) {
            val eyeFace = claimed.first()!!.face
            var eyeVtx: Vertex? = null
            var maxDist = 0.0
            var vtx = eyeFace!!.outside
            while (vtx != null && vtx.face === eyeFace) {
                val dist = eyeFace.distanceToPlane(vtx.pnt)
                if (dist > maxDist) {
                    maxDist = dist
                    eyeVtx = vtx
                }
                vtx = vtx.next
            }
            eyeVtx
        } else {
            null
        }
    }

    private fun addPointToHull(eyeVtx: Vertex) {
        horizon.clear()
        unclaimed.clear()
        logger.info("Adding point: " + eyeVtx.index)
        logger.info(
            " which is " + eyeVtx.face!!.distanceToPlane(eyeVtx.pnt) +
                " above face " + eyeVtx.face!!.vertexString
        )
        removePointFromFace(eyeVtx, eyeVtx.face)
        calculateHorizon(eyeVtx.pnt, null, eyeVtx.face, horizon)
        newFaces.clear()
        addNewFaces(newFaces, eyeVtx, horizon)

        // first merge pass ... merge faces which are non-convex
        // as determined by the larger face
        run {
            var face = newFaces.first()
            while (face != null) {
                if (face!!.mark == Face.VISIBLE) {
                    while (doAdjacentMerge(face!!, NONCONVEX_WRT_LARGER_FACE));
                }
                face = face!!.next
            }
        }
        // second merge pass ... merge faces which are non-convex
        // wrt either face
        var face = newFaces.first()
        while (face != null) {
            if (face!!.mark == Face.NON_CONVEX) {
                face!!.mark = Face.VISIBLE
                while (doAdjacentMerge(face!!, NONCONVEX));
            }
            face = face!!.next
        }
        resolveUnclaimedPoints(newFaces)
    }

    private fun buildHull() {
        var cnt = 0
        var eyeVtx: Vertex?
        computeMaxAndMin()
        createInitialSimplex()
        while (nextPointToAdd().also { eyeVtx = it } != null) {
            addPointToHull(eyeVtx!!)
            cnt++
            logger.info("iteration $cnt done")
        }
        reindexFacesAndVertices()
        logger.info("hull done")
    }

    private fun markFaceVertices(face: Face, mark: Int) {
        val he0 = face.firstEdge
        var he = he0
        do {
            he!!.head().index = mark
            he = he.next
        } while (he !== he0)
    }

    private fun reindexFacesAndVertices() {
        for (i in 0 until numPoints) {
            pointBuffer[i].index = -1
        }
        // remove inactive faces and mark active vertices
        numFaces = 0
        val it = faces.iterator()
        while (it.hasNext()) {
            val face = it.next() as Face
            if (face.mark != Face.VISIBLE) {
                it.remove()
            } else {
                markFaceVertices(face, 0)
                numFaces++
            }
        }
        // reindex vertices
        numVertices = 0
        for (i in 0 until numPoints) {
            val vtx = pointBuffer[i]
            if (vtx.index == 0) {
                vertexPointIndices[numVertices] = i
                vtx.index = numVertices++
            }
        }
    }

    private fun checkFaceConvexity(
        face: Face,
        tol: Double
    ): Boolean {
        var dist: Double
        var he = face.he0
        do {
            face.checkConsistency()
            // make sure edge is convex
            dist = oppFaceDistance(he)
            if (dist > tol) {
                logger.info(
                    "Edge " + he!!.vertexString +
                        " non-convex by " + dist
                )
                return false
            }
            dist = oppFaceDistance(he!!.opposite)
            if (dist > tol) {
                logger.info(
                    "Opposite edge " +
                        he.opposite!!.vertexString +
                        " non-convex by " + dist
                )
                return false
            }
            if (he.next!!.oppositeFace() === he.oppositeFace()) {
                logger.info(
                    "Redundant vertex " + he.head().index +
                        " in face " + face.vertexString
                )
                return false
            }
            he = he.next
        } while (he !== face.he0)
        return true
    }

    private fun checkFaces(tol: Double): Boolean {
        // check edge convexity
        var convex = true
        val it: Iterator<*> = faces.iterator()
        while (it.hasNext()) {
            val face = it.next() as Face
            if (face.mark == Face.VISIBLE) {
                if (!checkFaceConvexity(face, tol)) {
                    convex = false
                }
            }
        }
        return convex
    }


    /**
     * Checks the correctness of the hull. This is done by making sure that
     * no faces are non-convex and that no points are outside any face.
     * These tests are performed using the distance tolerance *tol*.
     * Faces are considered non-convex if any edge is non-convex, and an
     * edge is non-convex if the centroid of either adjoining face is more
     * than *tol* above the plane of the other face. Similarly,
     * a point is considered outside a face if its distance to that face's
     * plane is more than 10 times *tol*.
     *
     *
     * If the hull has been [triangulated][.triangulate],
     * then this routine may fail if some of the resulting
     * triangles are very small or thin.
     *
     * set to `null` if no messages are desired.
     * @param tol distance tolerance
     * @return true if the hull is valid
     * @see QuickHull3D.check
     */
    /**
     * Checks the correctness of the hull using the distance tolerance
     * returned by [ getDistanceTolerance][QuickHull3D.getDistanceTolerance]; see
     * [ check(PrintStream,double)][QuickHull3D.check] for details.
     *
     * set to `null` if no messages are desired.
     * @return true if the hull is valid
     * @see QuickHull3D.check
     */
    @JvmOverloads
    fun check(tol: Double = distanceTolerance): Boolean {
        // check to make sure all edges are fully connected
        // and that the edges are convex
        var dist: Double
        val pointTol = 10 * tol
        if (!checkFaces(tolerance)) {
            return false
        }

        // check point inclusion
        for (i in 0 until numPoints) {
            val pnt = pointBuffer[i].pnt
            val it: Iterator<*> = faces.iterator()
            while (it.hasNext()) {
                val face = it.next() as Face
                if (face.mark == Face.VISIBLE) {
                    dist = face.distanceToPlane(pnt)
                    if (dist > pointTol) {
                        logger.info(
                            "Point " + i + " " + dist + " above face " +
                                face.vertexString
                        )
                        return false
                    }
                }
            }
        }
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger("KCSG.QuickHull3D")

        /**
         * Specifies that (on output) vertex indices for a face should be
         * listed in clockwise order.
         */
        const val CLOCKWISE = 0x1

        /**
         * Specifies that (on output) the vertex indices for a face should be
         * numbered starting from 1.
         */
        const val INDEXED_FROM_ONE = 0x2

        /**
         * Specifies that (on output) the vertex indices for a face should be
         * numbered starting from 0.
         */
        const val INDEXED_FROM_ZERO = 0x4

        /**
         * Specifies that (on output) the vertex indices for a face should be
         * numbered with respect to the original input points.
         */
        const val POINT_RELATIVE = 0x8

        /**
         * Specifies that the distance tolerance should be
         * computed automatically from the input point data.
         */
        const val AUTOMATIC_TOLERANCE = -1.0

        /**
         * Precision of a double.
         */
        private const val DOUBLE_PREC = 2.2204460492503131e-16
        private const val NONCONVEX_WRT_LARGER_FACE = 1
        private const val NONCONVEX = 2
    }
}

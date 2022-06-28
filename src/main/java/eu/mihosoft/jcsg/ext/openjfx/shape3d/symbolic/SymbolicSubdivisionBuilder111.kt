/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.mihosoft.jcsg.ext.openjfx.shape3d.symbolic

import eu.mihosoft.jcsg.ext.openjfx.shape3d.SubdivisionMesh.BoundaryMode
import eu.mihosoft.jcsg.ext.openjfx.shape3d.SubdivisionMesh.MapBorderMode
import javafx.geometry.Point2D

/**
 *
 * Data structure builder for Catmull Clark subdivision surface
 */
class SymbolicSubdivisionBuilder(
    private val oldMesh: SymbolicPolygonMesh,
    private val boundaryMode: BoundaryMode,
    private val mapBorderMode: MapBorderMode
) {
    private var edgeInfos: MutableMap<Edge?, EdgeInfo>? = null
    private lateinit var faceInfos: Array<FaceInfo?>
    private lateinit var pointInfos: Array<PointInfo?>
    private var points: SubdividedPointArray? = null
    private lateinit var texCoords: FloatArray
    private lateinit var reindex: IntArray
    private var newTexCoordIndex = 0
    fun subdivide(): SymbolicPolygonMesh {
        collectInfo()
        texCoords = FloatArray((oldMesh.numEdgesInFaces * 3 + oldMesh.faces!!.size) * 2)
        val faces =
            Array<IntArray?>(oldMesh.numEdgesInFaces) { IntArray(8) }
        val faceSmoothingGroups = IntArray(oldMesh.numEdgesInFaces)
        newTexCoordIndex = 0
        reindex =
            IntArray(oldMesh.points.numPoints) // indexes incremented by 1, 0 reserved for empty

        // face points first
        var newFacesInd = 0
        for (f in oldMesh.faces!!.indices) {
            val faceInfo = faceInfos[f]
            val oldFaces = oldMesh.faces!![f]
            var p = 0
            while (p < oldFaces!!.size) {
                faces[newFacesInd]!![4] = getPointNewIndex(faceInfo)
                faces[newFacesInd]!![5] = getTexCoordNewIndex(faceInfo)
                faceSmoothingGroups[newFacesInd] = oldMesh.faceSmoothingGroups[f]
                newFacesInd++
                p += 2
            }
        }
        // then, add edge points
        newFacesInd = 0
        for (f in oldMesh.faces!!.indices) {
            val faceInfo = faceInfos[f]
            val oldFaces = oldMesh.faces!![f]
            var p = 0
            while (p < oldFaces!!.size) {
                faces[newFacesInd]!![2] =
                    getPointNewIndex(faceInfo, (p / 2 + 1) % faceInfo!!.edges.size)
                faces[newFacesInd]!![3] =
                    getTexCoordNewIndex(faceInfo, (p / 2 + 1) % faceInfo.edges.size)
                faces[newFacesInd]!![6] = getPointNewIndex(faceInfo, p / 2)
                faces[newFacesInd]!![7] = getTexCoordNewIndex(faceInfo, p / 2)
                newFacesInd++
                p += 2
            }
        }
        // finally, add control points
        newFacesInd = 0
        for (f in oldMesh.faces!!.indices) {
            val faceInfo = faceInfos[f]
            val oldFaces = oldMesh.faces!![f]
            var p = 0
            while (p < oldFaces!!.size) {
                faces[newFacesInd]!![0] = getPointNewIndex(oldFaces[p])
                faces[newFacesInd]!![1] =
                    getTexCoordNewIndex(faceInfo, oldFaces[p], oldFaces[p + 1])
                newFacesInd++
                p += 2
            }
        }
        return SymbolicPolygonMesh(points!!, texCoords, faces.requireNoNulls(), faceSmoothingGroups)
    }

    private fun addEdge(edge: Edge, faceInfo: FaceInfo) {
        var edgeInfo = edgeInfos!![edge]
        if (edgeInfo == null) {
            edgeInfo = EdgeInfo()
            edgeInfo.edge = edge
            edgeInfos!![edge] = edgeInfo
        }
        edgeInfo.faces.add(faceInfo)
    }

    private fun addPoint(point: Int, faceInfo: FaceInfo, edge: Edge) {
        var pointInfo = pointInfos[point]
        if (pointInfo == null) {
            pointInfo = PointInfo()
            pointInfos[point] = pointInfo
        }
        pointInfo.edges.add(edge)
        pointInfo.faces.add(faceInfo)
    }

    private fun addPoint(point: Int, edge: Edge) {
        var pointInfo = pointInfos[point]
        if (pointInfo == null) {
            pointInfo = PointInfo()
            pointInfos[point] = pointInfo
        }
        pointInfo.edges.add(edge)
    }

    private fun collectInfo() {
        edgeInfos = HashMap(
            oldMesh.faces!!.size * 2
        )
        faceInfos = arrayOfNulls(oldMesh.faces!!.size)
        pointInfos = arrayOfNulls(oldMesh.points.numPoints)
        for (f in oldMesh.faces!!.indices) {
            val face = oldMesh.faces!![f]
            val n = face!!.size / 2
            val faceInfo = FaceInfo(n)
            faceInfos[f] = faceInfo
            if (n < 3) {
                continue
            }
            var from = face[(n - 1) * 2]
            var texFrom = face[(n - 1) * 2 + 1]
            var fu: Double
            var fv: Double
            var tu: Double
            var tv: Double
            var u = 0.0
            var v = 0.0
            fu = oldMesh.texCoords[texFrom * 2].toDouble()
            fv = oldMesh.texCoords[texFrom * 2 + 1].toDouble()
            for (i in 0 until n) {
                val to = face[i * 2]
                val texTo = face[i * 2 + 1]
                tu = oldMesh.texCoords[texTo * 2].toDouble()
                tv = oldMesh.texCoords[texTo * 2 + 1].toDouble()
                val midTexCoord = Point2D((fu + tu) / 2, (fv + tv) / 2)
                val edge = Edge(from, to)
                faceInfo.edges[i] = edge
                faceInfo.edgeTexCoords[i] = midTexCoord
                addEdge(edge, faceInfo)
                addPoint(to, faceInfo, edge)
                addPoint(from, edge)
                fu = tu
                fv = tv
                u += tu / n
                v += tv / n
                from = to
                texFrom = texTo
            }
            faceInfo.texCoord = Point2D(u, v)
        }
        points = SubdividedPointArray(
            oldMesh.points,
            oldMesh.points.numPoints + faceInfos.size + edgeInfos!!.size,
            boundaryMode
        )
        for (f in oldMesh.faces!!.indices) {
            val face = oldMesh.faces!![f]
            val n = face!!.size / 2
            val faceVertices = IntArray(n)
            for (i in 0 until n) {
                faceVertices[i] = face[i * 2]
            }
            faceInfos[f]!!.facePoint = points!!.addFacePoint(faceVertices)
        }
        for (edgeInfo in edgeInfos!!.values) {
            val edgeFacePoints = IntArray(edgeInfo.faces.size)
            for (f in edgeInfo.faces.indices) {
                edgeFacePoints[f] = edgeInfo.faces[f].facePoint
            }
            edgeInfo.edgePoint = points!!.addEdgePoint(
                edgeFacePoints,
                edgeInfo.edge!!.from,
                edgeInfo.edge!!.to,
                edgeInfo.isBoundary
            )
        }
    }

    private fun calcControlPoint(srcPointIndex: Int): Int {
        val pointInfo = pointInfos[srcPointIndex]
        val facePoints = IntArray(pointInfo!!.faces.size)
        for (f in facePoints.indices) {
            facePoints[f] = pointInfo.faces[f].facePoint
        }
        val edgePoints = IntArray(pointInfo.edges.size)
        val isEdgeBoundary = BooleanArray(pointInfo.edges.size)
        val fromEdgePoints = IntArray(pointInfo.edges.size)
        val toEdgePoints = IntArray(pointInfo.edges.size)
        var i = 0
        for (edge in pointInfo.edges) {
            val edgeInfo =
                edgeInfos!![edge]
            edgePoints[i] = edgeInfo!!.edgePoint
            isEdgeBoundary[i] = edgeInfo.isBoundary
            fromEdgePoints[i] = edgeInfo.edge!!.from
            toEdgePoints[i] = edgeInfo.edge!!.to
            i++
        }
        return points!!.addControlPoint(
            facePoints,
            edgePoints,
            fromEdgePoints,
            toEdgePoints,
            isEdgeBoundary,
            srcPointIndex,
            pointInfo.isBoundary,
            pointInfo.hasInternalEdge()
        )
    }

    private fun calcControlTexCoord(
        faceInfo: FaceInfo?,
        srcPointIndex: Int,
        srcTexCoordIndex: Int,
        destTexCoordIndex: Int
    ) {
        val pointInfo = pointInfos[srcPointIndex]
        val pointBelongsToCrease = oldMesh.points is OriginalPointArray
        if (mapBorderMode == MapBorderMode.SMOOTH_ALL && (pointInfo!!.isBoundary || pointBelongsToCrease) ||
            mapBorderMode == MapBorderMode.SMOOTH_INTERNAL && !pointInfo!!.hasInternalEdge()
        ) {
            var u = (oldMesh.texCoords[srcTexCoordIndex * 2] / 2).toDouble()
            var v = (oldMesh.texCoords[srcTexCoordIndex * 2 + 1] / 2).toDouble()
            for (i in faceInfo!!.edges.indices) {
                if (faceInfo.edges[i]!!.to == srcPointIndex || faceInfo.edges[i]!!.from == srcPointIndex) {
                    u += faceInfo.edgeTexCoords[i]!!.x / 4
                    v += faceInfo.edgeTexCoords[i]!!.y / 4
                }
            }
            texCoords[destTexCoordIndex * 2] = u.toFloat()
            texCoords[destTexCoordIndex * 2 + 1] = v.toFloat()
        } else {
            texCoords[destTexCoordIndex * 2] = oldMesh.texCoords[srcTexCoordIndex * 2]
            texCoords[destTexCoordIndex * 2 + 1] = oldMesh.texCoords[srcTexCoordIndex * 2 + 1]
        }
    }

    private fun getPointNewIndex(srcPointIndex: Int): Int {
        var destPointIndex = reindex[srcPointIndex] - 1
        if (destPointIndex == -1) {
            destPointIndex = calcControlPoint(srcPointIndex)
            reindex[srcPointIndex] = destPointIndex + 1
        }
        return destPointIndex
    }

    private fun getPointNewIndex(faceInfo: FaceInfo?, edgeInd: Int): Int {
        val edge = faceInfo!!.edges[edgeInd]
        val edgeInfo = edgeInfos!![edge]
        return edgeInfo!!.edgePoint
    }

    private fun getPointNewIndex(faceInfo: FaceInfo?): Int {
        return faceInfo!!.facePoint
    }

    private fun getTexCoordNewIndex(
        faceInfo: FaceInfo?,
        srcPointIndex: Int,
        srcTexCoordIndex: Int
    ): Int {
        val destTexCoordIndex = newTexCoordIndex
        newTexCoordIndex++
        calcControlTexCoord(faceInfo, srcPointIndex, srcTexCoordIndex, destTexCoordIndex)
        return destTexCoordIndex
    }

    private fun getTexCoordNewIndex(faceInfo: FaceInfo?, edgeInd: Int): Int {
        val destTexCoordIndex = newTexCoordIndex
        newTexCoordIndex++
        texCoords[destTexCoordIndex * 2] = faceInfo!!.edgeTexCoords[edgeInd]!!.x.toFloat()
        texCoords[destTexCoordIndex * 2 + 1] = faceInfo.edgeTexCoords[edgeInd]!!.y.toFloat()
        return destTexCoordIndex
    }

    private fun getTexCoordNewIndex(faceInfo: FaceInfo?): Int {
        var destTexCoordIndex = faceInfo!!.newTexCoordIndex - 1
        if (destTexCoordIndex == -1) {
            destTexCoordIndex = newTexCoordIndex
            faceInfo.newTexCoordIndex = destTexCoordIndex + 1
            newTexCoordIndex++
            texCoords[destTexCoordIndex * 2] = faceInfo.texCoord!!.x.toFloat()
            texCoords[destTexCoordIndex * 2 + 1] = faceInfo.texCoord!!.y.toFloat()
        }
        return destTexCoordIndex
    }

    private class Edge(from: Int, to: Int) {
        var from: Int
        var to: Int
        override fun hashCode(): Int {
            var hash = 7
            hash = 41 * hash + from
            hash = 41 * hash + to
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
            if (from != other.from) {
                return false
            }
            return to == other.to
        }

        init {
            this.from = Math.min(from, to)
            this.to = Math.max(from, to)
        }
    }

    private class EdgeInfo {
        var edge: Edge? = null
        var edgePoint = 0
        var faces: MutableList<FaceInfo> = ArrayList(2)

        /**
         * an edge is in the boundary if it has only one adjacent face
         */
        val isBoundary: Boolean
            get() = faces.size == 1
    }

    private inner class PointInfo {
        var faces: MutableList<FaceInfo> = ArrayList(4)
        var edges: MutableSet<Edge> = HashSet(4)

        /**
         * A point is in the boundary if any of its adjacent edges is in the boundary
         */
        val isBoundary: Boolean
            get() {
                for (edge in edges) {
                    val edgeInfo = edgeInfos!![edge]
                    if (edgeInfo!!.isBoundary) return true
                }
                return false
            }

        /**
         * A point is internal if at least one of its adjacent edges is not in the boundary
         */
        fun hasInternalEdge(): Boolean {
            for (edge in edges) {
                val edgeInfo = edgeInfos!![edge]
                if (!edgeInfo!!.isBoundary) return true
            }
            return false
        }
    }

    private class FaceInfo(n: Int) {
        var facePoint = 0
        var texCoord: Point2D? = null
        var newTexCoordIndex = 0
        var edges: Array<Edge?>
        var edgeTexCoords: Array<Point2D?>

        init {
            edges = arrayOfNulls(n)
            edgeTexCoords = arrayOfNulls(n)
        }
    }

    companion object {
        fun subdivide(
            oldMesh: SymbolicPolygonMesh,
            boundaryMode: BoundaryMode,
            mapBorderMode: MapBorderMode
        ): SymbolicPolygonMesh {
            val subdivision = SymbolicSubdivisionBuilder(oldMesh, boundaryMode, mapBorderMode)
            return subdivision.subdivide()
        }
    }
}
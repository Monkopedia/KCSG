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
package eu.mihosoft.jcsg.ext.openjfx.importers

import javafx.scene.shape.TriangleMesh
import java.util.*
import kotlin.math.max
import kotlin.math.min

/** Util for converting Normals to Smoothing Groups  */
class SmoothingGroups(
    private val faces: Array<IntArray>,
    private val faceNormals: Array<IntArray>,
    private val normals: FloatArray
) {
    private val visited: BitSet = BitSet(faces.size)
    private val notVisited: BitSet
    private val q: Queue<Int>
    private lateinit var faceEdges: Array<Array<Edge>?>

    // edge -> [faces]
    private fun getNextConnectedComponent(adjacentFaces: Map<Edge, List<Int>>): List<Int> {
        val index = notVisited.previousSetBit(faces.size - 1)
        q.add(index)
        visited.set(index)
        notVisited[index] = false
        val res: MutableList<Int> = ArrayList()
        while (!q.isEmpty()) {
            val faceIndex = q.remove()
            res.add(faceIndex)
            for (edge in faceEdges[faceIndex]!!) {
                val adjFaces = adjacentFaces[edge] ?: continue
                val adjFaceIndex = adjFaces[if (adjFaces[0] == faceIndex) 1 else 0]
                if (!visited[adjFaceIndex]) {
                    q.add(adjFaceIndex)
                    visited.set(adjFaceIndex)
                    notVisited[adjFaceIndex] = false
                }
            }
        }
        return res
    }

    private fun hasNextConnectedComponent(): Boolean {
        return !notVisited.isEmpty
    }

    private fun computeFaceEdges() {
        faceEdges = arrayOfNulls(
            faces.size
        )
        for (f in faces.indices) {
            val face = faces[f]
            val faceNormal = faceNormals[f]
            val n = face.size / 2
            val arr = arrayOfNulls<Edge>(n)
            var from = face[(n - 1) * 2]
            var fromNormal = faceNormal[n - 1]
            for (i in 0 until n) {
                val to = face[i * 2]
                val toNormal = faceNormal[i]
                val edge: Edge = Edge(from, to, fromNormal, toNormal)
                arr[i] = edge
                from = to
                fromNormal = toNormal
            }
            faceEdges[f] = arr.requireNoNulls()
        }
    }

    // just skip them
    private val adjacentFaces: Map<Edge, MutableList<Int>>
        private get() {
            val adjacentFaces: MutableMap<Edge, MutableList<Int>> = HashMap()
            for (f in faceEdges.indices) {
                for (edge in faceEdges[f]!!) {
                    if (!adjacentFaces.containsKey(edge)) {
                        adjacentFaces[edge] = ArrayList()
                    }
                    adjacentFaces[edge]!!.add(f)
                }
            }
            val it: MutableIterator<Map.Entry<Edge, List<Int>>> = adjacentFaces.entries.iterator()
            while (it.hasNext()) {
                val (_, value) = it.next()
                if (value.size != 2) {
                    // just skip them
                    it.remove()
                }
            }
            return adjacentFaces
        }

    fun getNormal(index: Int): Vec3f {
        return Vec3f(normals[index * 3], normals[index * 3 + 1], normals[index * 3 + 2])
    }

    private fun getSmoothEdges(adjacentFaces: Map<Edge, MutableList<Int>>): Map<Edge, List<Int>> {
        val smoothEdges: MutableMap<Edge, List<Int>> = HashMap()
        for (face in faceEdges.indices) {
            for (edge in faceEdges[face]!!) {
                val adjFaces: List<Int>? = adjacentFaces[edge]
                if (adjFaces == null || adjFaces.size != 2) {
                    // could happen when we skip edges!
                    continue
                }
                val adjFace = adjFaces[if (adjFaces[0] == face) 1 else 0]
                val adjFaceEdges = faceEdges[adjFace]!!
                val adjEdgeInd = listOf(*adjFaceEdges).indexOf(edge)
                if (adjEdgeInd == -1) {
                    println("Can't find edge $edge in face $adjFace")
                    println(listOf(*adjFaceEdges))
                    continue
                }
                val adjEdge = adjFaceEdges[adjEdgeInd]
                if (edge.isSmooth(adjEdge)) {
                    if (!smoothEdges.containsKey(edge)) {
                        smoothEdges[edge] = adjFaces
                    }
                }
            }
        }
        return smoothEdges
    }

    private fun calcConnComponents(smoothEdges: Map<Edge, List<Int>>): List<List<Int>> {
        //System.out.println("smoothEdges = " + smoothEdges);
        val groups: MutableList<List<Int>> = ArrayList()
        while (hasNextConnectedComponent()) {
            val smoothGroup = getNextConnectedComponent(smoothEdges)
            groups.add(smoothGroup)
        }
        return groups
    }

    private fun generateSmGroups(groups: List<List<Int>>): IntArray {
        val smGroups = IntArray(faceNormals.size)
        var curGroup = 0
        for (i in groups.indices) {
            val list = groups[i]
            if (list.size == 1) {
                smGroups[list[0]] = 0
            } else {
                for (j in list.indices) {
                    val faceIndex = list[j]
                    smGroups[faceIndex] = 1 shl curGroup
                }
                if (curGroup++ == 31) {
                    curGroup = 0
                }
            }
        }
        return smGroups
    }

    private fun calcSmoothGroups(): IntArray {
        computeFaceEdges()

        // edge -> [faces]
        val adjacentFaces = adjacentFaces

        // smooth edge -> [faces]
        val smoothEdges = getSmoothEdges(adjacentFaces)

        //System.out.println("smoothEdges = " + smoothEdges);
        val groups = calcConnComponents(smoothEdges)
        return generateSmGroups(groups)
    }

    private inner class Edge(from: Int, to: Int, fromNormal: Int, toNormal: Int) {
        var from: Int
        var to: Int
        var fromNormal: Int
        var toNormal: Int
        fun isSmooth(edge: Edge): Boolean {
            return isNormalsEqual(
                getNormal(fromNormal),
                getNormal(edge.fromNormal)
            ) && isNormalsEqual(getNormal(toNormal), getNormal(edge.toNormal)) ||
                isNormalsEqual(getNormal(fromNormal), getNormal(edge.toNormal)) && isNormalsEqual(
                getNormal(toNormal),
                getNormal(edge.fromNormal)
            )
        }

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
            this.from = min(from, to)
            this.to = max(from, to)
            this.fromNormal = min(fromNormal, toNormal)
            this.toNormal = max(fromNormal, toNormal)
        }
    }

    companion object {
        private const val normalAngle = 0.9994f // cos(2)
        private fun isNormalsEqual(n1: Vec3f, n2: Vec3f): Boolean {
            if (n1.x == 1.0e20f || n1.y == 1.0e20f || n1.z == 1.0e20f || n2.x == 1.0e20f || n2.y == 1.0e20f || n2.z == 1.0e20f) {
                //System.out.println("unlocked normal found, skipping");
                return false
            }
            val myN1 = Vec3f(n1)
            myN1.normalize()
            val myN2 = Vec3f(n2)
            myN2.normalize()
            return myN1.dot(myN2) >= normalAngle
        }

        /**
         * Calculates smoothing groups for data formatted in PolygonMesh style
         * @param faces An array of faces, where each face consists of an array of vertex and uv indices
         * @param faceNormals An array of face normals, where each face normal consists of an array of normal indices
         * @param normals The array of normals
         * @return An array of smooth groups, where the length of the array is the number of faces
         */
        fun calcSmoothGroups(
            faces: Array<IntArray>,
            faceNormals: Array<IntArray>,
            normals: FloatArray
        ): IntArray {
            val smoothGroups = SmoothingGroups(faces, faceNormals, normals)
            return smoothGroups.calcSmoothGroups()
        }

        /**
         * Calculates smoothing groups for data formatted in TriangleMesh style
         * @param flatFaces An array of faces, where each triangle face is represented by 6 (vertex and uv) indices
         * @param flatFaceNormals An array of face normals, where each triangle face is represented by 3 normal indices
         * @param normals The array of normals
         * @return An array of smooth groups, where the length of the array is the number of faces
         */
        fun calcSmoothGroups(
            mesh: TriangleMesh,
            flatFaces: IntArray,
            flatFaceNormals: IntArray,
            normals: FloatArray
        ): IntArray {
            val faceElementSize = mesh.faceElementSize
            val faces =
                Array(flatFaces.size / faceElementSize) { IntArray(faceElementSize) }
            for (f in faces.indices) {
                for (e in 0 until faceElementSize) {
                    faces[f][e] = flatFaces[f * faceElementSize + e]
                }
            }
            val pointElementSize = mesh.pointElementSize
            val faceNormals = Array(flatFaceNormals.size / pointElementSize) {
                IntArray(pointElementSize)
            }
            for (f in faceNormals.indices) {
                for (e in 0 until pointElementSize) {
                    faceNormals[f][e] = flatFaceNormals[f * pointElementSize + e]
                }
            }
            val smoothGroups = SmoothingGroups(faces, faceNormals, normals)
            return smoothGroups.calcSmoothGroups()
        }
    }

    init {
        notVisited = BitSet(faces.size)
        notVisited[0, faces.size] = true
        q = LinkedList()
    }
}
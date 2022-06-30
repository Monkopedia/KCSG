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
import java.util.*

internal class SubdividedPointArray(
    controlPointArray: SymbolicPointArray,
    numPoints: Int,
    boundaryMode: BoundaryMode
) : SymbolicPointArray(
    FloatArray(NUM_COMPONENTS_PER_POINT * numPoints)
) {
    private val controlPoints // points of the previous subdivision level
        : FloatArray?
    private val controlInds // indices corresponding to controlPoints
        : Array<IntArray?>
    private val controlFactors // factors corresponding to controlPoints
        : Array<FloatArray?>
    private val inds: Array<IntArray?>
    private val factors: Array<FloatArray?>
    private val boundaryMode: BoundaryMode
    private var currPoint = 0
    fun addFacePoint(vertices: IntArray): Int {
        controlInds[currPoint] = vertices
        controlFactors[currPoint] = FloatArray(vertices.size)
        Arrays.fill(controlFactors[currPoint], 1.0f / vertices.size)
        inds[currPoint] = IntArray(0)
        factors[currPoint] = FloatArray(0)
        return currPoint++
    }

    fun addEdgePoint(facePoints: IntArray, fromPoint: Int, toPoint: Int, isBoundary: Boolean): Int {
        if (isBoundary) {
            controlInds[currPoint] = intArrayOf(fromPoint, toPoint)
            controlFactors[currPoint] = floatArrayOf(0.5f, 0.5f)
            inds[currPoint] = IntArray(0)
            factors[currPoint] = FloatArray(0)
        } else {
            val n = facePoints.size + 2
            controlInds[currPoint] = intArrayOf(fromPoint, toPoint)
            controlFactors[currPoint] = floatArrayOf(1.0f / n, 1.0f / n)
            inds[currPoint] = facePoints
            factors[currPoint] = FloatArray(facePoints.size)
            Arrays.fill(factors[currPoint], 1.0f / n)
        }
        return currPoint++
    }

    fun addControlPoint(
        facePoints: IntArray,
        edgePoints: IntArray,
        fromEdgePoints: IntArray,
        toEdgePoints: IntArray,
        isEdgeBoundary: BooleanArray,
        origPoint: Int,
        isBoundary: Boolean,
        hasInternalEdge: Boolean
    ): Int {
        if (isBoundary) {
            if (boundaryMode == BoundaryMode.CREASE_EDGES || hasInternalEdge) {
                controlInds[currPoint] = intArrayOf(origPoint)
                controlFactors[currPoint] = floatArrayOf(0.5f)
                var numBoundaryEdges = 0
                for (i in edgePoints.indices) {
                    if (isEdgeBoundary[i]) {
                        numBoundaryEdges++
                    }
                }
                inds[currPoint] = IntArray(numBoundaryEdges)
                factors[currPoint] = FloatArray(numBoundaryEdges)
                var boundaryEdgeInd = 0
                for (i in edgePoints.indices) {
                    if (isEdgeBoundary[i]) {
                        inds[currPoint]!![boundaryEdgeInd] = edgePoints[i]
                        factors[currPoint]!![boundaryEdgeInd] = 0.25f
                        boundaryEdgeInd++
                    }
                }
            } else {
                controlInds[currPoint] = intArrayOf(origPoint)
                controlFactors[currPoint] = floatArrayOf(1.0f)
                inds[currPoint] = IntArray(0)
                factors[currPoint] = FloatArray(0)
            }
        } else {
            val n = facePoints.size
            controlInds[currPoint] = IntArray(1 + edgePoints.size * 2)
            controlFactors[currPoint] = FloatArray(1 + edgePoints.size * 2)
            controlInds[currPoint]!![0] = origPoint
            controlFactors[currPoint]!![0] = (n - 3.0f) / n
            for (i in edgePoints.indices) {
                controlInds[currPoint]!![1 + 2 * i] = fromEdgePoints[i]
                controlFactors[currPoint]!![1 + 2 * i] = 1.0f / (n * n)
                controlInds[currPoint]!![1 + 2 * i + 1] = toEdgePoints[i]
                controlFactors[currPoint]!![1 + 2 * i + 1] = 1.0f / (n * n)
            }
            inds[currPoint] = facePoints
            factors[currPoint] = FloatArray(facePoints.size)
            Arrays.fill(factors[currPoint], 1.0f / (n * n))
        }
        return currPoint++
    }

    override fun update() {
        var ci: Int
        var f: Float
        var x: Float
        var y: Float
        var z: Float
        for (i in 0 until numPoints) {
            z = 0.0f
            y = z
            x = y
            for (j in 0 until controlInds[i]!!.size) {
                ci = 3 * controlInds[i]!![j]
                f = controlFactors[i]!![j]
                x += controlPoints!![ci] * f
                y += controlPoints[ci + 1] * f
                z += controlPoints[ci + 2] * f
            }
            for (j in 0 until inds[i]!!.size) {
                ci = 3 * inds[i]!![j]
                f = factors[i]!![j]
                x += data[ci] * f
                y += data[ci + 1] * f
                z += data[ci + 2] * f
            }
            data[3 * i] = x
            data[3 * i + 1] = y
            data[3 * i + 2] = z
        }
    }

    init {
        controlPoints = controlPointArray.data
        controlInds = arrayOfNulls(numPoints)
        controlFactors = arrayOfNulls(numPoints)
        inds = arrayOfNulls(numPoints)
        factors = arrayOfNulls(numPoints)
        this.boundaryMode = boundaryMode
    }
}
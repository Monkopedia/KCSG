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
package eu.mihosoft.jcsg.ext.openjfx.shape3d

import eu.mihosoft.jcsg.ext.openjfx.shape3d.symbolic.SymbolicPolygonMesh
import eu.mihosoft.jcsg.ext.openjfx.shape3d.symbolic.SymbolicSubdivisionBuilder
import javafx.collections.ObservableFloatArray

/**
 * Catmull Clark subdivision surface polygon mesh
 */
class SubdivisionMesh(
    val originalMesh: PolygonMesh,
    subdivisionLevel: Int,
    boundaryMode: BoundaryMode,
    mapBorderMode: MapBorderMode
) : PolygonMesh() {
    private var _subdivisionLevel = 0
    private var boundaryMode: BoundaryMode? = null
    private var mapBorderMode: MapBorderMode? = null
    private val symbolicMeshes: MutableList<SymbolicPolygonMesh>
    private var pointValuesDirty = false
    private var meshDirty = false
    private var subdivisionLevelDirty = false

    /**
     * Describes whether the edges and points at the boundary are treated as creases
     */
    enum class BoundaryMode {
        /**
         * Only edges at the boundary are treated as creases
         */
        CREASE_EDGES,

        /**
         * Edges and points at the boundary are treated as creases
         */
        CREASE_ALL
    }

    /**
     * Describes how the new texture coordinate for the control point is defined
     */
    enum class MapBorderMode {
        /**
         * Jeeps the same uvs for all control points
         */
        NOT_SMOOTH,

        /**
         * Smooths uvs of points at corners
         */
        SMOOTH_INTERNAL,

        /**
         * Smooths uvs of points at boundaries and original control points (and creases [in the future when creases are defined])
         */
        SMOOTH_ALL
    }

    /**
     * Updates the variables of the underlying polygon mesh.
     * It only updates the fields that need to be updated.
     */
    fun update() {
        if (meshDirty) {
            symbolicMeshes.clear()
            symbolicMeshes.add(SymbolicPolygonMesh(originalMesh))
            pointValuesDirty = true
            subdivisionLevelDirty = true
        }
        while (_subdivisionLevel >= symbolicMeshes.size) {
            symbolicMeshes.add(
                SymbolicSubdivisionBuilder.subdivide(
                    symbolicMeshes[symbolicMeshes.size - 1],
                    boundaryMode!!,
                    mapBorderMode!!
                )
            )
            pointValuesDirty = true
            subdivisionLevelDirty = true
        }
        if (pointValuesDirty) {
            for (i in 0.._subdivisionLevel) {
                val symbolicMesh = symbolicMeshes[i]
                symbolicMesh.points.update()
            }
        }
        if (pointValuesDirty || subdivisionLevelDirty) {
            points.setAll(*symbolicMeshes[_subdivisionLevel].points.data)
        }
        if (subdivisionLevelDirty) {
            faces = symbolicMeshes[_subdivisionLevel].faces!!
            _numEdgesInfaces = -1
            faceSmoothingGroups.setAll(*symbolicMeshes[_subdivisionLevel].faceSmoothingGroups)
            texCoords.setAll(*symbolicMeshes[_subdivisionLevel].texCoords)
        }
        meshDirty = false
        pointValuesDirty = false
        subdivisionLevelDirty = false
    }

    private fun setSubdivisionLevelForced(subdivisionLevel: Int) {
        this._subdivisionLevel = subdivisionLevel
        subdivisionLevelDirty = true
    }

    private fun setBoundaryModeForced(boundaryMode: BoundaryMode) {
        this.boundaryMode = boundaryMode
        meshDirty = true
    }

    private fun setMapBorderModeForced(mapBorderMode: MapBorderMode) {
        this.mapBorderMode = mapBorderMode
        meshDirty = true
    }

    var subdivisionLevel: Int
        get() {
            return _subdivisionLevel
        }
        set(subdivisionLevel) {
            if (subdivisionLevel != this._subdivisionLevel) {
                setSubdivisionLevelForced(subdivisionLevel)
            }
        }

    fun setBoundaryMode(boundaryMode: BoundaryMode) {
        if (boundaryMode != this.boundaryMode) {
            setBoundaryModeForced(boundaryMode)
        }
    }

    fun setMapBorderMode(mapBorderMode: MapBorderMode) {
        if (mapBorderMode != this.mapBorderMode) {
            setMapBorderModeForced(mapBorderMode)
        }
    }

    init {
        setSubdivisionLevelForced(subdivisionLevel)
        setBoundaryModeForced(boundaryMode)
        setMapBorderModeForced(mapBorderMode)
        symbolicMeshes = ArrayList(4) // the polymesh is usually subdivided up to 3 times
        originalMesh.points.addListener { observableArray: ObservableFloatArray?, sizeChanged: Boolean, from: Int, to: Int ->
            if (sizeChanged) {
                meshDirty = true
            } else {
                pointValuesDirty = true
            }
        }
        originalMesh.texCoords.addListener { observableArray: ObservableFloatArray?, sizeChanged: Boolean, from: Int, to: Int ->
            meshDirty = true
        }
    }
}

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

import eu.mihosoft.jcsg.ext.openjfx.shape3d.SubdivisionMesh.BoundaryMode
import eu.mihosoft.jcsg.ext.openjfx.shape3d.SubdivisionMesh.MapBorderMode
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.ArrayChangeListener
import javafx.collections.ObservableFloatArray
import javafx.scene.Parent
import javafx.scene.paint.Material
import javafx.scene.shape.CullFace
import javafx.scene.shape.DrawMode
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A MeshView node for Polygon Meshes
 */
class PolygonMeshView() : Parent() {
    private val meshView = MeshView()
    private var triangleMesh = TriangleMesh()

    // this is null if no subdivision is happening (i.e. subdivisionLevel = 0);
    private var subdivisionMesh: SubdivisionMesh? = null
    private val meshPointsListener =
        ArrayChangeListener { t: ObservableFloatArray?, bln: Boolean, i: Int, i1: Int ->
            pointsDirty = true
            updateMesh()
        }
    private val meshTexCoordListener =
        ArrayChangeListener { t: ObservableFloatArray?, bln: Boolean, i: Int, i1: Int ->
            texCoordsDirty = true
            updateMesh()
        }
    private var pointsDirty = true
    private var pointsSizeDirty = true
    private var texCoordsDirty = true
    private var facesDirty = true
    // =========================================================================
    // PROPERTIES
    /**
     * Specifies the 3D mesh data of this `MeshView`.
     *
     * @defaultValue null
     */
    private var meshProperty: ObjectProperty<PolygonMesh?>? = null
    var mesh: PolygonMesh?
        get() = meshProperty().get()
        set(mesh) {
            meshProperty().set(mesh)
        }

    private fun meshProperty(): ObjectProperty<PolygonMesh?> {
        if (meshProperty == null) {
            meshProperty = SimpleObjectProperty<PolygonMesh>().also { meshProperty ->
                meshProperty.addListener(
                    { observable: ObservableValue<out PolygonMesh?>?, oldValue: PolygonMesh?, newValue: PolygonMesh? ->
                        if (oldValue != null) {
                            oldValue.points.removeListener(meshPointsListener)
                            oldValue.points.removeListener(meshTexCoordListener)
                        }
                        meshProperty.set(newValue)
                        facesDirty = true
                        texCoordsDirty = facesDirty
                        pointsSizeDirty = texCoordsDirty
                        pointsDirty = pointsSizeDirty
                        updateMesh()
                        if (newValue != null) {
                            newValue.points.addListener(meshPointsListener)
                            newValue.texCoords.addListener(meshTexCoordListener)
                        }
                    }
                )
            }
        }
        return meshProperty!!
    }

    /**
     * Defines the drawMode this `Shape3D`.
     *
     * @defaultValue DrawMode.FILL
     */
    private var drawMode: ObjectProperty<DrawMode>? = null
    fun setDrawMode(value: DrawMode) {
        drawModeProperty().set(value)
    }

    private fun getDrawMode(): DrawMode {
        return if (drawMode == null) DrawMode.FILL else drawMode!!.get()
    }

    private fun drawModeProperty(): ObjectProperty<DrawMode> {
        if (drawMode == null) {
            drawMode = object :
                SimpleObjectProperty<DrawMode>(this@PolygonMeshView, "drawMode", DrawMode.FILL) {
                override fun invalidated() {
                    meshView.drawMode = get()
                    facesDirty = true
                    texCoordsDirty = facesDirty
                    pointsSizeDirty = texCoordsDirty
                    pointsDirty = pointsSizeDirty
                    updateMesh()
                }
            }
        }
        return drawMode!!
    }

    /**
     * Defines the drawMode this `Shape3D`.
     *
     * @defaultValue CullFace.BACK
     */
    private var cullFace: ObjectProperty<CullFace>? = null
    fun setCullFace(value: CullFace) {
        cullFaceProperty().set(value)
    }

    fun getCullFace(): CullFace {
        return if (cullFace == null) CullFace.BACK else cullFace!!.get()
    }

    private fun cullFaceProperty(): ObjectProperty<CullFace> {
        if (cullFace == null) {
            cullFace = object :
                SimpleObjectProperty<CullFace>(this@PolygonMeshView, "cullFace", CullFace.BACK) {
                override fun invalidated() {
                    meshView.cullFace = get()
                }
            }
        }
        return cullFace!!
    }

    /**
     * Defines the material this `Shape3D`.
     * The default material is null. If `Material` is null, a PhongMaterial
     * with a diffuse color of Color.LIGHTGRAY is used for rendering.
     *
     * @defaultValue null
     */
    private val materialProperty: ObjectProperty<Material> = SimpleObjectProperty()
    var material: Material
        get() = materialProperty.get()
        set(material) {
            materialProperty.set(material)
        }

    private fun materialProperty(): ObjectProperty<Material> {
        return materialProperty
    }

    /**
     * Number of iterations of Catmull Clark subdivision to apply to the mesh
     *
     * @defaultValue 0
     */
    private var subdivisionLevelProperty: SimpleIntegerProperty? = null
    var subdivisionLevel: Int
        get() = if (subdivisionLevelProperty == null) 0 else subdivisionLevelProperty!!.get()
        set(subdivisionLevel) {
            subdivisionLevelProperty().set(subdivisionLevel)
        }

    private fun subdivisionLevelProperty(): SimpleIntegerProperty {
        if (subdivisionLevelProperty == null) {
            subdivisionLevelProperty = object : SimpleIntegerProperty(subdivisionLevel) {
                override fun invalidated() {
                    // create SubdivisionMesh if subdivisionLevel is greater than 0
                    if (subdivisionLevel > 0 && subdivisionMesh == null) {
                        subdivisionMesh = SubdivisionMesh(
                            mesh!!,
                            subdivisionLevel,
                            getBoundaryMode(),
                            getMapBorderMode()
                        )
                        subdivisionMesh!!.originalMesh.points.addListener { t: ObservableFloatArray?, bln: Boolean, i: Int, i1: Int -> subdivisionMesh!!.update() }
                        mesh = subdivisionMesh
                    }
                    if (subdivisionMesh != null) {
                        subdivisionMesh!!.subdivisionLevel = subdivisionLevel
                        subdivisionMesh!!.update()
                    }
                    facesDirty = true
                    texCoordsDirty = facesDirty
                    pointsSizeDirty = texCoordsDirty
                    pointsDirty = pointsSizeDirty
                    updateMesh()
                }
            }
        }
        return subdivisionLevelProperty!!
    }

    /**
     * Texture mapping boundary rule for Catmull Clark subdivision applied to the mesh
     *
     * @defaultValue BoundaryMode.CREASE_EDGES
     */
    private var boundaryMode: SimpleObjectProperty<BoundaryMode>? = null
    fun setBoundaryMode(boundaryMode: BoundaryMode) {
        boundaryModeProperty().set(boundaryMode)
    }

    fun getBoundaryMode(): BoundaryMode {
        return if (boundaryMode == null) BoundaryMode.CREASE_EDGES else boundaryMode!!.get()
    }

    private fun boundaryModeProperty(): SimpleObjectProperty<BoundaryMode> {
        if (boundaryMode == null) {
            boundaryMode = object : SimpleObjectProperty<BoundaryMode>(getBoundaryMode()) {
                override fun invalidated() {
                    if (subdivisionMesh != null) {
                        subdivisionMesh!!.setBoundaryMode(getBoundaryMode())
                        subdivisionMesh!!.update()
                    }
                    pointsDirty = true
                    updateMesh()
                }
            }
        }
        return boundaryMode!!
    }

    /**
     * Texture mapping smoothness option for Catmull Clark subdivision applied to the mesh
     *
     * @defaultValue MapBorderMode.NOT_SMOOTH
     */
    private var mapBorderMode: SimpleObjectProperty<MapBorderMode>? = null
    fun setMapBorderMode(mapBorderMode: MapBorderMode) {
        mapBorderModeProperty().set(mapBorderMode)
    }

    fun getMapBorderMode(): MapBorderMode {
        return if (mapBorderMode == null) MapBorderMode.NOT_SMOOTH else mapBorderMode!!.get()
    }

    private fun mapBorderModeProperty(): SimpleObjectProperty<MapBorderMode> {
        if (mapBorderMode == null) {
            mapBorderMode = object : SimpleObjectProperty<MapBorderMode>(getMapBorderMode()) {
                override fun invalidated() {
                    if (subdivisionMesh != null) {
                        subdivisionMesh!!.setMapBorderMode(getMapBorderMode())
                        subdivisionMesh!!.update()
                    }
                    texCoordsDirty = true
                    updateMesh()
                }
            }
        }
        return mapBorderMode!!
    }

    constructor(mesh: PolygonMesh?) : this() {
        this.mesh = mesh
    }

    // =========================================================================
    // PRIVATE METHODS
    private fun updateMesh() {
        val pmesh = mesh
        if (pmesh?.faces == null) {
            triangleMesh = TriangleMesh()
            meshView.mesh = triangleMesh
            return
        }
        val pointElementSize = triangleMesh.pointElementSize
        val faceElementSize = triangleMesh.faceElementSize
        val isWireframe = getDrawMode() == DrawMode.LINE
        if (DEBUG) println("UPDATE MESH -- " + if (isWireframe) "WIREFRAME" else "SOLID")
        val numOfPoints = pmesh.points.size() / pointElementSize
        if (DEBUG) println("numOfPoints = $numOfPoints")
        if (isWireframe) {
            // The current triangleMesh implementation gives buggy behavior when the size of faces are shrunken
            // Create a new TriangleMesh as a work around
            // [JIRA] (RT-31178)
            if (texCoordsDirty || facesDirty || pointsSizeDirty) {
                triangleMesh = TriangleMesh()
                facesDirty = true
                texCoordsDirty = facesDirty
                pointsSizeDirty = texCoordsDirty
                pointsDirty = pointsSizeDirty // to fill in the new triangle mesh
            }
            if (facesDirty) {
                facesDirty = false
                // create faces for each edge
                val facesArray = IntArray(pmesh.numEdgesInFaces * faceElementSize)
                var facesInd = 0
                var pointsInd = pmesh.points.size()
                for (face in pmesh.faces) {
                    if (DEBUG) println(
                        "face.length = " + face.size / 2 + "  -- " + face.contentToString()
                    )
                    var lastPointIndex = face[face.size - 2]
                    if (DEBUG) println("    lastPointIndex = $lastPointIndex")
                    var p = 0
                    while (p < face.size) {
                        val pointIndex = face[p]
                        if (DEBUG) println("        connecting point[$lastPointIndex] to point[$pointIndex]")
                        facesArray[facesInd++] = lastPointIndex
                        facesArray[facesInd++] = 0
                        facesArray[facesInd++] = pointIndex
                        facesArray[facesInd++] = 0
                        facesArray[facesInd++] = pointsInd / pointElementSize
                        facesArray[facesInd++] = 0
                        if (DEBUG) println("            facesInd = $facesInd")
                        pointsInd += pointElementSize
                        lastPointIndex = pointIndex
                        p += 2
                    }
                }
                triangleMesh.faces.setAll(*facesArray)
                triangleMesh.faceSmoothingGroups.clear()
            }
            if (texCoordsDirty) {
                texCoordsDirty = false
                // set simple texCoords for wireframe
                triangleMesh.texCoords.setAll(0f, 0f)
            }
            if (pointsDirty) {
                pointsDirty = false
                // create points and copy over points to the first part of the array
                val pointsArray = FloatArray(pmesh.points.size() + pmesh.numEdgesInFaces * 3)
                pmesh.points.copyTo(0, pointsArray, 0, pmesh.points.size())

                // add point for each edge
                var pointsInd = pmesh.points.size()
                for (face in pmesh.faces) {
                    var lastPointIndex = face[face.size - 2]
                    var p = 0
                    while (p < face.size) {
                        val pointIndex = face[p]
                        // get start and end point
                        val x1 = pointsArray[lastPointIndex * pointElementSize]
                        val y1 = pointsArray[lastPointIndex * pointElementSize + 1]
                        val z1 = pointsArray[lastPointIndex * pointElementSize + 2]
                        val x2 = pointsArray[pointIndex * pointElementSize]
                        val y2 = pointsArray[pointIndex * pointElementSize + 1]
                        val z2 = pointsArray[pointIndex * pointElementSize + 2]
                        val distance = abs(distanceBetweenPoints(x1, y1, z1, x2, y2, z2))
                        val offset = distance / 1000
                        // add new point
                        pointsArray[pointsInd++] = x2 + offset
                        pointsArray[pointsInd++] = y2 + offset
                        pointsArray[pointsInd++] = z2 + offset
                        lastPointIndex = pointIndex
                        p += 2
                    }
                }
                triangleMesh.points.setAll(*pointsArray)
            }
        } else {
            // The current triangleMesh implementation gives buggy behavior when the size of faces are shrunken
            // Create a new TriangleMesh as a work around
            // [JIRA] (RT-31178)
            if (texCoordsDirty || facesDirty || pointsSizeDirty) {
                triangleMesh = TriangleMesh()
                facesDirty = true
                texCoordsDirty = facesDirty
                pointsSizeDirty = texCoordsDirty
                pointsDirty = pointsSizeDirty // to fill in the new triangle mesh
            }
            if (facesDirty) {
                facesDirty = false
                // create faces and break into triangles
                val numOfFacesBefore = pmesh.faces.size
                val numOfFacesAfter = pmesh.numEdgesInFaces - 2 * numOfFacesBefore
                val facesArray = IntArray(numOfFacesAfter * faceElementSize)
                val smoothingGroupsArray = IntArray(numOfFacesAfter)
                var facesInd = 0
                for (f in pmesh.faces.indices) {
                    val face = pmesh.faces[f]
                    val currentSmoothGroup = pmesh.faceSmoothingGroups[f]
                    if (DEBUG) println(
                        "face.length = " + face.size + "  -- " + face.contentToString()
                    )
                    val firstPointIndex = face[0]
                    val firstTexIndex = face[1]
                    var lastPointIndex = face[2]
                    var lastTexIndex = face[3]
                    var p = 4
                    while (p < face.size) {
                        val pointIndex = face[p]
                        val texIndex = face[p + 1]
                        facesArray[facesInd * faceElementSize] = firstPointIndex
                        facesArray[facesInd * faceElementSize + 1] = firstTexIndex
                        facesArray[facesInd * faceElementSize + 2] = lastPointIndex
                        facesArray[facesInd * faceElementSize + 3] = lastTexIndex
                        facesArray[facesInd * faceElementSize + 4] = pointIndex
                        facesArray[facesInd * faceElementSize + 5] = texIndex
                        smoothingGroupsArray[facesInd] = currentSmoothGroup
                        facesInd++
                        lastPointIndex = pointIndex
                        lastTexIndex = texIndex
                        p += 2
                    }
                }
                triangleMesh.faces.setAll(*facesArray)
                triangleMesh.faceSmoothingGroups.setAll(*smoothingGroupsArray)
            }
            if (texCoordsDirty) {
                texCoordsDirty = false
                triangleMesh.texCoords.setAll(pmesh.texCoords)
            }
            if (pointsDirty) {
                pointsDirty = false
                triangleMesh.points.setAll(pmesh.points)
            }
        }
        if (DEBUG) println("CREATING TRIANGLE MESH")
        if (DEBUG) println(
            "    points    = " + Arrays.toString(
                (meshView.mesh as TriangleMesh).points.toArray(
                    null
                )
            )
        )
        if (DEBUG) println(
            "    texCoords = " + Arrays.toString(
                (meshView.mesh as TriangleMesh).texCoords.toArray(
                    null
                )
            )
        )
        if (DEBUG) println(
            "    faces     = " + Arrays.toString(
                (meshView.mesh as TriangleMesh).faces.toArray(
                    null
                )
            )
        )
        if (meshView.mesh !== triangleMesh) {
            meshView.mesh = triangleMesh
        }
        facesDirty = false
        texCoordsDirty = facesDirty
        pointsSizeDirty = texCoordsDirty
        pointsDirty = pointsSizeDirty
    }

    private fun distanceBetweenPoints(
        x1: Float,
        y1: Float,
        z1: Float,
        x2: Float,
        y2: Float,
        z2: Float
    ): Float {
        return sqrt(
            (z2 - z1).toDouble().pow(2.0) +
                (x2 - x1).toDouble().pow(2.0) +
                (y2 - y1).toDouble().pow(2.0)
        ).toFloat()
    }

    companion object {
        private const val DEBUG = false
    }

    // =========================================================================
    // CONSTRUCTORS
    init {
        meshView.materialProperty().bind(materialProperty())
        children.add(meshView)
    }
}

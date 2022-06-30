/*
 * Copyright (c) 2008, 2014, Oracle and/or its affiliates.
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
package eu.mihosoft.jcsg.ext.openjfx.importers.obj

import eu.mihosoft.jcsg.ext.openjfx.importers.SmoothingGroups
import eu.mihosoft.jcsg.ext.openjfx.shape3d.PolygonMesh
import eu.mihosoft.jcsg.ext.openjfx.shape3d.PolygonMeshView
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * OBJ object loader
 */
internal class PolyObjImporter {
    private fun vertexIndex(vertexIndex: Int): Int {
        return if (vertexIndex < 0) {
            vertexIndex + vertexes.size / 3
        } else {
            vertexIndex - 1
        }
    }

    private fun uvIndex(uvIndex: Int): Int {
        return if (uvIndex < 0) {
            uvIndex + uvs.size / 2
        } else {
            uvIndex - 1
        }
    }

    private fun normalIndex(normalIndex: Int): Int {
        return if (normalIndex < 0) {
            normalIndex + normals.size / 3
        } else {
            normalIndex - 1
        }
    }

    val meshes: Set<String>
        get() {
            return _meshes.keys
        }

    private val _meshes: MutableMap<String, PolygonMesh> = HashMap()
    private val _materials: MutableMap<String, Material> = HashMap()
    private val _materialLibrary: MutableList<Map<String, Material>> = ArrayList()
    private var _objFilename: String? = null

    constructor(filename: String) {
        _objFilename = filename
        log("Reading filename = $filename")
        read(URL(filename).openStream())
    }

    constructor(inputStream: InputStream) {
        read(inputStream)
    }

    val mesh: PolygonMesh
        get() = _meshes.values.iterator().next()

    val material: Material
        get() {
            return _materials.values.iterator().next()
        }

    fun getMesh(key: String): PolygonMesh? {
        return _meshes[key]
    }

    fun getMaterial(key: String): Material? {
        return _materials[key]
    }

    fun buildPolygonMeshView(key: String): PolygonMeshView {
        val polygonMeshView = PolygonMeshView()
        polygonMeshView.id = key
        polygonMeshView.material = _materials[key]!!
        polygonMeshView.mesh = _meshes[key]
        //        polygonMeshView.setCullFace(CullFace.NONE); TODO
        return polygonMeshView
    }

    private val vertexes = FloatArrayList()
    private val uvs = FloatArrayList()
    private val faces: MutableList<IntArray> = ArrayList()
    private val smoothingGroups = IntegerArrayList()
    private val normals = FloatArrayList()
    private val faceNormals: MutableList<IntArray> = ArrayList()
    private var _material: Material = PhongMaterial(Color.WHITE)
    private var facesStart = 0
    private var facesNormalStart = 0
    private var smoothingGroupsStart = 0
    @Throws(IOException::class)
    private fun read(inputStream: InputStream) {
        val br = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var currentSmoothGroup = 0
        var key = "default"
        while (br.readLine().also { line = it } != null) {
            try {
                if (line.startsWith("g ") || line == "g") {
                    addMesh(key)
                    key = if (line.length > 2) line.substring(2) else "default"
                    log("key = $key")
                } else if (line.startsWith("v ")) {
                    val split = line.substring(2).trim { it <= ' ' }.split(" +").toTypedArray()
                    val x = split[0].toFloat() * scale
                    val y = split[1].toFloat() * scale
                    val z = split[2].toFloat() * scale

                    //                log("x = " + x + ", y = " + y + ", z = " + z);
                    vertexes.add(x)
                    vertexes.add(y)
                    vertexes.add(z)
                    if (flatXZ) {
                        uvs.add(x)
                        uvs.add(z)
                    }
                } else if (line.startsWith("vt ")) {
                    val split = line.substring(3).trim { it <= ' ' }.split(" +").toTypedArray()
                    val u = if (split[0].trim { it <= ' ' }
                            .equals("nan", ignoreCase = true)) Float.NaN else split[0].toFloat()
                    val v = if (split[1].trim { it <= ' ' }
                            .equals("nan", ignoreCase = true)) Float.NaN else split[1].toFloat()

                    //                log("u = " + u + ", v = " + v);
                    uvs.add(u)
                    uvs.add(1 - v)
                } else if (line.startsWith("f ")) {
                    val split = line.substring(2).trim { it <= ' ' }.split(" +").toTypedArray()
                    val faceIndexes = IntArray(split.size * 2)
                    val faceNormalIndexes = IntArray(split.size)
                    for (i in split.indices) {
                        val split2 = split[i].split("/").toTypedArray()
                        faceIndexes[i * 2] = vertexIndex(split2[0].toInt())
                        faceIndexes[i * 2 + 1] =
                            if (split2.size > 1 && split2[1].isNotEmpty()) uvIndex(
                                split2[1].toInt()
                            ) else -1
                        faceNormalIndexes[i] =
                            if (split2.size > 2 && split2[2].isNotEmpty()) normalIndex(
                                split2[2].toInt()
                            ) else -1
                    }
                    faces.add(faceIndexes)
                    faceNormals.add(faceNormalIndexes)
                    smoothingGroups.add(currentSmoothGroup)
                } else if (line.startsWith("s ")) {
                    currentSmoothGroup = if (line.substring(2) == "off") {
                        0
                    } else {
                        line.substring(2).toInt()
                    }
                } else if (line.startsWith("mtllib ")) {
                    // setting materials lib
                    val split = line.substring("mtllib ".length).trim { it <= ' ' }.split(" +")
                        .toTypedArray()
                    for (filename in split) {
                        val mtlReader = MtlReader(filename, _objFilename!!)
                        _materialLibrary.add(mtlReader.materials)
                    }
                } else if (line.startsWith("usemtl ")) {
                    addMesh(key)
                    // setting new material for next mesh
                    val materialName = line.substring("usemtl ".length)
                    for (mm in _materialLibrary) {
                        val m = mm[materialName]
                        if (m != null) {
                            _material = m
                            break
                        }
                    }
                } else if (line.isEmpty() || line.startsWith("#")) {
                    // comments and empty lines are ignored
                } else if (line.startsWith("vn ")) {
                    val split = line.substring(2).trim { it <= ' ' }.split(" +").toTypedArray()
                    val x = split[0].toFloat()
                    val y = split[1].toFloat()
                    val z = split[2].toFloat()
                    normals.add(x)
                    normals.add(y)
                    normals.add(z)
                } else {
                    log("line skipped: $line")
                }
            } catch (ex: Exception) {
                Logger.getLogger(MtlReader::class.java.name)
                    .log(Level.SEVERE, "Failed to parse line:$line", ex)
            }
        }
        addMesh(key)
        log(
            "Totally loaded " + vertexes.size / 3.0 + " vertexes, "
                + uvs.size / 2.0 + " uvs, "
                + faces.size / 6.0 + " faces, "
                + smoothingGroups.size + " smoothing groups."
        )
    }

    private fun addMesh(key: String) {
        var key = key
        if (facesStart >= faces.size) {
            // we're only interested in faces
            smoothingGroupsStart = smoothingGroups.size
            return
        }
        val vertexMap: MutableMap<Int, Int> = HashMap(vertexes.size / 2)
        val uvMap: MutableMap<Int, Int> = HashMap(uvs.size / 2)
        val normalMap: MutableMap<Int, Int> = HashMap(normals.size / 2)
        val newVertexes = FloatArrayList(vertexes.size / 2)
        val newUVs = FloatArrayList(uvs.size / 2)
        val newNormals = FloatArrayList(normals.size / 2)
        var useNormals = true
        val faceArrays = arrayOfNulls<IntArray>(faces.size - facesStart)
        val faceNormalArrays = arrayOfNulls<IntArray>(faceNormals.size - facesNormalStart)
        for (i in facesStart until faces.size) {
            val faceIndexes = faces[i]
            val faceNormalIndexes = faceNormals[i]
            var j = 0
            while (j < faceIndexes.size) {
                val vi = faceIndexes[j]
                var nvi = vertexMap[vi]
                if (nvi == null) {
                    nvi = newVertexes.size / 3
                    vertexMap[vi] = nvi
                    newVertexes.add(vertexes[vi * 3])
                    newVertexes.add(vertexes[vi * 3 + 1])
                    newVertexes.add(vertexes[vi * 3 + 2])
                }
                faceIndexes[j] = nvi
                //                faces.set(i, nvi);
                val uvi = faceIndexes[j + 1]
                var nuvi = uvMap[uvi]
                if (nuvi == null) {
                    nuvi = newUVs.size / 2
                    uvMap[uvi] = nuvi
                    if (uvi >= 0) {
                        newUVs.add(uvs[uvi * 2])
                        newUVs.add(uvs[uvi * 2 + 1])
                    } else {
                        newUVs.add(0f)
                        newUVs.add(0f)
                    }
                }
                faceIndexes[j + 1] = nuvi
                //                faces.set(i + 1, nuvi);
                val ni = faceNormalIndexes[j / 2]
                var nni = normalMap[ni]
                if (nni == null) {
                    nni = newNormals.size / 3
                    normalMap[ni] = nni
                    if (ni >= 0 && normals.size >= (ni + 1) * 3) {
                        newNormals.add(normals[ni * 3])
                        newNormals.add(normals[ni * 3 + 1])
                        newNormals.add(normals[ni * 3 + 2])
                    } else {
                        useNormals = false
                        newNormals.add(0f)
                        newNormals.add(0f)
                        newNormals.add(0f)
                    }
                }
                faceNormalIndexes[j / 2] = nni
                j += 2
            }
            faceArrays[i - facesStart] = faceIndexes
            faceNormalArrays[i - facesNormalStart] = faceNormalIndexes
        }
        val mesh = PolygonMesh(
            newVertexes.toFloatArray(),
            newUVs.toFloatArray(),
            faceArrays
        )

        // Use normals if they are provided
        if (useNormals) {
            val smGroups: IntArray = SmoothingGroups.calcSmoothGroups(
                faceArrays.requireNoNulls(),
                faceNormalArrays.requireNoNulls(),
                newNormals.toFloatArray()
            )
            mesh.faceSmoothingGroups.setAll(*smGroups)
        } else {
            mesh.faceSmoothingGroups.setAll(
                *(smoothingGroups.subList(
                    smoothingGroupsStart,
                    smoothingGroups.size
                ) as IntegerArrayList).toIntArray()
            )
        }
        if (debug) {
            println("mesh.points = " + mesh.points)
            println("mesh.texCoords = " + mesh.texCoords)
            println("mesh.faces: ")
            for (face in mesh.faces) {
                println("    face:: " + face.contentToString())
            }
        }
        var keyIndex = 2
        val keyBase = key
        while (_meshes[key] != null) {
            key = keyBase + " (" + keyIndex++ + ")"
        }
        _meshes[key] = mesh
        _materials[key] = _material
        log(
            "Added mesh '" + key + "' of " + mesh.points.size() / 3 + " vertexes, "
                + mesh.texCoords.size() / 2 + " uvs, "
                + mesh.faces.size + " faces, "
                + 0 + " smoothing groups."
        )
        log("material diffuse color = " + (_material as PhongMaterial).diffuseColor)
        log("material diffuse map = " + (_material as PhongMaterial).diffuseMap)
        facesStart = faces.size
        facesNormalStart = faceNormals.size
        smoothingGroupsStart = smoothingGroups.size
    }

    companion object {
        private var debug = false
        private var scale = 1f
        private var flatXZ = false
        fun log(string: String?) {
            if (debug) {
                println(string)
            }
        }

        fun setDebug(debug: Boolean) {
            Companion.debug = debug
        }

        fun setScale(scale: Float) {
            Companion.scale = scale
        }

        fun setFlatXZ(flatXZ: Boolean) {
            Companion.flatXZ = flatXZ
        }
    }
}
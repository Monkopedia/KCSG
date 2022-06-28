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
package eu.mihosoft.jcsg.ext.openjfx.importers.obj

import eu.mihosoft.jcsg.ObjFile
import eu.mihosoft.jcsg.ext.openjfx.importers.SmoothingGroups
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.CullFace
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Obj file reader
 */
class ObjImporter {
    private fun vertexIndex(vertexIndex: Int): Int {
        return if (vertexIndex < 0) {
            vertexIndex + _vertexes.size / 3
        } else {
            vertexIndex - 1
        }
    }

    private fun uvIndex(uvIndex: Int): Int {
        return if (uvIndex < 0) {
            uvIndex + _uvs.size / 2
        } else {
            uvIndex - 1
        }
    }

    private fun normalIndex(normalIndex: Int): Int {
        return if (normalIndex < 0) {
            normalIndex + _normals.size / 3
        } else {
            normalIndex - 1
        }
    }

    val meshes: Set<String>
        get() {
            return _meshes.keys
        }

    private val _meshes: MutableMap<String, TriangleMesh?> = HashMap()
    private val _materials: MutableMap<String, Material> = HashMap()
    private val _materialLibrary: MutableList<Map<String, Material>> = ArrayList()
    private var _objFileUrl: String? = null

    constructor(objFileUrl: String) {
        this._objFileUrl = objFileUrl
        log("Reading filename = $objFileUrl")
        read(URL(objFileUrl).openStream())
    }

    constructor(inputStream: InputStream?) {
        read(inputStream)
    }

    constructor(obj: ObjFile) {
        read(obj.objStream, obj.mtlStream)
    }

    val mesh: TriangleMesh?
        get() = _meshes.values.iterator().next()
    val meshCollection: Collection<TriangleMesh?>
        get() = _meshes.values
    val materialCollection: Collection<Material>
        get() = _materials.values

    val material: Material
        get() {
            return _materials.values.iterator().next()
        }

    fun getMesh(key: String): TriangleMesh? {
        return _meshes[key]
    }

    fun getMaterial(key: String): Material? {
        return _materials[key]
    }

    fun buildMeshView(key: String): MeshView {
        val meshView = MeshView()
        meshView.id = key
        meshView.material = _materials[key]
        meshView.mesh = _meshes[key]
        meshView.cullFace = CullFace.NONE
        return meshView
    }

    private val _vertexes = FloatArrayList()
    private val _uvs = FloatArrayList()
    private val _faces = IntegerArrayList()
    private val _smoothingGroups = IntegerArrayList()
    private val _normals = FloatArrayList()
    private val _faceNormals = IntegerArrayList()
    private var _material: Material = PhongMaterial(Color.WHITE)
    private var _facesStart = 0
    private var _facesNormalStart = 0
    private var _smoothingGrouStart = 0

    @Throws(IOException::class)
    private fun read(objInputStream: InputStream?, mtlInputStream: InputStream? = null) {
        val br = BufferedReader(InputStreamReader(objInputStream))
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
                    _vertexes.add(x)
                    _vertexes.add(y)
                    _vertexes.add(z)
                    if (flatXZ) {
                        _uvs.add(x)
                        _uvs.add(z)
                    }
                } else if (line.startsWith("vt ")) {
                    val split = line.substring(3).trim { it <= ' ' }.split(" +").toTypedArray()
                    val u = split[0].toFloat()
                    val v = split[1].toFloat()

                    //                log("u = " + u + ", v = " + v);
                    _uvs.add(u)
                    _uvs.add(1 - v)
                } else if (line.startsWith("f ")) {
                    val split = line.substring(2).trim { it <= ' ' }.split(" +").toTypedArray()
                    val data = arrayOfNulls<IntArray>(split.size)
                    var uvProvided = true
                    var normalProvided = true
                    for (i in split.indices) {
                        val split2 = split[i].split("/").toTypedArray()
                        if (split2.size < 2) {
                            uvProvided = false
                        }
                        if (split2.size < 3) {
                            normalProvided = false
                        }
                        data[i] = IntArray(split2.size)
                        for (j in split2.indices) {
                            if (split2[j].length == 0) {
                                data[i]!![j] = 0
                                if (j == 1) {
                                    uvProvided = false
                                }
                                if (j == 2) {
                                    normalProvided = false
                                }
                            } else {
                                data[i]!![j] = split2[j].toInt()
                            }
                        }
                    }
                    val v1 = vertexIndex(data[0]!![0])
                    var uv1 = -1
                    var n1 = -1
                    if (uvProvided && !flatXZ) {
                        uv1 = uvIndex(data[0]!![1])
                        if (uv1 < 0) {
                            uvProvided = false
                        }
                    }
                    if (normalProvided) {
                        n1 = normalIndex(data[0]!![2])
                        if (n1 < 0) {
                            normalProvided = false
                        }
                    }
                    for (i in 1 until data.size - 1) {
                        val v2 = vertexIndex(data[i]!![0])
                        val v3 = vertexIndex(data[i + 1]!![0])
                        var uv2 = -1
                        var uv3 = -1
                        var n2 = -1
                        var n3 = -1
                        if (uvProvided && !flatXZ) {
                            uv2 = uvIndex(data[i]!![1])
                            uv3 = uvIndex(data[i + 1]!![1])
                        }
                        if (normalProvided) {
                            n2 = normalIndex(data[i]!![2])
                            n3 = normalIndex(data[i + 1]!![2])
                        }

                        //                    log("v1 = " + v1 + ", v2 = " + v2 + ", v3 = " + v3);
                        //                    log("uv1 = " + uv1 + ", uv2 = " + uv2 + ", uv3 = " + uv3);
                        _faces.add(v1)
                        _faces.add(uv1)
                        _faces.add(v2)
                        _faces.add(uv2)
                        _faces.add(v3)
                        _faces.add(uv3)
                        _faceNormals.add(n1)
                        _faceNormals.add(n2)
                        _faceNormals.add(n3)
                        _smoothingGroups.add(currentSmoothGroup)
                    }
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
                    if (mtlInputStream == null) {
                        for (filename in split) {
                            val mtlReader = MtlReader(filename, _objFileUrl!!)
                            _materialLibrary.add(mtlReader.materials)
                        }
                    } else {
                        if (split.size > 1) {
                            log("WARNING: more than one mtllib not supported if reading from streams! Using only one mtllib.")
                            val mtlReader = MtlReader(mtlInputStream)
                            _materialLibrary.add(mtlReader.materials)
                        }
                    }
                } else if (line.startsWith("usemtl ")) {
                    addMesh(key)
                    // setting new material for next mesh
                    val materialName = line.substring("usemtl ".length)
                    for (mm in _materialLibrary) {
                        val m = mm!![materialName]
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
                    _normals.add(x)
                    _normals.add(y)
                    _normals.add(z)
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
            "Totally loaded ${_vertexes.size / 3.0} vertexes, " +
                "${_uvs.size / 2.0} uvs, ${_faces.size / 6.0} faces, " +
                "${_smoothingGroups.size} smoothing groups."
        )
    }

    private fun addMesh(key: String) {
        var key = key
        if (_facesStart >= _faces.size) {
            // we're only interested in faces
            _smoothingGrouStart = _smoothingGroups.size
            return
        }
        val vertexMap: MutableMap<Int, Int> = HashMap(_vertexes.size / 2)
        val uvMap: MutableMap<Int, Int> = HashMap(_uvs.size / 2)
        val normalMap: MutableMap<Int, Int> = HashMap(_normals.size / 2)
        val newVertexes = FloatArrayList(_vertexes.size / 2)
        val newUVs = FloatArrayList(_uvs.size / 2)
        val newNormals = FloatArrayList(_normals.size / 2)
        var useNormals = true
        var i = _facesStart
        while (i < _faces.size) {
            val vi = _faces[i]
            var nvi = vertexMap[vi]
            if (nvi == null) {
                nvi = newVertexes.size / 3
                vertexMap[vi] = nvi
                newVertexes.add(_vertexes[vi * 3])
                newVertexes.add(_vertexes[vi * 3 + 1])
                newVertexes.add(_vertexes[vi * 3 + 2])
            }
            _faces[i] = nvi
            val uvi = _faces[i + 1]
            var nuvi = uvMap[uvi]
            if (nuvi == null) {
                nuvi = newUVs.size / 2
                uvMap[uvi] = nuvi
                if (uvi >= 0) {
                    newUVs.add(_uvs[uvi * 2])
                    newUVs.add(_uvs[uvi * 2 + 1])
                } else {
                    newUVs.add(0f)
                    newUVs.add(0f)
                }
            }
            _faces[i + 1] = nuvi
            if (useNormals) {
                val ni = _faceNormals[i / 2]
                var nni = normalMap[ni]
                if (nni == null) {
                    nni = newNormals.size / 3
                    normalMap[ni] = nni
                    if (ni >= 0 && _normals.size >= (ni + 1) * 3) {
                        newNormals.add(_normals[ni * 3])
                        newNormals.add(_normals[ni * 3 + 1])
                        newNormals.add(_normals[ni * 3 + 2])
                    } else {
                        useNormals = false
                        newNormals.add(0f)
                        newNormals.add(0f)
                        newNormals.add(0f)
                    }
                }
                _faceNormals[i / 2] = nni
            }
            i += 2
        }
        val mesh = TriangleMesh()
        mesh.points.setAll(*newVertexes.toFloatArray())
        mesh.texCoords.setAll(*newUVs.toFloatArray())
        mesh.faces.setAll(
            *(
                _faces.subList(
                    _facesStart,
                    _faces.size
                ) as IntegerArrayList
                ).toIntArray()
        )

        // Use normals if they are provided
        if (useNormals) {
            val newFaces =
                (_faces.subList(_facesStart, _faces.size) as IntegerArrayList).toIntArray()
            val newFaceNormals = (
                _faceNormals.subList(
                    _facesNormalStart,
                    _faceNormals.size
                ) as IntegerArrayList
                ).toIntArray()
            val smGroups: IntArray = SmoothingGroups.Companion.calcSmoothGroups(
                mesh,
                newFaces,
                newFaceNormals,
                newNormals.toFloatArray()
            )
            mesh.faceSmoothingGroups.setAll(*smGroups)
        } else {
            mesh.faceSmoothingGroups.setAll(
                *(
                    _smoothingGroups.subList(
                        _smoothingGrouStart,
                        _smoothingGroups.size
                    ) as IntegerArrayList
                    ).toIntArray()
            )
        }
        var keyIndex = 2
        val keyBase = key
        while (_meshes[key] != null) {
            key = keyBase + " (" + keyIndex++ + ")"
        }
        _meshes[key] = mesh
        _materials[key] = _material
        log(
            "Added mesh '" + key + "' of " + mesh.points.size() / mesh.pointElementSize + " vertexes, " +
                mesh.texCoords.size() / mesh.texCoordElementSize + " uvs, " +
                mesh.faces.size() / mesh.faceElementSize + " faces, " +
                mesh.faceSmoothingGroups.size() + " smoothing groups."
        )
        log("material diffuse color = " + (_material as PhongMaterial).diffuseColor)
        log("material diffuse map = " + (_material as PhongMaterial).diffuseMap)
        _facesStart = _faces.size
        _facesNormalStart = _faceNormals.size
        _smoothingGrouStart = _smoothingGroups.size
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

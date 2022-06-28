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

import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import java.io.*
import java.net.URL
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/** Reader for OBJ file MTL material files.  */
class MtlReader {
    private var baseUrl: String? = null

    constructor(filename: String, parentUrl: String) {
        baseUrl = parentUrl.substring(0, parentUrl.lastIndexOf('/') + 1)
        val fileUrl = baseUrl + filename
        try {
            val mtlUrl = URL(fileUrl)
            ObjImporter.Companion.log("Reading material from filename = $mtlUrl")
            read(mtlUrl.openStream())
        } catch (ex: FileNotFoundException) {
            System.err.println("No material file found for obj. [$fileUrl]")
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    constructor(stream: InputStream) {
        try {
            ObjImporter.Companion.log("Reading material from stream")
            read(stream)
        } catch (ex: FileNotFoundException) {
            System.err.println("No material file found for obj. [stream]")
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private val _materials: MutableMap<String, Material> = HashMap()
    private var _material = PhongMaterial()
    private var _modified = false

    @Throws(IOException::class)
    private fun read(inputStream: InputStream) {
        val br = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var name = "default"
        while (br.readLine().also { line = it } != null) {
            try {
                if (line.isEmpty() || line.startsWith("#")) {
                    // comments and empty lines are ignored
                } else if (line.startsWith("newmtl ")) {
                    addMaterial(name)
                    name = line.substring("newmtl ".length)
                } else if (line.startsWith("Kd ")) {
                    _material.diffuseColor = readColor(line.substring(3))
                    _modified = true
                } else if (line.startsWith("Ks ")) {
                    _material.specularColor = readColor(line.substring(3))
                    _modified = true
                } else if (line.startsWith("Ns ")) {
                    _material.specularPower = line.substring(3).toDouble()
                    _modified = true
                } else if (line.startsWith("map_Kd ")) {
                    _material.diffuseColor = Color.WHITE
                    _material.diffuseMap = loadImage(line.substring("map_Kd ".length))
                    //                    material.setSelfIlluminationMap(loadImage(line.substring("map_Kd ".length())));
//                    material.setSpecularColor(Color.WHITE);
                    _modified = true
                    //            } else if (line.startsWith("illum ")) {
                    //                int illumNo = Integer.parseInt(line.substring("illum ".length()));
                    /*
                        0	 Color on and Ambient off
                        1	 Color on and Ambient on
                        2	 Highlight on
                        3	 Reflection on and Ray trace on
                        4	 Transparency: Glass on
                             Reflection: Ray trace on
                        5	 Reflection: Fresnel on and Ray trace on
                        6	 Transparency: Refraction on
                             Reflection: Fresnel off and Ray trace on
                        7	 Transparency: Refraction on
                             Reflection: Fresnel on and Ray trace on
                        8	 Reflection on and Ray trace off
                        9	 Transparency: Glass on
                             Reflection: Ray trace off
                        10	 Casts shadows onto invisible surfaces
                     */
                } else {
                    ObjImporter.Companion.log("material line ignored for $name: $line")
                }
            } catch (ex: Exception) {
                Logger.getLogger(MtlReader::class.java.name)
                    .log(Level.SEVERE, "Failed to parse line:$line", ex)
            }
        }
        addMaterial(name)
    }

    private fun addMaterial(name: String) {
        if (_modified) {
            if (!_materials.containsKey(name)) {
                _materials[name] = _material
            } else {
                ObjImporter.Companion.log("This material is already added. Ignoring $name")
            }
            _material = PhongMaterial(Color.WHITE)
        }
    }

    private fun readColor(line: String): Color {
        val split = line.trim { it <= ' ' }.split(" +").toTypedArray()
        val red = split[0].toFloat()
        val green = split[1].toFloat()
        val blue = split[2].toFloat()
        return Color.color(red.toDouble(), green.toDouble(), blue.toDouble())
    }

    private fun loadImage(filename: String): Image {
        var filename = filename
        filename = baseUrl + filename
        ObjImporter.Companion.log("Loading image from $filename")
        val image = Image(filename)
        return Image(filename)
    }

    val materials: Map<String, Material>
        get() {
            return Collections.unmodifiableMap(_materials)
        }
}

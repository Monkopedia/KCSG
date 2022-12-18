/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

import kotlin.Throws
import java.util.Locale
import java.nio.file.Paths
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class ObjFile internal constructor(private var _obj: String, private val _mtl: String) {
    private var _objStream: InputStream? = null
    private var _mtlStream: InputStream? = null
    @Throws(IOException::class)
    fun toFiles(p: Path) {
        val parent = p.parent
        var fileName = p.fileName.toString()
        if (fileName.lowercase(Locale.getDefault()).endsWith(".obj")
            || fileName.lowercase(Locale.getDefault()).endsWith(".mtl")
        ) {
            fileName = fileName.substring(0, fileName.length - 4)
        }
        val objName = "$fileName.obj"
        val mtlName = "$fileName.mtl"
        _obj = _obj.replace(MTL_NAME, mtlName)
        _objStream = null
        if (parent == null) {
            FileUtil.write(Paths.get(objName), _obj)
            FileUtil.write(Paths.get(mtlName), _mtl)
        } else {
            FileUtil.write(Paths.get(parent.toString(), objName), _obj)
            FileUtil.write(Paths.get(parent.toString(), mtlName), _mtl)
        }
    }

    val obj: String
        get() {
            return _obj
        }

    val mtl: String
        get() {
            return _mtl
        }

    val objStream: InputStream
        get() {
            if (_objStream == null) {
                _objStream = ByteArrayInputStream(_obj.toByteArray(StandardCharsets.UTF_8))
            }
            return _objStream!!
        }

    val mtlStream: InputStream
        get() {
            if (_mtlStream == null) {
                _mtlStream = ByteArrayInputStream(_mtl.toByteArray(StandardCharsets.UTF_8))
            }
            return _mtlStream!!
        }

    companion object {
        const val MTL_NAME = "\$JCSG_MTL_NAME$"
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.files.Path
import kotlinx.io.writeString

/**
 */
class ObjFile internal constructor(private val _obj: String, private val _mtl: String) {
    fun toFiles(p: Path) {
        val parent = p.parent
        var fileName = p.name
        // Strip a .obj/.mtl extension, but never down to an empty base name.
        if (fileName.length > 4
            && (fileName.lowercase().endsWith(".obj") || fileName.lowercase().endsWith(".mtl"))
        ) {
            fileName = fileName.substring(0, fileName.length - 4)
        }
        val objName = "$fileName.obj"
        val mtlName = "$fileName.mtl"
        // Substitute locally: the placeholder must survive for later exports.
        val objText = _obj.replace(MTL_NAME, mtlName)
        if (parent == null) {
            FileUtil.write(Path(objName), objText)
            FileUtil.write(Path(mtlName), _mtl)
        } else {
            FileUtil.write(Path(parent, objName), objText)
            FileUtil.write(Path(parent, mtlName), _mtl)
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

    val objSource: Source
        get() {
            return Buffer().apply {
                writeString(_obj)
            }
        }

    val mtlSource: Source
        get() {
            return Buffer().apply {
                writeString(_mtl)
            }
        }

    companion object {
        const val MTL_NAME = "\$JCSG_MTL_NAME$"
    }
}

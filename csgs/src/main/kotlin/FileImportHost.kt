package com.monkopedia.csgs

import com.monkopedia.kcsg.KcsgHost
import java.io.File
import java.nio.file.Path

class FileImportHost(imports: List<File>) : KcsgHost {
    private val dirs = imports.filter { it.isDirectory }
    private val files = imports.filter { it.isFile }

    override fun findStl(stlName: String): Path {
        files.forEach { f ->
            if (f.nameWithoutExtension == stlName && f.extension == "stl") {
                return f.toPath()
            }
        }
        dirs.forEach { d ->
            File(d, "$stlName.stl").takeIf { it.exists() }
                ?.let { return it.toPath() }
        }
        return error("Cannot resolve file $stlName")
    }
}

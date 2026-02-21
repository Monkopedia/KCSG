/**
 * FileUtil.java
 *
 * Copyright 2014-2014 Michael Hoffer <info></info>@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info></info>@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info></info>@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info></info>@michaelhoffer.de>.
 */
package com.monkopedia.kcsg

import java.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

/**
 * File util class.
 */
object FileUtil {
    private val fileSystem: FileSystem = SystemFileSystem
    private val logger = Logger.tagged("KCSG.FileUtil")

    /**
     * Writes the specified string to a file.
     *
     * @param p file destination (existing files will be overwritten)
     * @param s string to save
     *
    * @throws IOException if writing to file fails
     */
    @Throws(IOException::class)
    fun write(p: Path, s: String) {
        fileSystem.sink(p).buffered().use { sink ->
            sink.writeString(s)
        }
    }

    /**
     * Reads the specified file to a string.
     *
     * @param p file to read
     * @return the content of the file
     *
    * @throws IOException if reading from file failed
     */
    @Throws(IOException::class)
    fun read(p: Path): String {
        return fileSystem.source(p).buffered().use { source ->
            source.readString()
        }
    }

    /**
     * Saves the specified csg using STL ASCII format.
     *
     * @param path destination path
     * @param csg csg to save
     * @throws java.io.IOException
     */
    @Throws(IOException::class)
    fun toStlFile(path: Path, csg: CSG) {
        fileSystem.sink(path).buffered().use { out ->
            out.writeString("solid v3d.csg\n")
            csg.polygons.forEach { p: Polygon ->
                try {
                    out.writeString(p.toStlString())
                } catch (ex: Exception) {
                    logger.error("Error writing polygon to STL", ex)
                    throw RuntimeException(ex)
                }
            }
            out.writeString("endsolid v3d.csg\n")
        }
    }
}

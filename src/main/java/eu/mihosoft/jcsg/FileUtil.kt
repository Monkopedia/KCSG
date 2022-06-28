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
package eu.mihosoft.jcsg

import eu.mihosoft.jcsg.CSG
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.logging.Level
import java.util.logging.Logger

/**
 * File util class.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class FileUtil private constructor() {
    companion object {
        /**
         * Writes the specified string to a file.
         *
         * @param p file destination (existing files will be overwritten)
         * @param s string to save
         *
         * @throws IOException if writing to file fails
         */
        @Throws(IOException::class)
        fun write(p: Path?, s: String?) {
            Files.newBufferedWriter(
                p, Charset.forName("UTF-8"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            ).use { writer -> writer.write(s, 0, s!!.length) }
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
        fun read(p: Path?): String {
            return String(Files.readAllBytes(p), Charset.forName("UTF-8"))
        }

        /**
         * Saves the specified csg using STL ASCII format.
         *
         * @param path destination path
         * @param csg csg to save
         * @throws java.io.IOException
         */
        @Throws(IOException::class)
        fun toStlFile(path: Path?, csg: CSG) {
            Files.newBufferedWriter(
                path, Charset.forName("UTF-8"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            ).use { out ->
                out.append("solid v3d.csg\n")
                csg.polygons!!.stream().forEach { p: Polygon? ->
                    try {
                        out.append(p!!.toStlString())
                    } catch (ex: IOException) {
                        Logger.getLogger(CSG::class.java.name).log(Level.SEVERE, null, ex)
                        throw RuntimeException(ex)
                    }
                }
                out.append("endsolid v3d.csg\n")
            }
        }
    }

    init {
        throw AssertionError("Don't instantiate me", null)
    }
}
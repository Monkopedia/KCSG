/**
 * STL.java
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

import com.monkopedia.kcsg.ext.imagej.STLLoader
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path

/**
 * Loads a CSG from stl.
 */
object STL {
    /**
     * Loads a CSG from stl.
     * @param path file path
     * @return CSG
     * @throws IOException if loading failed
     */
    @Throws(IOException::class)
    fun file(path: Path): CSG {
        val loader = STLLoader()
        val polygons: MutableList<Polygon> = ArrayList()
        var vertices: MutableList<Vector3d> = ArrayList()
        for (p in loader.parse(path.toFile())) {
            vertices.add(p.copy())
            if (vertices.size == 3) {
                polygons.add(Polygon.fromPoints(vertices))
                vertices = ArrayList()
            }
        }
        return CSG.fromPolygons(PropertyStorage(), polygons)
    }
    /**
     * Loads a CSG from stl.
     * @return CSG
     * @throws IOException if loading failed
     */
    @Throws(IOException::class)
    fun from(inputStreamFactory: () -> InputStream, length: () -> Long): CSG {
        val loader = STLLoader()
        val polygons: MutableList<Polygon> = ArrayList()
        var vertices: MutableList<Vector3d> = ArrayList()
        for (p in loader.parse(inputStreamFactory, length)) {
            vertices.add(p.copy())
            if (vertices.size == 3) {
                polygons.add(Polygon.fromPoints(vertices))
                vertices = ArrayList()
            }
        }
        return CSG.fromPolygons(PropertyStorage(), polygons)
    }
}

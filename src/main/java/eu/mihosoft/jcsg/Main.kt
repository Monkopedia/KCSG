/**
 * Main.java
 *
 * Copyright 2014-2014 Michael Hoffer <info></info>@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info></info>@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info></info>@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info></info>@michaelhoffer.de>.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.vvecmath.Transform
import javafx.scene.paint.Color
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
object Main {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        // we use cube and sphere as base geometries
        val cube = Cube(2.0).toCSG()!!.color(Color.RED)
        val sphere = Sphere(1.25).toCSG()!!.color(Color.BLUE)
        val cyl = Cylinder(0.5, 3.0, 16).toCSG()!!
            .transformed(Transform.unity().translateZ(-1.5)).color(Color.GREEN)
        val cyl2 = cyl.transformed(Transform.unity().rotY(90.0))
        val cyl3 = cyl.transformed(Transform.unity().rotX(90.0))

        // perform union, difference and intersection
        val cubePlusSphere = cube.union(sphere)
        val cubeMinusSphere = cube.difference(sphere)
        val cubeIntersectSphere = cube.intersect(sphere)
        val cubeIntersectSphereCyl =
            cubeIntersectSphere.difference(cyl).difference(cyl2).difference(cyl3)

        // translate geometries to prevent overlapping 
        val union = cube.union(sphere.transformed(Transform.unity().translateX(3.0))).union(
            cyl.transformed(Transform.unity().translateX(6.0))
        ).union(
            cubePlusSphere.transformed(Transform.unity().translateX(9.0))
        ).union(
            cubeMinusSphere.transformed(Transform.unity().translateX(12.0))
        ).union(
            cubeIntersectSphere.transformed(Transform.unity().translateX(15.0))
        ).union(
            cubeIntersectSphereCyl.transformed(Transform.unity().translateX(18.0))
        )
        FileUtil.Companion.write(Paths.get("sample.stl"), union.toStlString())
        union.toObj().toFiles(Paths.get("sample-color.obj"))
    }
}
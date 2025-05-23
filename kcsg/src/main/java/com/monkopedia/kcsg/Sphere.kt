/**
 * Sphere.java
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
package com.monkopedia.kcsg

/**
 * A solid sphere.
 *
 * Tthe tessellation along the longitude and latitude directions can be
 * controlled via the [.numSlices] and [.numStacks] parameters.
 */
data class Sphere(
    var radius: Double = 1.0,
    var numSlices: Int = 16,
    var numStacks: Int = 8,
    var center: Vector3d = Vector3d.xyz(0.0, 0.0, 0.0)
) : Primitive {
    private val properties = PropertyStorage()

    /**
     * Constructor. Creates a sphere with the specified center, radius, number
     * of slices and stacks.
     *
     * @param center center of the sphere
     * @param radius sphere radius
     * @param numSlices number of slices
     * @param numStacks number of stacks
     */
    constructor(center: Vector3d, radius: Double, numSlices: Int, numStacks: Int) : this(
        radius,
        numSlices,
        numStacks,
        center
    )

    private fun sphereVertex(c: Vector3d, r: Double, theta: Double, phi: Double): Vertex {
        var theta = theta
        var phi = phi
        theta *= Math.PI * 2
        phi *= Math.PI
        val dir = Vector3d.xyz(
            kotlin.math.cos(theta) * kotlin.math.sin(phi),
            kotlin.math.cos(phi),
            kotlin.math.sin(theta) * kotlin.math.sin(phi)
        )
        return Vertex(c.plus(dir.times(r)), dir)
    }

    override fun toPolygons(): MutableList<Polygon> {
        val polygons: MutableList<Polygon> = ArrayList()
        for (i in 0 until numSlices) {
            for (j in 0 until numStacks) {
                val vertices: MutableList<Vertex> = ArrayList()
                vertices.add(
                    sphereVertex(
                        center,
                        radius,
                        i / numSlices.toDouble(),
                        j / numStacks.toDouble()
                    )
                )
                if (j > 0) {
                    vertices.add(
                        sphereVertex(
                            center,
                            radius,
                            (i + 1) / numSlices.toDouble(),
                            j / numStacks.toDouble()
                        )
                    )
                }
                if (j < numStacks - 1) {
                    vertices.add(
                        sphereVertex(
                            center,
                            radius,
                            (i + 1) / numSlices.toDouble(),
                            (j + 1) / numStacks.toDouble()
                        )
                    )
                }
                vertices.add(
                    sphereVertex(
                        center,
                        radius,
                        i / numSlices.toDouble(),
                        (j + 1) / numStacks.toDouble()
                    )
                )
                polygons.add(Polygon(vertices, getProperties()))
            }
        }
        return polygons
    }

    override fun getProperties(): PropertyStorage {
        return properties
    }
}

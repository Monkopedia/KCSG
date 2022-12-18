/**
 * Cylinder.java
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

import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A solid cylinder.
 *
 * The tessellation can be controlled via the [.numSlices] parameter.
 */
data class Cylinder(
    var start: Vector3d = Vector3d.xyz(0.0, -0.5, 0.0),
    var end: Vector3d = Vector3d.xyz(0.0, 0.5, 0.0),
    var startRadius: Double = 1.0,
    var endRadius: Double = 1.0,
    var numSlices: Int = 16
) : Primitive {
    private val properties = PropertyStorage()

    /**
     * Constructor. Creates a cylinder ranging from `start` to `end`
     * with the specified `radius`. The resolution of the tessellation can
     * be controlled with `numSlices`.
     *
     * @param start cylinder start
     * @param end cylinder end
     * @param radius cylinder radius
     * @param numSlices number of slices (used for tessellation)
     */
    constructor(start: Vector3d, end: Vector3d, radius: Double, numSlices: Int) : this(
        start,
        end,
        radius,
        radius,
        numSlices
    )

    /**
     * Constructor. Creates a cylinder ranging from `[0,0,0]` to
     * `[0,0,height]` with the specified `radius` and
     * `height`. The resolution of the tessellation can be controlled with
     * `numSlices`.
     *
     * @param radius cylinder radius
     * @param height cylinder height
     * @param numSlices number of slices (used for tessellation)
     */
    constructor(radius: Double, height: Double, numSlices: Int) : this(
        start = Vector3d.ZERO,
        end = Vector3d.Z_ONE.times(height),
        startRadius = radius,
        endRadius = radius,
        numSlices = numSlices
    )

    /**
     * Constructor. Creates a cylinder ranging from `[0,0,0]` to
     * `[0,0,height]` with the specified `radius` and
     * `height`. The resolution of the tessellation can be controlled with
     * `numSlices`.
     *
     * @param startRadius cylinder start radius
     * @param endRadius cylinder end radius
     * @param height cylinder height
     * @param numSlices number of slices (used for tessellation)
     */
    constructor(startRadius: Double, endRadius: Double, height: Double, numSlices: Int) : this(
        start = Vector3d.ZERO,
        end = Vector3d.Z_ONE.times(height),
        startRadius = startRadius,
        endRadius = endRadius,
        numSlices = numSlices
    )

    override fun toPolygons(): MutableList<Polygon> {
        val s = start
        val e = end
        val ray = e.minus(s)
        val axisZ = ray.normalized()
        val isY = abs(axisZ.y) > 0.5
        val axisX =
            Vector3d.xyz(if (isY) 1.0 else 0.toDouble(), if (!isY) 1.0 else 0.toDouble(), 0.0)
                .crossed(axisZ).normalized()
        val axisY = axisX.crossed(axisZ).normalized()
        val startV = Vertex(s, axisZ.negated())
        val endV = Vertex(e, axisZ.normalized())
        val polygons: MutableList<Polygon> = ArrayList()
        for (i in 0 until numSlices) {
            val t0 = i / numSlices.toDouble()
            val t1 = (i + 1) / numSlices.toDouble()
            polygons.add(
                Polygon(
                    listOf(
                        startV,
                        cylPoint(axisX, axisY, axisZ, ray, s, startRadius, 0.0, t0, -1.0),
                        cylPoint(axisX, axisY, axisZ, ray, s, startRadius, 0.0, t1, -1.0)
                    ),
                    properties
                )
            )
            polygons.add(
                Polygon(
                    listOf(
                        cylPoint(axisX, axisY, axisZ, ray, s, startRadius, 0.0, t1, 0.0),
                        cylPoint(axisX, axisY, axisZ, ray, s, startRadius, 0.0, t0, 0.0),
                        cylPoint(axisX, axisY, axisZ, ray, s, endRadius, 1.0, t0, 0.0),
                        cylPoint(axisX, axisY, axisZ, ray, s, endRadius, 1.0, t1, 0.0)
                    ),
                    properties
                )
            )
            polygons.add(
                Polygon(
                    listOf(
                        endV,
                        cylPoint(axisX, axisY, axisZ, ray, s, endRadius, 1.0, t1, 1.0),
                        cylPoint(axisX, axisY, axisZ, ray, s, endRadius, 1.0, t0, 1.0)
                    ),
                    properties
                )
            )
        }
        return polygons
    }

    private fun cylPoint(
        axisX: Vector3d,
        axisY: Vector3d,
        axisZ: Vector3d,
        ray: Vector3d,
        s: Vector3d,
        r: Double,
        stack: Double,
        slice: Double,
        normalBlend: Double
    ): Vertex {
        val angle = slice * Math.PI * 2
        val out = axisX.times(cos(angle)).plus(axisY.times(sin(angle)))
        val pos = s.plus(ray.times(stack)).plus(out.times(r))
        val normal = out.times(1.0 - abs(normalBlend)).plus(axisZ.times(normalBlend))
        return Vertex(pos, normal)
    }

    override fun getProperties(): PropertyStorage {
        return properties
    }
}

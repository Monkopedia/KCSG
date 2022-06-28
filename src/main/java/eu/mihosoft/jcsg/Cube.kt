/**
 * Cube.java
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
import eu.mihosoft.vvecmath.Vector3d

/**
 * An axis-aligned solid cuboid defined by `center` and
 * `dimensions`.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Cube : Primitive {
    /**
     * Center of this cube.
     */
    private var center: Vector3d

    /**
     * Cube dimensions.
     */
    private var dimensions: Vector3d
    private var centered = true
    private val properties = PropertyStorage()

    /**
     * Constructor. Creates a new cube with center `[0,0,0]` and
     * dimensions `[1,1,1]`.
     */
    constructor() {
        center = Vector3d.xyz(0.0, 0.0, 0.0)
        dimensions = Vector3d.xyz(1.0, 1.0, 1.0)
    }

    /**
     * Constructor. Creates a new cube with center `[0,0,0]` and
     * dimensions `[size,size,size]`.
     *
     * @param size size
     */
    constructor(size: Double) {
        center = Vector3d.xyz(0.0, 0.0, 0.0)
        dimensions = Vector3d.xyz(size, size, size)
    }

    /**
     * Constructor. Creates a new cuboid with the specified center and
     * dimensions.
     *
     * @param center center of the cuboid
     * @param dimensions cube dimensions
     */
    constructor(center: Vector3d, dimensions: Vector3d) {
        this.center = center
        this.dimensions = dimensions
    }

    /**
     * Constructor. Creates a new cuboid with center `[0,0,0]` and with
     * the specified dimensions.
     *
     * @param w width
     * @param h height
     * @param d depth
     */
    constructor(w: Double, h: Double, d: Double) : this(Vector3d.ZERO, Vector3d.xyz(w, h, d))

    //    public List<Polygon> toPolygons() {
    //        List<Polygon> result = new ArrayList<>(6);
    //
    //        Vector3d centerOffset = dimensions.times(0.5);
    //
    //        result.addAll(Arrays.asList(new Polygon[]{
    //            Polygon.fromPoints(
    //            centerOffset.times(-1, -1, -1),
    //            centerOffset.times(1, -1, -1),
    //            centerOffset.times(1, -1, 1),
    //            centerOffset.times(-1, -1, 1)
    //            ),
    //            Polygon.fromPoints(
    //            centerOffset.times(1, -1, -1),
    //            centerOffset.times(1, 1, -1),
    //            centerOffset.times(1, 1, 1),
    //            centerOffset.times(1, -1, 1)
    //            ),
    //            Polygon.fromPoints(
    //            centerOffset.times(1, 1, -1),
    //            centerOffset.times(-1, 1, -1),
    //            centerOffset.times(-1, 1, 1),
    //            centerOffset.times(1, 1, 1)
    //            ),
    //            Polygon.fromPoints(
    //            centerOffset.times(1, 1, 1),
    //            centerOffset.times(-1, 1, 1),
    //            centerOffset.times(-1, -1, 1),
    //            centerOffset.times(1, -1, 1)
    //            ),
    //            Polygon.fromPoints(
    //            centerOffset.times(-1, 1, 1),
    //            centerOffset.times(-1, 1, -1),
    //            centerOffset.times(-1, -1, -1),
    //            centerOffset.times(-1, -1, 1)
    //            ),
    //            Polygon.fromPoints(
    //            centerOffset.times(-1, 1, -1),
    //            centerOffset.times(1, 1, -1),
    //            centerOffset.times(1, -1, -1),
    //            centerOffset.times(-1, -1, -1)
    //            )
    //        }
    //        ));
    //        
    //        if(!centered) {
    //            Transform centerTransform = Transform.unity().
    //                    translate(dimensions.x() / 2.0,
    //                            dimensions.y() / 2.0,
    //                            dimensions.z() / 2.0);
    //
    //            for (Polygon p : result) {
    //                p.transform(centerTransform);
    //            }
    //        }
    //
    //        return result;
    //    }
    override fun toPolygons(): MutableList<Polygon>? {
        val a = arrayOf(
            arrayOf(intArrayOf(0, 4, 6, 2), intArrayOf(-1, 0, 0)),
            arrayOf(intArrayOf(1, 3, 7, 5), intArrayOf(+1, 0, 0)),
            arrayOf(intArrayOf(0, 1, 5, 4), intArrayOf(0, -1, 0)),
            arrayOf(intArrayOf(2, 6, 7, 3), intArrayOf(0, +1, 0)),
            arrayOf(intArrayOf(0, 2, 3, 1), intArrayOf(0, 0, -1)),
            arrayOf(intArrayOf(4, 5, 7, 6), intArrayOf(0, 0, +1))
        )
        val polygons: MutableList<Polygon> = ArrayList()
        for (info in a) {
            val vertices: MutableList<Vertex?> = ArrayList()
            for (i in info[0]) {
                val pos = Vector3d.xyz(
                    center.x() + dimensions.x() * (1 * Math.min(1, i and 1) - 0.5),
                    center.y() + dimensions.y() * (1 * Math.min(1, i and 2) - 0.5),
                    center.z() + dimensions.z() * (1 * Math.min(1, i and 4) - 0.5)
                )
                vertices.add(
                    Vertex(
                        pos, Vector3d.xyz(
                            info[1][0].toDouble(),
                            info[1][1].toDouble(),
                            info[1][2].toDouble()
                        )
                    )
                )
            }
            polygons.add(Polygon(vertices, properties))
        }
        if (!centered) {
            val centerTransform = Transform.unity().translate(
                dimensions.x() / 2.0,
                dimensions.y() / 2.0,
                dimensions.z() / 2.0
            )
            for (p in polygons) {
                p!!.transform(centerTransform)
            }
        }
        return polygons
    }

    /**
     * @return the center
     */
    fun getCenter(): Vector3d {
        return center
    }

    /**
     * @param center the center to set
     */
    fun setCenter(center: Vector3d) {
        this.center = center
    }

    /**
     * @return the dimensions
     */
    fun getDimensions(): Vector3d {
        return dimensions
    }

    /**
     * @param dimensions the dimensions to set
     */
    fun setDimensions(dimensions: Vector3d) {
        this.dimensions = dimensions
    }

    override fun getProperties(): PropertyStorage {
        return properties
    }

    /**
     * Defines that this cube will not be centered.
     *
     * @return this cube
     */
    fun noCenter(): Cube {
        centered = false
        return this
    }
}
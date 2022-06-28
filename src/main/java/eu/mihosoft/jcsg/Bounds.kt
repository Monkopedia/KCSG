/**
 * Bounds.java
 *
 * Copyright 2014-2017 Michael Hoffer <info></info>@michaelhoffer.de>. All rights
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

import eu.mihosoft.vvecmath.Vector3d

/**
 * Bounding box for CSGs.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Bounds(min: Vector3d, max: Vector3d) : Cloneable {
    private val _center: Vector3d
    private val _bounds: Vector3d

    /**
     * @return the min x,y,z values
     */
    val min: Vector3d

    /**
     * @return the max x,y,z values
     */
    val max: Vector3d
    private var csg: CSG? = null
    private val cube: Cube

    public override fun clone(): Bounds {
        return Bounds(min.clone(), max.clone())
    }

    /**
     * Returns the position of the center.
     *
     * @return the center position
     */
    val center: Vector3d
        get() {
            return _center
        }

    /**
     * Returns the bounds (width,height,depth).
     *
     * @return the bounds (width,height,depth)
     */
    val bounds: Vector3d
        get() {
            return _bounds
        }

    /**
     * Returns this bounding box as csg.
     *
     * @return this bounding box as csg
     */
    fun toCSG(): CSG? {
        if (csg == null) {
            csg = cube.toCSG()
        }
        return csg
    }

    /**
     * Returns this bounding box as cube.
     *
     * @return this bounding box as cube
     */
    fun toCube(): Cube {
        return cube
    }

    /**
     * Indicates whether the specified vertex is contained within this bounding
     * box (check includes box boundary).
     *
     * @param v vertex to check
     * @return `true` if the vertex is contained within this bounding box;
     * `false` otherwise
     */
    operator fun contains(v: Vertex?): Boolean {
        return contains(v!!.pos)
    }

    /**
     * Indicates whether the specified point is contained within this bounding
     * box (check includes box boundary).
     *
     * @param v vertex to check
     * @return `true` if the point is contained within this bounding box;
     * `false` otherwise
     */
    operator fun contains(v: Vector3d?): Boolean {
        val inX = min.x() <= v!!.x() && v.x() <= max.x()
        val inY = min.y() <= v.y() && v.y() <= max.y()
        val inZ = min.z() <= v.z() && v.z() <= max.z()
        return inX && inY && inZ
    }

    /**
     * Indicates whether the specified polygon is contained within this bounding
     * box (check includes box boundary).
     *
     * @param p polygon to check
     * @return `true` if the polygon is contained within this bounding
     * box; `false` otherwise
     */
    operator fun contains(p: Polygon): Boolean {
        return p.vertices.stream().allMatch { v: Vertex? -> contains(v) }
    }

    /**
     * Indicates whether the specified polygon intersects with this bounding box
     * (check includes box boundary).
     *
     * @param p polygon to check
     * @return `true` if the polygon intersects this bounding box;
     * `false` otherwise
     */
    fun intersects(p: Polygon): Boolean {
        return p.vertices.stream().filter { v: Vertex? -> this.contains(v) }.count() > 0
    }

    /**
     * Indicates whether the specified bounding box intersects with this
     * bounding box (check includes box boundary).
     *
     * @param b box to check
     * @return `true` if the bounding box intersects this bounding box;
     * `false` otherwise
     */
    fun intersects(b: Bounds?): Boolean {
        if (b!!.min.x() > max.x() || b.max.x() < min.x()) {
            return false
        }
        if (b.min.y() > max.y() || b.max.y() < min.y()) {
            return false
        }
        return !(b.min.z() > max.z() || b.max.z() < min.z())
    }

    override fun toString(): String {
        return "[center: $_center, bounds: $_bounds]"
    }

    /**
     * Constructor.
     *
     * @param min min x,y,z values
     * @param max max x,y,z values
     */
    init {
        _center = Vector3d.xyz(
            (max.x() + min.x()) / 2,
            (max.y() + min.y()) / 2,
            (max.z() + min.z()) / 2
        )
        _bounds = Vector3d.xyz(
            Math.abs(max.x() - min.x()),
            Math.abs(max.y() - min.y()),
            Math.abs(max.z() - min.z())
        )
        this.min = min.clone()
        this.max = max.clone()
        cube = Cube(_center, _bounds)
    }
}

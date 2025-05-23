/**
 * Vertex.java
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

import java.lang.StringBuilder
import java.util.Objects

/**
 * Represents a vertex of a polygon. This class provides [.normal] so
 * primitives like [Cube] can return a smooth vertex normal, but
 * [.normal] is not used anywhere else.
 */
data class Vertex(
    /**
     * Vertex position.
     */
    var pos: Vector3d,

    /**
     * Normal.
     */
    var normal: Vector3d,
    var weight: Double = 1.0
) {

    /**
     * Inverts all orientation-specific data. (e.g. vertex normal).
     */
    fun flip() {
        normal = normal.negated()
    }

    /**
     * Create a new vertex between this vertex and the specified vertex by
     * linearly interpolating all properties using a parameter t.
     *
     * @param other vertex
     * @param t interpolation parameter
     * @return a new vertex between this and the specified vertex
     */
    fun interpolate(other: Vertex, t: Double): Vertex {
        return Vertex(
            pos.lerp(other.pos, t),
            normal.lerp(other.normal, t)
        )
    }

    /**
     * Returns this vertex in STL string format.
     *
     * @return this vertex in STL string format
     */
    fun toStlString(): String {
        return "vertex " + pos.toStlString()
    }

    /**
     * Returns this vertex in STL string format.
     *
     * @param sb string builder
     * @return the specified string builder
     */
    fun toStlString(sb: StringBuilder): StringBuilder {
        sb.append("vertex ")
        return pos.toStlString(sb)
    }

    /**
     * Returns this vertex in OBJ string format.
     *
     * @param sb string builder
     * @return the specified string builder
     */
    fun toObjString(sb: StringBuilder): StringBuilder {
        sb.append("v ")
        return pos.toObjString(sb).append("\n")
    }

    /**
     * Returns this vertex in OBJ string format.
     *
     * @return this vertex in OBJ string format
     */
    fun toObjString(): String {
        return toObjString(StringBuilder()).toString()
    }

    /**
     * Applies the specified transform to this vertex.
     *
     * @param transform the transform to apply
     * @return this vertex
     */
    fun transform(transform: Transform): Vertex {
        pos = pos.transformed(transform, weight)
        return this
    }

    /**
     * Applies the specified transform to a copy of this vertex.
     *
     * @param transform the transform to apply
     * @return a copy of this transform
     */
    fun transformed(transform: Transform): Vertex {
        return copy().transform(transform)
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 53 * hash + Objects.hashCode(pos)
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (javaClass != other.javaClass) {
            return false
        }
        val other = other as Vertex
        return pos == other.pos
    }

    override fun toString(): String {
        return pos.toString()
    }
}

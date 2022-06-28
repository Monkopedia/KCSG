/*
 * Copyright (c) 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.mihosoft.jcsg.ext.openjfx.importers

/**
 * A 3-dimensional, single-precision, floating-point vector.
 *
 */
class Vec3f {
    /**
     * The x coordinate.
     */
    var x = 0f

    /**
     * The y coordinate.
     */
    var y = 0f

    /**
     * The z coordinate.
     */
    var z = 0f

    constructor()
    constructor(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(v: Vec3f) {
        x = v.x
        y = v.y
        z = v.z
    }

    fun set(v: Vec3f) {
        x = v.x
        y = v.y
        z = v.z
    }

    operator fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun mul(s: Float) {
        x *= s
        y *= s
        z *= s
    }

    /**
     * Sets the value of this vector to the difference
     * of vectors t1 and t2 (this = t1 - t2).
     * @param t1 the first vector
     * @param t2 the second vector
     */
    fun sub(t1: Vec3f, t2: Vec3f) {
        x = t1.x - t2.x
        y = t1.y - t2.y
        z = t1.z - t2.z
    }

    /**
     * Sets the value of this vector to the difference of
     * itself and vector t1 (this = this - t1) .
     * @param t1 the other vector
     */
    fun sub(t1: Vec3f) {
        x -= t1.x
        y -= t1.y
        z -= t1.z
    }

    /**
     * Sets the value of this vector to the sum
     * of vectors t1 and t2 (this = t1 + t2).
     * @param t1 the first vector
     * @param t2 the second vector
     */
    fun add(t1: Vec3f, t2: Vec3f) {
        x = t1.x + t2.x
        y = t1.y + t2.y
        z = t1.z + t2.z
    }

    /**
     * Sets the value of this vector to the sum of
     * itself and vector t1 (this = this + t1) .
     * @param t1 the other vector
     */
    fun add(t1: Vec3f) {
        x += t1.x
        y += t1.y
        z += t1.z
    }

    /**
     * Returns the length of this vector.
     * @return the length of this vector
     */
    fun length(): Float {
        return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    /**
     * Normalize this vector.
     */
    fun normalize() {
        val norm = 1.0f / length()
        x = x * norm
        y = y * norm
        z = z * norm
    }

    /**
     * Sets this vector to be the vector cross product of vectors v1 and v2.
     * @param v1 the first vector
     * @param v2 the second vector
     */
    fun cross(v1: Vec3f, v2: Vec3f) {
        val tmpX: Float
        val tmpY: Float
        tmpX = v1.y * v2.z - v1.z * v2.y
        tmpY = v2.x * v1.z - v2.z * v1.x
        z = v1.x * v2.y - v1.y * v2.x
        x = tmpX
        y = tmpY
    }

    /**
     * Computes the dot product of this vector and vector v1.
     * @param v1 the other vector
     * @return the dot product of this vector and v1
     */
    fun dot(v1: Vec3f): Float {
        return x * v1.x + y * v1.y + z * v1.z
    }

    /**
     * Returns the hashcode for this `Vec3f`.
     * @return      a hash code for this `Vec3f`.
     */
    override fun hashCode(): Int {
        var bits = 7
        bits = 31 * bits + java.lang.Float.floatToIntBits(x)
        bits = 31 * bits + java.lang.Float.floatToIntBits(y)
        bits = 31 * bits + java.lang.Float.floatToIntBits(z)
        return bits
    }

    /**
     * Determines whether or not two 3D points or vectors are equal.
     * Two instances of `Vec3f` are equal if the values of their
     * `x`, `y` and `z` member fields,
     * representing their position in the coordinate space, are the same.
     * @param obj an object to be compared with this `Vec3f`
     * @return `true` if the object to be compared is
     * an instance of `Vec3f` and has
     * the same values; `false` otherwise.
     */
    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is Vec3f) {
            val v = obj
            return x == v.x && y == v.y && z == v.z
        }
        return false
    }

    /**
     * Returns a `String` that represents the value
     * of this `Vec3f`.
     * @return a string representation of this `Vec3f`.
     */
    override fun toString(): String {
        return "Vec3f[$x, $y, $z]"
    }
}
/**
 * Copyright John E. Lloyd, 2004. All rights reserved. Permission to use,
 * copy, modify and redistribute is granted, provided that this copyright
 * notice is retained and the author is given credit whenever appropriate.
 *
 * This  software is distributed "as is", without any warranty, including
 * any implied warranty of merchantability or fitness for a particular
 * use. The author assumes no responsibility for, and shall not be liable
 * for, any special, indirect, or consequential damages, or any damages
 * whatsoever, arising out of or in connection with the use of this
 * software.
 */
package eu.mihosoft.jcsg.ext.quickhull3d

import kotlin.math.sqrt

/**
 * A three-element vector. This class is actually a reduced version of the
 * Vector3d class contained in the author's matlib package (which was partly
 * inspired by javax.vecmath). Only a mininal number of methods
 * which are relevant to convex hull generation are supplied here.
 *
 * @author John E. Lloyd, Fall 2004
 */
internal open class Vector3d {
    /**
     * First element
     */
    var x = 0.0

    /**
     * Second element
     */
    var y = 0.0

    /**
     * Third element
     */
    var z = 0.0

    /**
     * Creates a 3-vector and initializes its elements to 0.
     */
    constructor()

    /**
     * Creates a 3-vector by copying an existing one.
     *
     * @param v vector to be copied
     */
    constructor(v: Vector3d) {
        set(v)
    }

    /**
     * Creates a 3-vector with the supplied element values.
     *
     * @param x first element
     * @param y second element
     * @param z third element
     */
    constructor(x: Double, y: Double, z: Double) {
        set(x, y, z)
    }

    /**
     * Gets a single element of this vector.
     * Elements 0, 1, and 2 correspond to x, y, and z.
     *
     * @param i element index
     * @return element value throws ArrayIndexOutOfBoundsException
     * if i is not in the range 0 to 2.
     */
    operator fun get(i: Int): Double {
        return when (i) {
            0 -> {
                x
            }
            1 -> {
                y
            }
            2 -> {
                z
            }
            else -> {
                throw ArrayIndexOutOfBoundsException(i)
            }
        }
    }

    /**
     * Sets a single element of this vector.
     * Elements 0, 1, and 2 correspond to x, y, and z.
     *
     * @param i element index
     * @param value element value
     * @return element value throws ArrayIndexOutOfBoundsException
     * if i is not in the range 0 to 2.
     */
    operator fun set(i: Int, value: Double) {
        when (i) {
            0 -> {
                x = value
            }
            1 -> {
                y = value
            }
            2 -> {
                z = value
            }
            else -> {
                throw ArrayIndexOutOfBoundsException(i)
            }
        }
    }

    /**
     * Sets the values of this vector to those of v1.
     *
     * @param v1 vector whose values are copied
     */
    fun set(v1: Vector3d) {
        x = v1.x
        y = v1.y
        z = v1.z
    }

    /**
     * Adds vector v1 to v2 and places the result in this vector.
     *
     * @param v1 left-hand vector
     * @param v2 right-hand vector
     */
    fun add(v1: Vector3d, v2: Vector3d) {
        x = v1.x + v2.x
        y = v1.y + v2.y
        z = v1.z + v2.z
    }

    /**
     * Adds this vector to v1 and places the result in this vector.
     *
     * @param v1 right-hand vector
     */
    fun add(v1: Vector3d) {
        x += v1.x
        y += v1.y
        z += v1.z
    }

    /**
     * Subtracts vector v1 from v2 and places the result in this vector.
     *
     * @param v1 left-hand vector
     * @param v2 right-hand vector
     */
    fun sub(v1: Vector3d, v2: Vector3d) {
        x = v1.x - v2.x
        y = v1.y - v2.y
        z = v1.z - v2.z
    }

    /**
     * Scales the elements of this vector by `s`.
     *
     * @param s scaling factor
     */
    fun scale(s: Double) {
        x *= s
        y *= s
        z *= s
    }

    /**
     * Scales the elements of vector v1 by `s` and places
     * the results in this vector.
     *
     * @param s scaling factor
     * @param v1 vector to be scaled
     */
    fun scale(s: Double, v1: Vector3d) {
        x = s * v1.x
        y = s * v1.y
        z = s * v1.z
    }

    /**
     * Returns the 2 norm of this vector. This is the square root of the
     * sum of the squares of the elements.
     *
     * @return vector 2 norm
     */
    fun norm(): Double {
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * Returns the square of the 2 norm of this vector. This
     * is the sum of the squares of the elements.
     *
     * @return square of the 2 norm
     */
    fun normSquared(): Double {
        return x * x + y * y + z * z
    }

    /**
     * Returns the Euclidean distance between this vector and vector v.
     *
     * @return distance between this vector and v
     */
    fun distance(v: Vector3d): Double {
        val dx = x - v.x
        val dy = y - v.y
        val dz = z - v.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    /**
     * Returns the squared of the Euclidean distance between this vector
     * and vector v.
     *
     * @return squared distance between this vector and v
     */
    fun distanceSquared(v: Vector3d): Double {
        val dx = x - v.x
        val dy = y - v.y
        val dz = z - v.z
        return dx * dx + dy * dy + dz * dz
    }

    /**
     * Returns the dot product of this vector and v1.
     *
     * @param v1 right-hand vector
     * @return dot product
     */
    fun dot(v1: Vector3d): Double {
        return x * v1.x + y * v1.y + z * v1.z
    }

    /**
     * Normalizes this vector in place.
     */
    fun normalize() {
        val lenSqr = x * x + y * y + z * z
        val err = lenSqr - 1
        if (err > 2 * DOUBLE_PREC ||
            err < -(2 * DOUBLE_PREC)
        ) {
            val len = sqrt(lenSqr)
            x /= len
            y /= len
            z /= len
        }
    }

    /**
     * Sets the elements of this vector to zero.
     */
    fun setZero() {
        x = 0.0
        y = 0.0
        z = 0.0
    }

    /**
     * Sets the elements of this vector to the prescribed values.
     *
     * @param x value for first element
     * @param y value for second element
     * @param z value for third element
     */
    operator fun set(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Computes the cross product of v1 and v2 and places the result
     * in this vector.
     *
     * @param v1 left-hand vector
     * @param v2 right-hand vector
     */
    fun cross(v1: Vector3d, v2: Vector3d) {
        val tmpx = v1.y * v2.z - v1.z * v2.y
        val tmpy = v1.z * v2.x - v1.x * v2.z
        val tmpz = v1.x * v2.y - v1.y * v2.x
        x = tmpx
        y = tmpy
        z = tmpz
    }

    /**
     * Returns a string representation of this vector, consisting
     * of the x, y, and z coordinates.
     *
     * @return string representation
     */
    override fun toString(): String {
        return "$x $y $z"
    }

    companion object {
        /**
         * Precision of a double.
         */
        private const val DOUBLE_PREC = 2.2204460492503131e-16
    }
}
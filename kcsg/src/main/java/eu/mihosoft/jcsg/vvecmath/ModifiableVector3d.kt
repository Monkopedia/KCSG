/*
 * Copyright 2017-2019 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package eu.mihosoft.vvecmath

import eu.mihosoft.vvecmath.Matrix4d
import eu.mihosoft.vvecmath.StoredVector3d
import eu.mihosoft.vvecmath.ModifiableStoredVector3d
import eu.mihosoft.vvecmath.Spline.Cubic
import java.lang.RuntimeException
import eu.mihosoft.vvecmath.StoredVector3dImpl
import java.lang.IllegalArgumentException
import java.lang.Math
import java.lang.StringBuilder
import eu.mihosoft.vvecmath.Vector3dImpl
import java.lang.AssertionError
import java.util.stream.DoubleStream
import java.util.function.DoubleFunction
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.function.IntPredicate
import java.util.function.IntFunction

/**
 * Modifiable 3d vector.
 *
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
interface ModifiableVector3d : Vector3d {
    /**
     * Sets the specified vector components.
     *
     * @param xyz vector components to set (number of components `<= 3` are valid)
     * @return this vector
     */
    fun set(vararg xyz: Double): Vector3d

    /**
     * Sets the i-th component of this vector.
     *
     * @param i component index
     * @param value value to set
     * @return this vector
     */
    operator fun set(i: Int, value: Double): Vector3d

    /**
     * Sets the `x` component of this vector.
     *
     * @param x component to set
     */
    override var x: Double

    /**
     * Sets the `y` component of this vector.
     *
     * @param y component to set
     */
    override var y: Double

    /**
     * Sets the `z` component of this vector.
     *
     * @param z component to set
     */
    override var z: Double

    /**
     * Adds the specified vector to this vector.
     *
     * @param v the vector to add
     *
     * **Note:** this vector **is** not modified.
     *
     * @return this vector
     */
    fun add(v: Vector3d): Vector3d {
        x += v.x
        y += v.y
        z += v.z
        return this
    }

    /**
     * Adds the specified vector to this vector.
     *
     * @param x x coordinate of the vector to add
     * @param y y coordinate of the vector to add
     * @param z z coordinate of the vector to add
     *
     * **Note:** this vector **is** modified.
     *
     * @return this vector
     */
    fun add(x: Double, y: Double, z: Double): Vector3d {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    /**
     * Subtracts the specified vector from this vector.
     *
     * **Note:** this vector **is** modified.
     *
     * @param v vector to subtract
     * @return this vector
     */
    fun subtract(v: Vector3d): Vector3d {
        x -= v.x
        y -= v.y
        z -= v.z
        return this
    }

    /**
     * Subtracts the specified vector from this vector.
     *
     * **Note:** this vector **is** modified.
     *
     * @param x x coordinate of the vector to subtract
     * @param y y coordinate of the vector to subtract
     * @param z z coordinate of the vector to subtract
     *
     * @return this vector
     */
    fun subtract(x: Double, y: Double, z: Double): Vector3d {
        this.x -= x
        this.y -= y
        this.z -= z
        return this
    }

    /**
     * Multiplies this vector with the specified value.
     *
     * @param a the value
     *
     * **Note:** this vector **is** modified.
     *
     * @return this vector
     */
    fun multiply(a: Double): Vector3d {
        x *= a
        y *= a
        z *= a
        return this
    }

    /**
     * Multiplies this vector with the specified vector.
     *
     * @param a the vector
     *
     * **Note:** this vector **is** modified.
     *
     * @return this vector
     */
    fun multiply(a: Vector3d): Vector3d {
        x *= a.x
        y *= a.y
        z *= a.z
        return this
    }

    /**
     * Devides this vector with the specified value.
     *
     * @param a the value
     *
     * **Note:** this vector **is** modified.
     *
     * @return this vector
     */
    fun divide(a: Double): Vector3d {
        x /= a
        y /= a
        z /= a
        return this
    }

    /**
     * Divides this vector with the specified vector.
     *
     * @param v the vector
     *
     * **Note:** this vector **is** modified.
     *
     * @return this vector
     */
    fun divide(v: Vector3d): Vector3d {
        x /= v.x
        y /= v.y
        z /= v.z
        return this
    }

    /**
     * Stores the cross product of this vector and the specified vector in this
     * vector.
     *
     * **Note:** this vector **is**modified.
     *
     * @param a the vector
     *
     * @return this vector
     */
    fun cross(a: Vector3d): Vector3d {
        x = (this.y * a.z - this.z * a.y)
        y = (this.z * a.x - this.x * a.z)
        z = (this.x * a.y - this.y * a.x)
        return this
    }

    /**
     * Negates this vector.
     *
     * **Note:** this vector **is** modified.
     *
     * @return this vector
     */
    fun negate(): Vector3d {
        return multiply(-1.0)
    }

    /**
     * Normalizes this vector with length `1`.
     *
     * **Note:** this vector **is** modified.
     *
     * @return this vector
     */
    fun normalize(): Vector3d {
        return this.divide(this.magnitude())
    }
}
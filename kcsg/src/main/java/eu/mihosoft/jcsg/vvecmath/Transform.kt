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

import kotlin.math.cos
import kotlin.math.sin

/**
 * Transform. Transformations (translation, rotation, scale) can be applied to
 * geometrical objects like [CSG], [Polygon], [Vertex] and
 * [Vector3d].
 *
 * This transform class uses the builder pattern to define combined
 * transformations.<br></br><br></br>
 *
 * **Example:**
 *
 * <blockquote><pre>
 * // t applies rotation and translation
 * Transform t = Transform.unity().rotX(45).translate(2,1,0);
</pre></blockquote> *
 *
 * **TODO:** use quaternions for rotations.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Transform {
    /**
     * Internal 4x4 matrix.
     */
    private var m: Matrix4d

    /**
     * Constructor.
     *
     * Creates a unit transform.
     */
    constructor() {
        m = Matrix4d()
        m.m00 = 1.0
        m.m11 = 1.0
        m.m22 = 1.0
        m.m33 = 1.0
    }

    /**
     * Returns this transform as matrix (4x4).
     *
     * @param values target array (16 values)
     * @return a new transform
     */
    fun to(values: DoubleArray): DoubleArray {
        return m[values]
    }

    /**
     * Constructor.
     *
     * @param m matrix
     */
    private constructor(m: Matrix4d) {
        this.m = m
    }

    /**
     * Applies rotation operation around the x axis to this transform.
     *
     * @param degrees degrees
     * @return this transform
     */
    fun rotX(degrees: Double): Transform {
        val radians = degrees * Math.PI * (1.0 / 180.0)
        val cos = cos(radians)
        val sin = sin(radians)
        val elemenents = doubleArrayOf(
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            cos,
            sin,
            0.0,
            0.0,
            -sin,
            cos,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0
        )
        m *= Matrix4d(elemenents)
        return this
    }

    /**
     * Applies rotation operation around the y axis to this transform.
     *
     * @param degrees degrees
     *
     * @return this transform
     */
    fun rotY(degrees: Double): Transform {
        val radians = degrees * Math.PI * (1.0 / 180.0)
        val cos = cos(radians)
        val sin = sin(radians)
        val elemenents = doubleArrayOf(
            cos, 0.0, -sin, 0.0, 0.0, 1.0, 0.0, 0.0, sin, 0.0, cos, 0.0, 0.0, 0.0, 0.0, 1.0
        )
        m *= Matrix4d(elemenents)
        return this
    }

    /**
     * Applies rotation operation around the z axis to this transform.
     *
     * @param degrees degrees
     *
     * @return this transform
     */
    fun rotZ(degrees: Double): Transform {
        val radians = degrees * Math.PI * (1.0 / 180.0)
        val cos = cos(radians)
        val sin = sin(radians)
        val elemenents = doubleArrayOf(
            cos, sin, 0.0, 0.0, -sin, cos, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0
        )
        m *= Matrix4d(elemenents)
        return this
    }

    /**
     * Applies a rotation operation to this transform.
     *
     * @param x x axis rotation (degrees)
     * @param y y axis rotation (degrees)
     * @param z z axis rotation (degrees)
     *
     * @return this transform
     */
    fun rot(x: Double, y: Double, z: Double): Transform {
        return rotX(x).rotY(y).rotZ(z)
    }

    /**
     * Applies a rotation operation to this transform.
     *
     * @param vec axis rotation for x, y, z (degrees)
     *
     * @return this transform
     */
    fun rot(vec: Vector3d): Transform {
        // TODO: use quaternions
        return rotX(vec.x).rotY(vec.y).rotZ(vec.z)
    }

    /**
     * Applies a transformation that rotates one vector into another.
     *
     * @param from source vector
     * @param to target vector
     * @return this transformation
     */
    fun rot(from: Vector3d, to: Vector3d): Transform {
        val a: Vector3d = from.normalized()
        val b: Vector3d = to.normalized()
        val c: Vector3d = a.crossed(b)
        val l: Double = c.magnitude() // sine of angle
        if (l > 1e-9) {
            val axis: Vector3d = c.normalized()
            val angle: Double = a.angle(b)
            rot(Vector3d.ZERO, axis, angle)
        }
        return this
    }

    /**
     * Applies a rotation operation about the specified rotation axis.
     *
     * @param axisPos axis point
     * @param axisDir axis direction (may be unnormalized)
     * @param degrees rotantion angle in degrees
     * @return this transform
     */
    fun rot(axisPos: Vector3d, axisDir: Vector3d, degrees: Double): Transform {
        val normalAxisDir: Vector3d = axisDir.normalized()
        val tmp = unity()
        val dir2: Vector3d = normalAxisDir.times(normalAxisDir)
        val posx: Double = axisPos.x
        val posy: Double = axisPos.y
        val posz: Double = axisPos.z
        val dirx: Double = normalAxisDir.x
        val diry: Double = normalAxisDir.y
        val dirz: Double = normalAxisDir.z
        val dirxSquare: Double = dir2.x
        val dirySquare: Double = dir2.y
        val dirzSquare: Double = dir2.z
        val radians = degrees * Math.PI * (1.0 / 180.0)
        val cosOfAngle = cos(radians)
        val oneMinusCosOfangle = 1 - cosOfAngle
        val sinOfangle = sin(radians)
        tmp.m.m00 = dirxSquare + (dirySquare + dirzSquare) * cosOfAngle
        tmp.m.m01 = dirx * diry * oneMinusCosOfangle - dirz * sinOfangle
        tmp.m.m02 = dirx * dirz * oneMinusCosOfangle + diry * sinOfangle
        tmp.m.m03 = (
            (
                posx * (dirySquare + dirzSquare) -
                    dirx * (posy * diry + posz * dirz)
                ) * oneMinusCosOfangle +
                (posy * dirz - posz * diry) * sinOfangle
            )
        tmp.m.m10 = dirx * diry * oneMinusCosOfangle + dirz * sinOfangle
        tmp.m.m11 = dirySquare + (dirxSquare + dirzSquare) * cosOfAngle
        tmp.m.m12 = diry * dirz * oneMinusCosOfangle - dirx * sinOfangle
        tmp.m.m13 = (
            (
                posy * (dirxSquare + dirzSquare) -
                    diry * (posx * dirx + posz * dirz)
                ) * oneMinusCosOfangle +
                (posz * dirx - posx * dirz) * sinOfangle
            )
        tmp.m.m20 = dirx * dirz * oneMinusCosOfangle - diry * sinOfangle
        tmp.m.m21 = diry * dirz * oneMinusCosOfangle + dirx * sinOfangle
        tmp.m.m22 = dirzSquare + (dirxSquare + dirySquare) * cosOfAngle
        tmp.m.m23 = (
            (
                posz * (dirxSquare + dirySquare) -
                    dirz * (posx * dirx + posy * diry)
                ) * oneMinusCosOfangle +
                (posx * diry - posy * dirx) * sinOfangle
            )
        apply(tmp)
        return this
    }

    /**
     * Applies a translation operation to this transform.
     *
     * @param vec translation vector (x,y,z)
     *
     * @return this transform
     */
    fun translate(vec: Vector3d): Transform {
        return translate(vec.x, vec.y, vec.z)
    }

    /**
     * Applies a translation operation to this transform.
     *
     * @param x translation (x axis)
     * @param y translation (y axis)
     * @param z translation (z axis)
     *
     * @return this transform
     */
    fun translate(x: Double, y: Double, z: Double): Transform {
        val elemenents =
            doubleArrayOf(1.0, 0.0, 0.0, x, 0.0, 1.0, 0.0, y, 0.0, 0.0, 1.0, z, 0.0, 0.0, 0.0, 1.0)
        m *= Matrix4d(elemenents)
        return this
    }

    /**
     * Applies a translation operation to this transform.
     *
     * @param value translation (x axis)
     *
     * @return this transform
     */
    fun translateX(value: Double): Transform {
        val elemenents = doubleArrayOf(
            1.0,
            0.0,
            0.0,
            value,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0
        )
        m *= Matrix4d(elemenents)
        return this
    }

    /**
     * Applies a translation operation to this transform.
     *
     * @param value translation (y axis)
     *
     * @return this transform
     */
    fun translateY(value: Double): Transform {
        val elemenents = doubleArrayOf(
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            value,
            0.0,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0
        )
        m *= Matrix4d(elemenents)
        return this
    }

    /**
     * Applies a translation operation to this transform.
     *
     * @param value translation (z axis)
     *
     * @return this transform
     */
    fun translateZ(value: Double): Transform {
        val elemenents = doubleArrayOf(
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            value,
            0.0,
            0.0,
            0.0,
            1.0
        )
        m *= Matrix4d(elemenents)
        return this
    }

    /**
     * Applies a mirror operation to this transform.
     *
     * @param plane the plane that defines the mirror operation
     *
     * @return this transform
     */
    fun mirror(plane: Plane): Transform {
        System.err.println("WARNING: I'm too dumb to implement the mirror() operation correctly. Please fix me!")
        val nx: Double = plane.normal.x
        val ny: Double = plane.normal.y
        val nz: Double = plane.normal.z
        val w: Double = plane.dist
        val elemenents = doubleArrayOf(
            1.0 - 2.0 * nx * nx, -2.0 * ny * nx, -2.0 * nz * nx, 0.0,
            -2.0 * nx * ny, 1.0 - 2.0 * ny * ny, -2.0 * nz * ny, 0.0,
            -2.0 * nx * nz, -2.0 * ny * nz, 1.0 - 2.0 * nz * nz, 0.0,
            -2.0 * nx * w, -2.0 * ny * w, -2.0 * nz * w, 1.0
        )
        m *= Matrix4d(elemenents)
        m.mul(Matrix4d(elemenents))
        return this
    }

    /**
     * Applies a scale operation to this transform.
     *
     * @param vec vector that specifies scale (x,y,z)
     *
     * @return this transform
     */
    fun scale(vec: Vector3d): Transform {
        require(!(vec.x == 0.0 || vec.y == 0.0 || vec.z == 0.0)) { "scale by 0 not allowed!" }
        val elemenents = doubleArrayOf(
            vec.x,
            0.0,
            0.0,
            0.0,
            0.0,
            vec.y,
            0.0,
            0.0,
            0.0,
            0.0,
            vec.z,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0
        )
        m.mul(Matrix4d(elemenents))
        return this
    }

    /**
     * Applies a scale operation to this transform.
     *
     * @param x x scale value
     * @param y y scale value
     * @param z z scale value
     *
     * @return this transform
     */
    fun scale(x: Double, y: Double, z: Double): Transform {
        require(!(x == 0.0 || y == 0.0 || z == 0.0)) { "scale by 0 not allowed!" }
        val elemenents = doubleArrayOf(
            x, 0.0, 0.0, 0.0, 0.0, y, 0.0, 0.0, 0.0, 0.0, z, 0.0, 0.0, 0.0, 0.0, 1.0
        )
        m.mul(Matrix4d(elemenents))
        return this
    }

    /**
     * Applies a scale operation to this transform.
     *
     * @param s s scale value (x, y and z)
     *
     * @return this transform
     */
    fun scale(s: Double): Transform {
        require(s != 0.0) { "scale by 0 not allowed!" }
        val elemenents = doubleArrayOf(
            s, 0.0, 0.0, 0.0, 0.0, s, 0.0, 0.0, 0.0, 0.0, s, 0.0, 0.0, 0.0, 0.0, 1.0
        )
        m.mul(Matrix4d(elemenents))
        return this
    }

    /**
     * Applies a scale operation (x axis) to this transform.
     *
     * @param s x scale value
     *
     * @return this transform
     */
    fun scaleX(s: Double): Transform {
        require(s != 0.0) { "scale by 0 not allowed!" }
        val elemenents = doubleArrayOf(
            s, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0
        )
        m.mul(Matrix4d(elemenents))
        return this
    }

    /**
     * Applies a scale operation (y axis) to this transform.
     *
     * @param s y scale value
     *
     * @return this transform
     */
    fun scaleY(s: Double): Transform {
        require(s != 0.0) { "scale by 0 not allowed!" }
        val elemenents = doubleArrayOf(
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            s,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0
        )
        m.mul(Matrix4d(elemenents))
        return this
    }

    /**
     * Applies a scale operation (z axis) to this transform.
     *
     * @param s z scale value
     *
     * @return this transform
     */
    fun scaleZ(s: Double): Transform {
        require(s != 0.0) { "scale by 0 not allowed!" }
        val elemenents = doubleArrayOf(
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0,
            0.0,
            0.0,
            0.0,
            0.0,
            s,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0
        )
        m.mul(Matrix4d(elemenents))
        return this
    }

    /**
     * Applies this transform to the specified vector.
     *
     * @param vec vector to transform
     *
     * @return the specified vector
     */
    fun transform(vec: Vector3d): Vector3d {
        val x: Double
        val y: Double
        x = m.m00 * vec.x + m.m01 * vec.y + m.m02 * vec.z + m.m03
        y = m.m10 * vec.x + m.m11 * vec.y + m.m12 * vec.z + m.m13
        return Vector3d.xyz(
            x,
            y,
            m.m20 * vec.x + m.m21 * vec.y + m.m22 * vec.z + m.m23,
        )
    }

    /**
     * Applies this transform to the specified vector.
     *
     * @param vec vector to transform
     * @param amount transform amount (0 = 0 %, 1 = 100%)
     *
     * @return the specified vector
     */
    fun transform(vec: Vector3d, amount: Double): Vector3d {
        val prevX: Double = vec.x
        val prevY: Double = vec.y
        val prevZ: Double = vec.z
        val x: Double
        val y: Double
        x = m.m00 * vec.x + m.m01 * vec.y + m.m02 * vec.z + m.m03
        y = m.m10 * vec.x + m.m11 * vec.y + m.m12 * vec.z + m.m13
        val vz = (m.m20 * vec.x + m.m21 * vec.y + m.m22 * vec.z + m.m23)
        val vx = (x)
        val vy = (y)
        val diffX: Double = vx - prevX
        val diffY: Double = vy - prevY
        val diffZ: Double = vz - prevZ
        return Vector3d.xyz(
            (prevX + diffX * amount),
            (prevY + diffY * amount),
            (prevZ + diffZ * amount),
        )
    }
    //    /**
    //     * Performs an SVD normalization of the underlying matrix to calculate and
    //     * return the uniform scale factor. If the matrix has non-uniform scale
    //     * factors, the largest of the x, y, and z scale factors distill be
    //     * returned.
    //     *
    //     * <b>Note:</b> this transformation is not modified.
    //     *
    //     * @return the scale factor of this transformation
    //     */
    //    public double getScale() {
    //        return m.getScale();
    //    }
    /**
     * Indicates whether this transform performs a mirror operation, i.e., flips
     * the orientation.
     *
     * @return `true` if this transform performs a mirror operation;
     * `false` otherwise
     */
    val isMirror: Boolean
        get() = m.determinant() < 0

    /**
     * Applies the specified transform to this transform.
     *
     * @param t transform to apply
     *
     * @return this transform
     */
    fun apply(t: Transform): Transform {
        m.mul(t.m)
        return this
    }

    override fun toString(): String {
        return m.toString()
    }

    companion object {
        /**
         * Returns a new transform based on the specified matrix values (4x4).
         *
         * @param values 16 double values that represent the transformation matrix
         * @return a new transform
         */
        fun from(vararg values: Double): Transform {
            val m = Matrix4d(values)
            return Transform(m)
        }

        /**
         * Returns a new unity transform.
         *
         * @return unity transform
         */
        fun unity(): Transform {
            return Transform()
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.vvecmath.Vector3d

/**
 * 3D Matrix3d
 *
 * @author cpoliwoda
 */
class Matrix3d(
    var m11: Double,
    var m12: Double,
    var m13: Double,
    var m21: Double,
    var m22: Double,
    var m23: Double,
    var m31: Double,
    var m32: Double,
    var m33: Double
) {
    override fun toString(): String {
        return """
              [$m11, $m12, $m13]
              [$m21, $m22, $m23]
              [$m31, $m32, $m33]
        """.trimIndent()
    }

    /**
     * Returns the product of this matrix and the specified value.
     *
     * @param a the value
     *
     * **Note:** this matrix is not modified.
     *
     * @return the product of this matrix and the specified value
     */
    operator fun times(a: Double): Matrix3d {
        return Matrix3d(
            m11 * a, m12 * a, m13 * a,
            m21 * a, m22 * a, m23 * a,
            m31 * a, m32 * a, m33 * a
        )
    }

    /**
     * Returns the product of this matrix and the specified vector.
     *
     * @param a the vector
     *
     * **Note:** the vector is not modified.
     *
     * @return the product of this matrix and the specified vector
     */
    operator fun times(a: Vector3d): Vector3d {
        return Vector3d.xyz(
            m11 * a.x() + m12 * a.y() + m13 * a.z(),
            m21 * a.x() + m22 * a.y() + m23 * a.z(),
            m31 * a.x() + m32 * a.y() + m33 * a.z()
        )
    }

    companion object {
        val ZERO = Matrix3d(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        val UNITY = Matrix3d(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0)
        fun maxOf3Values(values: DoubleArray): Double {
            return if (values[0] > values[1]) {
                if (values[0] > values[2]) {
                    values[0]
                } else {
                    values[2]
                }
            } else {
                if (values[1] > values[2]) {
                    values[1]
                } else {
                    values[2]
                }
            }
        }
    }
}

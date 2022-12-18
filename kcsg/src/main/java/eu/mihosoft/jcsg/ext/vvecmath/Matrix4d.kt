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
package eu.mihosoft.jcsg.ext.vvecmath

/**
 *
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
@JvmInline
internal value class Matrix4d(
    private val marr: DoubleArray = DoubleArray(16)
) {
    internal inline var m00: Double
        get() = marr[0]
        set(value) {
            marr[0] = value
        }
    internal inline var m01: Double
        get() = marr[1]
        set(value) {
            marr[1] = value
        }
    internal inline var m02: Double
        get() = marr[2]
        set(value) {
            marr[2] = value
        }
    internal inline var m03: Double
        get() = marr[3]
        set(value) {
            marr[3] = value
        }
    internal inline var m10: Double
        get() = marr[4]
        set(value) {
            marr[4] = value
        }
    internal inline var m11: Double
        get() = marr[5]
        set(value) {
            marr[5] = value
        }
    internal inline var m12: Double
        get() = marr[6]
        set(value) {
            marr[6] = value
        }
    internal inline var m13: Double
        get() = marr[7]
        set(value) {
            marr[7] = value
        }
    internal inline var m20: Double
        get() = marr[8]
        set(value) {
            marr[8] = value
        }
    internal inline var m21: Double
        get() = marr[9]
        set(value) {
            marr[9] = value
        }
    internal inline var m22: Double
        get() = marr[10]
        set(value) {
            marr[10] = value
        }
    internal inline var m23: Double
        get() = marr[11]
        set(value) {
            marr[11] = value
        }
    internal inline var m30: Double
        get() = marr[12]
        set(value) {
            marr[12] = value
        }
    internal inline var m31: Double
        get() = marr[13]
        set(value) {
            marr[13] = value
        }
    internal inline var m32: Double
        get() = marr[14]
        set(value) {
            marr[14] = value
        }
    internal inline var m33: Double
        get() = marr[15]
        set(value) {
            marr[15] = value
        }

    operator fun get(values: DoubleArray? = null): DoubleArray {
        return values?.also {
            marr.copyInto(values)
        } ?: marr.clone()
    }

    /**
     * Multiplies this matrix with the specified matrix.
     *
     * @param m matrix to multiply
     */
    operator fun times(m: Matrix4d): Matrix4d {
        val newArray = doubleArrayOf(
            this.m00 * m.m00 + this.m01 * m.m10 + this.m02 * m.m20 + this.m03 * m.m30,
            this.m00 * m.m01 + this.m01 * m.m11 + this.m02 * m.m21 + this.m03 * m.m31,
            this.m00 * m.m02 + this.m01 * m.m12 + this.m02 * m.m22 + this.m03 * m.m32,
            this.m00 * m.m03 + this.m01 * m.m13 + this.m02 * m.m23 + this.m03 * m.m33,
            this.m10 * m.m00 + this.m11 * m.m10 + this.m12 * m.m20 + this.m13 * m.m30,
            this.m10 * m.m01 + this.m11 * m.m11 + this.m12 * m.m21 + this.m13 * m.m31,
            this.m10 * m.m02 + this.m11 * m.m12 + this.m12 * m.m22 + this.m13 * m.m32,
            this.m10 * m.m03 + this.m11 * m.m13 + this.m12 * m.m23 + this.m13 * m.m33,
            this.m20 * m.m00 + this.m21 * m.m10 + this.m22 * m.m20 + this.m23 * m.m30,
            this.m20 * m.m01 + this.m21 * m.m11 + this.m22 * m.m21 + this.m23 * m.m31,
            this.m20 * m.m02 + this.m21 * m.m12 + this.m22 * m.m22 + this.m23 * m.m32,
            this.m20 * m.m03 + this.m21 * m.m13 + this.m22 * m.m23 + this.m23 * m.m33,
            this.m30 * m.m00 + this.m31 * m.m10 + this.m32 * m.m20 + this.m33 * m.m30,
            this.m30 * m.m01 + this.m31 * m.m11 + this.m32 * m.m21 + this.m33 * m.m31,
            this.m30 * m.m02 + this.m31 * m.m12 + this.m32 * m.m22 + this.m33 * m.m32,
            this.m30 * m.m03 + this.m31 * m.m13 + this.m32 * m.m23 + this.m33 * m.m33
        )
        return Matrix4d( newArray)
    }

    //    public final double getScale() {
    //
    //        double[] tmp_rot = new double[9];  // scratch matrix
    //
    //        double[] tmp_scale = new double[3];  // scratch matrix
    //
    //        getScaleRotate(tmp_scale, tmp_rot);
    //
    //        return (Matrix3d.maxOf3Values(tmp_scale));
    //
    //    }
    //    private final void getScaleRotate(double scales[], double rots[]) {
    //
    //        double[] tmp = new double[9];  // scratch matrix
    //
    //        tmp[0] = m00;
    //
    //        tmp[1] = m01;
    //
    //        tmp[2] = m02;
    //
    //        tmp[3] = m10;
    //
    //        tmp[4] = m11;
    //
    //        tmp[5] = m12;
    //
    //        tmp[6] = m20;
    //
    //        tmp[7] = m21;
    //
    //        tmp[8] = m22;
    //
    //        Matrix3d.compute_svd(tmp, scales, rots);
    //
    //        return;
    //
    //    }
    fun determinant(): Double {
        var det: Double =
            m00 * (m11 * m22 * m33 + m12 * m23 * m31 + m13 * m21 * m32 - m13 * m22 * m31 - m11 * m23 * m32 - m12 * m21 * m33)
        det -= m01 * (m10 * m22 * m33 + m12 * m23 * m30 + m13 * m20 * m32 - m13 * m22 * m30 - m10 * m23 * m32 - m12 * m20 * m33)
        det += m02 * (m10 * m21 * m33 + m11 * m23 * m30 + m13 * m20 * m31 - m13 * m21 * m30 - m10 * m23 * m31 - m11 * m20 * m33)
        det -= m03 * (m10 * m21 * m32 + m11 * m22 * m30 + m12 * m20 * m31 - m12 * m21 * m30 - m10 * m22 * m31 - m11 * m20 * m32)
        return det
    }
}

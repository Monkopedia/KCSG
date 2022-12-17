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

/**
 *
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
internal class Matrix4d {
    var m00 = 0.0
    var m01 = 0.0
    var m02 = 0.0
    var m03 = 0.0
    var m10 = 0.0
    var m11 = 0.0
    var m12 = 0.0
    var m13 = 0.0
    var m20 = 0.0
    var m21 = 0.0
    var m22 = 0.0
    var m23 = 0.0
    var m30 = 0.0
    var m31 = 0.0
    var m32 = 0.0
    var m33 = 0.0

    constructor()
    constructor(v: DoubleArray) {
        m00 = v[0]
        m01 = v[1]
        m02 = v[2]
        m03 = v[3]
        m10 = v[4]
        m11 = v[5]
        m12 = v[6]
        m13 = v[7]
        m20 = v[8]
        m21 = v[9]
        m22 = v[10]
        m23 = v[11]
        m30 = v[12]
        m31 = v[13]
        m32 = v[14]
        m33 = v[15]
    }

    fun set(values: DoubleArray = DoubleArray(16)) {
        m00 = values[0]
        m01 = values[1]
        m02 = values[2]
        m03 = values[3]
        m10 = values[4]
        m11 = values[5]
        m12 = values[6]
        m13 = values[7]
        m20 = values[8]
        m21 = values[9]
        m22 = values[10]
        m23 = values[11]
        m30 = values[12]
        m31 = values[13]
        m32 = values[14]
        m33 = values[15]
    }

    operator fun get(values: DoubleArray = DoubleArray(16)): DoubleArray {
        values[0] = m00
        values[1] = m01
        values[2] = m02
        values[3] = m03
        values[4] = m10
        values[5] = m11
        values[6] = m12
        values[7] = m13
        values[8] = m20
        values[9] = m21
        values[10] = m22
        values[11] = m23
        values[12] = m30
        values[13] = m31
        values[14] = m32
        values[15] = m33
        return values
    }

    /**
     * Multiplies this matrix with the specified matrix.
     *
     * @param m matrix to multiply
     */
    fun mul(m: Matrix4d) {
        val m00: Double
        val m01: Double
        val m02: Double
        val m03: Double
        val m10: Double
        val m11: Double
        val m12: Double
        val m13: Double
        val m20: Double
        val m21: Double
        val m22: Double
        val m23: Double
        val m30: Double
        val m31: Double
        val m32: Double
        val m33: Double // vars for temp result matrix
        m00 = this.m00 * m.m00 + this.m01 * m.m10 + this.m02 * m.m20 + this.m03 * m.m30
        m01 = this.m00 * m.m01 + this.m01 * m.m11 + this.m02 * m.m21 + this.m03 * m.m31
        m02 = this.m00 * m.m02 + this.m01 * m.m12 + this.m02 * m.m22 + this.m03 * m.m32
        m03 = this.m00 * m.m03 + this.m01 * m.m13 + this.m02 * m.m23 + this.m03 * m.m33
        m10 = this.m10 * m.m00 + this.m11 * m.m10 + this.m12 * m.m20 + this.m13 * m.m30
        m11 = this.m10 * m.m01 + this.m11 * m.m11 + this.m12 * m.m21 + this.m13 * m.m31
        m12 = this.m10 * m.m02 + this.m11 * m.m12 + this.m12 * m.m22 + this.m13 * m.m32
        m13 = this.m10 * m.m03 + this.m11 * m.m13 + this.m12 * m.m23 + this.m13 * m.m33
        m20 = this.m20 * m.m00 + this.m21 * m.m10 + this.m22 * m.m20 + this.m23 * m.m30
        m21 = this.m20 * m.m01 + this.m21 * m.m11 + this.m22 * m.m21 + this.m23 * m.m31
        m22 = this.m20 * m.m02 + this.m21 * m.m12 + this.m22 * m.m22 + this.m23 * m.m32
        m23 = this.m20 * m.m03 + this.m21 * m.m13 + this.m22 * m.m23 + this.m23 * m.m33
        m30 = this.m30 * m.m00 + this.m31 * m.m10 + this.m32 * m.m20 + this.m33 * m.m30
        m31 = this.m30 * m.m01 + this.m31 * m.m11 + this.m32 * m.m21 + this.m33 * m.m31
        m32 = this.m30 * m.m02 + this.m31 * m.m12 + this.m32 * m.m22 + this.m33 * m.m32
        m33 = this.m30 * m.m03 + this.m31 * m.m13 + this.m32 * m.m23 + this.m33 * m.m33
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m03 = m03
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m13 = m13
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        this.m23 = m23
        this.m30 = m30
        this.m31 = m31
        this.m32 = m32
        this.m33 = m33
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

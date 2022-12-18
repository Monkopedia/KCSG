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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.ext.vvecmath

import com.monkopedia.kcsg.Vector3d
import kotlin.math.abs

/**
 * Internal utility class.
 */
internal object VectorUtilInternal {
    fun toString(v: Vector3d): String {
        return "[" + v.x.toString() + ", " + v.y.toString() + ", " + v.z.toString() + "]"
    }

    fun equals(thisV: Vector3d, obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (thisV.javaClass !== obj.javaClass) {
            return false
        }
        val other: Vector3d = obj as Vector3d
        if (abs(thisV.x - other.x) > Plane.TOL) {
            return false
        }
        if (abs(thisV.y - other.y) > Plane.TOL) {
            return false
        }
        return abs(thisV.z - other.z) <= Plane.TOL
    }

    fun getHashCode(v: Vector3d): Int {
        var hash = 7
        hash =
            67 * hash + (
            java.lang.Double.doubleToLongBits(v.x) xor (
                java.lang.Double.doubleToLongBits(
                    v.x
                ) ushr 32
                )
            ).toInt()
        hash =
            67 * hash + (
            java.lang.Double.doubleToLongBits(v.y) xor (
                java.lang.Double.doubleToLongBits(
                    v.y
                ) ushr 32
                )
            ).toInt()
        hash =
            67 * hash + (
            java.lang.Double.doubleToLongBits(v.z) xor (
                java.lang.Double.doubleToLongBits(
                    v.z
                ) ushr 32
                )
            ).toInt()
        return hash
    }
}

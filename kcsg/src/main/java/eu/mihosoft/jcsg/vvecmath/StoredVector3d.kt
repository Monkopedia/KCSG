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
 * A 3d vector that is stored in an external double array. As of today Java does
 * not support value objects. This class is designed to be used to store vast
 * amouts of vectors in large double arrays without the overhead that typically
 * comes with large object arrays. Another use case is shared memory with native
 * code.
 *
 * @author Michael Hoffer <info></info>@michaelhoffer.de>
 */
interface StoredVector3d : Vector3d {
    /**
     * Sets the array that is used to store this vector.
     *
     * @param storage storage array to set
     */
    fun setStorage(storage: DoubleArray?)
    /**
     * Returns the storage offset used by this vector.
     *
     * @return the storage offset used by this vector
     */
    /**
     * Sets the storage offset used by this vector.
     *
     * @param offset offset to set
     */
    var offset: Int
    /**
     * Returns the stride used to store the vector elements (x,y,z).
     *
     * @return the stride used to store the vector elements (x,y,z)
     */
    /**
     * Sets the stride used to store the vector elements (x,y,z).
     *
     * @param stride the stride to set
     */
    var stride: Int

    companion object {
        /**
         * Returns the struct size of this vector (3).
         *
         * @return the struct size of this vector (3)
         */
        val structSize: Int
            get() = 3
        /**
         * Creates a new stored vector from the specified double array.
         *
         * @param storage double array used to store the vector
         * @param offset the storage offset used by the vector
         * @param stride the stride used to store the vector elements (x,y,z)
         * @return a new stored vector from the specified double array
         */
        /**
         * Creates a new stored vector from the specified double array.
         *
         * @param storage double array used to store the vector
         * @param offset the storage offset used by the vector
         * @return a new stored vector from the specified double array
         */
        @JvmOverloads
        fun from(storage: DoubleArray, offset: Int, stride: Int = 1): StoredVector3d? {
            val result = StoredVector3dImpl()
            result.setStorage(storage)
            result.offset = (offset)
            result.stride = (stride)
            return result
        }
    }
}
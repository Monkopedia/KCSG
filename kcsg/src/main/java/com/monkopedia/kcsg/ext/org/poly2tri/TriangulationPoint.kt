/**
 * TriangulationPoint.java
 *
 * Copyright 2014-2014 Michael Hoffer <info></info>@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info></info>@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info></info>@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info></info>@michaelhoffer.de>.
 */
package com.monkopedia.kcsg.ext.org.poly2tri

import com.monkopedia.kcsg.Vector3d

/* Poly2Tri
 * Copyright (c) 2009-2010, Poly2Tri Contributors
 * http://code.google.com/p/poly2tri/
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Poly2Tri nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
internal open class TriangulationPoint(val vec: Vector3d) {
    constructor(x: Double, y: Double, z: Double = 0.0) : this(Vector3d.xyz(x, y, z))

    // List of edges this point constitutes an upper ending point (CDT)
    lateinit var edges: ArrayList<DTSweepConstraint>

    override fun toString(): String {
        return "[${vec.x},${vec.y}]"
    }

    fun addEdge(e: DTSweepConstraint) {
        if (!::edges.isInitialized) {
            edges = ArrayList()
        }
        edges.add(e)
    }

    fun hasEdges(): Boolean {
        return ::edges.isInitialized
    }

    /**
     * @param p - edge destination point
     * @return the edge from this point to given point
     */
    fun getEdge(p: TriangulationPoint): DTSweepConstraint? {
        for (c in edges) {
            if (c.p === p) {
                return c
            }
        }
        return null
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is TriangulationPoint) {
            return vec.x == obj.vec.x && vec.y == obj.vec.y
        }
        return super.equals(obj)
    }

    override fun hashCode(): Int {
        var bits = java.lang.Double.doubleToLongBits(vec.x)
        bits = bits xor java.lang.Double.doubleToLongBits(vec.y) * 31
        return bits.toInt() xor (bits shr 32).toInt()
    }
}

internal inline val TriangulationPoint.x: Double
    get() = vec.x
internal inline val TriangulationPoint.y: Double
    get() = vec.y
internal inline val TriangulationPoint.z: Double
    get() = vec.z
internal inline val TriangulationPoint.xf: Float
    get() = vec.xf
internal inline val TriangulationPoint.yf: Float
    get() = vec.yf
internal inline val TriangulationPoint.zf: Float
    get() = vec.zf

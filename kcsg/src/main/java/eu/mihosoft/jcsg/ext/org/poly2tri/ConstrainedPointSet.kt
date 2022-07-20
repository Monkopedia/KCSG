/**
 * ConstrainedPointSet.java
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
package eu.mihosoft.jcsg.ext.org.poly2tri

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
 */ /**
 * Exteet by adding some Constraints on how it will be triangulated<br></br>
 * A constraint defines an edge between two points in the set, these edges can not
 * be crossed. They will be enforced triangle edges after a triangulation.
 *
 *
 *
 *
 * @author Thomas ???, thahlen@gmail.com
 */
internal class ConstrainedPointSet : PointSet {
    private var edgeIndex: IntArray = intArrayOf()
    private var _constrainedPointList: MutableList<TriangulationPoint>? = null

    constructor(points: List<TriangulationPoint>, index: IntArray) : super(points) {
        edgeIndex = index
    }

    /**
     *
     * @param points - A list of all points in PointSet
     * @param constraints - Pairs of two points defining a constraint, all points **must** be part of given PointSet!
     */
    constructor(points: List<TriangulationPoint>, constraints: List<TriangulationPoint>) : super(
        points
    ) {
        _constrainedPointList = ArrayList<TriangulationPoint>().apply {
            addAll(constraints)
        }
    }

    override val triangulationMode: TriangulationMode
        get() = TriangulationMode.CONSTRAINED

    override fun prepareTriangulation(tcx: TriangulationContext<*>) {
        super.prepareTriangulation(tcx)
        if (_constrainedPointList != null) {
            var p1: TriangulationPoint
            var p2: TriangulationPoint
            val iterator: Iterator<*> = _constrainedPointList!!.iterator()
            while (iterator.hasNext()) {
                p1 = iterator.next() as TriangulationPoint
                p2 = iterator.next() as TriangulationPoint
                tcx.newConstraint(p1, p2)
            }
        } else {
            var i = 0
            while (i < edgeIndex.size) {

                // XXX: must change!!
                tcx.newConstraint(mutablePoints[edgeIndex[i]], mutablePoints[edgeIndex[i + 1]])
                i += 2
            }
        }
    }

    /**
     * TODO: TO BE IMPLEMENTED!
     * Peforms a validation on given input<br></br>
     * 1. Check's if there any constraint edges are crossing or collinear<br></br>
     * 2.
     * @return
     */
    val isValid: Boolean
        get() = true
}

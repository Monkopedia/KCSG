/**
 * DTSweepContext.java
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

import java.util.*

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
 * @author Thomas ??? (thahlen@gmail.com)
 */
internal class DTSweepContext : TriangulationContext<DTSweepDebugContext>() {

    /** Advancing front  */
    lateinit var advancingFront: AdvancingFront

    /** head point used with advancing front  */
    var head: TriangulationPoint? = null

    /** tail point used with advancing front  */
    private var tail: TriangulationPoint? = null
    var basin = Basin()
    var edgeEvent = EdgeEvent()
    private val _comparator = DTSweepPointComparator()

    override fun isDebugEnabled(b: Boolean) {
        if (b) {
            if (debugContext == null) {
                debugContext = DTSweepDebugContext(this)
            }
        }
        isDebugEnabled = b
    }

    fun removeFromList(triangle: DelaunayTriangle?) {
        mutableTriangles.remove(triangle)
        // TODO: remove all neighbor pointers to this triangle
//        triangle?.neighbors?.forEach { it?.clearNeighbor(triangle) }
//        triangle?.clearNeighbors()
    }

    fun meshClean(triangle: DelaunayTriangle?) {
        var t1: DelaunayTriangle
        var t2: DelaunayTriangle?
        if (triangle != null) {
            val deque = ArrayDeque<DelaunayTriangle>()
            deque.addFirst(triangle)
            triangle.isInterior(true)
            while (!deque.isEmpty()) {
                t1 = deque.removeFirst()
                triangulatable!!.addTriangle(t1)
                for (i in 0 until 3) {
                    if (!t1.cEdge[i]) {
                        t2 = t1.neighbors[i]
                        if (t2 != null && !t2.isInterior) {
                            t2.isInterior(true)
                            deque.addLast(t2)
                        }
                    }
                }
            }
        }
    }

    override fun clear() {
        super.clear()
        mutableTriangles.clear()
    }

    fun locateNode(point: TriangulationPoint): AdvancingFrontNode? {
        return advancingFront.locateNode(point)
    }

    fun createAdvancingFront() {
        val head: AdvancingFrontNode
        val tail: AdvancingFrontNode
        val middle: AdvancingFrontNode
        // Initial triangle
        val iTriangle = DelaunayTriangle(
            mutablePoints[0],
            this.tail,
            this.head
        )
        addToList(iTriangle)
        head = AdvancingFrontNode(iTriangle.points[1]!!)
        head.triangle = iTriangle
        middle = AdvancingFrontNode(iTriangle.points[0]!!)
        middle.triangle = iTriangle
        tail = AdvancingFrontNode(iTriangle.points[2]!!)
        advancingFront = AdvancingFront(head, tail)

        // TODO: I think it would be more intuitive if head is middles next and not previous
        //       so swap head and tail
        advancingFront.head.next = middle
        middle.next = advancingFront.tail
        middle.previous = advancingFront.head
        advancingFront.tail.previous = middle
    }

    internal inner class Basin {
        lateinit var leftNode: AdvancingFrontNode
        lateinit var bottomNode: AdvancingFrontNode
        lateinit var rightNode: AdvancingFrontNode
        var width = 0.0
        var leftHighest = false
    }

    internal inner class EdgeEvent {
        var constrainedEdge: DTSweepConstraint? = null
        var right = false
    }

    /**
     * Try to map a node to all sides of this triangle that don't have
     * a neighbor.
     *
     * @param t
     */
    fun mapTriangleToNodes(t: DelaunayTriangle) {
        var n: AdvancingFrontNode?
        for (i in 0..2) {
            if (t.neighbors[i] == null) {
                n = advancingFront.locatePoint(t.pointCW(t.points[i]!!))
                n.triangle = t
            }
        }
    }

    override fun prepareTriangulation(t: Triangulatable) {
        super.prepareTriangulation(t)
        var xmax: Double
        var xmin: Double
        var ymax: Double
        var ymin: Double
        xmin = mutablePoints[0].x
        xmax = xmin
        ymin = mutablePoints[0].y
        ymax = ymin
        // Calculate bounds. Should be combined with the sorting
        for (p in mutablePoints) {
            if (p.x > xmax) xmax = p.x
            if (p.x < xmin) xmin = p.x
            if (p.y > ymax) ymax = p.y
            if (p.y < ymin) ymin = p.y
        }
        val deltaX = ALPHA * (xmax - xmin)
        val deltaY = ALPHA * (ymax - ymin)
        val p1 = TPoint(xmax + deltaX, ymin - deltaY)
        val p2 = TPoint(xmin - deltaX, ymin - deltaY)
        head = p1
        tail = p2

        mutablePoints.sortWith(_comparator)
    }

    fun finalizeTriangulation() {
        triangulatable!!.addTriangles(mutableTriangles)
        mutableTriangles.clear()
    }

    override fun newConstraint(
        a: TriangulationPoint,
        b: TriangulationPoint
    ): TriangulationConstraint {
        return DTSweepConstraint(a, b)
    }

    companion object {
        // Inital triangle factor, seed triangle will extend 30% of
        // PointSet width to both left and right.
        private const val ALPHA = 0.3f
    }

    init {
        clear()
    }
}

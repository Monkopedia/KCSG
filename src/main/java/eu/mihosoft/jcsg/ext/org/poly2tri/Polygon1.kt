/**
 * Polygon.java
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

import org.slf4j.LoggerFactory
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
 */internal class Polygon : Triangulatable {
    private var _points = ArrayList<TriangulationPoint>()
    private var _steinerPoints: ArrayList<TriangulationPoint>? = null
    private var _holes: ArrayList<Polygon>? = null
    private var m_triangles: MutableList<DelaunayTriangle>? = null
    var point: PolygonPoint? = null
        private set

    /**
     * To create a polygon we need atleast 3 separate points
     *
     * @param p1
     * @param p2
     * @param p3
     */
    constructor(p1: PolygonPoint, p2: PolygonPoint, p3: PolygonPoint) {
        p1.next = p2
        p2.next = p3
        p3.next = p1
        p1.previous = p3
        p2.previous = p1
        p3.previous = p2
        _points.add(p1)
        _points.add(p2)
        _points.add(p3)
    }

    /**
     * Requires atleast 3 points
     * @param points - ordered list of points forming the polygon.
     * No duplicates are allowed
     */
    constructor(points: MutableList<PolygonPoint>) {
        // Lets do one sanity check that first and last point hasn't got same position
        // Its something that often happen when importing polygon data from other formats
        if (points[0] == points[points.size - 1]) {
            logger.warn("Removed duplicate point")
            points.removeAt(points.size - 1)
        }
        _points.addAll(points)
    }

    /**
     * Requires atleast 3 points
     *
     * @param points
     */
    constructor(points: Array<PolygonPoint>) : this(mutableListOf<PolygonPoint>(*points))

    override val triangulationMode: TriangulationMode
        get() = TriangulationMode.POLYGON

    fun pointCount(): Int {
        var count = _points.size
        if (_steinerPoints != null) {
            count += _steinerPoints!!.size
        }
        return count
    }

    fun addSteinerPoint(point: TriangulationPoint) {
        if (_steinerPoints == null) {
            _steinerPoints = ArrayList()
        }
        _steinerPoints!!.add(point)
    }

    fun addSteinerPoints(points: List<TriangulationPoint>) {
        if (_steinerPoints == null) {
            _steinerPoints = ArrayList()
        }
        _steinerPoints!!.addAll(points)
    }

    fun clearSteinerPoints() {
        if (_steinerPoints != null) {
            _steinerPoints!!.clear()
        }
    }

    /**
     * Assumes: that given polygon is fully inside the current polygon
     * @param poly - a subtraction polygon
     */
    fun addHole(poly: Polygon) {
        if (_holes == null) {
            _holes = ArrayList()
        }
        _holes!!.add(poly)
        // XXX: tests could be made here to be sure it is fully inside
//        addSubtraction( poly.getPoints() );
    }

    /**
     * Will insert a point in the polygon after given point
     *
     * @param a
     * @param b
     * @param p
     */
    fun insertPointAfter(a: PolygonPoint, newPoint: PolygonPoint) {
        // Validate that 
        val index = _points.indexOf(a)
        if (index != -1) {
            newPoint.next = a.next
            newPoint.previous = a
            a.next!!.previous = newPoint
            a.next = newPoint
            _points.add(index + 1, newPoint)
        } else {
            throw RuntimeException("Tried to insert a point into a Polygon after a point not belonging to the Polygon")
        }
    }

    fun addPoints(list: List<PolygonPoint>) {
        for (p in list) {
            p.previous = point
            if (point != null) {
                p.next = point!!.next
                point!!.next = p
            }
            point = p
            _points.add(p)
        }
        val first: PolygonPoint? = _points[0] as PolygonPoint?
        point!!.next = first
        first!!.previous = point
    }

    /**
     * Will add a point after the last point added
     *
     * @param p
     */
    fun addPoint(p: PolygonPoint) {
        p.previous = point
        p.next = point!!.next
        point!!.next = p
        _points.add(p)
    }

    fun removePoint(p: PolygonPoint) {
        val next: PolygonPoint? = p.next
        val prev: PolygonPoint? = p.previous
        prev!!.next = next
        next!!.previous = prev
        _points.remove(p)
    }

    override val points: List<TriangulationPoint>
        get() = _points
    override val triangles: List<DelaunayTriangle>?
        get() = m_triangles

    override fun addTriangle(t: DelaunayTriangle) {
        m_triangles!!.add(t)
    }

    override fun addTriangles(list: List<DelaunayTriangle>?) {
        m_triangles!!.addAll(list!!)
    }

    override fun clearTriangulation() {
        if (m_triangles != null) {
            m_triangles!!.clear()
        }
    }

    /**
     * Creates constraints and populates the context with points
     */
    override fun prepareTriangulation(tcx: TriangulationContext<*>) {
        if (m_triangles == null) {
            m_triangles = ArrayList(_points.size)
        } else {
            m_triangles!!.clear()
        }

        // Outer constraints
        for (i in 0 until _points.size - 1) {
            tcx.newConstraint(_points[i], _points[i + 1])
        }
        tcx.newConstraint(_points[0], _points[_points.size - 1])
        tcx.addPoints(_points)

        // Hole constraints
        if (_holes != null) {
            for (p in _holes!!) {
                for (i in 0 until p._points.size - 1) {
                    tcx.newConstraint(p._points[i], p._points[i + 1])
                }
                tcx.newConstraint(p._points[0], p._points[p._points.size - 1])
                tcx.addPoints(p._points)
            }
        }
        if (_steinerPoints != null) {
            tcx.addPoints(_steinerPoints!!)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Polygon::class.java)
    }
}
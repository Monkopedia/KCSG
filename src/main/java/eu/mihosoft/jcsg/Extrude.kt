/**
 * Extrude.java
 *
 * Copyright 2014-2017 Michael Hoffer <info></info>@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info></info>@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info></info>@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info></info>@michaelhoffer.de>.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.jcsg.ext.org.poly2tri.PolygonUtil
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.util.*

/**
 * Extrudes concave and convex polygons.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Extrude private constructor() {
    companion object {
        /**
         * Extrudes the specified path (convex or concave polygon without holes or
         * intersections, specified in CCW) into the specified direction.
         *
         * @param dir direction
         * @param points path (convex or concave polygon without holes or
         * intersections)
         *
         * @return a CSG object that consists of the extruded polygon
         */
        fun points(dir: Vector3d, vararg points: Vector3d?): CSG {
            return extrude(dir, Polygon.fromPoints(toCCW(listOf(*points))))
        }

        /**
         * Extrudes the specified path (convex or concave polygon without holes or
         * intersections, specified in CCW) into the specified direction.
         *
         * @param dir direction
         * @param points path (convex or concave polygon without holes or
         * intersections)
         *
         * @return a CSG object that consists of the extruded polygon
         */
        fun points(dir: Vector3d, points: List<Vector3d?>?): CSG {
            val newList: List<Vector3d?> = ArrayList(points)
            return extrude(dir, Polygon.fromPoints(toCCW(newList)))
        }

        /**
         * Extrudes the specified path (convex or concave polygon without holes or
         * intersections, specified in CCW) into the specified direction.
         *
         * @param dir direction
         * @param points path (convex or concave polygon without holes or
         * intersections)
         *
         * @return a list containing the extruded polygon
         */
        fun points(
            dir: Vector3d,
            top: Boolean,
            bottom: Boolean,
            vararg points: Vector3d?
        ): List<Polygon?> {
            return extrude(
                dir,
                Polygon.fromPoints(toCCW(listOf(*points))),
                top,
                bottom
            )
        }

        /**
         * Extrudes the specified path (convex or concave polygon without holes or
         * intersections, specified in CCW) into the specified direction.
         *
         * @param dir direction
         * @param points1 path (convex or concave polygon without holes or
         * intersections)
         * @param points1 path (convex or concave polygon without holes or
         * intersections)
         *
         * @return a list containing the extruded polygon
         */
        fun points(
            dir: Vector3d,
            top: Boolean,
            bottom: Boolean,
            points1: List<Vector3d?>?
        ): List<Polygon?> {
            val newList1: List<Vector3d?> = ArrayList(points1)
            return extrude(dir, Polygon.fromPoints(toCCW(newList1)), top, bottom)
        }

        /**
         * Combines two polygons into one CSG object. Polygons p1 and p2 are treated as top and
         * bottom of a tube segment with p1 and p2 as the profile. **Note:** both polygons must have the
         * same number of vertices. This method does not guarantee intersection-free CSGs. It is in the
         * responsibility of the caller to ensure that the orientation of p1 and p2 allow for
         * intersection-free combination of both.
         *
         * @param p1 first polygon
         * @param p2 second polygon
         * @return List of polygons
         */
        fun combine(p1: Polygon, p2: Polygon): CSG {
            return CSG.fromPolygons(combine(p1, p2, true, true))
        }

        /**
         * Combines two polygons into one CSG object. Polygons p1 and p2 are treated as top and
         * bottom of a tube segment with p1 and p2 as the profile. **Note:** both polygons must have the
         * same number of vertices. This method does not guarantee intersection-free CSGs. It is in the
         * responsibility of the caller to ensure that the orientation of p1 and p2 allow for
         * intersection-free combination of both.
         *
         * @param p1 first polygon
         * @param p2 second polygon
         * @param bottom defines whether to close the bottom of the tube
         * @param top defines whether to close the top of the tube
         * @return List of polygons
         */
        private fun combine(
            p1: Polygon,
            p2: Polygon,
            bottom: Boolean,
            top: Boolean
        ): MutableList<Polygon> {
            val newPolygons: MutableList<Polygon> = ArrayList()
            if (p1.vertices.size != p2.vertices.size) {
                throw RuntimeException("Polygons must have the same number of vertices")
            }
            val numVertices = p1.vertices.size
            if (bottom) {
                newPolygons.add(p1.flipped())
            }
            for (i in 0 until numVertices) {
                val nexti = (i + 1) % numVertices
                val bottomV1 = p1.vertices[i]!!.pos
                val topV1 = p2.vertices[i]!!.pos
                val bottomV2 = p1.vertices[nexti]!!.pos
                val topV2 = p2.vertices[nexti]!!.pos
                var pPoints: List<Vector3d?>
                pPoints = listOf(bottomV2, topV2, topV1)
                newPolygons.add(Polygon.fromPoints(pPoints, p1.storage))
                pPoints = listOf(bottomV2, topV1, bottomV1)
                newPolygons.add(Polygon.fromPoints(pPoints, p1.storage))
            }
            if (top) {
                newPolygons.add(p2)
            }
            return newPolygons
        }

        private fun extrude(dir: Vector3d, polygon1: Polygon): CSG {
            val newPolygons: MutableList<Polygon> = ArrayList()
            require(dir.z() >= 0) { "z < 0 currently not supported for extrude: $dir" }
            newPolygons.addAll(PolygonUtil.concaveToConvex(polygon1))
            var polygon2 = polygon1.translated(dir)
            val numvertices = polygon1.vertices.size
            for (i in 0 until numvertices) {
                val nexti = (i + 1) % numvertices
                val bottomV1 = polygon1.vertices[i]!!.pos
                val topV1 = polygon2.vertices[i]!!.pos
                val bottomV2 = polygon1.vertices[nexti]!!.pos
                val topV2 = polygon2.vertices[nexti]!!.pos
                val pPoints = listOf(bottomV2, topV2, topV1, bottomV1)
                newPolygons.add(Polygon.fromPoints(pPoints, polygon1.storage))
            }
            polygon2 = polygon2.flipped()
            val topPolygons: List<Polygon> = PolygonUtil.concaveToConvex(polygon2)
            newPolygons.addAll(topPolygons)
            return CSG.fromPolygons(newPolygons)
        }

        private fun extrude(
            dir: Vector3d,
            polygon1: Polygon,
            top: Boolean,
            bottom: Boolean
        ): List<Polygon> {
            val newPolygons: MutableList<Polygon> = ArrayList()
            if (bottom) {
                newPolygons.addAll(PolygonUtil.concaveToConvex(polygon1))
            }
            var polygon2 = polygon1.translated(dir)
            var rot = Transform.unity()
            val a = polygon2.plane.normal.normalized()
            val b = dir.normalized()
            val c = a.crossed(b)
            val l = c.magnitude() // sine of angle
            if (l > 1e-9) {
                val axis = c.times(1.0 / l)
                val angle = a.angle(b)
                var sx = 0.0
                var sy = 0.0
                var sz = 0.0
                val n = polygon2.vertices.size
                for (v in polygon2.vertices) {
                    sx += v!!.pos.x()
                    sy += v.pos.y()
                    sz += v.pos.z()
                }
                val center = Vector3d.xyz(sx / n, sy / n, sz / n)
                rot = rot.rot(center, axis, angle * Math.PI / 180.0)
                for (v in polygon2.vertices) {
                    v!!.pos = rot.transform(v.pos)
                }
            }
            val numvertices = polygon1.vertices.size
            for (i in 0 until numvertices) {
                val nexti = (i + 1) % numvertices
                val bottomV1 = polygon1.vertices[i]!!.pos
                val topV1 = polygon2.vertices[i]!!.pos
                val bottomV2 = polygon1.vertices[nexti]!!.pos
                val topV2 = polygon2.vertices[nexti]!!.pos
                val pPoints = listOf(bottomV2, topV2, topV1, bottomV1)
                newPolygons.add(Polygon.fromPoints(pPoints, polygon1.storage))
            }
            polygon2 = polygon2.flipped()
            val topPolygons: List<Polygon> = PolygonUtil.concaveToConvex(polygon2)
            if (top) {
                newPolygons.addAll(topPolygons)
            }
            return newPolygons
        }

        private fun toCCW(points: List<Vector3d?>?): List<Vector3d?> {
            val result: List<Vector3d?> = ArrayList(points)
            if (!isCCW(Polygon.fromPoints(result))) {
                Collections.reverse(result)
            }
            return result
        }

        fun toCW(points: List<Vector3d?>?): List<Vector3d?> {
            val result: List<Vector3d?> = ArrayList(points)
            if (isCCW(Polygon.fromPoints(result))) {
                Collections.reverse(result)
            }
            return result
        }

        /**
         * Indicates whether the specified polygon is defined counter-clockwise.
         * @param polygon polygon
         * @return `true` if the specified polygon is defined counter-clockwise;
         * `false` otherwise
         */
        fun isCCW(polygon: Polygon?): Boolean {
            // thanks to Sepp Reiter for explaining me the algorithm!
            require(polygon!!.vertices.size >= 3) { "Only polygons with at least 3 vertices are supported!" }

            // search highest left vertex
            var highestLeftVertexIndex = 0
            var highestLeftVertex = polygon.vertices[0]
            for (i in polygon.vertices.indices) {
                val v = polygon.vertices[i]
                if (v!!.pos.y() > highestLeftVertex!!.pos.y()) {
                    highestLeftVertex = v
                    highestLeftVertexIndex = i
                } else if (v.pos.y() == highestLeftVertex.pos.y()
                    && v.pos.x() < highestLeftVertex.pos.x()
                ) {
                    highestLeftVertex = v
                    highestLeftVertexIndex = i
                }
            }

            // determine next and previous vertex indices
            val nextVertexIndex = (highestLeftVertexIndex + 1) % polygon.vertices.size
            var prevVertexIndex = highestLeftVertexIndex - 1
            if (prevVertexIndex < 0) {
                prevVertexIndex = polygon.vertices.size - 1
            }
            val nextVertex = polygon.vertices[nextVertexIndex]
            val prevVertex = polygon.vertices[prevVertexIndex]

            // edge 1
            val a1 = normalizedX(highestLeftVertex!!.pos, nextVertex!!.pos)

            // edge 2
            val a2 = normalizedX(highestLeftVertex.pos, prevVertex!!.pos)

            // select vertex with lowest x value
            var selectedVIndex: Int
            selectedVIndex = if (a2 > a1) {
                nextVertexIndex
            } else {
                prevVertexIndex
            }
            if (selectedVIndex == 0
                && highestLeftVertexIndex == polygon.vertices.size - 1
            ) {
                selectedVIndex = polygon.vertices.size
            }
            if (highestLeftVertexIndex == 0
                && selectedVIndex == polygon.vertices.size - 1
            ) {
                highestLeftVertexIndex = polygon.vertices.size
            }

            // indicates whether edge points from highestLeftVertexIndex towards
            // the sel index (ccw)
            return selectedVIndex > highestLeftVertexIndex
        }

        private fun normalizedX(v1: Vector3d?, v2: Vector3d?): Double {
            val v2MinusV1 = v2!!.minus(v1)
            return v2MinusV1.divided(v2MinusV1.magnitude()).times(Vector3d.X_ONE).x()
        } //    public static void main(String[] args) {
        //        System.out.println("1 CCW: " + isCCW(Polygon.fromPoints(
        //                new Vector3d(-1, -1),
        //                new Vector3d(0, -1),
        //                new Vector3d(1, 0),
        //                new Vector3d(1, 1)
        //        )));
        //
        //        System.out.println("3 CCW: " + isCCW(Polygon.fromPoints(
        //                new Vector3d(1, 1),
        //                new Vector3d(1, 0),
        //                new Vector3d(0, -1),
        //                new Vector3d(-1, -1)
        //        )));
        //
        //        System.out.println("2 CCW: " + isCCW(Polygon.fromPoints(
        //                new Vector3d(0, -1),
        //                new Vector3d(1, 0),
        //                new Vector3d(1, 1),
        //                new Vector3d(-1, -1)
        //        )));
        //
        //        System.out.println("4 CCW: " + isCCW(Polygon.fromPoints(
        //                new Vector3d(-1, -1),
        //                new Vector3d(-1, 1),
        //                new Vector3d(0, 0)
        //        )));
        //
        //        System.out.println("5 CCW: " + isCCW(Polygon.fromPoints(
        //                new Vector3d(0, 0),
        //                new Vector3d(0, 1),
        //                new Vector3d(0.5, 0.5),
        //                new Vector3d(1, 1.1),
        //                new Vector3d(1, 0)
        //        )));
        //    }
    }

    init {
        throw AssertionError("Don't instantiate me!", null)
    }
}
/**
 * Polygon.java
 *
 * Copyright 2014-2014 Michael Hoffer <info></info>@michaelhoffer.de>. All rights
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
import java.util.function.Consumer
import kotlin.math.abs

/**
 * Represents a convex polygon.
 *
 * Each convex polygon has a `shared` property, which is shared between
 * all polygons that are clones of each other or where split from the same
 * polygon. This can be used to define per-polygon properties (such as surface
 * color).
 */
class Polygon : Cloneable {
    /**
     * Polygon vertices
     */
    var vertices: List<Vertex>
        private set

    /**
     * Shared property (can be used for shared color etc.).
     */
    private var shared: PropertyStorage? = null

    /**
     * Plane defined by this polygon.
     *
     * **Note:** uses first three vertices to define the plane.
     */
    val _csg_plane: Plane
    private var _plane: eu.mihosoft.vvecmath.Plane

    /**
     * Returns the plane defined by this triangle.
     *
     * @return plane
     */
    val plane: eu.mihosoft.vvecmath.Plane
        get() {
            return _plane
        }

    /**
     * Indicates whether this polyon is valid, i.e., if it
     *
     * @return
     */
    val isValid: Boolean
        get() {
            return valid
        }

    private var valid = true

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * **Note:** the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     * @param shared shared property
     */
    constructor(vertices: List<Vertex>, shared: PropertyStorage?) {
        this.vertices = vertices
        this.shared = shared
        _csg_plane = Plane.createFromPoints(
            vertices[0].pos,
            vertices[1].pos,
            vertices[2].pos
        )
        _plane = eu.mihosoft.vvecmath.Plane.fromPointAndNormal(centroid(), _csg_plane.normal)
        validateAndInit(vertices)
    }

    private fun validateAndInit(vertices1: List<Vertex>) {
        for (v in vertices1) {
            v.normal = _csg_plane.normal
        }
        if (Vector3d.ZERO == _csg_plane.normal) {
            valid = false
            System.err.println(
                """
                    Normal is zero! Probably, duplicate points have been specified!
                    
                    ${toStlString()}
                """.trimIndent()
            )
            //            throw new RuntimeException(
//                    "Normal is zero! Probably, duplicate points have been specified!\n\n"+toStlString());
        }
        if (vertices.size < 3) {
            throw RuntimeException(
                "Invalid polygon: at least 3 vertices expected, got: " +
                    vertices.size
            )
        }
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * **Note:** the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     */
    constructor(vertices: List<Vertex>) {
        this.vertices = vertices
        _csg_plane = Plane.createFromPoints(
            vertices[0].pos,
            vertices[1].pos,
            vertices[2].pos
        )
        _plane = eu.mihosoft.vvecmath.Plane.fromPointAndNormal(centroid(), _csg_plane.normal)
        validateAndInit(vertices)
    }

    /**
     * Constructor. Creates a new polygon that consists of the specified
     * vertices.
     *
     * **Note:** the vertices used to initialize a polygon must be coplanar
     * and form a convex loop.
     *
     * @param vertices polygon vertices
     */
    constructor(vararg vertices: Vertex) : this(listOf<Vertex>(*vertices))

    public override fun clone(): Polygon {
        val newVertices: MutableList<Vertex> = ArrayList()
        vertices.forEach(
            Consumer { vertex: Vertex ->
                newVertices.add(
                    vertex.clone()
                )
            }
        )
        return Polygon(newVertices, storage)
    }

    /**
     * Flips this polygon.
     *
     * @return this polygon
     */
    fun flip(): Polygon {
        vertices.forEach { vertex: Vertex -> vertex.flip() }
        vertices = vertices.asReversed()
        _csg_plane.flip()
        _plane = _plane.flipped()
        return this
    }

    /**
     * Returns a flipped copy of this polygon.
     *
     * **Note:** this polygon is not modified.
     *
     * @return a flipped copy of this polygon
     */
    fun flipped(): Polygon {
        return clone().flip()
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @return this polygon in STL string format
     */
    fun toStlString(): String {
        return toStlString(StringBuilder()).toString()
    }

    /**
     * Returns this polygon in STL string format.
     *
     * @param sb string builder
     *
     * @return the specified string builder
     */
    fun toStlString(sb: StringBuilder): StringBuilder {
        if (vertices.size >= 3) {

            // TODO: improve the triangulation?
            //
            // STL requires triangular polygons.
            // If our polygon has more vertices, create
            // multiple triangles:
            val firstVertexStl = vertices[0].toStlString()
            for (i in 0 until vertices.size - 2) {
                sb.append("  facet normal ").append(_csg_plane.normal.toStlString()).append("\n")
                    .append("    outer loop\n").append("      ").append(firstVertexStl).append("\n")
                    .append("      ")
                vertices[i + 1].toStlString(sb).append("\n").append("      ")
                vertices[i + 2].toStlString(sb).append("\n").append("    endloop\n")
                    .append("  endfacet\n")
            }
        }
        return sb
    }

    /**
     * Returns a triangulated version of this polygon.
     *
     * @return triangles
     */
    fun toTriangles(): List<Polygon> {
        val result: MutableList<Polygon> = ArrayList()
        if (vertices.size >= 3) {

            // TODO: improve the triangulation?
            //
            // If our polygon has more vertices, create
            // multiple triangles:
            val firstVertexStl = vertices[0]
            for (i in 0 until vertices.size - 2) {

                // create triangle
                val polygon = fromPoints(
                    firstVertexStl.pos,
                    vertices[i + 1].pos,
                    vertices[i + 2].pos
                )
                result.add(polygon)
            }
        }
        return result
    }

    /**
     * Translates this polygon.
     *
     * @param v the vector that defines the translation
     * @return this polygon
     */
    fun translate(v: Vector3d?): Polygon {
        vertices.forEach(Consumer { vertex: Vertex -> vertex.pos = vertex.pos.plus(v) })
        val a = vertices[0].pos
        val b = vertices[1].pos
        val c = vertices[2].pos

        // TODO plane update correct?
        _csg_plane.normal = b.minus(a).crossed(c.minus(a))
        _plane = eu.mihosoft.vvecmath.Plane.fromPointAndNormal(centroid(), _csg_plane.normal)
        return this
    }

    /**
     * Returns a translated copy of this polygon.
     *
     * **Note:** this polygon is not modified
     *
     * @param v the vector that defines the translation
     *
     * @return a translated copy of this polygon
     */
    fun translated(v: Vector3d?): Polygon {
        return clone().translate(v)
    }

    /**
     * Applies the specified transformation to this polygon.
     *
     * **Note:** if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * @param transform the transformation to apply
     *
     * @return this polygon
     */
    fun transform(transform: Transform): Polygon {
        vertices.stream().forEach { v: Vertex -> v.transform(transform) }
        val a = vertices[0].pos
        val b = vertices[1].pos
        val c = vertices[2].pos
        _csg_plane.normal = b.minus(a).crossed(c.minus(a)).normalized()
        _csg_plane.dist = _csg_plane.normal.dot(a)
        _plane = eu.mihosoft.vvecmath.Plane.fromPointAndNormal(centroid(), _csg_plane.normal)
        vertices.forEach(Consumer { vertex: Vertex -> vertex.normal = _plane.normal })
        if (transform.isMirror) {
            // the transformation includes mirroring. flip polygon
            flip()
        }
        return this
    }

    /**
     * Returns a transformed copy of this polygon.
     *
     * **Note:** if the applied transformation performs a mirror operation
     * the vertex order of this polygon is reversed.
     *
     * **Note:** this polygon is not modified
     *
     * @param transform the transformation to apply
     * @return a transformed copy of this polygon
     */
    fun transformed(transform: Transform): Polygon {
        return clone().transform(transform)
    }

    /**
     * Returns the bounds of this polygon.
     *
     * @return bouds of this polygon
     */
    val bounds: Bounds
        get() {
            var minX = Double.POSITIVE_INFINITY
            var minY = Double.POSITIVE_INFINITY
            var minZ = Double.POSITIVE_INFINITY
            var maxX = Double.NEGATIVE_INFINITY
            var maxY = Double.NEGATIVE_INFINITY
            var maxZ = Double.NEGATIVE_INFINITY
            for (i in vertices.indices) {
                val vert = vertices[i]
                if (vert.pos.x() < minX) {
                    minX = vert.pos.x()
                }
                if (vert.pos.y() < minY) {
                    minY = vert.pos.y()
                }
                if (vert.pos.z() < minZ) {
                    minZ = vert.pos.z()
                }
                if (vert.pos.x() > maxX) {
                    maxX = vert.pos.x()
                }
                if (vert.pos.y() > maxY) {
                    maxY = vert.pos.y()
                }
                if (vert.pos.z() > maxZ) {
                    maxZ = vert.pos.z()
                }
            } // end for vertices
            return Bounds(
                Vector3d.xyz(minX, minY, minZ),
                Vector3d.xyz(maxX, maxY, maxZ)
            )
        }

    fun centroid(): Vector3d {
        var sum = Vector3d.zero()
        for (v in vertices) {
            sum = sum.plus(v.pos)
        }
        return sum.times(1.0 / vertices.size)
    }

    /**
     * Indicates whether the specified point is contained within this polygon.
     *
     * @param p point
     * @return `true` if the point is inside the polygon or on one of the
     * edges; `false` otherwise
     */
    operator fun contains(p: Vector3d?): Boolean {

        // P not on the plane
        if (_plane.distance(p) > Plane.EPSILON) {
            return false
        }

        // if P is on one of the vertices, return true
        for (i in 0 until vertices.size - 1) {
            if (p!!.minus(vertices[i].pos).magnitude() < Plane.EPSILON) {
                return true
            }
        }

        // if P is on the plane, we proceed with projection to XY plane
        //
        // P1--P------P2
        //     ^
        //     |
        // P is on the segment if( dist(P1,P) + dist(P2,P) - dist(P1,P2) < TOL)
        for (i in 0 until vertices.size - 1) {
            val p1 = vertices[i].pos
            val p2 = vertices[i + 1].pos
            val onASegment: Boolean =
                (p1.minus(p).magnitude() + p2.minus(p).magnitude() - p1.minus(p2).magnitude()) <
                    Plane.EPSILON
            if (onASegment) {
                return true
            }
        }

        // find projection plane
        // we start with XY plane
        var coordIndex1 = 0
        var coordIndex2 = 1
        val orthogonalToXY: Boolean = abs(
            eu.mihosoft.vvecmath.Plane.XY_PLANE.normal
                .dot(_plane.normal)
        ) < Plane.EPSILON
        var foundProjectionPlane = false
        if (!orthogonalToXY && !foundProjectionPlane) {
            coordIndex1 = 0
            coordIndex2 = 1
            foundProjectionPlane = true
        }
        val orthogonalToXZ: Boolean = abs(
            eu.mihosoft.vvecmath.Plane.XZ_PLANE.normal
                .dot(_plane.normal)
        ) < Plane.EPSILON
        if (!orthogonalToXZ && !foundProjectionPlane) {
            coordIndex1 = 0
            coordIndex2 = 2
            foundProjectionPlane = true
        }
        val orthogonalToYZ: Boolean = abs(
            eu.mihosoft.vvecmath.Plane.YZ_PLANE.normal
                .dot(_plane.normal)
        ) < Plane.EPSILON
        if (!orthogonalToYZ && !foundProjectionPlane) {
            coordIndex1 = 1
            coordIndex2 = 2
            foundProjectionPlane = true
        }

        // see from http://www.java-gaming.org/index.php?topic=26013.0
        // see http://alienryderflex.com/polygon/
        // see http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        var j = vertices.size - 1
        var oddNodes = false
        val x = p!![coordIndex1]
        val y = p[coordIndex2]
        var i: Int = 0
        while (i < vertices.size) {
            val xi = vertices[i].pos[coordIndex1]
            val yi = vertices[i].pos[coordIndex2]
            val xj = vertices[j].pos[coordIndex1]
            val yj = vertices[j].pos[coordIndex2]
            if ((
                yi < y && yj >= y ||
                    yj < y && yi >= y
                ) &&
                (xi <= x || xj <= x)
            ) {
                oddNodes = oddNodes xor (xi + (y - yi) / (yj - yi) * (xj - xi) < x)
            }
            j = i
            i++
        }
        return oddNodes
    }

    @Deprecated("")
    fun intersects(p: Polygon): Boolean {
        if (!bounds.intersects(p.bounds)) {
            return false
        }
        throw UnsupportedOperationException("Not implemented")
    }

    operator fun contains(p: Polygon?): Boolean {
        for (v in p!!.vertices) {
            if (!contains(v.pos)) {
                return false
            }
        }
        return true
    }
    //    private static List<Polygon> concaveToConvex(Polygon concave) {
    //        List<Polygon> result = new ArrayList<>();
    //
    //        Triangulation t = new Triangulation();
    //
    //        double[] xv = new double[concave.vertices.size()];
    //        double[] yv = new double[concave.vertices.size()];
    //
    //        for(int i = 0; i < xv.length;i++) {
    //            Vector3d pos = concave.vertices.get(i).pos;
    //            xv[i] = pos.x;
    //            yv[i] = pos.y;
    //        }
    //
    //        TriangleTri[] triangles = t.triangulatePolygon(xv, yv, xv.length);
    //
    //        for(TriangleTri tr : triangles) {
    //            double x1 = tr.x[0];
    //            double x2 = tr.x[1];
    //            double x3 = tr.x[2];
    //            double y1 = tr.y[0];
    //            double y2 = tr.y[1];
    //            double y3 = tr.y[2];
    //
    //            Vertex v1 = new Vertex(new Vector3d(x1, y1), new Vector3d(0, 0));
    //            Vertex v2 = new Vertex(new Vector3d(x2, y2), new Vector3d(0, 0));
    //            Vertex v3 = new Vertex(new Vector3d(x3, y3), new Vector3d(0, 0));
    //
    //            result.add(new Polygon(v1,v2,v3));
    //        }
    //
    //        return result;
    //    }
    //    private static List<Polygon> concaveToConvex(Polygon concave) {
    //        List<Polygon> result = new ArrayList<>();
    //
    //        //convert polygon to convex polygons
    //        EarClippingTriangulator clippingTriangulator = new EarClippingTriangulator();
    //        double[] vertexArray = new double[concave.vertices.size() * 2];
    //        for (int i = 0; i < vertexArray.length; i += 2) {
    //            Vertex v = concave.vertices.get(i / 2);
    //            vertexArray[i + 0] = v.pos.x;
    //            vertexArray[i + 1] = v.pos.y;
    //        }
    //
    //        IntArray indices = clippingTriangulator.computeTriangles(vertexArray);
    //
    //        System.out.println("indices: " + indices.size + ", vertices: " + vertexArray.length);
    //
    //        for (double i : vertexArray) {
    //            System.out.println("vertices: " + i);
    //        }
    //
    //        Vertex[] newPolygonVerts = new Vertex[3];
    //
    //        int count = 0;
    //        for (int i = 0; i < indices.size; i+=2) {
    //            double x = vertexArray[indices.items[i]+0];
    //            double y = vertexArray[indices.items[i]+1];
    //
    //            Vector3d pos = new Vector3d(x, y);
    //            Vertex v = new Vertex(pos, new Vector3d(0, 0, 0));
    //
    //            System.out.println("writing vertex: " + (count));
    //            newPolygonVerts[count] = v;
    //
    //            if (count == 2) {
    //                result.add(new Polygon(newPolygonVerts));
    //                count = 0;
    //            } else {
    //                count++;
    //            }
    //        }
    //
    //        System.out.println("---");
    //
    //        for (Polygon p : result) {
    //            System.out.println(p.toStlString());
    //        }
    //
    //        return result;
    //
    // //        Point3d[] points = new Point3d[concave.vertices.size()];
    // //
    // //        for (int i = 0; i < points.length;i++) {
    // //            Vector3d pos = concave.vertices.get(i).pos;
    // //            points[i] = new Point3d(pos.x, pos.y, pos.z);
    // //        }
    // //
    // //        QuickHull3D hull = new QuickHull3D();
    // //        hull.build(points);
    // //
    // //        System.out.println("Vertices:");
    // //        Point3d[] vertices = hull.getVertices();
    // //        for (int i = 0; i < vertices.length; i++) {
    // //            Point3d pnt = vertices[i];
    // //            System.out.println(pnt.x + " " + pnt.y + " " + pnt.z);
    // //        }
    // //
    // //        System.out.println("Faces:");
    // //        int[][] faceIndices = hull.getFaces();
    // //        for (int i = 0; i < faceIndices.length; i++) {
    // //            for (int k = 0; k < faceIndices[i].length; k++) {
    // //                System.out.print(faceIndices[i][k] + " ");
    // //            }
    // //            System.out.println("");
    // //        }
    //
    // //        return result;
    //    }
    /**
     * @return the shared
     */
    var storage: PropertyStorage
        get() {
            if (shared == null) {
                shared = PropertyStorage()
            }
            return shared!!
        }
        set(storage) {
            shared = storage
        }

    companion object {
        /**
         * Decomposes the specified concave polygon into convex polygons.
         *
         * @param points the points that define the polygon
         * @return the decomposed concave polygon (list of convex polygons)
         */
        fun fromConcavePoints(vararg points: Vector3d): List<Polygon> {
            val p = fromPoints(*points)
            return PolygonUtil.concaveToConvex(p)
        }

        /**
         * Decomposes the specified concave polygon into convex polygons.
         *
         * @param points the points that define the polygon
         * @return the decomposed concave polygon (list of convex polygons)
         */
        fun fromConcavePoints(points: List<Vector3d>): List<Polygon> {
            val p = fromPoints(points)
            return PolygonUtil.concaveToConvex(p)
        }

        /**
         * Creates a polygon from the specified point list.
         *
         * @param points the points that define the polygon
         * @param shared shared property storage
         * @return a polygon defined by the specified point list
         */
        fun fromPoints(
            points: List<Vector3d>,
            shared: PropertyStorage?
        ): Polygon {
            return fromPoints(points, shared, null)
        }

        /**
         * Creates a polygon from the specified point list.
         *
         * @param points the points that define the polygon
         * @return a polygon defined by the specified point list
         */
        fun fromPoints(points: List<Vector3d>): Polygon {
            return fromPoints(points, PropertyStorage(), null)
        }

        /**
         * Creates a polygon from the specified points.
         *
         * @param points the points that define the polygon
         * @return a polygon defined by the specified point list
         */
        fun fromPoints(vararg points: Vector3d): Polygon {
            return fromPoints(listOf(*points), PropertyStorage(), null)
        }

        /**
         * Creates a polygon from the specified point list.
         *
         * @param points the points that define the polygon
         * @param shared
         * @param plane may be null
         * @return a polygon defined by the specified point list
         */
        private fun fromPoints(
            points: List<Vector3d>,
            shared: PropertyStorage?,
            plane: Plane?
        ): Polygon {
            var normal = plane?.normal?.clone()
            if (normal == null) {
                normal = Plane.createFromPoints(
                    points[0],
                    points[1],
                    points[2]
                ).normal
            }
            val vertices: MutableList<Vertex> = ArrayList()
            for (p in points) {
                val vec = p.clone()
                val vertex = Vertex(vec, normal)
                vertices.add(vertex)
            }
            return Polygon(vertices, shared)
        }
    }
}

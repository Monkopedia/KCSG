/**
 * CSG.java
 *
 * Copyright 2014-2014 Michael Hoffer <info></info>@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info></info>@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info></info>@michaelhoffer.de> OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the authors and should not be
 * interpreted as representing official policies, either expressed or implied, of Michael Hoffer
 * <info></info>@michaelhoffer.de>.
 */
package com.monkopedia.kcsg

import com.monkopedia.kcsg.CSG.OptType.CSG_BOUND
import com.monkopedia.kcsg.CSG.OptType.NONE
import com.monkopedia.kcsg.CSG.OptType.POLYGON_BOUND
import com.monkopedia.kcsg.ext.quickhull3d.HullUtil
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.abs

/**
 * Constructive Solid Geometry (CSG).
 *
 * This implementation is a Java port of
 * [https://github.com/evanw/csg.js/](https://github.com/evanw/csg.js/)
 * with some additional features like polygon extrude, transformations etc. Thanks to the author for creating the CSG.js
 * library.<br></br><br></br>
 *
 * **Implementation Details**
 *
 * All CSG operations are implemented in terms of two functions, [Node.clipTo] and [Node.invert],
 * which remove parts of a BSP tree inside another BSP tree and swap solid and empty space, respectively. To find the
 * union of `a` and `b`, we want to remove everything in `a` inside `b` and everything in
 * `b` inside `a`, then combine polygons from `a` and `b` into one solid:
 *
 * <blockquote><pre>
 * a.clipTo(b);
 * b.clipTo(a);
 * a.build(b.allPolygons());
</pre></blockquote> *
 *
 * The only tricky part is handling overlapping coplanar polygons in both trees. The code above keeps both copies, but
 * we need to keep them in one tree and remove them in the other tree. To remove them from `b` we can clip the
 * inverse of `b` against `a`. The code for union now looks like this:
 *
 * <blockquote><pre>
 * a.clipTo(b);
 * b.clipTo(a);
 * b.invert();
 * b.clipTo(a);
 * b.invert();
 * a.build(b.allPolygons());
</pre></blockquote> *
 *
 * Subtraction and intersection naturally follow from set operations. If union is `A | B`, differenceion is
 * `A - B = ~(~A | B)` and intersection is `A & B =
 * ~(~A | ~B)` where `~` is the complement operator.
 */
class CSG private constructor(
    private var _polygons: MutableList<Polygon>,
) {
    private var _optType: OptType? = null
    private var _storage: PropertyStorage = PropertyStorage()

    fun copy(): CSG {
        opOverride?.operation("copy", this)?.let { return it }
        val polygonStream: Stream<Polygon> = if (_polygons.size > 200) {
            _polygons.parallelStream()
        } else {
            _polygons.stream()
        }
        val csg = CSG(polygonStream.map { p: Polygon -> p.copy() }.collect(Collectors.toList()))
        csg._optType = getOptType()
        return csg
    }

    /**
     *
     * @return the polygons of this CSG
     */
    val polygons: List<Polygon>
        get() {
            return _polygons
        }

    /**
     * Defines the CSg optimization type.
     *
     * @param type optimization type
     * @return this CSG
     */
    fun optimization(type: OptType): CSG {
        this._optType = type
        return this
    }

    /**
     * Return a new CSG solid representing the union of this csg and the specified csg.
     *
     * **Note:** Neither this csg nor the specified csg are weighted.
     *
     * <blockquote><pre>
     * A.union(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |       +----+
     * +----+--+    |       +----+       |
     * |   B   |            |       |
     * |       |            |       |
     * +-------+            +-------+
     </pre></blockquote> *
     *
     *
     * @param csg other csg
     *
     * @return union of this csg and the specified csg
     */
    fun union(csg: CSG): CSG {
        opOverride?.operation("union", this, csg)?.let { return it }
        return when (getOptType()) {
            CSG_BOUND -> unionCSGBoundsOpt(csg)
            POLYGON_BOUND -> unionPolygonBoundsOpt(csg)
            else -> //                return _unionIntersectOpt(csg);
                unionNoOpt(csg)
        }
    }

    /**
     * Returns a csg consisting of the polygons of this csg and the specified csg.
     *
     * The purpose of this method is to allow fast union operations for objects that do not intersect.
     *
     *
     *
     * **WARNING:** this method does not apply the csg algorithms. Therefore, please ensure that this csg and the
     * specified csg do not intersect.
     *
     * @param csg csg
     *
     * @return a csg consisting of the polygons of this csg and the specified csg
     */
    fun dumbUnion(csg: CSG): CSG {
        opOverride?.operation("dumbUnion", this, csg)?.let { return it }
        val result = copy()
        val other = csg.copy()
        result._polygons.addAll(other._polygons)
        return result
    }

    /**
     * Return a new CSG solid representing the union of this csg and the specified csgs.
     *
     * **Note:** Neither this csg nor the specified csg are weighted.
     *
     * <blockquote><pre>
     * A.union(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |       +----+
     * +----+--+    |       +----+       |
     * |   B   |            |       |
     * |       |            |       |
     * +-------+            +-------+
     </pre></blockquote> *
     *
     *
     * @param csgs other csgs
     *
     * @return union of this csg and the specified csgs
     */
    fun union(csgs: List<CSG>): CSG {
        opOverride?.operation("union", this, *csgs.toTypedArray())?.let { return it }
        var result = this
        for (csg in csgs) {
            result = result.union(csg)
        }
        return result
    }

    /**
     * Return a new CSG solid representing the union of this csg and the specified csgs.
     *
     * **Note:** Neither this csg nor the specified csg are weighted.
     *
     * <blockquote><pre>
     * A.union(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |       +----+
     * +----+--+    |       +----+       |
     * |   B   |            |       |
     * |       |            |       |
     * +-------+            +-------+
     </pre></blockquote> *
     *
     *
     * @param csgs other csgs
     *
     * @return union of this csg and the specified csgs
     */
    fun union(vararg csgs: CSG): CSG {
        opOverride?.operation("union", this, *csgs)?.let { return it }
        return union(listOf(*csgs))
    }

    /**
     * Returns the convex hull of this csg.
     *
     * @return the convex hull of this csg
     */
    fun hull(): CSG {
        opOverride?.operation("hull", this)?.let { return it }
        return HullUtil.hull(this, _storage)
    }

    /**
     * Returns the convex hull of this csg and the union of the specified csgs.
     *
     * @param csgs csgs
     * @return the convex hull of this csg and the specified csgs
     */
    fun hull(csgs: List<CSG>): CSG {
        opOverride?.operation("hull", this, *csgs.toTypedArray())?.let { return it }
        val csgsUnion = CSG(copy()._polygons)
        csgsUnion._storage = _storage
        csgsUnion._optType = _optType
        csgs.stream().forEach { csg: CSG ->
            csgsUnion._polygons.addAll(
                csg.copy()._polygons,
            )
        }
        csgsUnion._polygons.forEach { p: Polygon -> p.storage = _storage }
        return csgsUnion.hull()
    }

    /**
     * Returns the convex hull of this csg and the union of the specified csgs.
     *
     * @param csgs csgs
     * @return the convex hull of this csg and the specified csgs
     */
    fun hull(vararg csgs: CSG): CSG {
        opOverride?.operation("hull", this, *csgs)?.let { return it }
        return hull(listOf(*csgs))
    }

    private fun unionCSGBoundsOpt(csg: CSG): CSG {
        logger.warn(
            "WARNING: using " + NONE +
                " since other optimization types missing for union operation.",
        )
        return unionIntersectOpt(csg)
    }

    private fun unionPolygonBoundsOpt(csg: CSG): CSG {
        val inner: MutableList<Polygon> = ArrayList()
        val outer: MutableList<Polygon> = ArrayList()
        val bounds = csg.bounds
        _polygons.stream().forEach { p: Polygon ->
            if (bounds.intersects(p.bounds)) {
                inner.add(p)
            } else {
                outer.add(p)
            }
        }
        val allPolygons: MutableList<Polygon> = ArrayList()
        if (inner.isNotEmpty()) {
            val innerCSG = fromPolygons(inner)
            allPolygons.addAll(outer)
            allPolygons.addAll(innerCSG.unionNoOpt(csg)._polygons)
        } else {
            allPolygons.addAll(_polygons)
            allPolygons.addAll(csg._polygons)
        }
        return fromPolygons(allPolygons).optimization(getOptType())
    }

    /**
     * Optimizes for intersection. If csgs do not intersect create a new csg that consists of the polygon lists of this
     * csg and the specified csg. In this case no further space partitioning is performed.
     *
     * @param csg csg
     * @return the union of this csg and the specified csg
     */
    private fun unionIntersectOpt(csg: CSG): CSG {
        var intersects = false
        val bounds = csg.bounds
        for (p in _polygons) {
            if (bounds.intersects(p.bounds)) {
                intersects = true
                break
            }
        }
        val allPolygons: MutableList<Polygon> = ArrayList()
        if (intersects) {
            return unionNoOpt(csg)
        } else {
            allPolygons.addAll(_polygons)
            allPolygons.addAll(csg._polygons)
        }
        return fromPolygons(allPolygons).optimization(getOptType())
    }

    private fun unionNoOpt(csg: CSG): CSG {
        val a = Node(copy()._polygons)
        val b = Node(csg.copy()._polygons)
        a.clipTo(b)
        b.clipTo(a)
        b.invert()
        b.clipTo(a)
        b.invert()
        a.build(b.allPolygons())
        return fromPolygons(a.allPolygons()).optimization(getOptType())
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the specified csgs.
     *
     * **Note:** Neither this csg nor the specified csgs are weighted.
     *
     * <blockquote><pre>
     * A.difference(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |    +--+
     * +----+--+    |       +----+
     * |   B   |
     * |       |
     * +-------+
     </pre></blockquote> *
     *
     * @param csgs other csgs
     * @return difference of this csg and the specified csgs
     */
    fun difference(csgs: List<CSG>): CSG {
        opOverride?.operation("diff", this, *csgs.toTypedArray())?.let { return it }
        if (csgs.isEmpty()) {
            return copy()
        }
        var csgsUnion = csgs[0]
        for (i in 1 until csgs.size) {
            csgsUnion = csgsUnion.union(csgs[i])
        }
        return difference(csgsUnion)
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the specified csgs.
     *
     * **Note:** Neither this csg nor the specified csgs are weighted.
     *
     * <blockquote><pre>
     * A.difference(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |    +--+
     * +----+--+    |       +----+
     * |   B   |
     * |       |
     * +-------+
     </pre></blockquote> *
     *
     * @param csgs other csgs
     * @return difference of this csg and the specified csgs
     */
    fun difference(vararg csgs: CSG): CSG {
        opOverride?.operation("diff", this, *csgs)?.let { return it }
        return difference(listOf(*csgs))
    }

    /**
     * Return a new CSG solid representing the difference of this csg and the specified csg.
     *
     * **Note:** Neither this csg nor the specified csg are weighted.
     *
     * <blockquote><pre>
     * A.difference(B)
     *
     * +-------+            +-------+
     * |       |            |       |
     * |   A   |            |       |
     * |    +--+----+   =   |    +--+
     * +----+--+    |       +----+
     * |   B   |
     * |       |
     * +-------+
     </pre></blockquote> *
     *
     * @param csg other csg
     * @return difference of this csg and the specified csg
     */
    fun difference(csg: CSG): CSG {
        opOverride?.operation("diff", this, csg)?.let { return it }
        return try {
            simpleDifference(csg)
        } catch (ex: Exception) {
            // System.err.println("CSG difference failed, performing workaround");
            val intersectingParts = csg.intersect(this)
            simpleDifference(intersectingParts)
        }
    }

    private fun simpleDifference(csg: CSG): CSG {
        // Check to see if a CSG operation is attempting to difference with
        // no
        // polygons
        return if (this.polygons.isNotEmpty() && csg.polygons.isNotEmpty()) {
            when (getOptType()) {
                CSG_BOUND -> differenceCSGBoundsOpt(csg)
                POLYGON_BOUND -> differencePolygonBoundsOpt(csg)
                else -> differenceNoOpt(csg)
            }
        } else {
            this
        }
    }

    private fun differenceCSGBoundsOpt(csg: CSG): CSG {
        val a1 = differenceNoOpt(csg.bounds.toCSG())
        val a2 = this.intersect(csg.bounds.toCSG())
        return a2.differenceNoOpt(csg).unionIntersectOpt(a1).optimization(getOptType())
    }

    private fun differencePolygonBoundsOpt(csg: CSG): CSG {
        val inner: MutableList<Polygon> = ArrayList()
        val outer: MutableList<Polygon> = ArrayList()
        val bounds = csg.bounds
        _polygons.stream().forEach { p: Polygon ->
            if (bounds.intersects(
                    p.bounds,
                )
            ) {
                inner.add(p)
            } else {
                outer.add(p)
            }
        }
        val innerCSG = fromPolygons(inner)
        val allPolygons: MutableList<Polygon> = ArrayList()
        allPolygons.addAll(outer)
        allPolygons.addAll(innerCSG.differenceNoOpt(csg)._polygons)
        return fromPolygons(allPolygons).optimization(getOptType())
    }

    private fun differenceNoOpt(csg: CSG): CSG {
        val a = Node(copy()._polygons)
        val b = Node(csg.copy()._polygons)
        a.invert()
        a.clipTo(b)
        b.clipTo(a)
        b.invert()
        b.clipTo(a)
        b.invert()
        a.build(b.allPolygons())
        a.invert()
        return fromPolygons(a.allPolygons()).optimization(getOptType())
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the specified csg.
     *
     * **Note:** Neither this csg nor the specified csg are weighted.
     *
     * <blockquote><pre>
     * A.intersect(B)
     *
     * +-------+
     * |       |
     * |   A   |
     * |    +--+----+   =   +--+
     * +----+--+    |       +--+
     * |   B   |
     * |       |
     * +-------+
     * }
     </pre></blockquote> *
     *
     * @param csg other csg
     * @return intersection of this csg and the specified csg
     */
    fun intersect(csg: CSG): CSG {
        opOverride?.operation("intersect", this, csg)?.let { return it }
        val a = Node(copy()._polygons)
        val b = Node(csg.copy()._polygons)
        a.invert()
        b.clipTo(a)
        b.invert()
        a.clipTo(b)
        b.clipTo(a)
        a.build(b.allPolygons())
        a.invert()
        return fromPolygons(a.allPolygons()).optimization(getOptType())
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the specified csgs.
     *
     * **Note:** Neither this csg nor the specified csgs are weighted.
     *
     * <blockquote><pre>
     * A.intersect(B)
     *
     * +-------+
     * |       |
     * |   A   |
     * |    +--+----+   =   +--+
     * +----+--+    |       +--+
     * |   B   |
     * |       |
     * +-------+
     * }
     </pre></blockquote> *
     *
     * @param csgs other csgs
     * @return intersection of this csg and the specified csgs
     */
    fun intersect(csgs: List<CSG>): CSG {
        opOverride?.operation("intersect", this, *csgs.toTypedArray())?.let { return it }
        if (csgs.isEmpty()) {
            return copy()
        }
        var csgsUnion = csgs[0]
        for (i in 1 until csgs.size) {
            csgsUnion = csgsUnion.union(csgs[i])
        }
        return intersect(csgsUnion)
    }

    /**
     * Return a new CSG solid representing the intersection of this csg and the specified csgs.
     *
     * **Note:** Neither this csg nor the specified csgs are weighted.
     *
     * <blockquote><pre>
     * A.intersect(B)
     *
     * +-------+
     * |       |
     * |   A   |
     * |    +--+----+   =   +--+
     * +----+--+    |       +--+
     * |   B   |
     * |       |
     * +-------+
     * }
     </pre></blockquote> *
     *
     * @param csgs other csgs
     * @return intersection of this csg and the specified csgs
     */
    fun intersect(vararg csgs: CSG): CSG {
        opOverride?.operation("intersect", this, *csgs)?.let { return it }
        return intersect(listOf(*csgs))
    }

    /**
     * Returns this csg in STL string format.
     *
     * @return this csg in STL string format
     */
    fun toStlString(): String {
        val sb = StringBuilder()
        toStlString(sb)
        return sb.toString()
    }

    /**
     * Returns this csg in STL string format.
     *
     * @param sb string builder
     *
     * @return the specified string builder
     */
    fun toStlString(sb: StringBuilder): StringBuilder {
        sb.append("solid v3d.csg\n")
        _polygons.stream().forEach { p: Polygon -> p.toStlString(sb) }
        sb.append("endsolid v3d.csg\n")
        return sb
    }

    fun color(c: Color): CSG {
        val result = copy()
        _storage["material:color"] = (
            "" + c.red +
                " " + c.green +
                " " + c.blue
            )
        return result
    }

    @JvmOverloads
    fun toObj(maxNumberOfVerts: Int = 3): ObjFile {
        if (maxNumberOfVerts != 3) {
            throw UnsupportedOperationException(
                "maxNumberOfVerts > 3 not supported yet",
            )
        }
        val objSb = StringBuilder()
        objSb.append("mtllib " + ObjFile.MTL_NAME)
        objSb.append("# Group").append("\n")
        objSb.append("g v3d.csg\n")
        class PolygonStruct(
            var storage: PropertyStorage,
            var indices: List<Int>,
            var materialName: String,
        )

        val vertices: MutableList<Vertex> = ArrayList()
        val indices: MutableList<PolygonStruct> = ArrayList()
        objSb.append("\n# Vertices\n")
        val materialNames: MutableMap<PropertyStorage, Int> = HashMap()
        var materialIndex = 0
        for (p in _polygons) {
            val polyIndices: MutableList<Int> = ArrayList()
            p.vertices.stream().forEach { v: Vertex ->
                if (!vertices.contains(v)) {
                    vertices.add(v)
                    v.toObjString(objSb)
                    polyIndices.add(vertices.size)
                } else {
                    polyIndices.add(vertices.indexOf(v) + 1)
                }
            }
            if (!materialNames.containsKey(p.storage)) {
                materialIndex++
                materialNames[p.storage] = materialIndex
                p.storage["material:name"] = materialIndex
            }
            indices.add(
                PolygonStruct(
                    p.storage,
                    polyIndices,
                    "material-" + materialNames[p.storage],
                ),
            )
        }
        objSb.append("\n# Faces").append("\n")
        for (ps in indices) {
            // add mtl info
            ps.storage.getValue<Any>("material:color")?.let {
                objSb.append("usemtl ").append(ps.materialName).append("\n")
            }

            // we triangulate the polygon to ensure
            // compatibility with 3d printer software
            val pVerts = ps.indices
            val index1 = pVerts[0]
            for (i in 0 until pVerts.size - 2) {
                val index2 = pVerts[i + 1]
                val index3 = pVerts[i + 2]
                objSb.append("f ").append(index1).append(" ").append(index2).append(" ")
                    .append(index3).append("\n")
            }
            objSb.append("\n")
        }
        objSb.append("\n# End Group v3d.csg").append("\n")
        val mtlSb = StringBuilder()
        materialNames.keys.forEach(
            Consumer { s: PropertyStorage? ->
                if (s!!.contains("material:color")) {
                    mtlSb.append("newmtl material-").append(s.getValue<Any>("material:name"))
                        .append("\n")
                    mtlSb.append("Kd ").append(s.getValue<Any>("material:color")).append("\n")
                }
            },
        )
        return ObjFile(objSb.toString(), mtlSb.toString())
    }

    /**
     * Returns this csg in OBJ string format.
     *
     * @param sb string builder
     * @return the specified string builder
     */
    private fun toObjString(sb: StringBuilder): StringBuilder {
        sb.append("# Group").append("\n")
        sb.append("g v3d.csg\n")
        class PolygonStruct(
            var storage: PropertyStorage,
            var indices: List<Int>,
            var materialName: String,
        )

        val vertices: MutableList<Vertex> = ArrayList()
        val indices: List<PolygonStruct> = ArrayList()
        sb.append("\n# Vertices\n")
        for (p in _polygons) {
            val polyIndices: MutableList<Int> = ArrayList()
            p.vertices.stream().forEach { v: Vertex ->
                if (!vertices.contains(v)) {
                    vertices.add(v)
                    v.toObjString(sb)
                    polyIndices.add(vertices.size)
                } else {
                    polyIndices.add(vertices.indexOf(v) + 1)
                }
            }
        }
        sb.append("\n# Faces").append("\n")
        for (ps in indices) {
            // we triangulate the polygon to ensure
            // compatibility with 3d printer software
            val pVerts = ps.indices
            val index1 = pVerts[0]
            for (i in 0 until pVerts.size - 2) {
                val index2 = pVerts[i + 1]
                val index3 = pVerts[i + 2]
                sb.append("f ").append(index1).append(" ").append(index2).append(" ").append(index3)
                    .append("\n")
            }
        }
        sb.append("\n# End Group v3d.csg").append("\n")
        return sb
    }

    /**
     * Returns this csg in OBJ string format.
     *
     * @return this csg in OBJ string format
     */
    fun toObjString(): String {
        val sb = StringBuilder()
        return toObjString(sb).toString()
    }

    fun weighted(f: WeightFunction): CSG {
        opOverride?.operation("weighted", this)?.let { return it }
        return Modifier(f).modified(this)
    }

    /**
     * Returns a transformed copy of this CSG.
     *
     * @param transform the transform to apply
     *
     * @return a transformed copy of this CSG
     */
    fun transformed(transform: Transform): CSG {
        opOverride?.operation("transformed", this, transform)?.let { return it }
        if (_polygons.isEmpty()) {
            return copy()
        }
        val newpolygons = _polygons.stream().map { p: Polygon -> p.transformed(transform) }
            .collect(Collectors.toList())
        val result = fromPolygons(newpolygons).optimization(getOptType())
        result._storage = _storage
        return result
    }

    /**
     * Returns the bounds of this csg.
     *
     * @return bouds of this csg
     */
    val bounds: Bounds
        get() {
            opOverride?.bounds("bounds", this)?.let { return it }
            if (_polygons.isEmpty()) {
                return Bounds(Vector3d.ZERO, Vector3d.ZERO)
            }
            val initial = _polygons[0].vertices[0].pos
            var minX = initial.x
            var minY = initial.y
            var minZ = initial.z
            var maxX = initial.x
            var maxY = initial.y
            var maxZ = initial.z
            for (p in polygons) {
                for (i in p.vertices.indices) {
                    val vert = p.vertices[i]
                    if (vert.pos.x < minX) {
                        minX = vert.pos.x
                    }
                    if (vert.pos.y < minY) {
                        minY = vert.pos.y
                    }
                    if (vert.pos.z < minZ) {
                        minZ = vert.pos.z
                    }
                    if (vert.pos.x > maxX) {
                        maxX = vert.pos.x
                    }
                    if (vert.pos.y > maxY) {
                        maxY = vert.pos.y
                    }
                    if (vert.pos.z > maxZ) {
                        maxZ = vert.pos.z
                    }
                } // end for vertices
            } // end for polygon
            return Bounds(
                Vector3d.xyz(minX, minY, minZ),
                Vector3d.xyz(maxX, maxY, maxZ),
            )
        }

    /**
     * @return the optType
     */
    private fun getOptType(): OptType {
        return _optType ?: defaultOptType
    }

    enum class OptType {
        CSG_BOUND, POLYGON_BOUND, NONE
    }

    /**
     * Computes and returns the volume of this CSG based on a triangulated version
     * of the internal mesh.
     * @return volume of this csg
     */
    fun computeVolume(): Double {
        opOverride?.double("volume", this)?.let { return it }
        if (polygons.isEmpty()) return 0.0

        // triangulate polygons (parallel for larger meshes)
        val polyStream: Stream<Polygon> = if (polygons.size > 200) {
            polygons.parallelStream()
        } else {
            polygons.stream()
        }
        logger.info("Mapping ${polygons.size}")
        val triangles = polyStream.flatMap { poly: Polygon -> poly.toTriangles().stream() }
//            .collect(Collectors.toList())

        // compute sum over signed volumes of triangles
        // we use parallel streams for larger meshes
        // see http://chenlab.ece.cornell.edu/Publication/Cha/icip01_Cha.pdf
        val triangleStream: Stream<Polygon> = triangles // if (triangles.size > 200) {
//            triangles.parallelStream()
//        } else {
//            triangles.stream()
//        }
        var volume = triangleStream.mapToDouble { tri: Polygon ->
            val p1 = tri.vertices[0].pos
            val p2 = tri.vertices[1].pos
            val p3 = tri.vertices[2].pos
            p1.dot(p2.crossed(p3)) / 6.0
        }.sum()
        volume = abs(volume)
        return volume
    }

    companion object {
        private val logger = LoggerFactory.getLogger("KCSG.CSG")
        private var defaultOptType = NONE

        /**
         * Constructs a CSG from a list of [Polygon] instances.
         *
         * @param polygons polygons
         * @return a CSG instance
         */
        fun fromPolygons(polygons: List<Polygon>): CSG {
            opOverride?.operation("fromPolygons", *polygons.toTypedArray())?.let { return it }
            return CSG(polygons.toMutableList())
        }

        /**
         * Constructs a CSG from the specified [Polygon] instances.
         *
         * @param polygons polygons
         * @return a CSG instance
         */
        fun fromPolygons(vararg polygons: Polygon): CSG {
            opOverride?.operation("fromPolygons", *polygons)?.let { return it }
            return fromPolygons(listOf(*polygons))
        }

        /**
         * Constructs a CSG from a list of [Polygon] instances.
         *
         * @param storage shared storage
         * @param polygons polygons
         * @return a CSG instance
         */
        fun fromPolygons(storage: PropertyStorage, polygons: List<Polygon>): CSG {
            opOverride?.operation("fromPolygons", *polygons.toTypedArray())?.let { return it }
            val csg = CSG(polygons.toMutableList())
            csg._storage = storage
            for (polygon in polygons) {
                polygon.storage = storage
            }
            return csg
        }

        /**
         * Constructs a CSG from the specified [Polygon] instances.
         *
         * @param storage shared storage
         * @param polygons polygons
         * @return a CSG instance
         */
        fun fromPolygons(storage: PropertyStorage, vararg polygons: Polygon): CSG {
            opOverride?.operation("fromPolygons", *polygons)?.let { return it }
            return fromPolygons(storage, listOf(*polygons))
        }

        /**
         * @param optType the optType to set
         */
        fun setDefaultOptType(optType: OptType) {
            defaultOptType = optType
        }

        var opOverride: OpOverride? = null

        inline fun <T> withOverride(override: OpOverride?, exec: () -> T): T {
            val previous = opOverride
            try {
                opOverride = override
                return exec()
            } finally {
                opOverride = previous
            }
        }
    }
}

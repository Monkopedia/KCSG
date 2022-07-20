/**
 * Node.java
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

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Holds a node in a BSP tree. A BSP tree is built from a collection of polygons
 * by picking a polygon to split along. That polygon (and all other coplanar
 * polygons) are added directly to that node and the other polygons are added to
 * the front and/or back subtrees. This is not a leafy BSP tree since there is
 * no distinction between internal and leaf nodes.
 */
internal class Node @JvmOverloads constructor(polygons: List<Polygon>? = null) : Cloneable {
    /**
     * Polygons.
     */
    private var polygons: MutableList<Polygon> = ArrayList()

    private var planeImpl: Plane? = null

    /**
     * Plane used for BSP.
     */
    private val plane: Plane
        get() = planeImpl ?: this.polygons.firstOrNull()?._csg_plane?.clone()?.also {
            planeImpl = it
        } ?: error("Please fix me! I don't know what to do?")

    /**
     * Polygons in front of the plane.
     */
    private var front: Node? = null

    /**
     * Polygons in back of the plane.
     */
    private var back: Node? = null
    public override fun clone(): Node {
        val node = Node()
        node.planeImpl = planeImpl?.clone()
        node.front = front?.clone()
        node.back = back?.clone()
        //        node.polygons = new ArrayList<>();
//        polygons.parallelStream().forEach((Polygon p) -> {
//            node.polygons.add(p.clone());
//        });
        val polygonStream: Stream<Polygon> = if (polygons.size > 200) {
            polygons.parallelStream()
        } else {
            polygons.stream()
        }
        node.polygons = polygonStream.map { p: Polygon -> p.clone() }
            .collect(Collectors.toList())
        return node
    }

    /**
     * Converts solid space to empty space and vice verca.
     */
    fun invert() {
        val polygonStream: Stream<Polygon> = if (polygons.size > 200) {
            polygons.parallelStream()
        } else {
            polygons.stream()
        }
        polygonStream.forEach { polygon: Polygon -> polygon.flip() }
        plane.flip()
        front?.invert()
        back?.invert()
        val temp = front
        front = back
        back = temp
    }

    /**
     * Recursively removes all polygons in the [polygons] list that are
     * contained within this BSP tree.
     *
     * **Note:** polygons are splitted if necessary.
     *
     * @param polygons the polygons to clip
     *
     * @return the cliped list of polygons
     */
    private fun clipPolygons(polygons: List<Polygon>): MutableList<Polygon> {
        var frontP: MutableList<Polygon> = ArrayList()
        var backP: MutableList<Polygon> = ArrayList()
        for (polygon in polygons) {
            plane.splitPolygon(polygon, frontP, backP, frontP, backP)
        }
        frontP = front?.clipPolygons(frontP) ?: frontP
        backP = back?.clipPolygons(backP) ?: ArrayList(0)
        frontP.addAll(backP)
        return frontP
    }
    // Remove all polygons in this BSP tree that are inside the other BSP tree
    // `bsp`.
    /**
     * Removes all polygons in this BSP tree that are inside the specified BSP
     * tree (`bsp`).
     *
     * **Note:** polygons are splitted if necessary.
     *
     * @param bsp bsp that shall be used for clipping
     */
    fun clipTo(bsp: Node) {
        polygons = bsp.clipPolygons(polygons)
        front?.clipTo(bsp)
        back?.clipTo(bsp)
    }

    /**
     * Returns a list of all polygons in this BSP tree.
     *
     * @return a list of all polygons in this BSP tree
     */
    fun allPolygons(): MutableList<Polygon> {
        val localPolygons: MutableList<Polygon> = ArrayList(polygons)
        front?.allPolygons()?.let { localPolygons.addAll(it) }
        //            polygons = Utils.concat(polygons, this.front.allPolygons());
//            polygons = Utils.concat(polygons, this.back.allPolygons());
        back?.allPolygons()?.let { localPolygons.addAll(it) }
        return localPolygons
    }

    /**
     * Build a BSP tree out of `polygons`. When called on an existing
     * tree, the new polygons are filtered down to the bottom of the tree and
     * become new nodes there. Each set of polygons is partitioned using the
     * first polygon (no heuristic is used to pick a good split).
     *
     * @param polygons polygons used to build the BSP
     */
    fun build(polygons: List<Polygon>) {
        var polygons = polygons
        if (polygons.isEmpty()) return
        if (planeImpl == null) {
            planeImpl = polygons.first()._csg_plane.clone()
        }
        polygons = polygons.stream().filter { p: Polygon -> p.isValid }.distinct()
            .collect(Collectors.toList())
        val frontP: MutableList<Polygon> = ArrayList()
        val backP: MutableList<Polygon> = ArrayList()

        // parellel version does not work here
        polygons.forEach { polygon: Polygon ->
            plane.splitPolygon(
                polygon, this.polygons, this.polygons, frontP, backP
            )
        }
        if (frontP.size > 0) {
            val front = front ?: Node().also { front = it }
            front.build(frontP)
        }
        if (backP.size > 0) {
            val back = back ?: Node().also { back = it }
            back.build(backP)
        }
    }
    /**
     * Constructor.
     *
     * Creates a BSP node consisting of the specified polygons.
     *
     * @param polygons polygons
     */
    /**
     * Constructor. Creates a node without polygons.
     */
    init {
        if (polygons != null) {
            this.build(polygons)
        }
    }
}

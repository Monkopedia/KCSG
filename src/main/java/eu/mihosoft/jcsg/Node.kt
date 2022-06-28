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

import java.util.function.Consumer
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
    private var polygons: MutableList<Polygon>

    /**
     * Plane used for BSP.
     */
    private var plane: Plane? = null

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
        node.plane = if (plane == null) null else plane!!.clone()
        node.front = if (front == null) null else front!!.clone()
        node.back = if (back == null) null else back!!.clone()
        //        node.polygons = new ArrayList<>();
//        polygons.parallelStream().forEach((Polygon p) -> {
//            node.polygons.add(p.clone());
//        });
        val polygonStream: Stream<Polygon>
        polygonStream = if (polygons.size > 200) {
            polygons.parallelStream()
        } else {
            polygons.stream()
        }
        node.polygons = polygonStream.map { p: Polygon -> p!!.clone() }
            .collect(Collectors.toList())
        return node
    }

    /**
     * Converts solid space to empty space and vice verca.
     */
    fun invert() {
        val polygonStream: Stream<Polygon>
        polygonStream = if (polygons.size > 200) {
            polygons.parallelStream()
        } else {
            polygons.stream()
        }
        polygonStream.forEach { polygon: Polygon -> polygon!!.flip() }
        if (plane == null && !polygons.isEmpty()) {
            plane = polygons[0]!!._csg_plane.clone()
        } else if (plane == null && polygons.isEmpty()) {
            System.err.println("Please fix me! I don't know what to do?")

            // throw new RuntimeException("Please fix me! I don't know what to do?");
            return
        }
        plane!!.flip()
        if (front != null) {
            front!!.invert()
        }
        if (back != null) {
            back!!.invert()
        }
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
        if (plane == null) {
            return ArrayList(polygons)
        }
        var frontP: MutableList<Polygon> = ArrayList()
        var backP: MutableList<Polygon> = ArrayList()
        for (polygon in polygons) {
            plane!!.splitPolygon(polygon, frontP, backP, frontP, backP)
        }
        if (front != null) {
            frontP = front!!.clipPolygons(frontP)
        }
        backP = if (back != null) {
            back!!.clipPolygons(backP)
        } else {
            ArrayList(0)
        }
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
        if (front != null) {
            front!!.clipTo(bsp)
        }
        if (back != null) {
            back!!.clipTo(bsp)
        }
    }

    /**
     * Returns a list of all polygons in this BSP tree.
     *
     * @return a list of all polygons in this BSP tree
     */
    fun allPolygons(): MutableList<Polygon> {
        val localPolygons: MutableList<Polygon> = ArrayList(polygons)
        if (front != null) {
            localPolygons.addAll(front!!.allPolygons())
            //            polygons = Utils.concat(polygons, this.front.allPolygons());
        }
        if (back != null) {
//            polygons = Utils.concat(polygons, this.back.allPolygons());
            localPolygons.addAll(back!!.allPolygons())
        }
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
    fun build(polygons: List<Polygon>?) {
        var polygons = polygons
        if (polygons!!.isEmpty()) return
        if (plane == null) {
            plane = polygons[0]!!._csg_plane.clone()
        }
        polygons = polygons.stream().filter { p: Polygon? -> p!!.isValid }.distinct()
            .collect(Collectors.toList())
        val frontP: MutableList<Polygon> = ArrayList()
        val backP: MutableList<Polygon> = ArrayList()

        // parellel version does not work here
        polygons.forEach(Consumer { polygon: Polygon ->
            plane!!.splitPolygon(
                polygon, this.polygons, this.polygons, frontP, backP
            )
        })
        if (frontP.size > 0) {
            if (front == null) {
                front = Node()
            }
            front!!.build(frontP)
        }
        if (backP.size > 0) {
            if (back == null) {
                back = Node()
            }
            back!!.build(backP)
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
        this.polygons = ArrayList()
        if (polygons != null) {
            this.build(polygons)
        }
    }
}
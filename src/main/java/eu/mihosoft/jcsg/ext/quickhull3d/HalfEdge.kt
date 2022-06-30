/*
  * Copyright John E. Lloyd, 2003. All rights reserved. Permission
  * to use, copy, and modify, without fee, is granted for non-commercial
  * and research purposes, provided that this copyright notice appears
  * in all copies.
  *
  * This  software is distributed "as is", without any warranty, including
  * any implied warranty of merchantability or fitness for a particular
  * use. The authors assume no responsibility for, and shall not be liable
  * for, any special, indirect, or consequential damages, or any damages
  * whatsoever, arising out of or in connection with the use of this
  * software.
  */
package eu.mihosoft.jcsg.ext.quickhull3d

/**
 * Represents the half-edges that surround each
 * face in a counter-clockwise direction.
 *
 * @author John E. Lloyd, Fall 2004
 */
internal class HalfEdge
/**
 * Constructs a HalfEdge with head vertex `v` and
 * left-hand triangular face `f`.
 *
 * @param v head vertex
 * @param f left-hand triangular face
 */(v: Vertex, f: Face) {
    /**
     * The vertex associated with the head of this half-edge.
     */
    var vertex: Vertex = v

    /**
     * Triangular face associated with this half-edge.
     */
    var face: Face = f

    /**
     * Next half-edge in the triangle.
     */
    var next: HalfEdge? = null

    /**
     * Previous half-edge in the triangle.
     */
    var prev: HalfEdge? = null

    /**
     * Half-edge associated with the opposite triangle
     * adjacent to this edge.
     */
    var opposite: HalfEdge? = null
        set(edge) {
            if (field == edge) return
            field = edge
            edge!!.opposite = this
        }

    /**
     * Returns the head vertex associated with this half-edge.
     *
     * @return head vertex
     */
    fun head(): Vertex {
        return vertex
    }

    /**
     * Returns the tail vertex associated with this half-edge.
     *
     * @return tail vertex
     */
    fun tail(): Vertex? {
        return prev?.vertex
    }

    /**
     * Returns the opposite triangular face associated with this
     * half-edge.
     *
     * @return opposite triangular face
     */
    fun oppositeFace(): Face? {
        return opposite?.face
    }

    /**
     * Produces a string identifying this half-edge by the point
     * index values of its tail and head vertices.
     *
     * @return identifying string
     */
    val vertexString: String
        get() {
            return if (tail() != null) {
                "" +
                    tail()!!.index + "-" +
                    head().index
            } else {
                "?-" + head().index
            }
        }

    /**
     * Returns the length of this half-edge.
     *
     * @return half-edge length
     */
    fun length(): Double {
        return if (tail() != null) {
            head().pnt.distance(tail()!!.pnt)
        } else {
            (-1).toDouble()
        }
    }

    /**
     * Returns the length squared of this half-edge.
     *
     * @return half-edge length squared
     */
    fun lengthSquared(): Double {
        return if (tail() != null) {
            head().pnt.distanceSquared(tail()!!.pnt)
        } else {
            (-1).toDouble()
        }
    } // 	/**
    // 	 * Computes nrml . (del0 X del1), where del0 and del1
    // 	 * are the direction vectors along this halfEdge, and the
    // 	 * halfEdge he1.
    // 	 *
    // 	 * A product > 0 indicates a left turn WRT the normal
    // 	 */
    // 	public double turnProduct (HalfEdge he1, Vector3d nrml)
    // 	 {
    // 	   Point3d pnt0 = tail().pnt;
    // 	   Point3d pnt1 = head().pnt;
    // 	   Point3d pnt2 = he1.head().pnt;
    // 	   double del0x = pnt1.x - pnt0.x;
    // 	   double del0y = pnt1.y - pnt0.y;
    // 	   double del0z = pnt1.z - pnt0.z;
    // 	   double del1x = pnt2.x - pnt1.x;
    // 	   double del1y = pnt2.y - pnt1.y;
    // 	   double del1z = pnt2.z - pnt1.z;
    // 	   return (nrml.x*(del0y*del1z - del0z*del1y) +
    // 		   nrml.y*(del0z*del1x - del0x*del1z) +
    // 		   nrml.z*(del0x*del1y - del0y*del1x));
    // 	 }
}

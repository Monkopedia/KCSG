package com.monkopedia.kcsg.ext.quickhull3d

/**
 * Maintains a single-linked list of faces for use by QuickHull3D
 */
internal class FaceList {
    private var head: Face? = null
    private var tail: Face? = null

    /**
     * Clears this list.
     */
    fun clear() {
        tail = null
        head = tail
    }

    /**
     * Adds a vertex to the end of this list.
     */
    fun add(vtx: Face) {
        if (head == null) {
            head = vtx
        } else {
            tail!!.next = vtx
        }
        vtx.next = null
        tail = vtx
    }

    fun first(): Face? {
        return head
    }

    /**
     * Returns true if this list is empty.
     */
    fun isEmpty(): Boolean {
        return head == null
    }
}
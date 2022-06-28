package eu.mihosoft.jcsg.ext.quickhull3d

/**
 * Maintains a double-linked list of vertices for use by QuickHull3D
 */
internal class VertexList {
    private var head: Vertex? = null
    private var tail: Vertex? = null

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
    fun add(vtx: Vertex) {
        if (head == null) {
            head = vtx
        } else {
            tail!!.next = vtx
        }
        vtx.prev = tail
        vtx.next = null
        tail = vtx
    }

    /**
     * Adds a chain of vertices to the end of this list.
     */
    fun addAll(vtx: Vertex?) {
        var vtx = vtx
        if (head == null) {
            head = vtx
        } else {
            tail!!.next = vtx
        }
        vtx!!.prev = tail
        while (vtx!!.next != null) {
            vtx = vtx.next
        }
        tail = vtx
    }

    /**
     * Deletes a vertex from this list.
     */
    fun delete(vtx: Vertex) {
        if (vtx.prev == null) {
            head = vtx.next
        } else {
            vtx.prev!!.next = vtx.next
        }
        if (vtx.next == null) {
            tail = vtx.prev
        } else {
            vtx.next!!.prev = vtx.prev
        }
    }

    /**
     * Deletes a chain of vertices from this list.
     */
    fun delete(vtx1: Vertex, vtx2: Vertex) {
        if (vtx1.prev == null) {
            head = vtx2.next
        } else {
            vtx1.prev!!.next = vtx2.next
        }
        if (vtx2.next == null) {
            tail = vtx1.prev
        } else {
            vtx2.next!!.prev = vtx1.prev
        }
    }

    /**
     * Inserts a vertex into this list before another
     * specificed vertex.
     */
    fun insertBefore(vtx: Vertex, next: Vertex) {
        vtx.prev = next.prev
        if (next.prev == null) {
            head = vtx
        } else {
            next.prev!!.next = vtx
        }
        vtx.next = next
        next.prev = vtx
    }

    /**
     * Returns the first element in this list.
     */
    fun first(): Vertex? {
        return head
    }

    /**
     * Returns true if this list is empty.
     */
    val isEmpty: Boolean
        get() {
            return head == null
        }
}
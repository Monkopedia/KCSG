/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
@file:Suppress("LeakingThis")

package eu.mihosoft.jcsg.ext.openjfx.importers.obj

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.*

/**
 * Resizable-array implementation of the <tt>List&lt;Float&gt;</tt> interface.  Implements all optional list operations,
 * and doesn't permit <tt>null</tt>s.  In addition to implementing the <tt>List</tt> interface, this class provides
 * methods to manipulate the size of the array that is used internally to store the list.  (This class is roughly
 * equivalent to <tt>Vector</tt>, except that it is unsynchronized.)
 *
 *
 * The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, <tt>set</tt>, <tt>iterator</tt>, and <tt>listIterator</tt>
 * operations run in constant time.  The <tt>add</tt> operation runs in *amortized constant time*, that is, adding
 * n elements requires O(n) time.  All of the other operations run in linear time (roughly speaking).  The constant
 * factor is low compared to that for the <tt>LinkedList</tt> implementation.
 *
 *
 * Each <tt>ArrayList</tt> instance has a *capacity*.  The capacity is the size of the array used to store the
 * elements in the list.  It is always at least as large as the list size.  As elements are added to an ArrayList, its
 * capacity grows automatically.  The details of the growth policy are not specified beyond the fact that adding an
 * element has constant amortized time cost.
 *
 *
 * An application can increase the capacity of an <tt>ArrayList</tt> instance before adding a large number of
 * elements using the <tt>ensureCapacity</tt> operation.  This may reduce the amount of incremental reallocation.
 *
 *
 * **Note that this implementation is not synchronized.** If multiple threads access an
 * <tt>ArrayList</tt> instance concurrently, and at least one of the threads modifies the list structurally, it
 * *must* be synchronized externally.  (A structural modification is any operation that adds or deletes one or more
 * elements, or explicitly resizes the backing array; merely setting the value of an element is not a structural
 * modification.)  This is typically accomplished by synchronizing on some object that naturally encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the [ Collections.synchronizedList][Collections.synchronizedList] method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 * List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *
 *
 * <a name="fail-fast"></a> The iterators returned by this class's [iterator][.iterator] and [ ][.listIterator] methods are *fail-fast*: if the list is structurally modified at any time
 * after the iterator is created, in any way except through the iterator's own [remove][ListIterator.remove] or
 * [add][ListIterator.add] methods, the iterator will throw a [ConcurrentModificationException].
 * Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary,
 * non-deterministic behavior at an undetermined time in the future.
 *
 *
 * Note that the fail-fast behavior of an iterator cannot be guaranteed as it is, generally speaking, impossible to
 * make any hard guarantees in the presence of unsynchronized concurrent modification.  Fail-fast iterators throw `ConcurrentModificationException` on a best-effort basis. Therefore, it would be wrong to write a program that
 * depended on this exception for its correctness:  *the fail-fast behavior of iterators should be used only to detect
 * bugs.*
 *
 *
 * This class is a member of the [ Java Collections
 * Framework]({@docRoot}/../technotes/guides/collections/index.html).
 *
 * @see Collection
 *
 * @see List
 *
 * @see LinkedList
 *
 * @see Vector
 *
 * TODO replace with ObservableFloatArray
 */
internal open class FloatArrayList :
    AbstractList<Float>,
    MutableList<Float>,
    RandomAccess,
    Cloneable,
    Serializable {
    /**
     * The array buffer into which the elements of the ArrayList are stored. The capacity of the ArrayList is the length
     * of this array buffer.
     */
    @Transient
    private var elementData: FloatArray

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    override var size = 0
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    /** Constructs an empty list with an initial capacity of ten.  */
    @JvmOverloads
    constructor(initialCapacity: Int = 10) : super() {
        require(initialCapacity >= 0) {
            "Illegal Capacity: " +
                initialCapacity
        }
        elementData = FloatArray(initialCapacity)
    }

    /**
     * Constructs a list containing the elements of the specified collection, in the order they are returned by the
     * collection's iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    constructor(c: Collection<Float>) {
        elementData = FloatArray(c.size)
        for ((i, d) in c.withIndex()) {
            elementData[i] = d
        }
        size = elementData.size
    }

    /**
     * Trims the capacity of this <tt>ArrayList</tt> instance to be the list current size.  An application can use this
     * operation to minimize the storage of an <tt>ArrayList</tt> instance.
     */
    fun trimToSize() {
        modCount++
        val oldCapacity = elementData.size
        if (size < oldCapacity) {
            elementData = elementData.copyOf(size)
        }
    }

    /**
     * Increases the capacity of this <tt>ArrayList</tt> instance, if necessary, to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > 0) {
            ensureCapacityInternal(minCapacity)
        }
    }

    private fun ensureCapacityInternal(minCapacity: Int) {
        modCount++
        // overflow-conscious code
        if (minCapacity - elementData.size > 0) {
            grow(minCapacity)
        }
    }

    /**
     * Increases the capacity to ensure that it can hold at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private fun grow(minCapacity: Int) {
        // overflow-conscious code
        val oldCapacity = elementData.size
        var newCapacity = oldCapacity + (oldCapacity shr 1)
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity
        if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity)
        // minCapacity is usually close to size, so this is a win:
        elementData = elementData.copyOf(newCapacity)
    }

    /**
     * Returns <tt>true</tt> if this list contains no elements.
     *
     * @return <tt>true</tt> if this list contains no elements
     */
    override fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element. More formally, returns <tt>true</tt> if and
     * only if this list contains at least one element <tt>e</tt> such that <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested
     * @return <tt>true</tt> if this list contains the specified element
     */
    override operator fun contains(o: Float): Boolean {
        return indexOf(o) >= 0
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list, or -1 if this list does not
     * contain the element. More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     */
    override fun indexOf(o: Float): Int {
        if (o is Float) {
            for (i in 0 until size) {
                if (o == elementData[i]) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Returns the index of the last occurrence of the specified element in this list, or -1 if this list does not
     * contain the element. More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>, or -1 if there is no such index.
     */
    override fun lastIndexOf(o: Float): Int {
        if (o is Float) {
            for (i in size - 1 downTo 0) if (o == elementData[i]) return i
        }
        return -1
    }

    /**
     * Returns a shallow copy of this <tt>ArrayList</tt> instance.  (The elements themselves are not copied.)
     *
     * @return a clone of this <tt>ArrayList</tt> instance
     */
    public override fun clone(): Any {
        return try {
            val v = super.clone() as FloatArrayList
            v.elementData = elementData.copyOf(size)
            v.modCount = 0
            v
        } catch (e: CloneNotSupportedException) {
            // this shouldn't happen, since we are Cloneable
            throw InternalError()
        }
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence (from first to last element).
     *
     *
     * The returned array will be "safe" in that no references to it are maintained by this list.  (In other words,
     * this method must allocate a new array).  The caller is thus free to modify the returned array.
     *
     *
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all of the elements in this list in proper sequence
     */
    override fun toArray(): Array<Float?> {
        val array = arrayOfNulls<Float>(size)
        for (i in 0 until size) {
            array[i] = elementData[i]
        }
        return array
    }

    /**
     * Returns an array containing all of the elements in this list in proper sequence (from first to last element); the
     * runtime type of the returned array is that of the specified array.  If the list fits in the specified array, it
     * is returned therein.  Otherwise, a new array is allocated with the runtime type of the specified array and the
     * size of this list.
     *
     *
     * If the list fits in the specified array with room to spare (i.e., the array has more elements than the list),
     * the element in the array immediately following the end of the collection is set to <tt>null</tt>.  (This is
     * useful in determining the length of the list *only* if the caller knows that the list does not contain any
     * null elements.)
     *
     * @param a the array into which the elements of the list are to be stored, if it is big enough; otherwise, a new
     * array of the same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException  if the runtime type of the specified array is not a supertype of the runtime type of
     * every element in this list
     * @throws NullPointerException if the specified array is null
     */
    override fun <T> toArray(a: Array<T?>): Array<T?> {
        if (a.size < size) {
            // Make a new array of a's runtime type, but my contents:
            return Arrays.copyOf<Any, Any>(toTypedArray(), size, a.javaClass) as Array<T?>
        }
        System.arraycopy(elementData, 0, a, 0, size)
        if (a.size > size) a[size] = null
        return a
    }

    open fun toFloatArray(): FloatArray {
        val res = FloatArray(size)
        System.arraycopy(elementData, 0, res, 0, size)
        return res
    }

    // Positional Access Operations
    fun elementData(index: Int): Float {
        return elementData[index]
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    override fun get(index: Int): Float {
        rangeCheck(index)
        return elementData(index)
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index   index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    override fun set(index: Int, element: Float): Float {
        rangeCheck(index)
        val oldValue = elementData(index)
        elementData[index] = element
        return oldValue
    }

    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by [Collection.add])
     */
    override fun add(e: Float): Boolean {
        ensureCapacityInternal(size + 1) // Increments modCount!!
        elementData[size++] = e
        return true
    }

    /**
     * Inserts the specified element at the specified position in this list. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (adds one to their indices).
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    override fun add(index: Int, element: Float) {
        rangeCheckForAdd(index)
        ensureCapacityInternal(size + 1) // Increments modCount!!
        System.arraycopy(
            elementData, index, elementData, index + 1,
            size - index
        )
        elementData[index] = element
        size++
    }

    /**
     * Removes the element at the specified position in this list. Shifts any subsequent elements to the left (subtracts
     * one from their indices).
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    override fun removeAt(index: Int): Float {
        rangeCheck(index)
        modCount++
        val oldValue = elementData(index)
        val numMoved = size - index - 1
        if (numMoved > 0) System.arraycopy(
            elementData, index + 1, elementData, index,
            numMoved
        )
        elementData[--size] = 0f // Forget the item completely
        return oldValue
    }

    /**
     * Removes the first occurrence of the specified element from this list, if it is present.  If the list does not
     * contain the element, it is unchanged.  More formally, removes the element with the lowest index <tt>i</tt> such
     * that <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt> (if such an element exists).
     * Returns <tt>true</tt> if this list contained the specified element (or equivalently, if this list changed as a
     * result of the call).
     *
     * @param o element to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     */
    override fun remove(o: Float): Boolean {
        if (o is Float) {
            for (index in 0 until size) if (o == elementData[index]) {
                fastRemove(index)
                return true
            }
        }
        return false
    }

    /*
     * Private remove method that skips bounds checking and does not
     * return the value removed.
     */
    private fun fastRemove(index: Int) {
        modCount++
        val numMoved = size - index - 1
        if (numMoved > 0) System.arraycopy(
            elementData, index + 1, elementData, index,
            numMoved
        )
        elementData[--size] = 0f // Forget the item completelyf
    }

    /** Removes all of the elements from this list.  The list will be empty after this call returns.  */
    override fun clear() {
        modCount++

        // Forget the items completely
        for (i in 0 until size) elementData[i] = 0f
        size = 0
    }

    /**
     * Appends all of the elements in the specified collection to the end of this list, in the order that they are
     * returned by the specified collection's Iterator.  The behavior of this operation is undefined if the specified
     * collection is modified while the operation is in progress.  (This implies that the behavior of this call is
     * undefined if the specified collection is this list, and this list is nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    override fun addAll(c: Collection<Float>): Boolean {
        val a: Array<Any> = c.toTypedArray()
        val numNew = a.size
        ensureCapacityInternal(size + numNew) // Increments modCount
        System.arraycopy(a, 0, elementData, size, numNew)
        size += numNew
        return numNew != 0
    }

    /**
     * Inserts all of the elements in the specified collection into this list, starting at the specified position.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right (increases their
     * indices).  The new elements will appear in the list in the order that they are returned by the specified
     * collection's iterator.
     *
     * @param index index at which to insert the first element from the specified collection
     * @param c     collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException      if the specified collection is null
     */
    override fun addAll(index: Int, c: Collection<Float>): Boolean {
        rangeCheckForAdd(index)
        val a: Array<Any> = c.toTypedArray()
        val numNew = a.size
        ensureCapacityInternal(size + numNew) // Increments modCount
        val numMoved = size - index
        if (numMoved > 0) System.arraycopy(
            elementData, index, elementData, index + numNew,
            numMoved
        )
        System.arraycopy(a, 0, elementData, index, numNew)
        size += numNew
        return numNew != 0
    }

    /**
     * Removes from this list all of the elements whose index is between `fromIndex`, inclusive, and `toIndex`, exclusive. Shifts any succeeding elements to the left (reduces their index). This call shortens the
     * list by `(toIndex - fromIndex)` elements. (If `toIndex==fromIndex`, this operation has no effect.)
     *
     * @throws IndexOutOfBoundsException if `fromIndex` or `toIndex` is out of range (`fromIndex < 0
     * || fromIndex >= size() || toIndex > size() || toIndex < fromIndex`)
     */
    override fun removeRange(fromIndex: Int, toIndex: Int) {
        modCount++
        val numMoved = size - toIndex
        System.arraycopy(
            elementData, toIndex, elementData, fromIndex,
            numMoved
        )

        // Forget the item completely
        val newSize = size - (toIndex - fromIndex)
        while (size != newSize) elementData[--size] = 0F
    }

    /**
     * Checks if the given index is in range.  If not, throws an appropriate runtime exception.  This method does *not*
     * check if the index is negative: It is always used immediately prior to an array access, which throws an
     * ArrayIndexOutOfBoundsException if index is negative.
     */
    private fun rangeCheck(index: Int) {
        if (index >= size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
    }

    /** A version of rangeCheck used by add and addAll.  */
    private fun rangeCheckForAdd(index: Int) {
        if (index > size || index < 0) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message. Of the many possible refactorings of the error handling
     * code, this "outlining" performs best with both server and client VMs.
     */
    private fun outOfBoundsMsg(index: Int): String {
        return "Index: $index, Size: $size"
    }

    /**
     * Removes from this list all of its elements that are contained in the specified collection.
     *
     * @param c collection containing elements to be removed from this list
     * @return `true` if this list changed as a result of the call
     * @throws ClassCastException   if the class of an element of this list is incompatible with the specified
     * collection ([optional](Collection.html#optional-restrictions))
     * @throws NullPointerException if this list contains a null element and the specified collection does not permit
     * null elements ([optional](Collection.html#optional-restrictions)), or if
     * the specified collection is null
     * @see Collection.contains
     */
    override fun removeAll(c: Collection<Float>): Boolean {
        return batchRemove(c, false)
    }

    /**
     * Retains only the elements in this list that are contained in the specified collection.  In other words, removes
     * from this list all of its elements that are not contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return `true` if this list changed as a result of the call
     * @throws ClassCastException   if the class of an element of this list is incompatible with the specified
     * collection ([optional](Collection.html#optional-restrictions))
     * @throws NullPointerException if this list contains a null element and the specified collection does not permit
     * null elements ([optional](Collection.html#optional-restrictions)), or if
     * the specified collection is null
     * @see Collection.contains
     */
    override fun retainAll(c: Collection<Float>): Boolean {
        return batchRemove(c, true)
    }

    private fun batchRemove(c: Collection<*>, complement: Boolean): Boolean {
        val elementData = elementData
        var r = 0
        var w = 0
        var modified = false
        try {
            while (r < size) {
                if (c.contains(elementData[r]) == complement) elementData[w++] = elementData[r]
                r++
            }
        } finally {
            // Preserve behavioral compatibility with AbstractCollection,
            // even if c.contains() throws.
            if (r != size) {
                System.arraycopy(
                    elementData, r,
                    elementData, w,
                    size - r
                )
                w += size - r
            }
            if (w != size) {
                for (i in w until size) elementData[i] = 0f
                modCount += size - w
                size = w
                modified = true
            }
        }
        return modified
    }

    /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt> instance is emitted (int), followed by all of
     * its elements (each an <tt>Object</tt>) in the proper order.
     */
    @Throws(IOException::class)
    private fun writeObject(s: ObjectOutputStream) {
        // Write out element count, and any hidden stuff
        val expectedModCount = modCount
        s.defaultWriteObject()

        // Write out array length
        s.writeInt(elementData.size)

        // Write out all elements in the proper order.
        for (i in 0 until size) s.writeObject(elementData[i])
        if (modCount != expectedModCount) {
            throw ConcurrentModificationException()
        }
    }

    /** Reconstitute the <tt>ArrayList</tt> instance from a stream (that is, deserialize it).  */
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(s: ObjectInputStream) {
        // Read in size, and any hidden stuff
        s.defaultReadObject()

        // Read in array length and allocate array
        val arrayLength = s.readInt()
        elementData = FloatArray(arrayLength)
        val a = elementData

        // Read in all elements in the proper order.
        for (i in 0 until size) a[i] = s.readObject() as Float
    }

    /**
     * Returns a list iterator over the elements in this list (in proper sequence), starting at the specified position
     * in the list. The specified index indicates the first element that would be returned by an initial call to [ ][ListIterator.next]. An initial call to [previous][ListIterator.previous] would return the element with
     * the specified index minus one.
     *
     *
     * The returned list iterator is [*fail-fast*](#fail-fast).
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    override fun listIterator(index: Int): MutableListIterator<Float> {
        if (index < 0 || index > size) throw IndexOutOfBoundsException("Index: $index")
        return ListItr(index)
    }

    /**
     * Returns a list iterator over the elements in this list (in proper sequence).
     *
     *
     * The returned list iterator is [*fail-fast*](#fail-fast).
     *
     * @see .listIterator
     */
    override fun listIterator(): MutableListIterator<Float> {
        return ListItr(0)
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     *
     * The returned iterator is [*fail-fast*](#fail-fast).
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    override fun iterator(): MutableIterator<Float> {
        return Itr()
    }

    /** An optimized version of AbstractList.Itr  */
    private open inner class Itr : MutableIterator<Float> {
        var cursor = // index of next element to return
            0
        var lastRet = -1 // index of last element returned; -1 if no such
        var expectedModCount = modCount
        override fun hasNext(): Boolean {
            return cursor != size
        }

        override fun next(): Float {
            checkForComodification()
            val i = cursor
            if (i >= size) throw NoSuchElementException()
            val elementData = elementData
            if (i >= elementData.size) throw ConcurrentModificationException()
            cursor = i + 1
            return elementData[i.also { lastRet = it }]
        }

        override fun remove() {
            check(lastRet >= 0)
            checkForComodification()
            try {
                removeAt(lastRet)
                cursor = lastRet
                lastRet = -1
                expectedModCount = modCount
            } catch (ex: IndexOutOfBoundsException) {
                throw ConcurrentModificationException()
            }
        }

        fun checkForComodification() {
            if (modCount != expectedModCount) throw ConcurrentModificationException()
        }
    }

    /** An optimized version of AbstractList.ListItr  */
    private inner class ListItr(index: Int) :
        Itr(),
        MutableListIterator<Float> {
        override fun hasPrevious(): Boolean {
            return cursor != 0
        }

        override fun nextIndex(): Int {
            return cursor
        }

        override fun previousIndex(): Int {
            return cursor - 1
        }

        override fun previous(): Float {
            checkForComodification()
            val i = cursor - 1
            if (i < 0) throw NoSuchElementException()
            val elementData = elementData
            if (i >= elementData.size) throw ConcurrentModificationException()
            cursor = i
            return elementData[i.also { lastRet = it }]
        }

        override fun set(e: Float) {
            check(lastRet >= 0)
            checkForComodification()
            try {
                this@FloatArrayList[lastRet] = e
            } catch (ex: IndexOutOfBoundsException) {
                throw ConcurrentModificationException()
            }
        }

        override fun add(e: Float) {
            checkForComodification()
            try {
                val i = cursor
                this@FloatArrayList.add(i, e)
                cursor = i + 1
                lastRet = -1
                expectedModCount = modCount
            } catch (ex: IndexOutOfBoundsException) {
                throw ConcurrentModificationException()
            }
        }

        init {
            cursor = index
        }
    }

    /**
     * Returns a view of the portion of this list between the specified `fromIndex`, inclusive, and `toIndex`, exclusive.  (If `fromIndex` and `toIndex` are equal, the returned list is empty.)  The
     * returned list is backed by this list, so non-structural changes in the returned list are reflected in this list,
     * and vice-versa. The returned list supports all of the optional list operations.
     *
     *
     * This method eliminates the need for explicit range operations (of the sort that commonly exist for arrays).
     * Any operation that expects a list can be used as a range operation by passing a subList view instead of a whole
     * list.  For example, the following idiom removes a range of elements from a list:
     * <pre>
     * list.subList(from, to).clear();
     </pre> *
     * Similar idioms may be constructed for [.indexOf] and [.lastIndexOf], and all of the
     * algorithms in the [Collections] class can be applied to a subList.
     *
     *
     * The semantics of the list returned by this method become undefined if the backing list (i.e., this list) is
     * *structurally modified* in any way other than via the returned list.  (Structural modifications are those
     * that change the size of this list, or otherwise perturb it in such a fashion that iterations in progress may
     * yield incorrect results.)
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException  {@inheritDoc}
     */
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Float> {
        subListRangeCheck(fromIndex, toIndex, size)
        return SubList(this, 0, fromIndex, toIndex)
    }

    private inner class SubList(
        private val parent: FloatArrayList,
        offset: Int,
        private val parentOffset: Int,
        toIndex: Int
    ) : FloatArrayList(), RandomAccess {
        private val offset: Int
        override var size: Int
            get() {
                checkForComodification()
                return super.size
            }
            set(value) {
                super.size = value
            }

        override fun toFloatArray(): FloatArray {
            val res = FloatArray(size)
            System.arraycopy(elementData, offset, res, 0, size)
            return res
        }

        override fun set(index: Int, e: Float): Float {
            rangeCheck(index)
            checkForComodification()
            val oldValue = this@FloatArrayList.elementData(offset + index)
            this@FloatArrayList.elementData[offset + index] = e
            return oldValue
        }

        override fun get(index: Int): Float {
            rangeCheck(index)
            checkForComodification()
            return this@FloatArrayList.elementData(offset + index)
        }

        override fun add(index: Int, e: Float) {
            rangeCheckForAdd(index)
            checkForComodification()
            parent.add(parentOffset + index, e)
            this.modCount = parent.modCount
            this.size++
        }

        override fun removeAt(index: Int): Float {
            rangeCheck(index)
            checkForComodification()
            val result = parent.removeAt(parentOffset + index)
            this.modCount = parent.modCount
            this.size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            checkForComodification()
            parent.removeRange(
                parentOffset + fromIndex,
                parentOffset + toIndex
            )
            this.modCount = parent.modCount
            this.size -= toIndex - fromIndex
        }

        override fun addAll(c: Collection<Float>): Boolean {
            return addAll(this.size, c)
        }

        override fun addAll(index: Int, c: Collection<Float>): Boolean {
            rangeCheckForAdd(index)
            val cSize = c.size
            if (cSize == 0) return false
            checkForComodification()
            parent.addAll(parentOffset + index, c)
            this.modCount = parent.modCount
            this.size += cSize
            return true
        }

        override fun iterator(): MutableIterator<Float> {
            return listIterator()
        }

        override fun listIterator(index: Int): MutableListIterator<Float> {
            checkForComodification()
            rangeCheckForAdd(index)
            val offset = offset
            return object : MutableListIterator<Float> {
                var cursor = index
                var lastRet = -1
                var expectedModCount = this@FloatArrayList.modCount
                override fun hasNext(): Boolean {
                    return cursor != this@SubList.size
                }

                override fun next(): Float {
                    checkForComodification()
                    val i = cursor
                    if (i >= this@SubList.size) throw NoSuchElementException()
                    val elementData = this@FloatArrayList.elementData
                    if (offset + i >= elementData.size) throw ConcurrentModificationException()
                    cursor = i + 1
                    return elementData[offset + i.also { lastRet = it }]
                }

                override fun hasPrevious(): Boolean {
                    return cursor != 0
                }

                override fun previous(): Float {
                    checkForComodification()
                    val i = cursor - 1
                    if (i < 0) throw NoSuchElementException()
                    val elementData = this@FloatArrayList.elementData
                    if (offset + i >= elementData.size) throw ConcurrentModificationException()
                    cursor = i
                    return elementData[offset + i.also { lastRet = it }]
                }

                override fun nextIndex(): Int {
                    return cursor
                }

                override fun previousIndex(): Int {
                    return cursor - 1
                }

                override fun remove() {
                    check(lastRet >= 0)
                    checkForComodification()
                    try {
                        this@SubList.removeAt(lastRet)
                        cursor = lastRet
                        lastRet = -1
                        expectedModCount = this@FloatArrayList.modCount
                    } catch (ex: IndexOutOfBoundsException) {
                        throw ConcurrentModificationException()
                    }
                }

                override fun set(e: Float) {
                    check(lastRet >= 0)
                    checkForComodification()
                    try {
                        this@FloatArrayList[offset + lastRet] = e
                    } catch (ex: IndexOutOfBoundsException) {
                        throw ConcurrentModificationException()
                    }
                }

                override fun add(e: Float) {
                    checkForComodification()
                    try {
                        val i = cursor
                        this@SubList.add(i, e)
                        cursor = i + 1
                        lastRet = -1
                        expectedModCount = this@FloatArrayList.modCount
                    } catch (ex: IndexOutOfBoundsException) {
                        throw ConcurrentModificationException()
                    }
                }

                fun checkForComodification() {
                    if (expectedModCount != this@FloatArrayList.modCount) throw ConcurrentModificationException()
                }
            }
        }

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Float> {
            subListRangeCheck(fromIndex, toIndex, size)
            return SubList(this, offset, fromIndex, toIndex)
        }

        private fun rangeCheck(index: Int) {
            if (index < 0 || index >= this.size) throw IndexOutOfBoundsException(
                outOfBoundsMsg(
                    index
                )
            )
        }

        private fun rangeCheckForAdd(index: Int) {
            if (index < 0 || index > this.size) throw IndexOutOfBoundsException(outOfBoundsMsg(index))
        }

        private fun outOfBoundsMsg(index: Int): String {
            return "Index: " + index + ", Size: " + this.size
        }

        private fun checkForComodification() {
            if (this@FloatArrayList.modCount != this.modCount) throw ConcurrentModificationException()
        }

        init {
            this.offset = offset + parentOffset
            this.size = toIndex - parentOffset
            this.modCount = this@FloatArrayList.modCount
        }
    }

    companion object {
        /**
         * The maximum size of array to allocate. Some VMs reserve some header words in an array. Attempts to allocate
         * larger arrays may result in OutOfMemoryError: Requested array size exceeds VM limit
         */
        private const val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8
        private fun hugeCapacity(minCapacity: Int): Int {
            if (minCapacity < 0) throw OutOfMemoryError()
            return if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
        }

        fun subListRangeCheck(fromIndex: Int, toIndex: Int, size: Int) {
            if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex = $fromIndex")
            if (toIndex > size) throw IndexOutOfBoundsException("toIndex = $toIndex")
            require(fromIndex <= toIndex) {
                "fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")"
            }
        }
    }
}

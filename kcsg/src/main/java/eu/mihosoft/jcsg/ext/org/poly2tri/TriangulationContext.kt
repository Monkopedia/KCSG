/**
 * TriangulationContext.java
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
 */
internal abstract class TriangulationContext<A : TriangulationDebugContext?> : Object() {
    var debugContext: A? = null
        protected set
    var isDebugEnabled = false
        protected set
    protected var mutableTriangles = mutableListOf<DelaunayTriangle>()
    var mutablePoints = mutableListOf<TriangulationPoint>()
    var triangulatable: Triangulatable? = null
        protected set
    private var _terminated = false
    private var _waitUntilNotified = false
    private val _stepTime = -1
    var stepCount = 0
        private set

    fun done() {
        stepCount++
    }

    open fun prepareTriangulation(t: Triangulatable) {
        triangulatable = t
        t.prepareTriangulation(this)
    }

    abstract fun newConstraint(
        a: TriangulationPoint,
        b: TriangulationPoint
    ): TriangulationConstraint

    fun addToList(triangle: DelaunayTriangle) {
        mutableTriangles.add(triangle)
    }

    val triangles: List<DelaunayTriangle>
        get() = mutableTriangles
    val points: List<TriangulationPoint>
        get() = mutablePoints

    @Synchronized
    fun update(message: String?) {
        if (isDebugEnabled) {
            try {
                synchronized(this) {
                    stepCount++
                    if (_stepTime > 0) {
                        wait(_stepTime.toLong())
                        /** Can we resume execution or are we expected to wait?  */
                        if (_waitUntilNotified) {
                            wait()
                        }
                    } else {
                        wait()
                    }
                    // We have been notified
                    _waitUntilNotified = false
                }
            } catch (e: InterruptedException) {
                update("Triangulation was interrupted")
            }
        }
        if (_terminated) {
            throw RuntimeException("Triangulation process terminated before completion")
        }
    }

    open fun clear() {
        mutablePoints.clear()
        _terminated = false
        if (isDebugEnabled) {
            debugContext?.clear()
        }
        stepCount = 0
    }

    @Synchronized
    fun waitUntilNotified(b: Boolean) {
        _waitUntilNotified = b
    }

    fun terminateTriangulation() {
        _terminated = true
    }

    abstract fun isDebugEnabled(b: Boolean)
    fun addPoints(points: List<TriangulationPoint>?) {
        mutablePoints.addAll(points!!)
    }
}

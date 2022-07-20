/**
 * TriangulationProcess.java
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

import org.slf4j.LoggerFactory

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
 */ /**
 *
 * @author Thomas ???, thahlen@gmail.com
 */
internal class TriangulationProcess @JvmOverloads constructor(private val _algorithm: TriangulationAlgorithm = TriangulationAlgorithm.DTSweep) :
    Runnable {
    val context: TriangulationContext<*>?
    private var _thread: Thread? = null
    private var isDone = false
    private var pointCount = 0
    private var timestamp: Long = 0
    private var triangulationTime = 0.0
    private var _awaitingTermination = false
    private var _restart = false
    private val _triangulations = ArrayList<Triangulatable>()
    private val _listeners = ArrayList<TriangulationProcessListener>()
    fun addListener(listener: TriangulationProcessListener) {
        _listeners.add(listener)
    }

    fun removeListener(listener: TriangulationProcessListener) {
        _listeners.remove(listener)
    }

    fun clearListeners() {
        _listeners.clear()
    }

    /**
     * Notify all listeners of this new event
     * @param event
     */
    private fun sendEvent(event: TriangulationProcessEvent) {
        for (l in _listeners) {
            l.triangulationEvent(event, context!!.triangulatable!!)
        }
    }

    val stepCount: Int
        get() = context!!.stepCount
    /**
     * This retriangulates same set as previous triangulation
     * useful if you want to do consecutive triangulations with
     * same data. Like when you when you want to do performance
     * tests.
     */
    //    public void triangulate()
    //    {
    //        start();
    //    }
    /**
     * Triangulate a PointSet with eventual constraints
     *
     * @param cps
     */
    fun triangulate(ps: PointSet) {
        _triangulations.clear()
        _triangulations.add(ps)
        start()
    }

    /**
     * Triangulate a PointSet with eventual constraints
     *
     * @param cps
     */
    fun triangulate(cps: ConstrainedPointSet) {
        _triangulations.clear()
        _triangulations.add(cps)
        start()
    }

    /**
     * Triangulate a PolygonSet
     *
     * @param ps
     */
    fun triangulate(ps: PolygonSet) {
        _triangulations.clear()
        _triangulations.addAll(ps.polygons)
        start()
    }

    /**
     * Triangulate a Polygon
     *
     * @param ps
     */
    fun triangulate(polygon: Polygon) {
        _triangulations.clear()
        _triangulations.add(polygon)
        start()
    }

    /**
     * Triangulate a List of Triangulatables
     *
     * @param ps
     */
    fun triangulate(list: List<Triangulatable>?) {
        _triangulations.clear()
        _triangulations.addAll(list!!)
        start()
    }

    private fun start() {
        if (_thread == null || _thread!!.state == Thread.State.TERMINATED) {
            isDone = false
            _thread = Thread(this, _algorithm.name + "." + context!!.triangulationMode)
            _thread!!.start()
            sendEvent(TriangulationProcessEvent.Started)
        } else {
            // Triangulation already running. Terminate it so we can start a new
            shutdown()
            _restart = true
        }
    }

    val isWaiting: Boolean
        get() = _thread != null && _thread!!.state == Thread.State.WAITING

    override fun run() {
        pointCount = 0
        try {
            val time = System.nanoTime()
            for (t in _triangulations) {
                context!!.clear()
                context.prepareTriangulation(t)
                pointCount += context.mutablePoints.size
                Poly2Tri.triangulate(context)
            }
            triangulationTime = (System.nanoTime() - time) / 1e6
            logger.info("Triangulation of {} points [{}ms]", pointCount, triangulationTime)
            sendEvent(TriangulationProcessEvent.Done)
        } catch (e: RuntimeException) {
            if (_awaitingTermination) {
                _awaitingTermination = false
                logger.info("Thread[{}] : {}", _thread!!.name, e.message)
                sendEvent(TriangulationProcessEvent.Aborted)
            } else {
                e.printStackTrace()
                sendEvent(TriangulationProcessEvent.Failed)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("Triangulation exception {}", e.message)
            sendEvent(TriangulationProcessEvent.Failed)
        } finally {
            timestamp = System.currentTimeMillis()
            isDone = true
            _thread = null
        }

        // Autostart a new triangulation?
        if (_restart) {
            _restart = false
            start()
        }
    }

    private fun resume() {
        if (_thread != null) {
            // Only force a resume when process is waiting for a notification
            if (_thread!!.state == Thread.State.WAITING) {
                synchronized(context!!) { context.notify() }
            } else if (_thread!!.state == Thread.State.TIMED_WAITING) {
                context!!.waitUntilNotified(false)
            }
        }
    }

    private fun shutdown() {
        _awaitingTermination = true
        context!!.terminateTriangulation()
        resume()
    }

    fun requestRead() {
        context!!.waitUntilNotified(true)
    }

    // Make sure that it stays readable
    val isReadable: Boolean
        get() = if (_thread == null) {
            true
        } else {
            synchronized(_thread!!) {
                if (_thread!!.state == Thread.State.WAITING) {
                    return true
                } else if (_thread!!.state == Thread.State.TIMED_WAITING) {
                    // Make sure that it stays readable
                    context!!.waitUntilNotified(true)
                    return true
                }
                return false
            }
        }

    companion object {
        private val logger = LoggerFactory.getLogger(
            TriangulationProcess::class.java
        )
    }

    /**
     * Uses SweepLine algorithm by default
     * @param algorithm
     */
    init {
        context = Poly2Tri.createContext(_algorithm)
    }
}
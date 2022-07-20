/**
 * Poly2Tri.java
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
 */internal object Poly2Tri {
    private val logger = LoggerFactory.getLogger(Poly2Tri::class.java)
    private val defaultAlgorithm = TriangulationAlgorithm.DTSweep
    fun triangulate(ps: PolygonSet) {
        val tcx = createContext(defaultAlgorithm)
        for (p in ps.polygons) {
            tcx.prepareTriangulation(p)
            triangulate(tcx)
            tcx.clear()
        }
    }

    fun triangulate(p: Polygon) {
        triangulate(defaultAlgorithm, p)
    }

    fun triangulate(cps: ConstrainedPointSet) {
        triangulate(defaultAlgorithm, cps)
    }

    fun triangulate(ps: PointSet) {
        triangulate(defaultAlgorithm, ps)
    }

    fun createContext(algorithm: TriangulationAlgorithm?): TriangulationContext<*> {
        return when (algorithm) {
            TriangulationAlgorithm.DTSweep -> DTSweepContext()
            else -> DTSweepContext()
        }
    }

    fun triangulate(
        algorithm: TriangulationAlgorithm?,
        t: Triangulatable
    ) {

//        long time = System.nanoTime();
        val tcx: TriangulationContext<*> = createContext(algorithm)
        tcx.prepareTriangulation(t)
        triangulate(tcx)
        //        logger.info( "Triangulation of {} points [{}ms]", tcx.getPoints().size(), ( System.nanoTime() - time ) / 1e6 );
    }

    fun triangulate(tcx: TriangulationContext<*>) {
        when (tcx.algorithm()) {
            TriangulationAlgorithm.DTSweep -> DTSweep.triangulate(tcx as DTSweepContext)
            else -> DTSweep.triangulate(tcx as DTSweepContext)
        }
    }

    /**
     * Will do a warmup run to let the JVM optimize the triangulation code
     */
    fun warmup() {
        /*
         * After a method is run 10000 times, the Hotspot compiler will compile
         * it into native code. Periodically, the Hotspot compiler may recompile
         * the method. After an unspecified amount of time, then the compilation
         * system should become quiet.
         */
        val poly = PolygonGenerator.randomCircleSweep2(50.0, 50000)
        val process = TriangulationProcess()
        process.triangulate(poly)
    }
}

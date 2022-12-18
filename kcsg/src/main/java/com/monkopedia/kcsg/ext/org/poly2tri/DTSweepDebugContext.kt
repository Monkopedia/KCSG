/**
 * DTSweepDebugContext.java
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
package com.monkopedia.kcsg.ext.org.poly2tri

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
internal class DTSweepDebugContext(tcx: DTSweepContext) : TriangulationDebugContext(tcx) {
    /*
     * Fields used for visual representation of current triangulation
     */
    private var _primaryTriangle: DelaunayTriangle? = null
    private var _secondaryTriangle: DelaunayTriangle? = null
    private var activePoint: TriangulationPoint? = null
    private var _activeNode: AdvancingFrontNode? = null
    private var _activeConstraint: DTSweepConstraint? = null

    //  private Tuple2<TPoint,Double> m_circumCircle = new Tuple2<TPoint,Double>( new TPoint(), new Double(0) );
    //  public Tuple2<TPoint,Double> getCircumCircle() { return m_circumCircle; }
    var primaryTriangle: DelaunayTriangle?
        get() = _primaryTriangle
        set(triangle) {
            _primaryTriangle = triangle
            _tcx.update("setPrimaryTriangle")
        }
    var secondaryTriangle: DelaunayTriangle?
        get() = _secondaryTriangle
        set(triangle) {
            _secondaryTriangle = triangle
            _tcx.update("setSecondaryTriangle")
        }
    var activeNode: AdvancingFrontNode?
        get() = _activeNode
        set(node) {
            _activeNode = node
            _tcx.update("setWorkingNode")
        }
    var activeConstraint: DTSweepConstraint?
        get() = _activeConstraint
        set(e) {
            _activeConstraint = e
            _tcx.update("setWorkingSegment")
        }

    override fun clear() {
        _primaryTriangle = null
        _secondaryTriangle = null
        activePoint = null
        _activeNode = null
        _activeConstraint = null
    }

    //  public void setWorkingCircumCircle( TPoint point, TPoint point2, TPoint point3 )
    //  {
    //          double dx,dy;
    //          
    //          CircleXY.circumCenter( point, point2, point3, m_circumCircle.a );
    //          dx = m_circumCircle.a.getX()-point.getX();
    //          dy = m_circumCircle.a.getY()-point.getY();
    //          m_circumCircle.b = Double.valueOf( Math.sqrt( dx*dx + dy*dy ) );
    //          
    //  }
}
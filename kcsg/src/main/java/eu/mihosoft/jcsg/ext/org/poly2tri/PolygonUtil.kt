/**
 * PolygonUtil.java
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
package eu.mihosoft.jcsg.ext.org.poly2tri

import eu.mihosoft.jcsg.Edge
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.jcsg.Vertex
import eu.mihosoft.vvecmath.Vector3d
import java.util.*

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
internal object PolygonUtil {
    /**
     * Converts a CSG polygon to a poly2tri polygon (including holes)
     * @param polygon the polygon to convert
     * @return a CSG polygon to a poly2tri polygon (including holes)
     */
    private fun fromCSGPolygon(
        polygon: eu.mihosoft.jcsg.Polygon
    ): Polygon {
        // convert polygon
        val points: MutableList<PolygonPoint> = ArrayList()
        for (v in polygon.vertices) {
            val vp = PolygonPoint(v.pos.x, v.pos.y, v.pos.z)
            points.add(vp)
        }
        val result = Polygon(points)

        // convert holes
        polygon.storage.getValue<List<eu.mihosoft.jcsg.Polygon>>(Edge.KEY_POLYGON_HOLES)
            ?.forEach { hP: eu.mihosoft.jcsg.Polygon ->
                result.addHole(
                    fromCSGPolygon(hP)
                )
            }
        return result
    }

    fun concaveToConvex(
        concave: eu.mihosoft.jcsg.Polygon
    ): List<eu.mihosoft.jcsg.Polygon> {
        val result: MutableList<eu.mihosoft.jcsg.Polygon> = ArrayList()
        val normal = concave.vertices[0].normal.copy()
        val cw: Boolean = !Extrude.isCCW(concave)
        val p = fromCSGPolygon(concave)

        Poly2Tri.triangulate(p)

        val triangles = p.triangles
        var triPoints: MutableList<Vertex> = ArrayList()
        for (t in triangles) {
            var counter = 0
            for (tp in t.points) {
                triPoints.add(
                    Vertex(
                        Vector3d.xyz(tp!!.x, tp.y, tp.z),
                        normal
                    )
                )
                if (counter == 2) {
                    if (!cw) {
                        triPoints.reverse()
                    }
                    val poly = eu.mihosoft.jcsg.Polygon(
                        triPoints,
                        concave.storage
                    )
                    result.add(poly)
                    counter = 0
                    triPoints = ArrayList()
                } else {
                    counter++
                }
            }
        }
        return result
    }
}

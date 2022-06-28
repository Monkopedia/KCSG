/**
 * Peg.java
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
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
object RaspberryPeg {
    fun peg(): CSG {
        //      ol
        //     | |
        //   __    _
        //  |  \   ptoph
        //  |   \  _
        //  |   /  pth 
        //  |  /   _
        //  | |    bt
        //  | |__  _
        //  |    | bh
        //  ------ -
        //  |pw |

        // pw    = peg width
        // bh    = board mounting height
        // bt    = board thickness
        // pth   = peg tooth hight
        // ptoph = peg top height
        // ol    = overlap between board and peg
        val outer_offset = 4.0
        val inner_offset = 4.0
        val board_mounting_height = 5.5
        val board_thickness = 2.0
        val overlap = 1.0
        val peg_depth = 3.0
        val peg_tooth_height = 1.0
        val peg_top_height = 2.0
        val board_spacing = 0.2

        // inner offset
        //outer offset

        // board spacing (small spacing between peg and board, should be < 0.5mm)
        val pw = outer_offset + inner_offset
        val peg_points: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, peg_depth),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(pw, 0.0),
            Vector3d.xy(pw, board_mounting_height / 5),
            Vector3d.xy(pw - inner_offset / 2, board_mounting_height),
            Vector3d.xy(outer_offset - board_spacing, board_mounting_height),
            Vector3d.xy(
                outer_offset - board_spacing,
                board_mounting_height + board_thickness
            ),
            Vector3d.xy(
                outer_offset + overlap,
                board_mounting_height + board_thickness + peg_tooth_height
            ),
            Vector3d.xy(
                outer_offset,
                board_mounting_height + board_thickness + peg_tooth_height + peg_top_height
            ),
            Vector3d.xy(
                0.0,
                board_mounting_height + board_thickness + peg_tooth_height + peg_top_height
            )
        )
        return peg_points.transformed(Transform.unity().translateX(-outer_offset))
            .transformed(Transform.unity().rotX(90.0).translateZ(-peg_depth / 2))
    }
}
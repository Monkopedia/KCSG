/**
 * RaspberryPiMount.java
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
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.Polygon
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
object RaspberryPiBPlusMount {
    private fun board(): CSG {
        val boardThickness = 2.0
        val bottomThickness = 3.0
        val boardMountingHeight = 4.0
        val outerOffset = 4.0
        val innerOffset = 4.0
        val boardWidth = 85.0
        val boardHeight = 56.0
        val sd1 = 14.0
        val sd2 = 11.0
        val sd3 = 18.0
        val boardPointsExact: Polygon = Polygon.fromPoints(
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(0.0, boardHeight),
            Vector3d.xy(boardWidth, boardHeight),
            Vector3d.xy(boardWidth, boardHeight - sd1),
            Vector3d.xy(boardWidth - sd3, boardHeight - sd1),
            Vector3d.xy(boardWidth - sd3, sd2),
            Vector3d.xy(boardWidth, sd2),
            Vector3d.xy(boardWidth, 0.0)
        )

// outer offset

// inner offset
        val outer: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, bottomThickness),
            Vector3d.xy(0 - outerOffset, 0 - outerOffset),
            Vector3d.xy(0 - outerOffset, boardHeight + outerOffset),
            Vector3d.xy(
                boardWidth + outerOffset,
                boardHeight + outerOffset
            ), //                Vector3d.xy(bw+ox1,bh-sd1),
            //                Vector3d.xy(bw-sd3,bh-sd1),
            //                Vector3d.xy(bw-sd3,sd2),
            Vector3d.xy(boardWidth + outerOffset, sd2),
            Vector3d.xy(boardWidth + outerOffset, 0 - outerOffset)
        )
        val inner: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, bottomThickness),
            Vector3d.xy(0 + innerOffset, 0 + innerOffset),
            Vector3d.xy(0 + innerOffset, boardHeight - innerOffset),
            Vector3d.xy(
                boardWidth - innerOffset,
                boardHeight - innerOffset
            ), //                Vector3d.xy(bw-ox2,bh-sd1+oy2),
            //                Vector3d.xy(bw-sd3-ox2,bh-sd1+oy2),
            //                Vector3d.xy(bw-sd3-ox2,sd2-oy2),
            Vector3d.xy(boardWidth - innerOffset, sd2 - innerOffset),
            Vector3d.xy(boardWidth - innerOffset, 0 + innerOffset)
        )
        return outer.difference(inner).transformed(
            Transform.unity().rotX(180.0).translateY(
                -boardHeight
            )
        )
    }

    internal fun boardAndPegs(): CSG {
        val boardWidth = 85.6
        val boardHeight = 56.0
        val outerOffset = 4.0
        val bottomThickness = 3.0
        val board = board()
        val peg1 =
            RaspberryPeg.peg().transformed(Transform.unity().scaleY(0.9))
                .transformed(
                    Transform.unity()
                        .translate(0.0, boardHeight - 36, -bottomThickness)
                )
        val peg2 =
            RaspberryPeg.peg().transformed(Transform.unity().scaleY(2.0))
                .transformed(
                    Transform.unity().translate(
                        22.0,
                        boardHeight,
                        -bottomThickness
                    )
                        .rotZ(90.0)
                )
        val peg3 = RaspberryPeg.peg()
            .transformed(
                Transform.unity()
                    .translate(boardWidth - outerOffset, boardHeight, -bottomThickness)
                    .rotZ(90.0)
            )

//        translate([bw,outer_offset,0])
// rotate([0,0,180])
        val peg4 = RaspberryPeg.peg().transformed(
            Transform.unity()
                .translate(boardWidth, boardHeight - outerOffset * 2, -bottomThickness)
                .rotZ(180.0)
        )
        val peg4b = RaspberryPeg.peg()
            .transformed(
                Transform.unity()
                    .translate(boardWidth, outerOffset, -bottomThickness).rotZ(180.0)
            )
        val peg5 =
            RaspberryPeg.peg().transformed(Transform.unity().scaleY(2.0))
                .transformed(
                    Transform.unity()
                        .translate(boardWidth - 19, 0.0, -bottomThickness).rotZ(270.0)
                )
        val peg6 = RaspberryPeg.peg()
            .transformed(
                Transform.unity().translate(
                    boardWidth - 62,
                    0.0,
                    -bottomThickness
                )
                    .rotZ(270.0)
            )
        return board.union(peg1, peg2, peg3, peg4, peg4b, peg5, peg6)

//        return peg1;
    }
}

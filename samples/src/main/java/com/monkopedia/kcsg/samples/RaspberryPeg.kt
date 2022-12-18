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
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d

/**
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
        val outerOffset = 4.0
        val innerOffset = 4.0
        val boardMountingHeight = 5.5
        val boardThickness = 2.0
        val overlap = 1.0
        val pegDepth = 3.0
        val pegToothHeight = 1.0
        val pegTopHeight = 2.0
        val boardSpacing = 0.2

        // inner offset
        // outer offset

        // board spacing (small spacing between peg and board, should be < 0.5mm)
        val pw = outerOffset + innerOffset
        val pegPoints: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, pegDepth),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(pw, 0.0),
            Vector3d.xy(pw, boardMountingHeight / 5),
            Vector3d.xy(pw - innerOffset / 2, boardMountingHeight),
            Vector3d.xy(outerOffset - boardSpacing, boardMountingHeight),
            Vector3d.xy(
                outerOffset - boardSpacing,
                boardMountingHeight + boardThickness
            ),
            Vector3d.xy(
                outerOffset + overlap,
                boardMountingHeight + boardThickness + pegToothHeight
            ),
            Vector3d.xy(
                outerOffset,
                boardMountingHeight + boardThickness + pegToothHeight + pegTopHeight
            ),
            Vector3d.xy(
                0.0,
                boardMountingHeight + boardThickness + pegToothHeight + pegTopHeight
            )
        )
        return pegPoints.transformed(Transform.unity().translateX(-outerOffset))
            .transformed(Transform.unity().rotX(90.0).translateZ(-pegDepth / 2))
    }
}

/**
 * Peg.java
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
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d

/**
 *
 * <pre>
 * ol
 * | |
 * __    _
 * |  \   ptoph
 * |   \  _
 * |   /  pth
 * |  /   _
 * | |    bt
 * | |__  _
 * |    | bh
 * -------
 * |pw  |
 *
 * pw    = peg width
 * bh    = board mounting height
 * bt    = board thickness
 * pth   = peg tooth hight
 * ptoph = peg top height
 * ol    = overlap between board and peg
</pre> *
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class Peg {
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
    private var outerOffset = 4.0
    private var innerOffset = 4.0
    private var boardMountingHeight = 4.0
    private var boardThickness = 2.0
    private var overlap = 1.0
    private var pegDepth = 3.0
    private var pegToothHeight = 1.0
    private var pegTopHeight = 2.0
    private var boardSpacing = 0.2
    fun toCSG(): CSG? {

        // inner offset
        val oi = getInnerOffset()
        //outer offset
        val oo = getOuterOffset()
        val bh = getBoardMountingHeight()
        val bt = getBoardThickness()
        val ol = getOverlap()
        val pth = getPegToothHeight()
        val ptoph = getPegTopHeight()

        // board spacing (small spacing between peg and board, should be < 0.5mm)
        val bs = getBoardSpacing()
        val pd = getPegDepth()
        val pw = oo + oi
        val peg_points: CSG = Extrude.Companion.points(
            Vector3d.xyz(0.0, 0.0, pd),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(pw, 0.0),
            Vector3d.xy(pw, bh / 5),
            Vector3d.xy(pw - oi / 2, bh),
            Vector3d.xy(oo - bs, bh),
            Vector3d.xy(oo - bs, bh + bt),
            Vector3d.xy(oo + ol, bh + bt + pth),
            Vector3d.xy(oo, bh + bt + pth + ptoph),
            Vector3d.xy(0.0, bh + bt + pth + ptoph)
        )
        return peg_points.transformed(Transform.unity().translateX(-oo))
            .transformed(Transform.unity().rotX(90.0).translateZ(-pd / 2))
    }

    /**
     * @return the outerOffset
     */
    fun getOuterOffset(): Double {
        return outerOffset
    }

    /**
     * @param outerOffset the outerOffset to set
     */
    fun setOuterOffset(outerOffset: Double): Peg {
        this.outerOffset = outerOffset
        return this
    }

    /**
     * @return the innerOffset
     */
    fun getInnerOffset(): Double {
        return innerOffset
    }

    /**
     * @param innerOffset the innerOffset to set
     */
    fun setInnerOffset(innerOffset: Double): Peg {
        this.innerOffset = innerOffset
        return this
    }

    /**
     * @return the boardMountingHeight
     */
    fun getBoardMountingHeight(): Double {
        return boardMountingHeight
    }

    /**
     * @param boardMountingHeight the boardMountingHeight to set
     */
    fun setBoardMountingHeight(boardMountingHeight: Double): Peg {
        this.boardMountingHeight = boardMountingHeight
        return this
    }

    /**
     * @return the boardThickness
     */
    fun getBoardThickness(): Double {
        return boardThickness
    }

    /**
     * @param boardThickness the boardThickness to set
     */
    fun setBoardThickness(boardThickness: Double): Peg {
        this.boardThickness = boardThickness
        return this
    }

    /**
     * @return the overlap
     */
    fun getOverlap(): Double {
        return overlap
    }

    /**
     * @param overlap the overlap to set
     */
    fun setOverlap(overlap: Double): Peg {
        this.overlap = overlap
        return this
    }

    /**
     * @return the pegDepth
     */
    fun getPegDepth(): Double {
        return pegDepth
    }

    /**
     * @param pegDepth the pegDepth to set
     */
    fun setPegDepth(pegDepth: Double): Peg {
        this.pegDepth = pegDepth
        return this
    }

    /**
     * @return the pegToothHeight
     */
    fun getPegToothHeight(): Double {
        return pegToothHeight
    }

    /**
     * @param pegToothHeight the pegToothHeight to set
     */
    fun setPegToothHeight(pegToothHeight: Double): Peg {
        this.pegToothHeight = pegToothHeight
        return this
    }

    /**
     * @return the pegTopHeight
     */
    fun getPegTopHeight(): Double {
        return pegTopHeight
    }

    /**
     * @param pegTopHeight the pegTopHeight to set
     */
    fun setPegTopHeight(pegTopHeight: Double): Peg {
        this.pegTopHeight = pegTopHeight
        return this
    }

    /**
     * @return the boardSpacing
     */
    fun getBoardSpacing(): Double {
        return boardSpacing
    }

    /**
     * @param boardSpacing the boardSpacing to set
     */
    fun setBoardSpacing(boardSpacing: Double): Peg {
        this.boardSpacing = boardSpacing
        return this
    }
}
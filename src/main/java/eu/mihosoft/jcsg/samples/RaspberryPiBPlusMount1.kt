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
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.jcsg.Polygon
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
object RaspberryPiBPlusMount {
    private fun board(): CSG {
        val board_thickness = 2.0
        val bottom_thickness = 3.0
        val board_mounting_height = 4.0
        val outer_offset = 4.0
        val inner_offset = 4.0
        val board_width = 85.0
        val board_height = 56.0
        val sd1 = 14.0
        val sd2 = 11.0
        val sd3 = 18.0
        val board_points_exact: Polygon = Polygon.fromPoints(
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(0.0, board_height),
            Vector3d.xy(board_width, board_height),
            Vector3d.xy(board_width, board_height - sd1),
            Vector3d.xy(board_width - sd3, board_height - sd1),
            Vector3d.xy(board_width - sd3, sd2),
            Vector3d.xy(board_width, sd2),
            Vector3d.xy(board_width, 0.0)
        )

// outer offset 

// inner offset
        val outer: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, bottom_thickness),
            Vector3d.xy(0 - outer_offset, 0 - outer_offset),
            Vector3d.xy(0 - outer_offset, board_height + outer_offset),
            Vector3d.xy(
                board_width + outer_offset,
                board_height + outer_offset
            ),  //                Vector3d.xy(bw+ox1,bh-sd1),
            //                Vector3d.xy(bw-sd3,bh-sd1),
            //                Vector3d.xy(bw-sd3,sd2),
            Vector3d.xy(board_width + outer_offset, sd2),
            Vector3d.xy(board_width + outer_offset, 0 - outer_offset)
        )
        val inner: CSG = Extrude.points(
            Vector3d.xyz(0.0, 0.0, bottom_thickness),
            Vector3d.xy(0 + inner_offset, 0 + inner_offset),
            Vector3d.xy(0 + inner_offset, board_height - inner_offset),
            Vector3d.xy(
                board_width - inner_offset,
                board_height - inner_offset
            ),  //                Vector3d.xy(bw-ox2,bh-sd1+oy2),
            //                Vector3d.xy(bw-sd3-ox2,bh-sd1+oy2),
            //                Vector3d.xy(bw-sd3-ox2,sd2-oy2),
            Vector3d.xy(board_width - inner_offset, sd2 - inner_offset),
            Vector3d.xy(board_width - inner_offset, 0 + inner_offset)
        )
        return outer.difference(inner).transformed(
            Transform.unity().rotX(180.0).translateY(
                -board_height
            )
        )
    }

    private fun boardAndPegs(): CSG {
        val board_width = 85.6
        val board_height = 56.0
        val outer_offset = 4.0
        val bottom_thickness = 3.0
        val board = board()
        val peg1 =
            RaspberryPeg.peg()!!.transformed(Transform.unity().scaleY(0.9))
                .transformed(
                    Transform.unity()
                        .translate(0.0, board_height - 36, -bottom_thickness)
                )
        val peg2 =
            RaspberryPeg.peg()!!.transformed(Transform.unity().scaleY(2.0))
                .transformed(
                    Transform.unity().translate(
                        22.0,
                        board_height,
                        -bottom_thickness
                    )
                        .rotZ(90.0)
                )
        val peg3 = RaspberryPeg.peg()!!
            .transformed(
                Transform.unity()
                    .translate(board_width - outer_offset, board_height, -bottom_thickness)
                    .rotZ(90.0)
            )

//        translate([bw,outer_offset,0])
//rotate([0,0,180])
        val peg4 = RaspberryPeg.peg()!!.transformed(
            Transform.unity()
                .translate(board_width, board_height - outer_offset * 2, -bottom_thickness)
                .rotZ(180.0)
        )
        val peg4b = RaspberryPeg.peg()!!
            .transformed(
                Transform.unity()
                    .translate(board_width, outer_offset, -bottom_thickness).rotZ(180.0)
            )
        val peg5 =
            RaspberryPeg.peg()!!.transformed(Transform.unity().scaleY(2.0))
                .transformed(
                    Transform.unity()
                        .translate(board_width - 19, 0.0, -bottom_thickness).rotZ(270.0)
                )
        val peg6 = RaspberryPeg.peg()!!
            .transformed(
                Transform.unity().translate(
                    board_width - 62,
                    0.0,
                    -bottom_thickness
                )
                    .rotZ(270.0)
            )
        return board!!.union(peg1, peg2, peg3, peg4, peg4b, peg5, peg6)

//        return peg1;
    }

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
        val board = boardAndPegs()!!
            .transformed(Transform.unity().rotX(180.0))
        FileUtil.write(
            Paths.get("raspberry-pi-bplus-mount-3mm.stl"),
            board.toStlString()
        )
        board.toObj().toFiles(Paths.get("raspberry-pi-bplus-mount-3mm.obj"))
    }
}
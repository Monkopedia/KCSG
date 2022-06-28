/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cylinder
import eu.mihosoft.jcsg.Extrude
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class LeapMotionCase {
    private val w = 80.5
    private val h = 30.5
    private val d = 12.0
    private val arc = 7.35
    private val arcRes = 64
    private val caseThickness = 2.0
    private val deviceMetalThickness = 1.0
    private val pegThickness = 0.8
    private val pegHeight = -1.0
    private val pegWidth = 16.0
    private val pegOffset = 0.6
    private val pegTopHeight = 2.4
    private val pegToCaseOffset = 2.5
    private val grabSpace = 16.0
    private fun outline(
        w: Double,
        h: Double,
        d: Double,
        arc: Double,
        thickness: Double,
        arcRes: Int
    ): CSG? {
        var arc = arc
        arc = arc + thickness
        val arcCyl1 = Cylinder(arc, d, arcRes).toCSG()!!.transformed(
            Transform.unity().translate(arc - thickness, arc - thickness, 0.0)
        )
        val arcCyl2 = Cylinder(arc, d, arcRes).toCSG()!!.transformed(
            Transform.unity().translate(w - arc + thickness, arc - thickness, 0.0)
        )
        val arcCyl3 = Cylinder(arc, d, arcRes).toCSG()!!.transformed(
            Transform.unity().translate(w - arc + thickness, h - arc + thickness, 0.0)
        )
        val arcCyl4 = Cylinder(arc, d, arcRes).toCSG()!!.transformed(
            Transform.unity().translate(arc - thickness, h - arc + thickness, 0.0)
        )
        val arcCyls = arcCyl1.union(arcCyl2, arcCyl3, arcCyl4)
        return arcCyls.hull()
    }

    private fun deviceOutline(): CSG? {
        return outline(w, h, d, arc, 0.0, arcRes)
    }

    private fun deviceInnerOutline(): CSG? {
        return outline(w, h, d, arc, -deviceMetalThickness, arcRes)
    }

    private fun caseOutline(): CSG? {
        val outline = outline(w, h, d + caseThickness, arc, caseThickness, arcRes)
        val cyl = Cylinder(grabSpace / 2.0, h + caseThickness * 2, arcRes).toCSG()!!
            .transformed(
                Transform.unity().rotX(90.0).translate(
                    outline!!.bounds.bounds.x() / 2.0 - caseThickness,
                    -d,
                    -caseThickness
                ).scaleX(3.0)
            )
        return outline.difference(
            deviceOutline()!!.transformed(
                Transform.unity().translateZ(caseThickness)
            )
        ).difference(cyl)
    }

    private fun peg(): CSG {
        val fullPegHeight = pegHeight + d + caseThickness + pegTopHeight
        return Extrude.Companion.points(
            Vector3d.z(pegWidth),
            Vector3d.xy(0.0, 0.0),
            Vector3d.xy(pegThickness, 0.0),
            Vector3d.xy(pegThickness, fullPegHeight),
            Vector3d.xy(0.0, fullPegHeight),
            Vector3d.xy(-pegOffset, fullPegHeight - pegTopHeight / 2.0),
            Vector3d.xy(0.0, fullPegHeight - pegTopHeight)
        )
    }

    private fun pegToFront(): CSG? {
        return peg().transformed(Transform.unity().rotX(-90.0).rotY(-90.0))
            .transformed(Transform.unity().translateX(-pegWidth / 2.0))
    }

    private fun pegToBack(): CSG? {
        return peg().transformed(Transform.unity().rotX(-90.0).rotY(90.0))
            .transformed(Transform.unity().translateX(-pegWidth / 2.0))
    }

    private fun fullCase(): CSG? {
        var caseOutline = caseOutline()

        // add protection space
        caseOutline = caseOutline!!.difference(
            deviceInnerOutline()!!.transformed(
                Transform.unity().translate(0.0, 0.0, caseThickness / 2.0)
            )
        )
        val outlineWidth = caseOutline.bounds.bounds.x()
        caseOutline = addPegsToOutline(caseOutline, outlineWidth * 0.25)
        caseOutline = addPegsToOutline(caseOutline, outlineWidth * 0.75)
        return caseOutline
    }

    private fun addPegsToOutline(caseOutline: CSG?, pos: Double): CSG? {
        var caseOutline = caseOutline
        var pos = pos
        pos = pos - caseThickness
        val cyl1 = Cylinder(pegWidth / 2.0 + pegToCaseOffset, h + caseThickness * 2, arcRes)
            .toCSG()!!
            .transformed(Transform.unity().rotX(90.0).translate(pos, -d, -caseThickness))
        caseOutline = caseOutline!!.difference(cyl1)
        val peg1 = pegToFront()!!.transformed(Transform.unity().translate(pos, h, 0.0))
        val peg2 = pegToBack()!!.transformed(Transform.unity().translate(pos + pegWidth, 0.0, 0.0))
        return caseOutline.union(peg1, peg2)
    }

    fun toCSG(): CSG? {
        return fullCase()
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            FileUtil.Companion.write(
                Paths.get("leapmotion.stl"),
                LeapMotionCase().toCSG()!!.toStlString()
            )
            LeapMotionCase().toCSG()!!.toObj().toFiles(Paths.get("leapmotion.obj"))
        }
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cylinder
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.jcsg.Sphere
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class FractalStructureBeam2D {
    private fun toCSG(): CSG? {
        return createBeam(5.0, Vector3d.xy(0.0, 0.0), Vector3d.xy(10.0, 0.0), 2)
    }

    private fun createBeam(b: Double, start: Vector3d, stop: Vector3d, i: Int): CSG? {
        if (i == 0) {
            return createBeamTerminal(b, start, stop)
        }
        val l = stop.minus(start).magnitude()
        val a = stop.y() - start.y()
        val alpha = Math.asin(a / l) * 180.0 / Math.PI
        println("level: $i alpha: $alpha : $start : $stop : l(c) = $l : a = $a")
        val localToGlobalTransform = Transform.unity().rotZ(-alpha).translate(start)
        val nextB = b / 5.0
        val innerStart = Vector3d.ZERO
        val innerStop = Vector3d.xy(l, 0.0)
        val incVec = Vector3d.xy(0.0, b / 2.0 - nextB / 2.0)
        val mainBeamStartUpper = innerStart.plus(incVec)
        val mainBeamStartLower = innerStart.minus(incVec)
        val mainBeamStopUpper = innerStop.plus(incVec)
        val mainBeamStopLower = innerStop.minus(incVec)
        val upperMainBeam = createBeam(nextB, mainBeamStartUpper, mainBeamStopUpper, i - 1)
        val lowerMainBeam = createBeam(nextB, mainBeamStartLower, mainBeamStopLower, i - 1)
        val mainBeams = upperMainBeam!!.union(lowerMainBeam)
        val switchDir = false
        var innerBeams: CSG? = null
        val stopMinorBeam = Vector3d.xy(b, 0.0).plus(incVec) //Vector3d.xy(b, b / 2.0);
        val startMinor = Sphere(mainBeamStartLower, 0.5, 4, 4).toCSG()
        val stopMinor = Sphere(stopMinorBeam, 0.5, 4, 4).toCSG()
        innerBeams = startMinor!!.union(stopMinor)
        val counter = 0

        /*while (stopMinorBeam.x < innerStop.x) {
        
        stopMinorBeam = Vector3d.xy((counter + 1) * b, !switchDir ? b / 2.0 : -b / 2.0);
        
        counter++;
        switchDir = !switchDir;
        
        //if (i == 2) {
        //   System.out.println("level: " + i + " counter: " + counter + " : " + startMinorBeam + " : " + stopMinorBeam);
        //}
        
        CSG innerB = createBeam(nextB, startMinorBeam, stopMinorBeam, i - 1);
        
        if (innerBeams == null) {
        innerBeams = innerB;
        } else {
        innerBeams = innerBeams.union(innerB);
        }
        
        startMinorBeam = stopMinorBeam.clone();
        }*/

//        if (innerBeams != null) {
        return mainBeams.union(innerBeams).transformed(localToGlobalTransform)
        //        } else {
//            return mainBeams.transformed(localToGlobalTransform);
//        }
    }

    private fun createBeamTerminal(b: Double, start: Vector3d, stop: Vector3d): CSG? {
        return Cylinder(start, stop, b / 2.0, 4).toCSG()
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            var result = FractalStructureBeam2D().toCSG()
            result = result!!.union(Sphere(Vector3d.ZERO, 1.0, 4, 4).toCSG())
            FileUtil.Companion.write(
                Paths.get("fractal-structure-beam-2d.stl"),
                result.toStlString()
            )
            result.toObj().toFiles(Paths.get("fractal-structure-beam-2d.stl"))
        }
    }
}
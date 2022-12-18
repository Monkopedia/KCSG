package com.monkopedia.kcsg

import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.CSG.Companion.fromPolygons
import com.monkopedia.kcsg.Cube
import com.monkopedia.kcsg.Sphere
import org.junit.Assert
import org.junit.Test

class VolumeTest {
    @Test
    fun vlumeTest() {
        run {

            // volume of empty CSG object is 0
            val emptyVolume: Double = fromPolygons().computeVolume()
            Assert.assertEquals(emptyVolume, 0.0, 1e-16)
        }
        run {

            // volume of unit cube is 1 unit^3
            val volumeUnitCube = Cube(1.0).toCSG().computeVolume()
            Assert.assertEquals(1.0, volumeUnitCube, 1e-16)
        }
        run {

            // volume of cube is w*h*d unit^3
            val w = 30.65
            val h = 24.17
            val d = 75.3
            val volumeBox = Cube(w, h, d).toCSG().computeVolume()
            Assert.assertEquals(w * h * d, volumeBox, 1e-16)
        }
        run {

            // volume of sphere is (4*PI*r^3)/3.0 unit^3
            val r = 3.4

            // bad approximation
            val volumeSphere1 = Sphere(r, 32, 16).toCSG().computeVolume()
            Assert.assertEquals(4.0 * Math.PI * r * r * r / 3.0, volumeSphere1, 10.0)

            // better approximation
            val volumeSphere2 = Sphere(r, 1024, 512).toCSG().computeVolume()
            Assert.assertEquals(4.0 * Math.PI * r * r * r / 3.0, volumeSphere2, 1e-2)
        }
        run {

            // volume of cylinder is PI*r^2*h unit^3
            val r = 5.9
            val h = 2.1

            // bad approximation
            val volumeCylinder1 = Cylinder(r, h, 16).toCSG().computeVolume()
            Assert.assertEquals(Math.PI * r * r * h, volumeCylinder1, 10.0)

            // better approximation
            val volumeCylinder2 = Cylinder(r, h, 1024).toCSG().computeVolume()
            Assert.assertEquals(Math.PI * r * r * h, volumeCylinder2, 1e-2)
        }
    }
}

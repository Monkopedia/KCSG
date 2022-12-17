/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.RoundedCube

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class RoundedCubeSample {
    fun toCSG(): CSG {
        return RoundedCube(3.0).apply {
            resolution = 8
            cornerRadius = 0.2
        }.toCSG()
    }
}

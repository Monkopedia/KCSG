/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import com.monkopedia.kcsg.*
import com.monkopedia.kcsg.TransformBuilder.translate
import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.FileUtil
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths

/**
 *
 * @author miho
 */
class ArduinoMount {
    private val bottomWidth = 68.6
    private val bottomHeight = 53.3
    private val bottomThickness = 2.0
    private val pinHeight = 12.0
    private val pinHoleHeight = 4.8
    private val pinRadius = 2.0
    private val boardThickness = 2.0
    private val servoConnectThickness = 7.0
    private fun board() = cube {
        dimensions = Vector3d.xyz(bottomWidth, bottomHeight, bottomThickness)
    }

    private fun pins(): CSG {
        val prototype = cylinder(pinRadius, pinHeight, 16)
        val first = prototype.transform {
            translate(x = bottomWidth / 2.0, y = bottomHeight / 2.0)
        }
        val second = prototype.transform {
            translate(x = bottomWidth / 2.0, y = -bottomHeight / 2.0)
        }
        val third = prototype.transform {
            translate(x = -bottomWidth / 2.0)
        }
        val pins = first + second + third
        val board = board().transform {
            translate(z = pinHoleHeight * 2)
        }
        return pins - board
    }

    private fun pinConnections(): CSG {
        val first = cube {
            dimensions = xyz(bottomWidth / 2, 3.0, bottomThickness)
        } * translate(x = -bottomWidth / 4, z = bottomThickness / 2)
        val second = cube {
            dimensions = xyz(bottomWidth / 2 + 10, 3.0, bottomThickness)
        }.transform {
            rotZ(37.8).translate(x = bottomWidth / 4 + 5, z = bottomThickness / 2)
        }
        val third = cube {
            dimensions = xyz(bottomWidth / 2 + 10, 3.0, bottomThickness)
        }.transform {
            rotZ(-37.8).translate(x = bottomWidth / 4 + 5, z = bottomThickness / 2)
        }
        return first + second + third
    }

    private fun servoConnect(): CSG {
        val firstA = cube {
            dimensions = xyz(bottomWidth, servoConnectThickness, bottomThickness)
        } * translate(y = -bottomHeight / 2, z = bottomThickness / 2)
        val firstB = cube {
            dimensions = xyz(
                3.0,
                bottomHeight / 2 + servoConnectThickness / 2,
                bottomThickness
            )
        } * translate(
            -bottomWidth / 2,
            -bottomHeight / 4 - servoConnectThickness / 4,
            bottomThickness / 2
        )
        val first = firstA + firstB
        val second = first.transform {
            rotX(180.0).translateZ(-bottomThickness)
        }
        return first + second
    }

    fun toCSG(): CSG {
        return pins() + pinConnections() + servoConnect()
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val aMount = ArduinoMount()

            // save union as stl
//        FileUtil.write(Paths.get("sample.stl"), new ServoHead().servoHeadFemale().transformed(Transform.unity().scale(1.0)).toStlString());
            FileUtil.write(Paths.get("sample.stl"), aMount.toCSG().toStlString())
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.*
import com.monkopedia.kcsg.TransformBuilder.translate
import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.FileUtil
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

/**
 */
class ArduinoMount {

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val properties = mutableMapOf<String, Lazy<CSG>>()
            val exportedProperties = mutableSetOf<String>()
            val scriptInstance = object : KcsgBuilder() {
                override fun exportProperty(propertyName: String) {
                    exportedProperties.add(propertyName)
                }

                override fun track(propertyName: String, lazy: Lazy<CSG>) {
                    properties[propertyName] = lazy
                }

                override fun findStl(stlName: String): Path = error("Not implemented")
            }
            scriptInstance.buildArduinoMount()

            for (property in exportedProperties) {
                FileUtil.write(Paths.get("$property.stl"), properties[property]!!.value.toStlString())
            }
        }
    }
}

fun KcsgBuilder.buildArduinoMount() {
    val bottomWidth = 68.6
    val bottomHeight = 53.3
    val bottomThickness = 2.0
    val pinHeight = 12.0
    val pinHoleHeight = 4.8
    val pinRadius = 2.0
    val boardThickness = 2.0
    val servoConnectThickness = 7.0

    val board by primitive {
        cube {
            dimensions = xyz(bottomWidth, bottomHeight, bottomThickness)
        }
    }

    val pins by csg {
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
        val board = board.transform {
            translate(z = pinHoleHeight * 2)
        }
        pins - board
    }

    val pinConnections by csg {
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
        first + second + third
    }

    val servoConnect by csg {
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
        first + second
    }

    val sample by csg(exported = true) {
        pins + pinConnections + servoConnect
    }
}

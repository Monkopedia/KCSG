/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.*
import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.FileUtil
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths


/**
 */
class SensorMount {

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
                override fun findScript(csgsName: String): ImportedScript = error("Not implemented")
            }
            scriptInstance.buildSensorMount()

            for (property in exportedProperties) {
                FileUtil.write(Paths.get("$property.stl"), properties[property]!!.value.toStlString())
            }
        }
    }
}

fun KcsgBuilder.buildSensorMount() {
    val sensorWidth = 11.0
    val sensorLength = 25.125
    val sensorHeight = 9.09
    val corners = sensorWidth / 5

    val sensor by csg {
        val sensorTall = roundedCube {
            dimensions = xyz(sensorWidth, sensorLength, sensorHeight * 2)
            cornerRadius = corners
        }
        val sensorBounds = cube {
            dimensions = xyz(sensorWidth, sensorLength, sensorHeight)
        }
        sensorTall and sensorBounds
    }
    val sensorWallSize = 1.5
    val baseWidth = sensorWidth + 25
    val baseLength = sensorLength + 25
    val baseHeight = 18.0
    val sensorXOffset = (baseWidth - sensorWidth) / 2 - sensorWallSize
    val sensorYOffset = (baseLength - sensorLength) / 2 - sensorWallSize
    val sensorZOffset = (baseHeight - sensorHeight) / 2 + 5
    val xCylSize = baseWidth - sensorWidth - sensorWallSize
    val yCylSize = baseLength - sensorLength - sensorWallSize

    val xCyl by primitive {
        cylinder {
            radius = xCylSize
            start = xyz(baseWidth / 2, baseLength / 2, baseHeight / 1)
            end = xyz(baseWidth / 2, -baseLength / 2, baseHeight / 1)
            numSlices = 64
        }
    }
    val yCyl by primitive {
        cylinder {
            radius = yCylSize
            start = xyz(baseWidth / 2, -baseLength / 2, baseHeight / 1)
            end = xyz(-baseWidth / 2, -baseLength / 2, baseHeight / 1)
            numSlices = 64
        }
    }

    val base by csg {
        val block = cube {
            dimensions = xyz(baseWidth, baseLength, baseHeight)
        }
        val positionedSensor = sensor.transform {
            translate(-sensorXOffset, sensorYOffset, sensorZOffset)
        }
        block - positionedSensor - xCyl - yCyl
    }
}

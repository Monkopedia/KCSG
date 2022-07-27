/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import com.monkopedia.kcsg.*
import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.FileUtil
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author monkopedia
 */
class SensorMount {

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val properties = mutableMapOf<String, Lazy<CSG>>()
            val exportedProperties = mutableSetOf<String>()
            val scriptInstance = object : KcsgScript() {
                override fun exportProperty(propertyName: String) {
                    exportedProperties.add(propertyName)
                }

                override fun track(propertyName: String, lazy: Lazy<CSG>) {
                    properties[propertyName] = lazy
                }

                override fun findStl(stlName: String): Path = error("Not implemented")
            }
            scriptInstance.buildSensorMount()

            for (property in exportedProperties) {
                FileUtil.write(Paths.get("$property.stl"), properties[property]!!.value.toStlString())
            }
        }
    }
}

fun KcsgScript.buildSensorMount() {
    val sensorWidth = 11.0
    val sensorLength = 25.125
    val sensorHeight = 9.09
    val corners = sensorWidth / 5

    val sensor by primitive {
        roundedCube {
            dimensions = xyz(sensorWidth, sensorLength, sensorHeight)
            cornerRadius = corners
        }
    }
}

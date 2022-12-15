package com.monkopedia.kcsg

import eu.mihosoft.jcsg.CSG
import java.nio.file.Path

class KcsgScript(var host: KcsgHost = EmptyHost) : KcsgBuilder() {
    private val properties = mutableMapOf<String, Lazy<CSG>>()
    private val exportedProperties = mutableSetOf<String>()

    override fun exportProperty(propertyName: String) {
        exportedProperties.add(propertyName)
    }

    override fun track(propertyName: String, lazy: Lazy<CSG>) {
        properties[propertyName] = lazy
    }

    override fun findStl(stlName: String): Path = host.findStl(stlName)

    fun overrideExport(propertyName: String, export: Boolean) {
        if (export) {
            exportedProperties.add(propertyName)
        } else {
            exportedProperties.remove(propertyName)
        }
    }

    fun generateExports(): Map<String, CSG> {
        return exportedProperties.associateWith {
            properties[it]!!.value
        }
    }

    fun generateTarget(target: String): CSG {
        return properties[target]?.value ?: error("Unknown target $target")
    }

    fun exports(): Collection<String> {
        return exportedProperties
    }

    fun targets(): Collection<String> {
        return properties.keys
    }

    companion object {

        val HEADER = """
            |import com.monkopedia.kcsg.*
            |import eu.mihosoft.jcsg.*
            |import eu.mihosoft.vvecmath.*
            |
            |com.monkopedia.kcsg.KcsgScript().apply {
        """.trimMargin()
        val FOOTER = """
            |}
        """.trimMargin()
    }
}

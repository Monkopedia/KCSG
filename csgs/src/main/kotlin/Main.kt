package com.monkopedia.csgs

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.monkopedia.csgs.OutputType.OBJ
import com.monkopedia.csgs.OutputType.STL
import com.monkopedia.kcsg.KcsgScript
import com.monkopedia.kcsg.KcsgScript.Companion.FOOTER
import com.monkopedia.kcsg.KcsgScript.Companion.HEADER
import java.io.File
import javax.script.ScriptEngineManager
import javax.script.ScriptException

fun main(vararg args: String) = Csgs().main(args)

enum class OutputType {
    STL,
    OBJ
}

class Csgs : CliktCommand() {
    val targetFile by argument("Main file to load and generate from")
    val outputDirectory by option("-o", "--output", help = "Directory to place exported files in")
        .file(mustExist = false, canBeFile = false, canBeDir = true)
        .default(File("out"))
    val clear by option(
        "-c",
        "--clean",
        help = "Remove contents of output directory before executing"
    ).flag()
    val outputType by option("-t", "--output-type", help = "The type of files to generate")
        .enum<OutputType>(ignoreCase = true)
        .default(STL)
    val exports by option(
        "-e",
        "--export",
        help = "Force a named target to be exported, use ? to list available"
    ).multiple()

    override fun run() {
        if (clear) {
            require(outputDirectory.deleteRecursively()) {
                "Failed to clear output directory ${outputDirectory.absolutePath}"
            }
        }
        if (!outputDirectory.exists()) {
            require(outputDirectory.mkdirs()) {
                "Cannot create output directory ${outputDirectory.absolutePath}"
            }
        }
        val content = File(targetFile).readText()
        val output = executeCode(content, targetFile)
        for (export in exports) {
            if (export.trim() == "?") {
                println("Listing targets:")
                output.targets().forEach {
                    println("  $it")
                }
            } else {
                output.overrideExport(export, true)
            }
        }
        output.generateExports().forEach { (name, csg) ->
            when (outputType) {
                STL -> {
                    val outputFile = File(outputDirectory, "$name.stl")
                    outputFile.writeText(csg.toStlString())
                }
                OBJ -> {
                    val outputFile = File(outputDirectory, "$name.obj")
                    outputFile.writeText(csg.toObjString())
                }
            }
        }
    }
}

private fun executeCode(testCode: String, file: String): KcsgScript {
    val engine = ScriptEngineManager().getEngineByExtension("kts")!!
    try {
        return engine.eval(HEADER + "\n" + testCode + "\n" + FOOTER) as KcsgScript
    } catch (t: ScriptException) {
        val offsetCount = HEADER.split("\n").count()
        if (t.lineNumber < offsetCount) {
            throw RuntimeException("Internal exception in scripting engine", t)
        } else {
            throw ScriptException(t.message, file, t.lineNumber - offsetCount, t.columnNumber)
        }
    }
}
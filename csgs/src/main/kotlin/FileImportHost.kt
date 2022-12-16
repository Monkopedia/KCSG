package com.monkopedia.csgs

import com.monkopedia.kcsg.ImportedKcsgScript
import com.monkopedia.kcsg.ImportedScript
import com.monkopedia.kcsg.KcsgHost
import com.monkopedia.kcsg.KcsgScript
import java.io.File
import java.nio.file.Path
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class FileImportHost(imports: List<File>) : KcsgHost {
    private val dirs = imports.filter { it.isDirectory }
    private val files = imports.filter { it.isFile }

    override fun findStl(stlName: String): Path {
        return resolve(stlName, "stl").toPath()
    }

    override fun findScript(csgsName: String): ImportedScript {
        val file = resolve(csgsName, "csgs")
        val script = createScript(file)
        return ImportedKcsgScript(script)
    }

    fun createScript(file: File): KcsgScript {
        return executeCode(file).also {
            it.host = this
        }
    }

    private fun resolve(name: String, ext: String): File {
        return findFile(name, ext) ?: findDir(name, ext) ?: error("Cannot resolve file $name")
    }

    private fun findDir(name: String, ext: String) =
        dirs.firstNotNullOfOrNull { d ->
            File(d, "$name.$ext").takeIf { it.exists() }
        }

    private fun findFile(name: String, ext: String) =
        files.find { f ->
            f.nameWithoutExtension == name && f.extension == ext
        }
}

private fun executeCode(file: File): KcsgScript {
    val testCode = file.readText()
    val engine = ScriptEngineManager().getEngineByExtension("kts")!!
    try {
        return engine.eval(KcsgScript.HEADER + "\n" + testCode + "\n" + KcsgScript.FOOTER) as KcsgScript
    } catch (t: ScriptException) {
        val offsetCount = KcsgScript.HEADER.split("\n").count()
        if (t.lineNumber < offsetCount) {
            throw RuntimeException("Internal exception in scripting engine", t)
        } else {
            throw ScriptException(
                t.message,
                file.toRelativeString(File(".")),
                t.lineNumber - offsetCount,
                t.columnNumber
            )
        }
    }
}

package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.FakeKcsgHost
import java.nio.file.Files
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KcsgScriptJvmIoTest {
    @Test
    fun hostDelegationAndScriptEnvelopeConstants() {
        val host = FakeKcsgHost(supportsCaching = false)
        val fixturePath = Files.createTempFile("kcsg-script-host-delegation", ".stl")
        FileUtil.toStlFile(Path(fixturePath.toString()), Cube(1.0).toCSG())
        host.registerStl("fixture", fixturePath.toString())
        val importedScript = object : ImportedScript {
            override val exports: Collection<String> = listOf("x")
            override val targets: Collection<String> = listOf("x")
            override fun get(name: String): CSG = Cube(1.0).toCSG()
        }
        host.registerScript("dep", importedScript)

        val script = KcsgScript(host)
        val stl by script.stl("fixture")
        assertTrue(stl.polygons.isNotEmpty())
        assertSame(importedScript, script.import("dep").value)
        assertEquals(listOf("fixture"), host.stlRequests)
        assertEquals(listOf("dep"), host.scriptRequests)

        assertTrue(KcsgScript.HEADER.contains("KcsgScript().apply"))
        assertNotNull(KcsgScript.FOOTER)
        assertTrue(KcsgScript.FOOTER.trim().endsWith("}"))

        fixturePath.toFile().delete()
    }
}

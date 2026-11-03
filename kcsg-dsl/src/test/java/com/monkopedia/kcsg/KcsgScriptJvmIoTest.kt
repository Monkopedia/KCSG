package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.FakeKcsgHost
import java.nio.file.Files
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KcsgScriptJvmIoTest {

    @Test
    fun stlVersionTokenChangesDependentCacheHash() {
        // A csg{} property importing an STL by name must change its cache key when the host
        // reports a new version token for that STL — so re-editing the source STL invalidates
        // cached results that consumed it.
        val host = FakeKcsgHost(supportsCaching = true)
        val stlPath = Files.createTempFile("kcsg-stl-version", ".stl")
        FileUtil.toStlFile(Path(stlPath.toString()), Cube(1.0).toCSG())
        host.registerStl("widget", stlPath.toString())

        fun cacheHashWithVersion(version: String): String {
            host.stlVersions["widget"] = version
            val script = KcsgScript(host)
            val widget by script.stl("widget")
            val part by script.csg { widget }
            part.polygons // force the real build → getCached → storeCached(hash)
            return host.cacheStores.last()
        }

        val v1 = cacheHashWithVersion("2026-01-01T00:00:00Z")
        val v2 = cacheHashWithVersion("2026-05-23T12:00:00Z")
        assertNotEquals(v1, v2)

        stlPath.toFile().delete()
    }

    @Test
    fun stlNameAndVersionBoundaryDoesNotCollideCacheKeys() {
        // #79: "stl:part" + "12:34" and "stl:part1" + "2:34" serialised to the same bytes, so two
        // different STLs could share a cache entry and the second build was served the first's
        // solid.
        val host = FakeKcsgHost(supportsCaching = true)
        val stlPath = Files.createTempFile("kcsg-stl-framing", ".stl")
        FileUtil.toStlFile(Path(stlPath.toString()), Cube(1.0).toCSG())
        host.registerStl("part", stlPath.toString())
        host.registerStl("part1", stlPath.toString())
        host.stlVersions["part"] = "12:34"
        host.stlVersions["part1"] = "2:34"

        fun cacheHashFor(stlName: String): String {
            val script = KcsgScript(host)
            val widget by script.stl(stlName)
            val part by script.csg { widget }
            part.polygons
            return host.cacheChecks.last()
        }

        assertNotEquals(cacheHashFor("part"), cacheHashFor("part1"))

        stlPath.toFile().delete()
    }

    @Test
    fun capturedSolidGeometryEntersCacheKey() {
        // #79: a solid built outside the hashing pass (here a script-level value captured by the
        // builder) was keyed by its slot in a lookup list, so a cube and a sphere shared a key.
        val host = FakeKcsgHost(supportsCaching = true)

        fun build(captured: CSG, remesh: Boolean): CSG {
            val script = KcsgScript(host)
            val part by script.csg(remesh = remesh) { captured }
            return part
        }

        // remesh = false returns the captured solid with no operation applied to it, so it only
        // enters the key if the builder's result itself is hashed; remesh = true passes it to
        // remesh(), so it enters as an argument.
        for (remesh in listOf(false, true)) {
            val cube = Cube(1.0).toCSG()
            val sphere = Sphere(1.0, 32, 16, Vector3d.ZERO).toCSG()
            build(cube, remesh)
            val cubeHash = host.cacheChecks.last()
            // refresh path: different geometry must miss and be built, not served the cube
            val builtSphere = build(sphere, remesh)
            val sphereHash = host.cacheChecks.last()
            assertNotEquals(cubeHash, sphereHash, "remesh=$remesh")
            assertTrue(builtSphere.polygons.size > cube.polygons.size, "remesh=$remesh")
            // hit path: the same geometry captured again maps to the same key and is served
            val storesBefore = host.cacheStores.size
            build(Cube(1.0).toCSG(), remesh)
            assertEquals(cubeHash, host.cacheChecks.last(), "remesh=$remesh")
            assertEquals(storesBefore, host.cacheStores.size, "remesh=$remesh")
        }
    }

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

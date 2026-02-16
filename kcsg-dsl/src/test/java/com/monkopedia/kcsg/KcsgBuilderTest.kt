package com.monkopedia.kcsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

class KcsgBuilderTest {
    @Test
    fun primitiveCsgImportStlAndExportFlow() {
        withTempDir("kcsg-builder-flow") { tempDir ->
            val stlPath = tempDir.resolve("mesh.stl")
            FileUtil.toStlFile(stlPath, Cube(1.0).toCSG())

            val importedCsg = Cube(2.0).toCSG()
            val importedScript = object : ImportedScript {
                override val exports: Collection<String> = listOf("piece")
                override val targets: Collection<String> = listOf("piece")
                override fun get(name: String): CSG = importedCsg
            }
            val builder = TestBuilder(
                stlPath = stlPath,
                importedScript = importedScript,
                cachingEnabled = false,
            )

            val primitive by builder.primitive(exported = true, allowCaching = true) { cube(1.0) }
            val cachedCsg by builder.csg(allowCaching = true) { cube(1.0).toCSG() }
            val imported by builder.csg { builder.import("dep").value["piece"] }
            val stl by builder.stl("mesh")

            assertTrue(primitive.computeVolume() > 0.0)
            assertTrue(cachedCsg.computeVolume() > 0.0)
            assertEquals(importedCsg.computeVolume(), imported.computeVolume(), 1e-4)
            assertTrue(stl.polygons.isNotEmpty())

            assertTrue(builder.exported.contains("primitive"))
            assertTrue(builder.tracked.containsAll(listOf("primitive", "cachedCsg", "imported", "stl")))
            assertEquals(listOf("dep"), builder.scriptRequests)
            assertEquals(listOf("mesh"), builder.stlRequests)
        }
    }

    @Test
    fun cachingAndOverrideShortCircuit() {
        withTempDir("kcsg-builder-cache") { tempDir ->
            val stlPath = tempDir.resolve("mesh.stl")
            FileUtil.toStlFile(stlPath, Cube(1.0).toCSG())
            val imported = object : ImportedScript {
                override val exports: Collection<String> = emptyList()
                override val targets: Collection<String> = emptyList()
                override fun get(name: String): CSG = error("unused")
            }

            val cachedBuilder = TestBuilder(
                stlPath = stlPath,
                importedScript = imported,
                cachingEnabled = true,
            )
            val cachedCsg by cachedBuilder.csg(allowCaching = true) { cube(1.0).toCSG() }
            cachedCsg
            val checksAfterFirstRead = cachedBuilder.cacheChecks.size
            val storesAfterFirstRead = cachedBuilder.cacheStores.size
            assertTrue(checksAfterFirstRead > 0)
            assertTrue(storesAfterFirstRead > 0)

            cachedCsg
            assertEquals(checksAfterFirstRead, cachedBuilder.cacheChecks.size)
            assertEquals(storesAfterFirstRead, cachedBuilder.cacheStores.size)

            val sentinel = Cube(0.5).toCSG()
            val previous = CSG.opOverride
            try {
                CSG.opOverride = object : OpOverride {
                    override fun operation(s: String, vararg csg: Any?): CSG? {
                        return if (s.startsWith("p:")) sentinel else null
                    }

                    override fun bounds(s: String, vararg csg: Any?): Bounds? = null
                    override fun double(s: String, vararg csg: Any?): Double? = null
                    override fun file(path: Path): CSG? = null
                    override fun inputStream(inputStreamFactory: () -> InputStream): CSG? = null
                }
                val overrideBuilder = TestBuilder(
                    stlPath = stlPath,
                    importedScript = imported,
                    cachingEnabled = false,
                )
                val overrideCsg by overrideBuilder.csg(allowCaching = true) { cube(1.0).toCSG() }
                assertSame(sentinel, overrideCsg)
            } finally {
                CSG.opOverride = previous
            }
        }
    }

    private class TestBuilder(
        private val stlPath: Path,
        private val importedScript: ImportedScript,
        private val cachingEnabled: Boolean,
    ) : KcsgBuilder() {
        val exported = mutableListOf<String>()
        val tracked = mutableListOf<String>()
        val stlRequests = mutableListOf<String>()
        val scriptRequests = mutableListOf<String>()
        val cacheChecks = mutableListOf<String>()
        val cacheStores = mutableListOf<String>()
        private val cache = mutableMapOf<String, CSG>()

        override fun exportProperty(propertyName: String) {
            exported.add(propertyName)
        }

        override fun track(propertyName: String, lazy: Lazy<CSG>) {
            tracked.add(propertyName)
        }

        override fun findStl(stlName: String): Path {
            stlRequests.add(stlName)
            return stlPath
        }

        override fun findScript(csgsName: String): ImportedScript {
            scriptRequests.add(csgsName)
            return importedScript
        }

        override val supportsCaching: Boolean
            get() = cachingEnabled

        override fun checkCached(hash: String): CSG? {
            cacheChecks.add(hash)
            return cache[hash]
        }

        override fun storeCached(hash: String, csg: CSG) {
            cacheStores.add(hash)
            cache[hash] = csg
        }
    }

    private fun withTempDir(prefix: String, block: (Path) -> Unit) {
        val dir = Files.createTempDirectory(prefix)
        try {
            block(dir)
        } finally {
            dir.toFile().deleteRecursively()
        }
    }
}

package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class ObjFileTest {
    @Test
    fun objAndMtlAccessorsAndStreams() {
        val objFile = Cube(1.0).toCSG().toObj()

        assertTrue(objFile.obj.contains("mtllib"))
        assertTrue(objFile.mtl.contains("newmtl"))
        assertEquals(objFile.obj, objFile.objStream.reader().readText())
        assertEquals(objFile.mtl, objFile.mtlStream.reader().readText())
    }

    @Test
    fun toFilesNormalizesObjAndMtlExtensions() {
        withTempDirectory("kcsg-objfile") { tempDir ->
            val noExtensionBase = tempDir.resolve("mesh")
            val noExtensionObj = Cube(1.0).toCSG().toObj()
            noExtensionObj.toFiles(noExtensionBase)
            val noExtensionObjPath = tempDir.resolve("mesh.obj")
            val noExtensionMtlPath = tempDir.resolve("mesh.mtl")
            assertTrue(noExtensionObjPath.toFile().exists())
            assertTrue(noExtensionMtlPath.toFile().exists())
            assertTrue(FileUtil.read(noExtensionObjPath).contains("mtllib mesh.mtl"))

            val objExtensionBase = tempDir.resolve("mesh2.obj")
            val objExtensionObj = Cube(1.0).toCSG().toObj()
            objExtensionObj.toFiles(objExtensionBase)
            val objExtensionObjPath = tempDir.resolve("mesh2.obj")
            val objExtensionMtlPath = tempDir.resolve("mesh2.mtl")
            assertTrue(objExtensionObjPath.toFile().exists())
            assertTrue(objExtensionMtlPath.toFile().exists())
            assertTrue(FileUtil.read(objExtensionObjPath).contains("mtllib mesh2.mtl"))

            val mtlExtensionBase = tempDir.resolve("mesh3.mtl")
            val mtlExtensionObj = Cube(1.0).toCSG().toObj()
            mtlExtensionObj.toFiles(mtlExtensionBase)
            val mtlExtensionObjPath = tempDir.resolve("mesh3.obj")
            val mtlExtensionMtlPath = tempDir.resolve("mesh3.mtl")
            assertTrue(mtlExtensionObjPath.toFile().exists())
            assertTrue(mtlExtensionMtlPath.toFile().exists())
            assertTrue(FileUtil.read(mtlExtensionObjPath).contains("mtllib mesh3.mtl"))
        }
    }

    @Test
    fun toFilesSupportsPathsWithoutParentDirectory() {
        val baseName = "kcsg-obj-no-parent-${System.nanoTime()}"
        val objPath = Paths.get("$baseName.obj")
        val mtlPath = Paths.get("$baseName.mtl")

        try {
            Cube(1.0).toCSG().toObj().toFiles(objPath)
            assertTrue(Files.exists(objPath))
            assertTrue(Files.exists(mtlPath))
            assertTrue(FileUtil.read(objPath).contains("mtllib $baseName.mtl"))
        } finally {
            Files.deleteIfExists(objPath)
            Files.deleteIfExists(mtlPath)
        }
    }
}

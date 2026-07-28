package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

class ObjFileTest {
    @Test
    fun objAndMtlAccessorsAndStreams() {
        val objFile = Cube(1.0).toCSG().toObj()

        assertTrue(objFile.obj.contains("mtllib"))
        assertTrue(objFile.mtl.contains("newmtl"))
        assertEquals(objFile.obj, objFile.objSource.readString())
        assertEquals(objFile.mtl, objFile.mtlSource.readString())
    }

    @Test
    fun toFilesWritesTerminatedMaterialLibraryDirective() {
        withTempDirectory("kcsg-objfile") { tempDir ->
            Cube(1.0).toCSG().toObj().toFiles(Path(tempDir, "mesh.obj"))

            val lines = FileUtil.read(Path(tempDir, "mesh.obj")).lineSequence().toList()
            assertEquals("mtllib mesh.mtl", lines[0])
            assertEquals("# Group", lines[1])
            assertTrue(SystemFileSystem.exists(Path(tempDir, "mesh.mtl")))
        }
    }

    @Test
    fun toFilesNormalizesObjAndMtlExtensions() {
        withTempDirectory("kcsg-objfile") { tempDir ->
            val noExtensionBase = Path(tempDir, "mesh")
            val noExtensionObj = Cube(1.0).toCSG().toObj()
            noExtensionObj.toFiles(noExtensionBase)
            val noExtensionObjPath = Path(tempDir, "mesh.obj")
            val noExtensionMtlPath = Path(tempDir, "mesh.mtl")
            assertTrue(SystemFileSystem.exists(noExtensionObjPath))
            assertTrue(SystemFileSystem.exists(noExtensionMtlPath))
            assertTrue(FileUtil.read(noExtensionObjPath).contains("mtllib mesh.mtl"))

            val objExtensionBase = Path(tempDir, "mesh2.obj")
            val objExtensionObj = Cube(1.0).toCSG().toObj()
            objExtensionObj.toFiles(objExtensionBase)
            val objExtensionObjPath = Path(tempDir, "mesh2.obj")
            val objExtensionMtlPath = Path(tempDir, "mesh2.mtl")
            assertTrue(SystemFileSystem.exists(objExtensionObjPath))
            assertTrue(SystemFileSystem.exists(objExtensionMtlPath))
            assertTrue(FileUtil.read(objExtensionObjPath).contains("mtllib mesh2.mtl"))

            val mtlExtensionBase = Path(tempDir, "mesh3.mtl")
            val mtlExtensionObj = Cube(1.0).toCSG().toObj()
            mtlExtensionObj.toFiles(mtlExtensionBase)
            val mtlExtensionObjPath = Path(tempDir, "mesh3.obj")
            val mtlExtensionMtlPath = Path(tempDir, "mesh3.mtl")
            assertTrue(SystemFileSystem.exists(mtlExtensionObjPath))
            assertTrue(SystemFileSystem.exists(mtlExtensionMtlPath))
            assertTrue(FileUtil.read(mtlExtensionObjPath).contains("mtllib mesh3.mtl"))
        }
    }

    @Test
    fun toFilesSupportsPathsWithoutParentDirectory() {
        val baseName = "kcsg-obj-no-parent-${System.nanoTime()}"
        val objPath = Path("$baseName.obj")
        val mtlPath = Path("$baseName.mtl")

        try {
            Cube(1.0).toCSG().toObj().toFiles(objPath)
            assertTrue(SystemFileSystem.exists(objPath))
            assertTrue(SystemFileSystem.exists(mtlPath))
            assertTrue(FileUtil.read(objPath).contains("mtllib $baseName.mtl"))
        } finally {
            SystemFileSystem.delete(objPath, false)
            SystemFileSystem.delete(mtlPath, false)
        }
    }
}

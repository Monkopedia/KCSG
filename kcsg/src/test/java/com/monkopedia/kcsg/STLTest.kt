package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import com.monkopedia.kcsg.testutil.TestIoFixtures.byteLength
import com.monkopedia.kcsg.testutil.TestIoFixtures.sourceFactory
import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.io.files.Path

class STLTest {
    @Test
    fun stlFileLoadsCsgFromDisk() {
        withTempDirectory("kcsg-stl-file") { tempDir ->
            val stlPath = Path(tempDir, "cube.stl")
            val source = Cube(1.0).toCSG()
            FileUtil.toStlFile(stlPath, source)

            val loaded = STL.file(stlPath)
            assertTrue(loaded.polygons.isNotEmpty())
            assertVolumeClose(source.computeVolume(), loaded.computeVolume(), relativeTolerance = 1e-4)
        }
    }

    @Test
    fun stlFromLoadsCsgFromStreamFactory() {
        withTempDirectory("kcsg-stl-stream") { tempDir ->
            val stlPath = Path(tempDir, "cube.stl")
            val source = Cube(1.0).toCSG()
            FileUtil.toStlFile(stlPath, source)
            val stlText = FileUtil.read(stlPath)

            val loaded = STL.from(sourceFactory(stlText), byteLength(stlText))
            assertTrue(loaded.polygons.isNotEmpty())
            assertVolumeClose(source.computeVolume(), loaded.computeVolume(), relativeTolerance = 1e-4)

            val fromFile = STL.file(stlPath)
            assertEquals(fromFile.polygons.size, loaded.polygons.size)
        }
    }
}

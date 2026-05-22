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
    fun asciiStlRoundTripPreservesDoublePrecision() {
        // A cached intermediate CSG is reloaded via STL.file; if the loader truncates coordinates
        // to float32, the ~1e-7 error flips BSP plane-classification on the next boolean op and the
        // cache stops being transparent. The ASCII STL stores full double precision, so the
        // round-trip must preserve it.
        withTempDirectory("kcsg-stl-precision") { tempDir ->
            val stlPath = Path(tempDir, "tri.stl")
            val preciseX = 19.017248553744356 // not representable exactly as float32
            val tri = Polygon.fromPoints(
                listOf(
                    Vector3d.xyz(preciseX, 0.0, 0.0),
                    Vector3d.xyz(0.0, 1.0, 0.0),
                    Vector3d.xyz(0.0, 0.0, 1.0),
                )
            )
            FileUtil.toStlFile(stlPath, CSG.fromPolygons(listOf(tri)))

            val loaded = STL.file(stlPath)
            val loadedX = loaded.polygons.flatMap { it.vertices }.maxOf { it.pos.x }
            // float32 truncation would miss by ~4e-7; double parsing is exact.
            assertEquals(preciseX, loadedX, 1e-9)
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

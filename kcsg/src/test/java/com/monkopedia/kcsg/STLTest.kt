package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import com.monkopedia.kcsg.testutil.TestIoFixtures.byteLength
import com.monkopedia.kcsg.testutil.TestIoFixtures.sourceFactory
import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
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

    @Test
    fun binaryStlReadFullyHandlesReadsAcrossSegmentBoundaries() {
        // Regression: STLLoader.readFully passed `buffer.size - offset` as kotlinx-io
        // readAtMostTo's END INDEX (it expects an endIndex, not a length). A single-read source is
        // unaffected, but once a 50-byte triangle read straddles a kotlinx-io 8192-byte segment
        // boundary the read goes partial, offset advances, and the next call becomes
        // readAtMostTo(tri, 42, 8) -> startIndex > endIndex -> throws, aborting binary STL import.
        // 400 triangles (20084 bytes) crosses several segment boundaries and reproduces it.
        val triangleCount = 400
        val stl = buildBinaryStl(triangleCount)

        val loaded = STL.from(sourceFactory(stl), byteLength(stl))

        // Triangle t encodes its index as the x of all three vertices, so the full set of x-values
        // proves every triangle round-tripped with correct alignment — none dropped or mis-read
        // past a segment boundary.
        assertEquals(triangleCount, loaded.polygons.size)
        val xs = loaded.polygons.flatMap { it.vertices }.map { it.pos.x }.toSet()
        assertEquals((0 until triangleCount).map(Int::toDouble).toSet(), xs)
    }

    /**
     * Builds a little-endian binary STL with [triangleCount] triangles. Triangle t places all
     * three vertices at x = t (y/z vary so the triangle is non-degenerate), so the parsed geometry
     * encodes the triangle index for the alignment assertion above.
     */
    private fun buildBinaryStl(triangleCount: Int): ByteArray {
        val buf = ByteBuffer.allocate(84 + triangleCount * 50).order(ByteOrder.LITTLE_ENDIAN)
        repeat(80) { buf.put(0) } // 80-byte header (zeros)
        buf.putInt(triangleCount) // little-endian triangle count
        for (t in 0 until triangleCount) {
            val x = t.toFloat()
            buf.putFloat(0f).putFloat(0f).putFloat(0f) // normal (ignored by the loader)
            buf.putFloat(x).putFloat(0f).putFloat(0f) // v0 = (t, 0, 0)
            buf.putFloat(x).putFloat(1f).putFloat(0f) // v1 = (t, 1, 0)
            buf.putFloat(x).putFloat(0f).putFloat(1f) // v2 = (t, 0, 1)
            buf.putShort(0) // attribute byte count
        }
        return buf.array()
    }
}

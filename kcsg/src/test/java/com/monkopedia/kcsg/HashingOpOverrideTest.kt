package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.charset.StandardCharsets
import kotlinx.io.Buffer
import kotlinx.io.files.Path

class HashingOpOverrideTest {
    @Test
    fun hashingSequenceIsDeterministicForSameInputs() {
        withTempDirectory("kcsg-hash-det") { tempDir ->
            val existingPath = Path(tempDir, "existing.bin")
            FileUtil.write(existingPath, "content-a")
            val missingPath = Path(tempDir, "missing.bin")

            val fixedInputs = FixedHashInputs(
                csg = Cube(1.0).toCSG(),
                transform = Transform.unity().translate(1.0, 2.0, 3.0),
                vector = Vector3d.xyz(4.0, 5.0, 6.0),
                cube = Cube(2.0),
                cylinder = Cylinder(1.0, 2.0, 12),
                polyhedron = Polyhedron(
                    points = listOf(
                        Vector3d.xyz(0.0, 0.0, 0.0),
                        Vector3d.xyz(1.0, 0.0, 0.0),
                        Vector3d.xyz(0.0, 1.0, 0.0),
                        Vector3d.xyz(0.0, 0.0, 1.0),
                    ),
                    faces = listOf(
                        listOf(0, 2, 1),
                        listOf(0, 1, 3),
                        listOf(1, 2, 3),
                        listOf(2, 0, 3),
                    ),
                ),
                roundedCube = RoundedCube(cornerRadius = 0.2, resolution = 3),
                sphere = Sphere(1.0, 12, 6, Vector3d.ZERO),
                existingFile = existingPath,
                missingFile = missingPath,
                streamBytes = "stream-bytes".toByteArray(StandardCharsets.UTF_8),
            )

            val hash1 = runSequence(HashingOpOverride(), fixedInputs)
            val hash2 = runSequence(HashingOpOverride(), fixedInputs)
            assertEquals(hash1, hash2)
        }
    }

    @Test
    fun hashingChangesWhenInputsChangeIncludingFileAndStream() {
        withTempDirectory("kcsg-hash-diff") { tempDir ->
            val existingPath = Path(tempDir, "existing.bin")
            val missingPath = Path(tempDir, "missing.bin")

            FileUtil.write(existingPath, "content-a")
            val baseInputs = FixedHashInputs(
                csg = Cube(1.0).toCSG(),
                transform = Transform.unity().translate(1.0, 2.0, 3.0),
                vector = Vector3d.xyz(4.0, 5.0, 6.0),
                cube = Cube(2.0),
                cylinder = Cylinder(1.0, 2.0, 12),
                polyhedron = Polyhedron(),
                roundedCube = RoundedCube(cornerRadius = 0.2, resolution = 3),
                sphere = Sphere(1.0, 12, 6, Vector3d.ZERO),
                existingFile = existingPath,
                missingFile = missingPath,
                streamBytes = "stream-bytes".toByteArray(StandardCharsets.UTF_8),
            )
            val baselineHash = runSequence(HashingOpOverride(), baseInputs)

            val changedVectorHash = runSequence(
                HashingOpOverride(),
                baseInputs.copy(vector = Vector3d.xyz(40.0, 5.0, 6.0)),
            )
            assertNotEquals(baselineHash, changedVectorHash)

            FileUtil.write(existingPath, "content-b")
            val changedFileHash = runSequence(HashingOpOverride(), baseInputs)
            assertNotEquals(baselineHash, changedFileHash)

            val changedStreamHash = runSequence(
                HashingOpOverride(),
                baseInputs.copy(streamBytes = "different-stream".toByteArray(StandardCharsets.UTF_8)),
            )
            assertNotEquals(baselineHash, changedStreamHash)
        }
    }

    @Test
    fun unhashableTypeFailsLoud() {
        assertThrows(IllegalStateException::class.java) {
            HashingOpOverride().operation("fallback", UnknownType(123))
        }
    }

    @Test
    fun builtInWeightFunctionsAreHashedDistinctly() {
        fun hashOf(f: WeightFunction): String =
            HashingOpOverride().apply { operation("weighted", f) }.hash()

        // centered flag must change the hash
        assertNotEquals(hashOf(XModifier(false)), hashOf(XModifier(true)))
        // axis must change the hash
        assertNotEquals(hashOf(XModifier(false)), hashOf(YModifier(false)))
        assertNotEquals(hashOf(YModifier(false)), hashOf(ZModifier(false)))
        // type must change the hash
        assertNotEquals(hashOf(UnityModifier()), hashOf(XModifier(false)))
        // and the same modifier configuration hashes identically (content, not identity)
        assertEquals(hashOf(XModifier(true)), hashOf(XModifier(true)))
    }

    @Test
    fun customWeightFunctionFailsLoud() {
        val custom = WeightFunction { _, _ -> 1.0 }
        assertThrows(IllegalStateException::class.java) {
            HashingOpOverride().operation("weighted", custom)
        }
    }

    @Test
    fun polygonIsHashedByContentNotIdentity() {
        val points = listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
        )
        fun hashOf(p: Polygon): String =
            HashingOpOverride().apply { operation("combine", p) }.hash()

        // two distinct Polygon objects with identical geometry must hash equal
        assertEquals(hashOf(Polygon.fromPoints(points)), hashOf(Polygon.fromPoints(points)))
        // different geometry must hash differently
        val moved = listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(2.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
        )
        assertNotEquals(hashOf(Polygon.fromPoints(points)), hashOf(Polygon.fromPoints(moved)))
    }

    @Test
    fun polyhedronFaceGroupingAffectsHash() {
        val points = listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 0.0),
            Vector3d.xyz(0.0, 0.0, 1.0),
            Vector3d.xyz(1.0, 1.0, 0.0),
            Vector3d.xyz(0.0, 1.0, 1.0),
        )
        fun hashOf(p: Polyhedron): String =
            HashingOpOverride().apply { operation("plh", p) }.hash()

        val twoFaces = Polyhedron(points = points, faces = listOf(listOf(0, 1, 2), listOf(3, 4, 5)))
        val oneFace = Polyhedron(points = points, faces = listOf(listOf(0, 1, 2, 3, 4, 5)))
        assertNotEquals(hashOf(twoFaces), hashOf(oneFace))
    }

    @Test
    fun cubeCenteredFlagChangesHash() {
        fun hashOf(cube: Cube): String =
            HashingOpOverride().apply { operation("test", cube) }.hash()

        val centered = hashOf(Cube(9.5, 7.5, 3.5))
        val notCentered = hashOf(Cube(9.5, 7.5, 3.5).also { it.centered = false })
        assertNotEquals(centered, notCentered)
    }

    // #79: a String argument was written as raw UTF-8 with no length framing, so moving the
    // boundary between two adjacent strings produced the same byte stream and the same key.
    @Test
    fun adjacentStringsAreLengthFramed() {
        fun hashOf(name: String, arg: String): String =
            HashingOpOverride().apply { operation(name, arg) }.hash()

        assertNotEquals(hashOf("stl:part", "12:34"), hashOf("stl:part1", "2:34"))
        assertNotEquals(hashOf("ab", "c"), hashOf("a", "bc"))
        assertNotEquals(hashOf("abc", ""), hashOf("ab", "c"))
    }

    // #79: the argument count of an operation was not framed, so a String argument could not be
    // told apart from the name of the next operation.
    @Test
    fun operationArgumentBoundaryIsFramed() {
        val oneOp = HashingOpOverride().apply { operation("a", "b") }.hash()
        val twoOps = HashingOpOverride().apply {
            operation("a")
            operation("b")
        }.hash()
        assertNotEquals(oneOp, twoOps)
    }

    // #79: stream/file content was unframed, so bytes could shift between a source and the
    // operation that follows it.
    @Test
    fun sourceContentIsLengthFramed() {
        fun hashOf(content: String, nextOp: String): String = HashingOpOverride().apply {
            source { Buffer().apply { write(content.encodeToByteArray()) } }
            operation(nextOp)
        }.hash()
        assertNotEquals(hashOf("abc", "def"), hashOf("abcd", "ef"))
    }

    // #79: a CSG that was not produced by the hashing pass (built with real geometry outside it)
    // was keyed by its slot in a lookup list, so any two such solids collided.
    @Test
    fun foreignCsgIsKeyedByGeometryNotLookupIndex() {
        val cube = CSG.withOverride(null) { Cube(1.0).toCSG() }
        val sphere = CSG.withOverride(null) { Sphere(1.0, 32, 16, Vector3d.ZERO).toCSG() }
        val movedCube = CSG.withOverride(null) {
            Cube(1.0).toCSG().transformed(Transform.unity().translate(1.0, 0.0, 0.0))
        }
        fun hashOf(csg: CSG): String =
            HashingOpOverride().apply { operation("union", csg) }.hash()

        assertNotEquals(hashOf(cube), hashOf(sphere))
        assertNotEquals(hashOf(cube), hashOf(movedCube))
        // hit path: distinct objects with identical geometry share a key
        assertEquals(
            hashOf(cube),
            hashOf(CSG.withOverride(null) { Cube(1.0).toCSG() }),
        )
    }

    // A placeholder returned by an earlier operation in the same pass is a reference to that
    // operation's result; it must stay distinct from a foreign solid that happens to be empty.
    @Test
    fun placeholderReferenceDiffersFromForeignEmptyCsg() {
        val viaPlaceholder = HashingOpOverride().apply {
            val placeholder = operation("cube")
            operation("union", placeholder)
        }.hash()
        val viaForeign = HashingOpOverride().apply {
            operation("cube")
            operation("union", CSG.withOverride(null) { CSG.fromPolygons() })
        }.hash()
        assertNotEquals(viaPlaceholder, viaForeign)

        // and which earlier result is referenced matters
        val refFirst = HashingOpOverride().apply {
            val a = operation("a")
            operation("b")
            operation("use", a)
        }.hash()
        val refSecond = HashingOpOverride().apply {
            operation("a")
            val b = operation("b")
            operation("use", b)
        }.hash()
        assertNotEquals(refFirst, refSecond)
    }

    private fun runSequence(override: HashingOpOverride, inputs: FixedHashInputs): String {
        override.operation(
            "operation",
            inputs.csg,
            inputs.transform,
            inputs.vector,
            inputs.cube,
            inputs.cylinder,
            inputs.polyhedron,
            inputs.roundedCube,
            inputs.sphere,
        )
        override.bounds("bounds", inputs.csg, inputs.vector)
        override.double("double", inputs.transform, inputs.vector)
        override.file(inputs.existingFile)
        override.file(inputs.missingFile)
        override.source {
            Buffer().apply {
                write(inputs.streamBytes, 0, inputs.streamBytes.size)
            }
        }
        return override.hash()
    }

    private data class FixedHashInputs(
        val csg: CSG,
        val transform: Transform,
        val vector: Vector3d,
        val cube: Cube,
        val cylinder: Cylinder,
        val polyhedron: Polyhedron,
        val roundedCube: RoundedCube,
        val sphere: Sphere,
        val existingFile: Path,
        val missingFile: Path,
        val streamBytes: ByteArray,
    )

    @Test
    fun hashGoldenValuesAreStable() {
        fun hashOf(vararg args: Any?): String {
            val override = HashingOpOverride()
            override.operation("test", *args)
            return override.hash()
        }

        assertEquals(
            "c82651946692aa884043eaa0fb3b82d0c8c3bda1e8323d4bd6780d3bc70dda2c",
            hashOf(Cube(2.0)),
        )
        assertEquals(
            "ffe93d90d5374fd46ee1535e903ccd96eb4f39729718b6b8c5e04c324204e63a",
            hashOf(Cylinder(1.0, 2.0, 16)),
        )
        assertEquals(
            "57b3d3ccf1e01dd41143649d3d1c474ee7b5d50c7f1bbebee91c01385738279d",
            hashOf(Sphere(1.0, 16, 8, Vector3d.ZERO)),
        )
        assertEquals(
            "f9ddefb837e9e06a959f6121ca7e1434fece4112df1c2c6a908bff7041bac69e",
            hashOf(RoundedCube(cornerRadius = 0.3, resolution = 4)),
        )
        assertEquals(
            "d3bdd525c8d4e3218882b13b3e4beaa5ca4c030c3d6e5dfc3ce639703a7599c1",
            hashOf(
                Polyhedron(
                    points = listOf(
                        Vector3d.xyz(0.0, 0.0, 0.0),
                        Vector3d.xyz(1.0, 0.0, 0.0),
                        Vector3d.xyz(0.0, 1.0, 0.0),
                        Vector3d.xyz(0.0, 0.0, 1.0),
                    ),
                    faces = listOf(
                        listOf(0, 2, 1),
                        listOf(0, 1, 3),
                        listOf(1, 2, 3),
                        listOf(2, 0, 3),
                    ),
                ),
            ),
        )
        assertEquals(
            "bfeae79a7d3a4c17e24c0620dc6cae114238eb50eef2fd6cd1f85c3a0c74cfbe",
            hashOf(Transform.unity().translate(1.0, 2.0, 3.0)),
        )
        assertEquals(
            "d5fcd8e199ad899d722d3ef699b59f2b60cc192d990618f880f5a18a9ede4338",
            hashOf(Vector3d.xyz(4.0, 5.0, 6.0)),
        )
        // pins the String and CSG-geometry encodings (both changed for #79)
        assertEquals(
            "957086f2b22604bfef5621feb53bc5810dc29134b28c9cc0f1422a45b1cf2087",
            hashOf("name", CSG.withOverride(null) { Cube(1.0).toCSG() }),
        )
    }

    private data class UnknownType(
        val value: Int,
    )
}

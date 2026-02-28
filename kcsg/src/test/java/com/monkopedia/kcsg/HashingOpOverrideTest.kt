package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun hashingFallbackTypeBranchIsDeterministic() {
        val hashA = HashingOpOverride().apply {
            operation("fallback", UnknownType(123))
        }.hash()
        val hashB = HashingOpOverride().apply {
            operation("fallback", UnknownType(123))
        }.hash()
        val hashC = HashingOpOverride().apply {
            operation("fallback", UnknownType(456))
        }.hash()

        assertEquals(hashA, hashB)
        assertNotEquals(hashA, hashC)
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
            "a8db8d9b8b04337a54f91ea09768e06679eee60fd540002492b5c96d777989c3",
            hashOf(Cube(2.0)),
        )
        assertEquals(
            "e43fef71f165ae291e1b12cf966466557906ad7136d482f20c1f1cac15c63656",
            hashOf(Cylinder(1.0, 2.0, 16)),
        )
        assertEquals(
            "262b0d3f90fba47fcf4f3e3ec6c7429c1aceba7cf929c9202727f0fc86199e2e",
            hashOf(Sphere(1.0, 16, 8, Vector3d.ZERO)),
        )
        assertEquals(
            "804169ad7d4fcec7ae7012b03d7637a5839255f1a77b4e5c54559d659d420f91",
            hashOf(RoundedCube(cornerRadius = 0.3, resolution = 4)),
        )
        assertEquals(
            "461ea95b34d5e7e1359201dfa6de5fe31246318558fb39b9971e0f6044902cf6",
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
            "ca31103c117b924b751e3e8dcd3000560e52142a103c8ebd64639fb0e2675948",
            hashOf(Transform.unity().translate(1.0, 2.0, 3.0)),
        )
        assertEquals(
            "dae0aca332b317136ad6e6b00174aec618f8089a9cc242f9972cbe781d71b8bb",
            hashOf(Vector3d.xyz(4.0, 5.0, 6.0)),
        )
    }

    private data class UnknownType(
        val value: Int,
    )
}

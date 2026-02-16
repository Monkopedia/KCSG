package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.TestIoFixtures.withTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

class HashingOpOverrideTest {
    @Test
    fun hashingSequenceIsDeterministicForSameInputs() {
        withTempDirectory("kcsg-hash-det") { tempDir ->
            val existingPath = tempDir.resolve("existing.bin")
            FileUtil.write(existingPath, "content-a")
            val missingPath = tempDir.resolve("missing.bin")

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
            val existingPath = tempDir.resolve("existing.bin")
            val missingPath = tempDir.resolve("missing.bin")

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
        override.inputStream { ByteArrayInputStream(inputs.streamBytes) }
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
        val existingFile: java.nio.file.Path,
        val missingFile: java.nio.file.Path,
        val streamBytes: ByteArray,
    )
}

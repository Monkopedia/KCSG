package com.monkopedia.kcsg.oracle

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.STL
import com.monkopedia.kcsg.Vector3d
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.abs

class OpenScadTiltedExtrudeRegressionTest {
    private val maxSymmetricDifferenceRatio = 0.05

    @Test
    fun tiltedExtrudeMatchesOpenScadOracle() {
        val dir = Vector3d.xyz(1.2, 0.4, 2.0)
        val points = listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.4, 0.2, 0.0),
            Vector3d.xyz(1.1, 1.3, 0.0),
            Vector3d.xyz(0.2, 0.9, 0.0)
        )
        val actual = CSG.fromPolygons(
            Extrude.points(
                dir = dir,
                top = true,
                bottom = true,
                points1 = points
            )
        )

        val expected = renderOpenScadTiltedExtrude(points, dir, "cgal")
        val expectedVolume = abs(expected.computeVolume())
        val symmetricDifference = expected.difference(actual).union(actual.difference(expected))
        val symmetricDifferenceRatio = abs(symmetricDifference.computeVolume()) / expectedVolume

        assertTrue(
            "tilted extrude symmetric difference should stay <= $maxSymmetricDifferenceRatio " +
                "(ratio=$symmetricDifferenceRatio)",
            symmetricDifferenceRatio <= maxSymmetricDifferenceRatio
        )
    }

    private fun renderOpenScadTiltedExtrude(
        points: List<Vector3d>,
        dir: Vector3d,
        backend: String
    ): CSG {
        val workDir = Files.createTempDirectory("kcsg-oracle-tilted-extrude")
        try {
            val scadPath = workDir.resolve("tilted_extrude.scad")
            val stlPath = workDir.resolve("tilted_extrude.stl")
            Files.writeString(scadPath, scadProgram(points, dir), StandardCharsets.UTF_8)
            runOpenScad(scadPath, stlPath, backend)
            return STL.file(stlPath)
        } finally {
            workDir.toFile().deleteRecursively()
        }
    }

    private fun scadProgram(points: List<Vector3d>, dir: Vector3d): String {
        val pointsLiteral = points.joinToString(", ") { "[${it.x}, ${it.y}]" }
        return """
            pts = [$pointsLiteral];
            dir = [${dir.x}, ${dir.y}, ${dir.z}];
            h = norm(dir);
            axis = cross([0, 0, 1], dir);
            a = acos(dir[2] / h);

            module tilted_extrude() {
              if (norm(axis) < 1e-9) {
                linear_extrude(height = h, center = false, convexity = 10)
                    polygon(points = pts);
              } else {
                rotate(a = a, v = axis)
                    linear_extrude(height = h, center = false, convexity = 10)
                        polygon(points = pts);
              }
            }

            tilted_extrude();
        """.trimIndent()
    }

    private fun runOpenScad(scadPath: Path, outputPath: Path, backend: String) {
        val bin = openScadBinary()
        val process = ProcessBuilder(
            bin.toString(),
            "--backend=$backend",
            "-o",
            outputPath.toString(),
            scadPath.toString()
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }
        val exit = process.waitFor()
        check(exit == 0) {
            "OpenSCAD failed with exit code $exit:\n$output"
        }
        check(Files.isRegularFile(outputPath)) {
            "OpenSCAD did not produce output STL at $outputPath"
        }
    }

    private fun openScadBinary(): Path {
        val candidates = listOf(
            Paths.get(".tools", "openscad", "openscad"),
            Paths.get("..", ".tools", "openscad", "openscad"),
            Paths.get("..", "..", ".tools", "openscad", "openscad")
        )
        return candidates.firstOrNull { Files.isRegularFile(it) && Files.isExecutable(it) }
            ?: error("OpenSCAD binary not found. Run ./gradlew :kcsg:oracleGenerateFixtures first.")
    }
}

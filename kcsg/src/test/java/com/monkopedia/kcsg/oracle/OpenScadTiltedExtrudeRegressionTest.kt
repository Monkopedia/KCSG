package com.monkopedia.kcsg.oracle

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Extrude
import com.monkopedia.kcsg.STL
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

class OpenScadTiltedExtrudeRegressionTest {
    private val maxVertexDistance = 1e-3

    /**
     * Upper bound on a single OpenSCAD render. The tilted extrude is a six-vertex polyhedron
     * that renders in well under a second; the heaviest scenario in the fixture set
     * (`many_reductions_union`) takes about five. Two minutes leaves roughly twenty times the
     * headroom of the slowest known render for slow runners and cold AppImage extraction,
     * while turning a wedged renderer into a failure in minutes rather than a job that runs
     * until the CI time limit.
     */
    private val openScadTimeoutSeconds = 120L

    /** Grace period for the destroyed process to be reaped before giving up on it. */
    private val destroyGraceSeconds = 10L

    @Test
    fun tiltedExtrudeMatchesOpenScadOracle() {
        val dir = Vector3d.xyz(1.2, 0.4, 2.0)
        val points = listOf(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.6, 0.1, 0.0),
            Vector3d.xyz(0.3, 1.3, 0.0)
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
        val expectedPoints = uniquePoints(expected)
        val actualPoints = uniquePoints(actual)
        val expectedToActual = directedMaxNearestDistance(expectedPoints, actualPoints)
        val actualToExpected = directedMaxNearestDistance(actualPoints, expectedPoints)
        val maxDistance = maxOf(expectedToActual, actualToExpected)

        assertTrue(
            "tilted extrude vertex distance should stay <= $maxVertexDistance " +
                "(maxDistance=$maxDistance expectedVertices=${expectedPoints.size} " +
                "actualVertices=${actualPoints.size} expectedPolygons=${expected.polygons.size} " +
                "actualPolygons=${actual.polygons.size})",
            maxDistance <= maxVertexDistance
        )
    }

    private fun renderOpenScadTiltedExtrude(
        points: List<Vector3d>,
        dir: Vector3d,
        backend: String
    ): CSG {
        val topPoints = rotatedTop(points, dir)
        val workDir = Files.createTempDirectory("kcsg-oracle-tilted-extrude")
        try {
            val scadPath = workDir.resolve("tilted_extrude.scad")
            val stlPath = workDir.resolve("tilted_extrude.stl")
            Files.newBufferedWriter(scadPath, StandardCharsets.UTF_8).use { writer ->
                writer.write(scadProgram(points, topPoints))
            }
            runOpenScad("tilted_extrude", scadPath, stlPath, backend)
            return STL.file(kotlinx.io.files.Path(stlPath.toString()))
        } finally {
            workDir.toFile().deleteRecursively()
        }
    }

    private fun scadProgram(bottomPoints: List<Vector3d>, topPoints: List<Vector3d>): String {
        require(bottomPoints.size == topPoints.size) {
            "Bottom and top point counts must match"
        }
        val n = bottomPoints.size
        val pointLiterals = (bottomPoints + topPoints).joinToString(", ") { p ->
            "[${p.x}, ${p.y}, ${p.z}]"
        }

        val faces = mutableListOf<String>()

        // Bottom face (normal points opposite top face).
        val bottom = (0 until n).reversed().toList()
        for (i in 1 until n - 1) {
            faces += "[${bottom[0]}, ${bottom[i]}, ${bottom[i + 1]}]"
        }
        // Top face.
        val top = (0 until n).map { n + it }
        for (i in 1 until n - 1) {
            faces += "[${top[0]}, ${top[i]}, ${top[i + 1]}]"
        }
        // Side faces.
        for (i in 0 until n) {
            val next = (i + 1) % n
            val b1 = i
            val b2 = next
            val t1 = n + i
            val t2 = n + next
            faces += "[$b2, $t2, $t1]"
            faces += "[$b2, $t1, $b1]"
        }
        val faceLiterals = faces.joinToString(",\n    ")

        return """
            polyhedron(
              points = [$pointLiterals],
              faces = [
                $faceLiterals
              ],
              convexity = 10
            );
        """.trimIndent()
    }

    private fun rotatedTop(points: List<Vector3d>, dir: Vector3d): List<Vector3d> {
        val translated = points.map { it.plus(dir) }
        val a = Vector3d.Z_ONE
        val b = dir.normalized()
        val c = a.crossed(b)
        val l = c.magnitude()
        if (l <= 1e-9) {
            return translated
        }
        val axis = c.times(1.0 / l)
        val angle = a.angle(b)
        val center = translated
            .reduce { acc, p -> acc.plus(p) }
            .times(1.0 / translated.size)
        val rotation = Transform.unity().rot(center, axis, angle)
        return translated.map { point -> rotation.transform(point) }
    }

    /**
     * Runs OpenSCAD with a hard upper bound on the whole interaction.
     *
     * Both stdout and stderr are redirected to a file rather than a pipe, so there is no
     * unbounded read-to-EOF on the way to [Process.waitFor]. That leaves the bounded
     * `waitFor` as the only blocking call, and a wedged renderer produces a diagnosable
     * failure instead of a job that runs until the CI time limit.
     */
    private fun runOpenScad(
        scenario: String,
        scadPath: Path,
        outputPath: Path,
        backend: String
    ) {
        val bin = openScadBinary()
        val logPath = scadPath.resolveSibling("openscad-output.log")
        val process = ProcessBuilder(
            bin.toString(),
            "--backend=$backend",
            "-o",
            outputPath.toString(),
            scadPath.toString()
        ).redirectErrorStream(true)
            .redirectOutput(logPath.toFile())
            .start()

        val finished = try {
            process.waitFor(openScadTimeoutSeconds, TimeUnit.SECONDS)
        } catch (interrupted: InterruptedException) {
            process.destroyForcibly()
            Thread.currentThread().interrupt()
            throw interrupted
        }
        if (!finished) {
            process.destroyForcibly()
            process.waitFor(destroyGraceSeconds, TimeUnit.SECONDS)
            error(
                "OpenSCAD timed out after ${openScadTimeoutSeconds}s and was destroyed " +
                    "(scenario=$scenario backend=$backend scad=$scadPath). " +
                    "Partial output:\n${readOpenScadLog(logPath)}"
            )
        }

        val output = readOpenScadLog(logPath)
        if (output.isNotBlank()) {
            println("OpenSCAD output:\n$output")
        }
        val exit = process.exitValue()
        check(exit == 0) {
            "OpenSCAD failed with exit code $exit " +
                "(scenario=$scenario backend=$backend):\n$output"
        }
        check(Files.isRegularFile(outputPath)) {
            "OpenSCAD did not produce output STL at $outputPath " +
                "(scenario=$scenario backend=$backend)"
        }
    }

    private fun readOpenScadLog(logPath: Path): String {
        if (!Files.isRegularFile(logPath)) {
            return ""
        }
        return runCatching { logPath.toFile().readText(StandardCharsets.UTF_8) }
            .getOrElse { failure -> "<unable to read $logPath: $failure>" }
    }

    private fun uniquePoints(csg: CSG): List<Vector3d> {
        val points = mutableListOf<Vector3d>()
        val dedupeEpsilon = 1e-6
        for (polygon in csg.polygons) {
            for (vertex in polygon.vertices) {
                if (points.none { it.minus(vertex.pos).magnitude() <= dedupeEpsilon }) {
                    points += vertex.pos
                }
            }
        }
        return points
    }

    private fun directedMaxNearestDistance(
        source: List<Vector3d>,
        target: List<Vector3d>
    ): Double {
        return source.maxOf { p ->
            target.minOf { q -> p.minus(q).magnitude() }
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

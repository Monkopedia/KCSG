package com.monkopedia.kcsg.oracle

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cube
import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.Sphere
import com.monkopedia.kcsg.STL
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.abs

class OpenScadRemeshOracleAgreementTest {
    private data class OracleScenario(
        val name: String,
        val oracleRelativeVolumeTolerance: Double,
        val oracleBoundsTolerance: Double,
        val remeshRelativeVolumeTolerance: Double,
        val remeshBoundsTolerance: Double,
        val maxOracleDistanceRegression: Double,
        val builder: () -> CSG
    )

    private val scenarios = listOf(
        OracleScenario("disjoint_union", 0.08, 0.03, 0.01, 0.005, 1e-3, ::disjointUnion),
        OracleScenario("overlap_intersection", 0.08, 0.03, 0.01, 0.005, 1e-3, ::overlapIntersection),
        OracleScenario("containment_difference", 0.06, 0.03, 0.01, 0.005, 1e-3, ::containmentDifference),
        OracleScenario("face_tangent_union", 0.01, 0.01, 0.005, 0.003, 5e-4, ::faceTangentUnion),
        OracleScenario("edge_tangent_union", 0.01, 0.01, 0.005, 0.003, 5e-4, ::edgeTangentUnion),
        OracleScenario("vertex_tangent_union", 0.01, 0.01, 0.005, 0.003, 5e-4, ::vertexTangentUnion),
        OracleScenario("offset_cylinder_union", 0.08, 0.03, 0.01, 0.005, 1e-3, ::offsetCylinderUnion),
        OracleScenario("many_reductions_union", 0.10, 0.05, 0.02, 0.01, 2e-3, ::manyReductionsUnion)
    )
    private val quickMode = java.lang.Boolean.getBoolean("kcsg.oracle.quick")
    private val quickExcludedScenarios = setOf("many_reductions_union")
    private val quickDistancePointLimit = 800

    @Before
    fun configureDefaults() {
        CSG.setDefaultOptType(CSG.OptType.NONE)
    }

    @Test
    fun cgalRemeshOracleAgreement() {
        assertBackendAgreement("cgal")
    }

    @Test
    fun manifoldRemeshOracleAgreement() {
        assertBackendAgreement("manifold")
    }

    private fun assertBackendAgreement(backend: String) {
        val oracleDir = fixtureRoot().resolve(backend)
        assertTrue(
            "Missing oracle backend directory: $oracleDir. Run ./gradlew :kcsg:oracleGenerateFixtures",
            Files.isDirectory(oracleDir)
        )

        val scenariosToRun = if (quickMode) {
            scenarios.filterNot { it.name in quickExcludedScenarios }
        } else {
            scenarios
        }

        for (scenario in scenariosToRun) {
            val referencePath = oracleDir.resolve("${scenario.name}.stl")
            assertTrue("Missing oracle fixture: $referencePath", Files.isRegularFile(referencePath))

            val oracle = STL.file(referencePath)
            val baseline = scenario.builder()
            val remeshed = applyRemesh(baseline)

            assertFiniteMesh(oracle)
            assertFiniteMesh(baseline)
            assertFiniteMesh(remeshed)

            assertVolumeClose(
                expected = abs(oracle.computeVolume()),
                actual = abs(remeshed.computeVolume()),
                relativeTolerance = scenario.oracleRelativeVolumeTolerance,
                absoluteTolerance = 1e-5,
                message = "backend=$backend scenario=${scenario.name} remesh-vs-oracle"
            )
            assertBoundsClose(
                expected = oracle.bounds,
                actual = remeshed.bounds,
                tolerance = scenario.oracleBoundsTolerance,
                message = "backend=$backend scenario=${scenario.name} remesh-vs-oracle"
            )

            assertVolumeClose(
                expected = abs(baseline.computeVolume()),
                actual = abs(remeshed.computeVolume()),
                relativeTolerance = scenario.remeshRelativeVolumeTolerance,
                absoluteTolerance = 1e-6,
                message = "backend=$backend scenario=${scenario.name} remesh-vs-baseline"
            )
            assertBoundsClose(
                expected = baseline.bounds,
                actual = remeshed.bounds,
                tolerance = scenario.remeshBoundsTolerance,
                message = "backend=$backend scenario=${scenario.name} remesh-vs-baseline"
            )

            val distancePointLimit = if (quickMode) quickDistancePointLimit else Int.MAX_VALUE
            val baselineDistance = symmetricNearestPointDistance(baseline, oracle, distancePointLimit)
            val remeshedDistance = symmetricNearestPointDistance(remeshed, oracle, distancePointLimit)
            assertTrue(
                "backend=$backend scenario=${scenario.name} remesh should not regress oracle distance " +
                    "(baselineDistance=$baselineDistance remeshedDistance=$remeshedDistance " +
                    "maxRegression=${scenario.maxOracleDistanceRegression})",
                remeshedDistance <= baselineDistance + scenario.maxOracleDistanceRegression
            )
        }
    }

    private fun applyRemesh(source: CSG): CSG {
        return source.remesh()
    }

    private fun symmetricNearestPointDistance(a: CSG, b: CSG, pointLimit: Int): Double {
        val aPoints = samplePoints(uniquePoints(a), pointLimit)
        val bPoints = samplePoints(uniquePoints(b), pointLimit)
        val aToB = directedMaxNearestDistance(aPoints, bPoints)
        val bToA = directedMaxNearestDistance(bPoints, aPoints)
        return maxOf(aToB, bToA)
    }

    private fun samplePoints(points: List<Vector3d>, limit: Int): List<Vector3d> {
        if (limit <= 0 || points.size <= limit) {
            return points
        }

        val result = ArrayList<Vector3d>(limit)
        val scale = points.size.toDouble() / limit.toDouble()
        for (i in 0 until limit) {
            val index = (i * scale).toInt().coerceAtMost(points.size - 1)
            result += points[index]
        }
        return result
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

    private fun directedMaxNearestDistance(source: List<Vector3d>, target: List<Vector3d>): Double {
        return source.maxOf { p ->
            target.minOf { q -> p.minus(q).magnitude() }
        }
    }

    private fun fixtureRoot(): Path {
        val localBuild = Paths.get("build", "oracle-fixtures")
        if (Files.isDirectory(localBuild)) {
            return localBuild
        }

        val rootBuild = Paths.get("kcsg", "build", "oracle-fixtures")
        return if (Files.isDirectory(rootBuild)) rootBuild else localBuild
    }

    private fun disjointUnion(): CSG {
        return cube(-2.0, 0.0, 0.0, 2.0).union(
            sphere(2.0, 0.0, 0.0, 1.0)
        )
    }

    private fun overlapIntersection(): CSG {
        return cube(0.0, 0.0, 0.0, 2.0).intersect(
            sphere(0.4, 0.0, 0.0, 1.35)
        )
    }

    private fun containmentDifference(): CSG {
        return cube(0.0, 0.0, 0.0, 3.0).difference(
            sphere(0.0, 0.0, 0.0, 0.75)
        )
    }

    private fun faceTangentUnion(): CSG {
        return cube(-1.0, 0.0, 0.0, 2.0).union(
            cube(1.0, 0.0, 0.0, 2.0)
        )
    }

    private fun edgeTangentUnion(): CSG {
        return cube(-1.0, -1.0, 0.0, 2.0).union(
            cube(1.0, 1.0, 0.0, 2.0)
        )
    }

    private fun vertexTangentUnion(): CSG {
        return cube(-1.0, -1.0, -1.0, 2.0).union(
            cube(1.0, 1.0, 1.0, 2.0)
        )
    }

    private fun offsetCylinderUnion(): CSG {
        return sphere(0.0, 0.0, 0.0, 1.2).union(
            centeredCylinder(0.35, -0.15, 0.0, 2.6, 0.55)
        )
    }

    private fun manyReductionsUnion(): CSG {
        val parts = mutableListOf<CSG>()

        for (i in -1..1) {
            for (j in -1..1) {
                parts += sphere(i * 0.9, j * 0.9, 0.0, 0.45)
            }
        }

        for (i in -1..1) {
            parts += centeredCylinder(i * 1.2, 0.0, 0.0, 2.0, 0.3)
            parts += centeredCylinder(0.0, i * 1.2, 0.0, 2.0, 0.3)
        }

        return parts.reduce { acc, csg -> acc.union(csg) }
    }

    private fun cube(x: Double, y: Double, z: Double, size: Double): CSG {
        return Cube(
            center = Vector3d.xyz(x, y, z),
            dimensions = Vector3d.xyz(size, size, size)
        ).toCSG()
    }

    private fun sphere(x: Double, y: Double, z: Double, radius: Double): CSG {
        return Sphere(radius, 32, 16, Vector3d.xyz(x, y, z)).toCSG()
    }

    private fun centeredCylinder(
        x: Double,
        y: Double,
        z: Double,
        height: Double,
        radius: Double
    ): CSG {
        val uncentered = Cylinder(radius, height, 32).toCSG()
        return uncentered.transformed(
            Transform.unity().translate(x, y, z - (height / 2.0))
        )
    }
}

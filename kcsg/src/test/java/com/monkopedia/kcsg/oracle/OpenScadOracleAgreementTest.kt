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

class OpenScadOracleAgreementTest {
    private data class OracleScenario(
        val name: String,
        val relativeVolumeTolerance: Double,
        val boundsTolerance: Double,
        val builder: () -> CSG
    )

    private val scenarios = listOf(
        OracleScenario("disjoint_union", 0.08, 0.03, ::disjointUnion),
        OracleScenario("overlap_intersection", 0.08, 0.03, ::overlapIntersection),
        OracleScenario("containment_difference", 0.06, 0.03, ::containmentDifference),
        OracleScenario("face_tangent_union", 0.01, 0.01, ::faceTangentUnion),
        OracleScenario("edge_tangent_union", 0.01, 0.01, ::edgeTangentUnion),
        OracleScenario("vertex_tangent_union", 0.01, 0.01, ::vertexTangentUnion),
        OracleScenario("offset_cylinder_union", 0.08, 0.03, ::offsetCylinderUnion),
        OracleScenario("many_reductions_union", 0.10, 0.05, ::manyReductionsUnion)
    )

    @Before
    fun configureDefaults() {
        CSG.setDefaultOptType(CSG.OptType.NONE)
    }

    @Test
    fun cgalOracleAgreesWithKcsg() {
        assertBackendAgreement("cgal")
    }

    @Test
    fun manifoldOracleAgreesWithKcsg() {
        assertBackendAgreement("manifold")
    }

    private fun assertBackendAgreement(backend: String) {
        val oracleDir = fixtureRoot().resolve(backend)
        assertTrue(
            "Missing oracle backend directory: $oracleDir. Run ./gradlew :kcsg:oracleGenerateFixtures",
            Files.isDirectory(oracleDir)
        )

        for (scenario in scenarios) {
            val referencePath = oracleDir.resolve("${scenario.name}.stl")
            assertTrue("Missing oracle fixture: $referencePath", Files.isRegularFile(referencePath))

            val expected = STL.file(kotlinx.io.files.Path(referencePath.toString()))
            val actual = scenario.builder()
            val expectedVolume = abs(expected.computeVolume())
            val actualVolume = abs(actual.computeVolume())

            assertFiniteMesh(expected)
            assertFiniteMesh(actual)
            assertVolumeClose(
                expected = expectedVolume,
                actual = actualVolume,
                relativeTolerance = scenario.relativeVolumeTolerance,
                absoluteTolerance = 1e-5,
                message = "backend=$backend scenario=${scenario.name}"
            )
            assertBoundsClose(
                expected = expected.bounds,
                actual = actual.bounds,
                tolerance = scenario.boundsTolerance,
                message = "backend=$backend scenario=${scenario.name}"
            )
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

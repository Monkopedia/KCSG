package com.monkopedia.kcsg.oracle

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Cylinder
import com.monkopedia.kcsg.Sphere
import com.monkopedia.kcsg.STL
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class OpenScadOracleRegressionTest {
    private val maxInflationRatio = 6.5

    @Before
    fun configureDefaults() {
        CSG.setDefaultOptType(CSG.OptType.NONE)
    }

    @Test
    fun manyReductionsUnionDoesNotExplodePolygonCountAgainstOracle() {
        val actual = manyReductionsUnion()
        val fixtureRoot = fixtureRoot()

        for (backend in listOf("cgal", "manifold")) {
            val referencePath = fixtureRoot.resolve(backend).resolve("many_reductions_union.stl")
            assertTrue("Missing oracle fixture: $referencePath", Files.isRegularFile(referencePath))

            val expected = STL.file(kotlinx.io.files.Path(referencePath.toString()))
            val expectedPolygonCount = expected.polygons.size
            val actualPolygonCount = actual.polygons.size
            val inflationRatio = actualPolygonCount.toDouble() / expectedPolygonCount.toDouble()

            assertTrue(
                "backend=$backend scenario=many_reductions_union expected polygons to remain " +
                    "within ${maxInflationRatio}x oracle (expected=$expectedPolygonCount actual=$actualPolygonCount " +
                    "ratio=$inflationRatio)",
                inflationRatio <= maxInflationRatio
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

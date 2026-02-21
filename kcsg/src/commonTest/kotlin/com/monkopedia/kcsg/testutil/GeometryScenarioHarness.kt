package com.monkopedia.kcsg.testutil

import com.monkopedia.kcsg.CSG
import kotlin.test.fail

class GeometryScenarioHarness(
    private val pairName: String,
    private val leftFactory: () -> CSG,
    private val rightFactory: () -> CSG,
) {
    data class ScenarioResult(
        val pairName: String,
        val scenarioId: String,
        val optType: CSG.OptType,
        val left: CSG,
        val right: CSG,
        val union: CSG,
        val intersection: CSG,
        val difference: CSG,
    ) {
        val leftVolume: Double = left.computeVolume()
        val rightVolume: Double = right.computeVolume()
        val unionVolume: Double = union.computeVolume()
        val intersectionVolume: Double = intersection.computeVolume()
        val differenceVolume: Double = difference.computeVolume()
        val sumVolume: Double = leftVolume + rightVolume
    }

    fun run(
        scenarioId: String,
        pairFactory: (CSG.OptType, () -> CSG, () -> CSG) -> Pair<CSG, CSG>,
        assertion: (ScenarioResult) -> Unit,
    ): Map<CSG.OptType, ScenarioResult> {
        return CSG.OptType.entries.associateWith { optType ->
            val (leftRaw, rightRaw) = pairFactory(optType, leftFactory, rightFactory)
            val left = leftRaw.copy().optimization(optType)
            val right = rightRaw.copy().optimization(optType)

            val union = left.copy().union(right.copy())
            val intersection = left.copy().intersect(right.copy())
            val difference = left.copy().difference(right.copy())

            assertMeshSanity(
                csg = union,
                label = "$pairName/$scenarioId opt=$optType union",
                allowEmpty = false,
            )
            assertMeshSanity(
                csg = intersection,
                label = "$pairName/$scenarioId opt=$optType intersection",
                allowEmpty = true,
            )
            assertMeshSanity(
                csg = difference,
                label = "$pairName/$scenarioId opt=$optType difference",
                allowEmpty = true,
            )

            ScenarioResult(
                pairName = pairName,
                scenarioId = scenarioId,
                optType = optType,
                left = left,
                right = right,
                union = union,
                intersection = intersection,
                difference = difference,
            ).also(assertion)
        }
    }

    private fun assertMeshSanity(
        csg: CSG,
        label: String,
        allowEmpty: Boolean,
        areaEpsilon: Double = 1e-12,
    ) {
        var triangleCount = 0
        csg.polygons.forEachIndexed { polyIndex, polygon ->
            polygon.vertices.forEachIndexed { vertexIndex, vertex ->
                val pos = vertex.pos
                if (!pos.x.isFinite() || !pos.y.isFinite() || !pos.z.isFinite()) {
                    fail("$label has non-finite vertex at polygon=$polyIndex vertex=$vertexIndex value=$pos")
                }
            }
            polygon.toTriangles().forEachIndexed { triIndex, triangle ->
                val p1 = triangle.vertices[0].pos
                val p2 = triangle.vertices[1].pos
                val p3 = triangle.vertices[2].pos
                val twiceArea = p2.minus(p1).crossed(p3.minus(p1)).magnitude()
                if (twiceArea <= areaEpsilon) {
                    fail(
                        "$label has zero-area triangle at polygon=$polyIndex triangle=$triIndex " +
                            "area=$twiceArea"
                    )
                }
                triangleCount++
            }
        }

        if (!allowEmpty && triangleCount == 0) {
            fail("$label produced no triangles")
        }
        if (triangleCount == 0 && csg.polygons.isNotEmpty()) {
            fail("$label has polygons but no triangles")
        }
    }
}

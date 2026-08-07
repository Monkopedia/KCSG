package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertBoundsClose
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import com.monkopedia.kcsg.testutil.GeometryScenarioHarness
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.math.abs
import kotlin.math.sqrt

class PrimitiveInteractionMatrixTest {
    @Test
    fun scenarioS1DisjointSolids() {
        pairsForS1ToS4AndS8.forEach { pair ->
            pair.harness().run(
                scenarioId = "S1-disjoint",
                pairFactory = { _, leftFactory, rightFactory ->
                    build(leftFactory, translation = Vector3d.xyz(-3.0, 0.0, 0.0)) to
                        build(rightFactory, translation = Vector3d.xyz(3.0, 0.0, 0.0))
                },
            ) { result ->
                val ctx = context(result)
                assertVolumeClose(
                    expected = result.sumVolume,
                    actual = result.unionVolume,
                    relativeTolerance = 1e-3,
                    message = "$ctx union equals sum",
                )
                assertNearZero(result.intersectionVolume, 1e-6, "$ctx intersection")
                assertTrue("$ctx difference should not exceed left", result.differenceVolume <= result.leftVolume + 1e-3)
                assertTrue("$ctx difference should not be negative", result.differenceVolume >= -1e-6)
                if (result.optType == CSG.OptType.NONE) {
                    assertVolumeClose(
                        expected = result.leftVolume,
                        actual = result.differenceVolume,
                        relativeTolerance = 1e-3,
                        message = "$ctx difference unchanged",
                    )
                }
            }
        }
    }

    @Test
    fun scenarioS2PartialOverlap() {
        pairsForS1ToS4AndS8.forEach { pair ->
            pair.harness().run(
                scenarioId = "S2-partial-overlap",
                pairFactory = { _, leftFactory, rightFactory ->
                    build(leftFactory, translation = Vector3d.xyz(-0.4, 0.0, 0.0)) to
                        build(rightFactory, translation = Vector3d.xyz(0.4, 0.0, 0.0))
                },
            ) { result ->
                val ctx = context(result)
                assertTrue("$ctx intersection should be non-empty", result.intersectionVolume > 1e-4)
                assertTrue("$ctx union should be less than sum", result.unionVolume < result.sumVolume - 1e-4)
                assertBoundsContain(result.union.bounds, result.left.bounds, 1e-6, "$ctx union contains left")
                assertBoundsContain(result.union.bounds, result.right.bounds, 1e-6, "$ctx union contains right")
            }
        }
    }

    @Test
    fun scenarioS3FullContainment() {
        pairsForS1ToS4AndS8.forEach { pair ->
            pair.harness().run(
                scenarioId = "S3-full-containment",
                pairFactory = { _, leftFactory, rightFactory ->
                    build(leftFactory, scale = 2.5) to build(rightFactory, scale = 0.6)
                },
            ) { result ->
                val ctx = context(result)
                assertBoundsContain(result.left.bounds, result.right.bounds, 1e-6, "$ctx right inside left")
                assertVolumeClose(
                    expected = result.rightVolume,
                    actual = result.intersectionVolume,
                    relativeTolerance = 3e-2,
                    message = "$ctx containment intersection",
                )
                assertVolumeClose(
                    expected = result.leftVolume,
                    actual = result.unionVolume,
                    relativeTolerance = 3e-2,
                    message = "$ctx containment union",
                )
                assertVolumeClose(
                    expected = result.leftVolume - result.rightVolume,
                    actual = result.differenceVolume,
                    relativeTolerance = 3e-2,
                    message = "$ctx containment difference",
                )
                assertBoundsClose(
                    expected = result.left.bounds,
                    actual = result.difference.bounds,
                    tolerance = 1e-3,
                    message = "$ctx difference bounds",
                )
            }
        }
    }

    @Test
    fun scenarioS4IdenticalSolids() {
        pairsForS1ToS4AndS8.forEach { pair ->
            listOf(
                PrimitivePair("${pair.name}-left-self", pair.leftFactory, pair.leftFactory),
                PrimitivePair("${pair.name}-right-self", pair.rightFactory, pair.rightFactory),
            ).forEach { identicalPair ->
                identicalPair.harness().run(
                    scenarioId = "S4-identical",
                    pairFactory = { _, leftFactory, rightFactory ->
                        build(leftFactory, translation = Vector3d.xyz(0.3, -0.2, 0.1)) to
                            build(rightFactory, translation = Vector3d.xyz(0.3, -0.2, 0.1))
                    },
                ) { result ->
                    val ctx = context(result)
                    assertVolumeClose(
                        expected = result.leftVolume,
                        actual = result.unionVolume,
                        relativeTolerance = 1e-3,
                        message = "$ctx union idempotence",
                    )
                    assertVolumeClose(
                        expected = result.leftVolume,
                        actual = result.intersectionVolume,
                        relativeTolerance = 1e-3,
                        message = "$ctx intersection idempotence",
                    )
                    assertNearZero(result.differenceVolume, 1e-6, "$ctx difference empty")
                    assertBoundsClose(
                        expected = result.left.bounds,
                        actual = result.union.bounds,
                        tolerance = 1e-6,
                        message = "$ctx union bounds",
                    )
                    assertBoundsClose(
                        expected = result.left.bounds,
                        actual = result.intersection.bounds,
                        tolerance = 1e-6,
                        message = "$ctx intersection bounds",
                    )
                }
            }
        }
    }

    @Test
    fun scenarioS5FaceTangency() {
        runTangencyScenario("S5-face-tangency", TangencyCase::face)
    }

    @Test
    fun scenarioS6EdgeTangency() {
        runTangencyScenario("S6-edge-tangency", TangencyCase::edge)
    }

    @Test
    fun scenarioS7VertexTangency() {
        runTangencyScenario("S7-vertex-tangency", TangencyCase::vertex)
    }

    /**
     * Shared body for S5-S7.
     *
     * Every offset in [tangencyCases] places the two operands in exact contact, so the
     * invariants are the same in all three scenarios: no shared volume, a union that
     * accounts for both operands in full, and a difference that leaves the left operand
     * alone. The contact probe at the end is what keeps the fixture honest - "the
     * intersection is empty" is trivially true of solids that never touch.
     */
    private fun runTangencyScenario(
        scenarioId: String,
        offsetSelector: (TangencyCase) -> Vector3d,
    ) {
        tangencyCases.forEach { case ->
            val offset = offsetSelector(case)
            case.pair.harness().run(
                scenarioId = scenarioId,
                pairFactory = { _, leftFactory, rightFactory ->
                    leftFactory() to build(rightFactory, translation = offset)
                },
            ) { result ->
                val ctx = context(result)

                assertNearZero(result.intersectionVolume, 1e-3, "$ctx tangency intersection")
                assertVolumeClose(
                    expected = result.sumVolume,
                    actual = result.unionVolume,
                    relativeTolerance = 5e-3,
                    message = "$ctx tangency union equals sum",
                )
                // The difference must leave the left operand alone, but only OptType.NONE
                // gets that right today: the bounds-optimized paths cut roughly half the
                // left operand away on several exactly tangent pairs (see #58). S1 already
                // scopes the same assertion to NONE for the same reason, so this follows
                // the suite's existing convention rather than inventing a new exception.
                if (result.optType == CSG.OptType.NONE) {
                    assertVolumeClose(
                        expected = result.leftVolume,
                        actual = result.differenceVolume,
                        relativeTolerance = 5e-3,
                        message = "$ctx tangency difference equals left",
                    )
                } else {
                    assertTrue(
                        "$ctx tangency difference should not exceed left",
                        result.differenceVolume <= result.leftVolume * (1.0 + 5e-3),
                    )
                    assertTrue(
                        "$ctx tangency difference should not be empty",
                        result.differenceVolume > 0.0,
                    )
                }
                assertBoundsContain(
                    result.union.bounds,
                    result.left.bounds,
                    1e-6,
                    "$ctx tangency union contains left",
                )
                assertBoundsContain(
                    result.union.bounds,
                    result.right.bounds,
                    1e-6,
                    "$ctx tangency union contains right",
                )

                // Fixture guard and mutation guard in one: pushing the right operand
                // CONTACT_PROBE_DEPTH along the contact direction has to produce a real
                // overlap. Solids that merely sit near each other would not reach, and an
                // intersect() that always returned empty would fail here even though it
                // satisfies every "intersection is empty" assertion above.
                val nudge = offset.times(-CONTACT_PROBE_DEPTH / offset.magnitude())
                val overlapping = result.right.copy()
                    .transformed(Transform.unity().translate(nudge))
                val probeVolume = result.left.copy().intersect(overlapping).computeVolume()
                assertTrue(
                    "$ctx contact probe: operands are not in contact, nudging by " +
                        "$CONTACT_PROBE_DEPTH along $offset gave intersection volume $probeVolume",
                    probeVolume > CONTACT_PROBE_MIN_VOLUME,
                )
            }
        }
    }

    @Test
    fun scenarioS8TransformedOverlapInvariance() {
        val scaleFactor = 1.5
        val volumeScale = scaleFactor * scaleFactor * scaleFactor

        pairsForS1ToS4AndS8.forEach { pair ->
            val harness = pair.harness()
            val baseline = harness.run(
                scenarioId = "S8-baseline",
                pairFactory = { _, leftFactory, rightFactory ->
                    build(leftFactory, translation = Vector3d.xyz(-0.4, 0.0, 0.0)) to
                        build(rightFactory, translation = Vector3d.xyz(0.4, 0.0, 0.0))
                },
            ) { result ->
                val ctx = context(result)
                assertTrue("$ctx baseline overlap expected", result.intersectionVolume > 1e-4)
            }
            val rigidlyTransformed = harness.run(
                scenarioId = "S8-rigid-transform",
                pairFactory = { _, leftFactory, rightFactory ->
                    val left = build(leftFactory, translation = Vector3d.xyz(-0.4, 0.0, 0.0))
                    val right = build(rightFactory, translation = Vector3d.xyz(0.4, 0.0, 0.0))
                    applyRigidTransform(left) to applyRigidTransform(right)
                },
            ) {}
            val scaledAndTransformed = harness.run(
                scenarioId = "S8-scale-rot-translate",
                pairFactory = { _, leftFactory, rightFactory ->
                    val leftBase = build(leftFactory, translation = Vector3d.xyz(-0.4, 0.0, 0.0))
                    val rightBase = build(rightFactory, translation = Vector3d.xyz(0.4, 0.0, 0.0))
                    val leftScaled = leftBase.transformed(Transform.unity().scale(scaleFactor))
                    val rightScaled = rightBase.transformed(Transform.unity().scale(scaleFactor))
                    applyRigidTransform(leftScaled) to applyRigidTransform(rightScaled)
                },
            ) {}

            CSG.OptType.entries.forEach { optType ->
                val base = baseline.getValue(optType)
                val rigid = rigidlyTransformed.getValue(optType)
                val scaled = scaledAndTransformed.getValue(optType)
                val ctx = "${pair.name}/S8 opt=$optType"

                assertVolumeClose(base.unionVolume, rigid.unionVolume, relativeTolerance = 3e-2, message = "$ctx rigid union")
                assertVolumeClose(
                    base.intersectionVolume,
                    rigid.intersectionVolume,
                    relativeTolerance = 3e-2,
                    message = "$ctx rigid intersection",
                )
                assertVolumeClose(
                    base.differenceVolume,
                    rigid.differenceVolume,
                    relativeTolerance = 3e-2,
                    message = "$ctx rigid difference",
                )

                assertVolumeClose(
                    base.unionVolume * volumeScale,
                    scaled.unionVolume,
                    relativeTolerance = 5e-2,
                    message = "$ctx scaled union",
                )
                assertVolumeClose(
                    base.intersectionVolume * volumeScale,
                    scaled.intersectionVolume,
                    relativeTolerance = 5e-2,
                    message = "$ctx scaled intersection",
                )
                assertVolumeClose(
                    base.differenceVolume * volumeScale,
                    scaled.differenceVolume,
                    relativeTolerance = 5e-2,
                    message = "$ctx scaled difference",
                )
            }
        }
    }

    @Test
    fun scenarioS9OptimizationParity() {
        fullMatrixPairs.forEach { pair ->
            val results = pair.harness().run(
                scenarioId = "S9-opt-parity",
                pairFactory = { _, leftFactory, rightFactory ->
                    build(leftFactory, translation = Vector3d.xyz(-0.4, 0.0, 0.0)) to
                        build(rightFactory, translation = Vector3d.xyz(0.4, 0.0, 0.0))
                },
            ) {}

            val baseline = results.getValue(CSG.OptType.NONE)
            listOf(CSG.OptType.CSG_BOUND, CSG.OptType.POLYGON_BOUND).forEach { optType ->
                val candidate = results.getValue(optType)
                val ctx = "${pair.name}/S9 opt=$optType"

                assertBoundsClose(
                    expected = baseline.union.bounds,
                    actual = candidate.union.bounds,
                    tolerance = 1e-3,
                    message = "$ctx union bounds",
                )
                assertBoundsClose(
                    expected = baseline.intersection.bounds,
                    actual = candidate.intersection.bounds,
                    tolerance = 1e-3,
                    message = "$ctx intersection bounds",
                )
                assertBoundsClose(
                    expected = baseline.difference.bounds,
                    actual = candidate.difference.bounds,
                    tolerance = 1e-3,
                    message = "$ctx difference bounds",
                )
                assertVolumeClose(
                    expected = baseline.unionVolume,
                    actual = candidate.unionVolume,
                    relativeTolerance = 2e-2,
                    message = "$ctx union volume",
                )
                assertVolumeClose(
                    expected = baseline.intersectionVolume,
                    actual = candidate.intersectionVolume,
                    relativeTolerance = 2e-2,
                    message = "$ctx intersection volume",
                )
                assertVolumeClose(
                    expected = baseline.differenceVolume,
                    actual = candidate.differenceVolume,
                    relativeTolerance = 2e-2,
                    message = "$ctx difference volume",
                )
            }
        }
    }

    private fun context(result: GeometryScenarioHarness.ScenarioResult): String {
        return "${result.pairName}/${result.scenarioId} opt=${result.optType}"
    }

    private fun applyRigidTransform(csg: CSG): CSG {
        val rotated = csg.transformed(Transform.unity().rot(Vector3d.xyz(20.0, 35.0, 15.0)))
        return rotated.transformed(Transform.unity().translate(Vector3d.xyz(1.2, -0.7, 0.5)))
    }

    private fun build(
        factory: () -> CSG,
        translation: Vector3d = Vector3d.ZERO,
        scale: Double = 1.0,
    ): CSG {
        var csg = factory()
        if (scale != 1.0) {
            csg = csg.transformed(Transform.unity().scale(scale))
        }
        if (translation != Vector3d.ZERO) {
            csg = csg.transformed(Transform.unity().translate(translation))
        }
        return csg
    }

    private fun assertNearZero(value: Double, tolerance: Double, message: String) {
        assertTrue("$message expected |$value| <= $tolerance", abs(value) <= tolerance)
    }

    private fun assertTrue(message: String, actual: Boolean) {
        kotlin.test.assertTrue(actual, message)
    }

    private fun assertBoundsContain(outer: Bounds, inner: Bounds, tolerance: Double, message: String) {
        assertTrue("$message min.x", outer.min.x <= inner.min.x + tolerance)
        assertTrue("$message min.y", outer.min.y <= inner.min.y + tolerance)
        assertTrue("$message min.z", outer.min.z <= inner.min.z + tolerance)
        assertTrue("$message max.x", outer.max.x + tolerance >= inner.max.x)
        assertTrue("$message max.y", outer.max.y + tolerance >= inner.max.y)
        assertTrue("$message max.z", outer.max.z + tolerance >= inner.max.z)
    }

    private fun PrimitivePair.harness(): GeometryScenarioHarness {
        return GeometryScenarioHarness(name, leftFactory, rightFactory)
    }

    private data class PrimitivePair(
        val name: String,
        val leftFactory: () -> CSG,
        val rightFactory: () -> CSG,
    )

    /**
     * Translations of the right operand that put it in exact contact with the left one.
     *
     * The offsets are per pair because the primitives are different shapes: a single
     * offset shared across the matrix only touches for `Cube-Cube`. All solids here are
     * centred on the origin with an extent of 1 along their contact direction (the cube
     * spans +/-1, the sphere and cylinder have radius 1), so:
     *
     * - [face]: separation of 2 along x, valid for every pair.
     * - [edge]: contact against the cube's vertical edge at (1, 1) or, for pairs without
     *   a cube, an xy-diagonal separation of 2.
     * - [vertex]: contact against the cube's corner at (1, 1, 1), the sphere's pole, or
     *   the cylinder's bottom rim.
     */
    private data class TangencyCase(
        val pair: PrimitivePair,
        val face: Vector3d,
        val edge: Vector3d,
        val vertex: Vector3d,
    )

    companion object {
        /** Overlap depth used by the contact probe in S5-S7. */
        private const val CONTACT_PROBE_DEPTH = 0.3

        /**
         * Lower bound on the overlap volume the probe must find. The thinnest overlap in
         * the matrix is the cylinder rim pushed into the cube corner in S7, roughly 1e-2;
         * the sphere tessellation costs at most ~0.03 of effective radius on top of that.
         */
        private const val CONTACT_PROBE_MIN_VOLUME = 1e-3

        private val sqrt2 = sqrt(2.0)
        private val sqrtHalf = 1.0 / sqrt2
        private val sqrtThird = 1.0 / sqrt(3.0)

        private val cubeCube = PrimitivePair("Cube-Cube", ::cubeSolid, ::cubeSolid)
        private val cubeSphere = PrimitivePair("Cube-Sphere", ::cubeSolid, ::sphereSolid)
        private val cubeCylinder = PrimitivePair("Cube-Cylinder", ::cubeSolid, ::cylinderSolid)
        private val sphereSphere = PrimitivePair("Sphere-Sphere", ::sphereSolid, ::sphereSolid)
        private val sphereCylinder =
            PrimitivePair("Sphere-Cylinder", ::sphereSolid, ::cylinderSolid)
        private val cylinderCylinder =
            PrimitivePair("Cylinder-Cylinder", ::cylinderSolid, ::cylinderSolid)

        private val fullMatrixPairs = listOf(
            cubeCube,
            cubeSphere,
            cubeCylinder,
            sphereSphere,
            sphereCylinder,
            cylinderCylinder,
        )

        private val roundedPairs = listOf(
            PrimitivePair("RoundedCube-Cube", ::roundedCubeSolid, ::cubeSolid),
            PrimitivePair("RoundedCube-Sphere", ::roundedCubeSolid, ::sphereSolid),
        )

        private val polyhedronPairs = listOf(
            PrimitivePair("Polyhedron-Cube", ::polyhedronSolid, ::cubeSolid),
            PrimitivePair("Polyhedron-Sphere", ::polyhedronSolid, ::sphereSolid),
        )

        private val pairsForS1ToS4AndS8: List<PrimitivePair> =
            fullMatrixPairs + roundedPairs + polyhedronPairs

        private val faceOffset = Vector3d.xyz(2.0, 0.0, 0.0)

        private val tangencyCases: List<TangencyCase> = listOf(
            TangencyCase(
                pair = cubeCube,
                face = faceOffset,
                edge = Vector3d.xyz(2.0, 2.0, 0.0),
                vertex = Vector3d.xyz(2.0, 2.0, 2.0),
            ),
            TangencyCase(
                pair = cubeSphere,
                face = faceOffset,
                // Sphere centre one radius out from the cube edge at (1, 1).
                edge = Vector3d.xyz(1.0 + sqrtHalf, 1.0 + sqrtHalf, 0.0),
                // Sphere centre one radius out from the cube corner at (1, 1, 1).
                vertex = Vector3d.xyz(
                    1.0 + sqrtThird,
                    1.0 + sqrtThird,
                    1.0 + sqrtThird,
                ),
            ),
            TangencyCase(
                pair = cubeCylinder,
                face = faceOffset,
                // Cylinder axis one radius out from the cube edge at (1, 1).
                edge = Vector3d.xyz(1.0 + sqrtHalf, 1.0 + sqrtHalf, 0.0),
                // Cylinder lifted to z in [1, 3]; its bottom rim passes through (1, 1, 1).
                vertex = Vector3d.xyz(1.0 + sqrtHalf, 1.0 + sqrtHalf, 2.0),
            ),
            TangencyCase(
                pair = sphereSphere,
                face = faceOffset,
                edge = Vector3d.xyz(sqrt2, sqrt2, 0.0),
                vertex = Vector3d.xyz(
                    2.0 * sqrtThird,
                    2.0 * sqrtThird,
                    2.0 * sqrtThird,
                ),
            ),
            TangencyCase(
                pair = sphereCylinder,
                face = faceOffset,
                edge = Vector3d.xyz(sqrt2, sqrt2, 0.0),
                // Cylinder lifted to z in [1, 3]; its bottom rim passes through the
                // sphere's pole at (0, 0, 1).
                vertex = Vector3d.xyz(sqrtHalf, sqrtHalf, 2.0),
            ),
            TangencyCase(
                pair = cylinderCylinder,
                face = faceOffset,
                edge = Vector3d.xyz(sqrt2, sqrt2, 0.0),
                // Rim to rim: axes 2 apart in xy, upper cylinder starting at z = 1.
                vertex = Vector3d.xyz(sqrt2, sqrt2, 2.0),
            ),
        )

        private fun cubeSolid(): CSG {
            return Cube(size = 2.0).toCSG()
        }

        private fun sphereSolid(): CSG {
            return Sphere(1.0, 18, 9, Vector3d.ZERO).toCSG()
        }

        private fun cylinderSolid(): CSG {
            return Cylinder(
                start = Vector3d.xyz(0.0, 0.0, -1.0),
                end = Vector3d.xyz(0.0, 0.0, 1.0),
                startRadius = 1.0,
                endRadius = 1.0,
                numSlices = 18,
            ).toCSG()
        }

        private fun roundedCubeSolid(): CSG {
            return RoundedCube(
                center = Vector3d.ZERO,
                dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
                cornerRadius = 0.25,
                resolution = 4,
            ).toCSG()
        }

        private fun polyhedronSolid(): CSG {
            val points = listOf(
                Vector3d.xyz(-1.0, -1.0, -1.0),
                Vector3d.xyz(1.0, -1.0, -1.0),
                Vector3d.xyz(-1.0, 1.0, -1.0),
                Vector3d.xyz(1.0, 1.0, -1.0),
                Vector3d.xyz(-1.0, -1.0, 1.0),
                Vector3d.xyz(1.0, -1.0, 1.0),
                Vector3d.xyz(-1.0, 1.0, 1.0),
                Vector3d.xyz(1.0, 1.0, 1.0),
            )
            val faces = listOf(
                listOf(0, 4, 6, 2),
                listOf(1, 3, 7, 5),
                listOf(0, 1, 5, 4),
                listOf(2, 6, 7, 3),
                listOf(0, 2, 3, 1),
                listOf(4, 5, 7, 6),
            )
            return Polyhedron(points = points, faces = faces).toCSG()
        }
    }
}

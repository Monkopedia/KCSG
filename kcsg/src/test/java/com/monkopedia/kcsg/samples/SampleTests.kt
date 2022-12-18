package com.monkopedia.kcsg.samples

import com.monkopedia.kcsg.CSG
import com.monkopedia.kcsg.Polygon
import com.monkopedia.kcsg.STL
import com.monkopedia.kcsg.Sphere
import com.monkopedia.kcsg.Transform
import com.monkopedia.kcsg.Vector3d
import com.monkopedia.kcsg.ext.vvecmath.Plane
import org.junit.Before
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max

class SampleTests {

    @Before
    fun setup() {
        CSG.setDefaultOptType(CSG.OptType.NONE)
    }

    private fun getResource(name: String): CSG {
        return STL.from({
            SampleTests::class.java.getResourceAsStream(name)
                ?: error("Missing resource $name")
        }, { SampleTests::class.java.getResource(name)?.openConnection()?.contentLengthLong ?: 0L })
    }

    private fun assertStlEquals(expected: CSG, actual: CSG) {
        val expectedPolys = expected.polygons
        val transformedActual = toStlAndBack(actual)
        val actualPolys = transformedActual.polygons
        if (expectedPolys.size != actualPolys.size) {
            throw AssertionError("Expected ${expectedPolys.size} but found ${actualPolys.size}")
        }
        for (i in expectedPolys.indices) {
            val expectedPoly = expectedPolys[i]
            val actualPoly = actualPolys[i]
            try {
                assertPolygonEquals(expectedPoly, actualPoly)
            } catch (t: AssertionError) {
                throw AssertionError(
                    "Difference in polygons found at $i of ${expectedPolys.size}",
                    t
                )
            }
        }
    }

    private fun toStlAndBack(actual: CSG): CSG {
        val stlString = actual.toStlString()
        val length = stlString.encodeToByteArray().size.toLong()
        return STL.from({ stlString.byteInputStream() }, { length })
    }

    private fun assertPolygonEquals(expectedPoly: Polygon, actualPoly: Polygon) {
        val expectedVertices = expectedPoly.vertices
        val actualVertices = actualPoly.vertices
        if (expectedVertices.size != actualVertices.size) {
            throw AssertionError(
                "Expected ${expectedVertices.size} but found ${actualVertices.size}"
            )
        }
        for (i in expectedVertices.indices) {
            val expected = expectedVertices[i]
            val actual = actualVertices[i]
            val diffPos = expected.pos - actual.pos
            val maxDiff = max(abs(diffPos.x), max(abs(diffPos.y), abs(diffPos.z)))
            if (maxDiff > .05) {
                throw AssertionError("Vertex $i of ${expectedVertices.size} differed too much\nExpected: $expected\nActual: $actual\nDifference: $diffPos")
            }
        }
    }

    @Test
    fun testArduinoMount() {
        val aMount = ArduinoMount()
        val expected = getResource("/arduino_mount.stl")

        assertStlEquals(expected, aMount.toCSG())
    }

    @Test
    fun testBatteryHolder() {
        val arConnect = BatteryHolder()
        val expected = getResource("/battery_holder.stl")

        assertStlEquals(expected, arConnect.toCSG())
    }

    @Test
    fun testBreadBoardConnector() {
        val arConnect = BreadBoardConnector()
        val expected = getResource("/bread-board-connector-tmp.stl")

        assertStlEquals(
            expected,
            arConnect.toCSG().transformed(
                Transform.unity().mirror(Plane.XY_PLANE).rotY(180.0)
            )
        )
    }

    @Test
    fun testBreadBoardMount() {
        val aMount = BreadBoardMount()
        val expected = getResource("/bread-board-mount.stl")

        assertStlEquals(expected, aMount.toCSG())
    }

    @Test
    fun testEdgeTest() {
        val aMount = EdgeTest()
        val expected = getResource("/edge-test.stl")

        assertStlEquals(expected, aMount.toCSG(true))
    }

    @Test
    fun testEdgeTestOrig() {
        val aMount = EdgeTest()
        val expected = getResource("/edge-test-orig.stl")

        assertStlEquals(expected, aMount.toCSG(false))
    }

    @Test
    fun testEgg() {
        val egg = Egg()
        val expected = getResource("/egg.stl")

        assertStlEquals(expected, egg.toCSG())
    }

    @Test
    fun testFractalLevel1() {
        val fractal = FractalStructure(
            Vector3d.ZERO,
            Vector3d.Z_ONE.times(1.0),
            4,
            15.0,
            0,
            Vector3d.X_ONE,
            Vector3d.Y_ONE //                null, null
        )
        val expected = getResource("/fractal-structure-level-1.stl")

        assertStlEquals(expected, fractal.toCSG())
    }

    @Test
    fun testFractalLevel2() {
        val fractal = FractalStructure(
            Vector3d.ZERO,
            Vector3d.Z_ONE.times(1.0),
            4,
            15.0,
            1,
            Vector3d.X_ONE,
            Vector3d.Y_ONE //                null, null
        )
        val expected = getResource("/fractal-structure-level-2.stl")

        assertStlEquals(expected, fractal.toCSG())
    }

    @Test
    fun testFractalStructureBeam2D() {
        var fractal = FractalStructureBeam2D().toCSG()
        fractal = fractal.union(Sphere(Vector3d.ZERO, 1.0, 4, 4).toCSG())
        val expected = getResource("/fractal-structure-beam-2d.stl")

        assertStlEquals(expected, fractal)
    }

    @Test
    fun testHexamail() {
        val fractal = HexaMail().toCSG(6, 3, 3)
        val expected = getResource("/hexamail.stl")

        assertStlEquals(expected, fractal!!)
    }

    @Test
    fun testHinge() {
        val hinge = Hinge()
        val expected = getResource("/hinge.stl")

        assertStlEquals(expected, hinge.toCSG())
    }

    @Test
    fun testLeapMotionCase() {
        val leapMotionCase = LeapMotionCase()
        val expected = getResource("/leapmotion.stl")

        assertStlEquals(expected, leapMotionCase.toCSG())
    }

    @Test
    fun testMicroSDCard() {
        val microSdCard = MicroSDCard()
        val expected = getResource("/mircosd.stl")

        assertStlEquals(expected, microSdCard.toCSG())
    }

    @Test
    fun testMoebiusband() {
        val moebius = Moebiusband()
        val expected = getResource("/möbiusband.stl")

        assertStlEquals(expected, moebius.toCSG())
    }

    @Test
    fun testMoebiusStairs() {
        val moebiusStairs = MoebiusStairs()
        val expected = getResource("/moebius-stairs.stl")

        assertStlEquals(expected, moebiusStairs.toCSG())
    }

    @Test
    fun testNaze32Holder() {
        val naze32 = Naze32Holder()
        val expected = getResource("/naze32-mount.stl")

        assertStlEquals(expected, naze32.toCSG())
    }

    @Test
    fun testPlaneWithHoles() {
        CSG.setDefaultOptType(CSG.OptType.CSG_BOUND)
        val csg = PlaneWithHoles()
        val expected = getResource("/plane_with_holes.stl")

        assertStlEquals(expected, csg.toCSG())
    }

    @Test
    fun testQuadrocopter() {
        val csg = QuadrocopterArm()
        val expected = getResource("/quadrocopter-arm.stl")

        assertStlEquals(expected, csg.toCSG())
    }

    @Test
    fun testQuadrocopterArmHolder() {
        val csg = QuadrocopterArmHolder().toCSG(18.0, 0.5, 18.0, 4.0, 20.0, 3.0)
        val expected = getResource("/quadrocopter-arm-holder.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testQuadrocopterBottom() {
        CSG.setDefaultOptType(CSG.OptType.POLYGON_BOUND)
        val csg = QuadrocopterBottom().toCSG()
        val expected = getResource("/quadrocopter-bottom.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testQuadrocopterCross() {
        CSG.setDefaultOptType(CSG.OptType.NONE)
        val csg = QuadrocopterCross().toCSG2()
        val expected = getResource("/quadrocopter-cross.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testQuadrocopterLandingGear() {
        CSG.setDefaultOptType(CSG.OptType.NONE)
        val csg = QuadrocopterLadingGears().toCSG()
        val expected = getResource("/quadcopter-landing-gear.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testQuadrocopterLandingGearAndHolders() {
        val csg = QuadrocopterLadingGearsAndHolders().toCSG()
        val expected = getResource("/quadcopter-landing-gear-and-holder.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testQuadrocopterPlatform() {
        val csg = QuadrocopterPlatform().toCSG()
        val expected = getResource("/quadrocopter-platform.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testRaspberryArduinoConnector() {
        val csg = RaspberryArduinoConnector().toCSG()
        val expected = getResource("/pi-arduino-connector.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testRaspberryPiBPlusMount() {
        val csg = RaspberryPiBPlusMount.boardAndPegs()
            .transformed(Transform.unity().rotX(180.0))
        val expected = getResource("/raspberry-pi-bplus-mount-3mm.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testRaspberryPiMount() {
        val csg = RaspberryPiMount.boardAndPegs()
            .transformed(Transform.unity().rotX(180.0))
        val expected = getResource("/raspberry_pi_mount.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testRoundedCubeSample() {
        val csg = RoundedCubeSample().toCSG()
        val expected = getResource("/rounded-cube.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testSabine() {
        val csg = Sabine().toCSG()
        val expected = getResource("/sabine.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testServoHeadFemale() {
        val csg = ServoHead().servoHeadFemale()
        val expected = getResource("/servo-head-female.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testServoHeadMale() {
        val csg = ServoHead().servoHeadMale()
        val expected = getResource("/servo-head-male.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testServoMount() {
        val csg = ServoMount().toCSG()
        val expected = getResource("/servo-mount.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testServoMountPixy() {
        val csg = ServoMountPixy().toCSG()
        val expected = getResource("/servo-mount-pixy.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testServoToServoConnector() {
        val csg = ServoToServoConnector().toCSG()
        val expected = getResource("/servo-to-servo.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testServoWheel() {
        val csg = ServoWheel().toCSG()
        val expected = getResource("/servo-wheel.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testSpheres() {
        val csg = Spheres().toCSG()
        val expected = getResource("/spheres.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testSquareMail() {
        val csg = SquareMail().toCSG(12, 4)
        val expected = getResource("/squaremail-test.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testSurfacePro2PenHolder() {
        val csg = SurfacePro2PenHolder().toCSG()
        val expected = getResource("/surfac2penholder.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testTriMail() {
        val csg = TriMail().toCSG(3, 3, 3)
        val expected = getResource("/trimail-test.stl")

        assertStlEquals(expected, csg)
    }

    @Test
    fun testWeightedSphere() {
        val csg = WeightedSphere().toCSG()
        val expected = getResource("/rounded-cube-mod.stl")

        assertStlEquals(expected, csg)
    }
}

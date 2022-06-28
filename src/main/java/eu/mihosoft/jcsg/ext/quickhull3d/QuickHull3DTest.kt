/**
 * Copyright John E. Lloyd, 2004. All rights reserved. Permission to use,
 * copy, modify and redistribute is granted, provided that this copyright
 * notice is retained and the author is given credit whenever appropriate.
 *
 * This  software is distributed "as is", without any warranty, including
 * any implied warranty of merchantability or fitness for a particular
 * use. The author assumes no responsibility for, and shall not be liable
 * for, any special, indirect, or consequential damages, or any damages
 * whatsoever, arising out of or in connection with the use of this
 * software.
 */
package eu.mihosoft.jcsg.ext.quickhull3d

import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

/**
 * Testing class for QuickHull3D. Running the command
 * <pre>
 * java quickhull3d.QuickHull3DTest
</pre> *
 * will cause QuickHull3D to be tested on a number of randomly
 * choosen input sets, with degenerate points added near
 * the edges and vertics of the convex hull.
 *
 *
 * The command
 * <pre>
 * java quickhull3d.QuickHull3DTest -timing
</pre> *
 * will cause timing information to be produced instead.
 *
 * @author John E. Lloyd, Fall 2004
 */
internal class QuickHull3DTest {
    private var rand: Random = Random()

    /**
     * Returns true if two face index sets are equal,
     * modulo a cyclical permuation.
     *
     * @param indices1 index set for first face
     * @param indices2 index set for second face
     * @return true if the index sets are equivalent
     */
    private fun faceIndicesEqual(indices1: IntArray, indices2: IntArray?): Boolean {
        if (indices1.size != indices2!!.size) {
            return false
        }
        val len = indices1.size
        var j: Int = 0
        while (j < len) {
            if (indices1[0] == indices2[j]) {
                break
            }
            j++
        }
        if (j == len) {
            return false
        }
        for (i in 1 until len) {
            if (indices1[i] != indices2[(j + i) % len]) {
                return false
            }
        }
        return true
    }

    /**
     * Returns the coordinates for `num` points whose x, y, and
     * z values are randomly chosen within a given range.
     *
     * @param num number of points to produce
     * @param range coordinate values will lie between -range and range
     * @return array of coordinate values
     */
    private fun randomPoints(num: Int, range: Double): DoubleArray {
        val coords = DoubleArray(num * 3)
        for (i in 0 until num) {
            for (k in 0..2) {
                coords[i * 3 + k] = 2 * range * (rand.nextDouble() - 0.5)
            }
        }
        return coords
    }

    private fun randomlyPerturb(pnt: Point3d, tol: Double) {
        pnt.x += tol * (rand.nextDouble() - 0.5)
        pnt.y += tol * (rand.nextDouble() - 0.5)
        pnt.z += tol * (rand.nextDouble() - 0.5)
    }

    /**
     * Returns the coordinates for `num` randomly
     * chosen points which are degenerate which respect
     * to the specified dimensionality.
     *
     * @param num number of points to produce
     * @param dimen dimensionality of degeneracy: 0 = coincident,
     * 1 = colinear, 2 = coplaner.
     * @return array of coordinate values
     */
    private fun randomDegeneratePoints(num: Int, dimen: Int): DoubleArray {
        val coords = DoubleArray(num * 3)
        val pnt = Point3d()
        val base = Point3d()
        base.setRandom(-1.0, 1.0, rand)
        val tol = DOUBLE_PREC
        if (dimen == 0) {
            for (i in 0 until num) {
                pnt.set(base)
                randomlyPerturb(pnt, tol)
                coords[i * 3 + 0] = pnt.x
                coords[i * 3 + 1] = pnt.y
                coords[i * 3 + 2] = pnt.z
            }
        } else if (dimen == 1) {
            val u = Vector3d()
            u.setRandom(-1.0, 1.0, rand)
            u.normalize()
            for (i in 0 until num) {
                val a = 2 * (rand.nextDouble() - 0.5)
                pnt.scale(a, u)
                pnt.add(base)
                randomlyPerturb(pnt, tol)
                coords[i * 3 + 0] = pnt.x
                coords[i * 3 + 1] = pnt.y
                coords[i * 3 + 2] = pnt.z
            }
        } else // dimen == 2
            {
                val nrm = Vector3d()
                nrm.setRandom(-1.0, 1.0, rand)
                nrm.normalize()
                for (i in 0 until num) { // compute a random point and project it to the plane
                    val perp = Vector3d()
                    pnt.setRandom(-1.0, 1.0, rand)
                    perp.scale(pnt.dot(nrm), nrm)
                    pnt.sub(perp)
                    pnt.add(base)
                    randomlyPerturb(pnt, tol)
                    coords[i * 3 + 0] = pnt.x
                    coords[i * 3 + 1] = pnt.y
                    coords[i * 3 + 2] = pnt.z
                }
            }
        return coords
    }

    /**
     * Returns the coordinates for `num` points whose x, y, and
     * z values are randomly chosen to lie within a sphere.
     *
     * @param num number of points to produce
     * @param radius radius of the sphere
     * @return array of coordinate values
     */
    private fun randomSphericalPoints(num: Int, radius: Double): DoubleArray {
        val coords = DoubleArray(num * 3)
        val pnt = Point3d()
        var i = 0
        while (i < num) {
            pnt.setRandom(-radius, radius, rand)
            if (pnt.norm() <= radius) {
                coords[i * 3 + 0] = pnt.x
                coords[i * 3 + 1] = pnt.y
                coords[i * 3 + 2] = pnt.z
                i++
            }
        }
        return coords
    }

    /**
     * Returns the coordinates for `num` points whose x, y, and
     * z values are each randomly chosen to lie within a specified
     * range, and then clipped to a maximum absolute
     * value. This means a large number of points
     * may lie on the surface of cube, which is useful
     * for creating degenerate convex hull situations.
     *
     * @param num number of points to produce
     * @param range coordinate values will lie between -range and
     * range, before clipping
     * @param max maximum absolute value to which the coordinates
     * are clipped
     * @return array of coordinate values
     */
    fun randomCubedPoints(num: Int, range: Double, max: Double): DoubleArray {
        val coords = DoubleArray(num * 3)
        for (i in 0 until num) {
            for (k in 0..2) {
                var x = 2 * range * (rand.nextDouble() - 0.5)
                if (x > max) {
                    x = max
                } else if (x < -max) {
                    x = -max
                }
                coords[i * 3 + k] = x
            }
        }
        return coords
    }

    private fun shuffleCoords(coords: DoubleArray): DoubleArray {
        val num = coords.size / 3
        for (i in 0 until num) {
            val i1 = rand.nextInt(num)
            val i2 = rand.nextInt(num)
            for (k in 0..2) {
                val tmp = coords[i1 * 3 + k]
                coords[i1 * 3 + k] = coords[i2 * 3 + k]
                coords[i2 * 3 + k] = tmp
            }
        }
        return coords
    }

    /**
     * Returns randomly shuffled coordinates for points on a
     * three-dimensional grid, with a presecribed width between each point.
     *
     * @param gridSize number of points in each direction,
     * so that the total number of points produced is the cube of
     * gridSize.
     * @param width distance between each point along a particular
     * direction
     * @return array of coordinate values
     */
    private fun randomGridPoints(gridSize: Int, width: Double): DoubleArray {
        // gridSize gives the number of points across a given dimension
        // any given coordinate indexed by i has value
        // (i/(gridSize-1) - 0.5)*width
        val num = gridSize * gridSize * gridSize
        val coords = DoubleArray(num * 3)
        var idx = 0
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                for (k in 0 until gridSize) {
                    coords[idx * 3 + 0] = (i / (gridSize - 1).toDouble() - 0.5) * width
                    coords[idx * 3 + 1] = (j / (gridSize - 1).toDouble() - 0.5) * width
                    coords[idx * 3 + 2] = (k / (gridSize - 1).toDouble() - 0.5) * width
                    idx++
                }
            }
        }
        shuffleCoords(coords)
        return coords
    }

    @Throws(Exception::class)
    fun explicitFaceCheck(hull: QuickHull3D, checkFaces: Array<IntArray>) {
        val faceIndices = hull.getFaces()
        if (faceIndices.size != checkFaces.size) {
            throw Exception(
                "Error: " + faceIndices.size + " faces vs. " + checkFaces.size
            )
        }
        // translate face indices back into original indices
        val pnts = hull.vertices
        val vtxIndices = hull.getVertexPointIndices()
        for (j in faceIndices.indices) {
            val idxs = faceIndices[j]
            for (k in idxs!!.indices) {
                idxs[k] = vtxIndices[idxs[k]]
            }
        }
        for (i in checkFaces.indices) {
            val cf = checkFaces[i]
            var j: Int = 0
            while (j < faceIndices.size) {
                if (faceIndices[j] != null) {
                    if (faceIndicesEqual(cf, faceIndices[j])) {
                        faceIndices[j] = null
                        break
                    }
                }
                j++
            }
            if (j == faceIndices.size) {
                var s = ""
                for (k in cf.indices) {
                    s += cf[k].toString() + " "
                }
                throw Exception("Error: face $s not found")
            }
        }
    }

    var cnt = 0

    @Throws(Exception::class)
    fun singleTest(coords: DoubleArray, checkFaces: Array<IntArray>?) {
        val hull = QuickHull3D()
        hull.debug = (debugEnable)
        hull.build(coords, coords.size / 3)
        if (triangulate) {
            hull.triangulate()
        }
        if (!hull.check(System.out)) {
            Throwable().printStackTrace()
            exitProcess(1)
        }
        checkFaces?.let { explicitFaceCheck(hull, it) }
        if (degeneracyTest != NO_DEGENERACY) {
            degenerateTest(hull, coords)
        }
    }

    fun addDegeneracy(
        type: Int,
        coords: DoubleArray?,
        hull: QuickHull3D
    ): DoubleArray {
        var numv = coords!!.size / 3
        val faces = hull.getFaces()
        val coordsx = DoubleArray(coords.size + faces.size * 3)
        for (i in coords.indices) {
            coordsx[i] = coords[i]
        }
        val lam = DoubleArray(3)
        val eps = hull.distanceTolerance
        for (i in faces.indices) {
            // random point on an edge
            lam[0] = rand.nextDouble()
            lam[1] = 1 - lam[0]
            lam[2] = 0.0
            if (type == VERTEX_DEGENERACY && i % 2 == 0) {
                lam[0] = 1.0
                lam[2] = 0.0
                lam[1] = lam[2]
            }
            for (j in 0..2) {
                val vtxi = faces[i]!![j]
                for (k in 0..2) {
                    coordsx[numv * 3 + k] += lam[j] * coords[vtxi * 3 + k] +
                        epsScale * eps * (rand.nextDouble() - 0.5)
                }
            }
            numv++
        }
        shuffleCoords(coordsx)
        return coordsx
    }

    @Throws(Exception::class)
    fun degenerateTest(hull: QuickHull3D, coords: DoubleArray?) {
        val coordsx = addDegeneracy(degeneracyTest, coords, hull)
        val xhull = QuickHull3D()
        xhull.debug = (debugEnable)
        try {
            xhull.build(coordsx, coordsx.size / 3)
            if (triangulate) {
                xhull.triangulate()
            }
        } catch (e: Exception) {
            var i = 0
            while (i < coordsx.size / 3) {
                println(
                    coordsx[i * 3 + 0].toString() + ", " +
                        coordsx[i * 3 + 1] + ", " +
                        coordsx[i * 3 + 2] + ", "
                )
                i++
            }
        }
        if (!xhull.check(System.out)) {
            Throwable().printStackTrace()
            exitProcess(1)
        }
    }

    private fun rotateCoords(
        res: DoubleArray,
        xyz: DoubleArray?,
        roll: Double,
        pitch: Double,
        yaw: Double
    ) {
        val sroll = sin(roll)
        val croll = cos(roll)
        val spitch = sin(pitch)
        val cpitch = cos(pitch)
        val syaw = sin(yaw)
        val cyaw = cos(yaw)
        val m00 = croll * cpitch
        val m10 = sroll * cpitch
        val m20 = -spitch
        val m01 = croll * spitch * syaw - sroll * cyaw
        val m11 = sroll * spitch * syaw + croll * cyaw
        val m21 = cpitch * syaw
        val m02 = croll * spitch * cyaw + sroll * syaw
        val m12 = sroll * spitch * cyaw - croll * syaw
        val m22 = cpitch * cyaw
        var x: Double
        var y: Double
        var z: Double
        var i = 0
        while (i < xyz!!.size - 2) {
            res[i + 0] = m00 * xyz[i + 0] + m01 * xyz[i + 1] + m02 * xyz[i + 2]
            res[i + 1] = m10 * xyz[i + 0] + m11 * xyz[i + 1] + m12 * xyz[i + 2]
            res[i + 2] = m20 * xyz[i + 0] + m21 * xyz[i + 1] + m22 * xyz[i + 2]
            i += 3
        }
    }

    private fun printCoords(coords: DoubleArray?) {
        val nump = coords!!.size / 3
        for (i in 0 until nump) {
            println(
                coords[i * 3 + 0].toString() + ", " +
                    coords[i * 3 + 1] + ", " +
                    coords[i * 3 + 2] + ", "
            )
        }
    }

    private fun testException(coords: DoubleArray, msg: String) {
        val hull = QuickHull3D()
        var ex: Exception? = null
        try {
            hull.build(coords)
        } catch (e: Exception) {
            ex = e
        }
        if (ex == null) {
            println("Expected exception $msg")
            println("Got no exception")
            println("Input pnts:")
            printCoords(coords)
            exitProcess(1)
        } else if (ex.message == null ||
            ex.message != msg
        ) {
            println("Expected exception $msg")
            println("Got exception " + ex.message)
            println("Input pnts:")
            printCoords(coords)
            exitProcess(1)
        }
    }

    @Throws(Exception::class)
    fun test(coords: DoubleArray?, checkFaces: Array<IntArray>?) {
        val rpyList = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(10.0, 20.0, 30.0),
            doubleArrayOf(-45.0, 60.0, 91.0),
            doubleArrayOf(125.0, 67.0, 81.0)
        )
        val xcoords = DoubleArray(coords!!.size)
        singleTest(coords, checkFaces)
        if (testRotation) {
            for (i in rpyList.indices) {
                val rpy = rpyList[i]
                rotateCoords(
                    xcoords, coords,
                    Math.toRadians(rpy[0]),
                    Math.toRadians(rpy[1]),
                    Math.toRadians(rpy[2])
                )
                singleTest(xcoords, checkFaces)
            }
        }
    }

    /**
     * Runs a set of explicit and random tests on QuickHull3D,
     * and prints `Passed` to System.out if all is well.
     */
    fun explicitAndRandomTests() {
        try {
            var coords: DoubleArray? = null
            println(
                "Testing degenerate input ..."
            )
            for (dimen in 0..2) {
                for (i in 0..9) {
                    coords = randomDegeneratePoints(10, dimen)
                    if (dimen == 0) {
                        testException(
                            coords, "Input points appear to be coincident"
                        )
                    } else if (dimen == 1) {
                        testException(
                            coords, "Input points appear to be colinear"
                        )
                    } else if (dimen == 2) {
                        testException(
                            coords, "Input points appear to be coplanar"
                        )
                    }
                }
            }
            println(
                "Explicit tests ..."
            )

            // test cases furnished by Mariano Zelke, Berlin
            coords = doubleArrayOf(
                21.0,
                0.0,
                0.0,
                0.0,
                21.0,
                0.0,
                0.0,
                0.0,
                0.0,
                18.0,
                2.0,
                6.0,
                1.0,
                18.0,
                5.0,
                2.0,
                1.0,
                3.0,
                14.0,
                3.0,
                10.0,
                4.0,
                14.0,
                14.0,
                3.0,
                4.0,
                10.0,
                10.0,
                6.0,
                12.0,
                5.0,
                10.0,
                15.0
            )
            test(coords, null)
            coords = doubleArrayOf(
                0.0, 0.0, 0.0,
                21.0, 0.0, 0.0,
                0.0, 21.0, 0.0,
                2.0, 1.0, 2.0,
                17.0, 2.0, 3.0,
                1.0, 19.0, 6.0,
                4.0, 3.0, 5.0,
                13.0, 4.0, 5.0,
                3.0, 15.0, 8.0,
                6.0, 5.0, 6.0,
                9.0, 6.0, 11.0
            )
            test(coords, null)
            println(
                "Testing 20 to 200 random points ..."
            )
            run {
                var n = 20
                while (n < 200) {
                    // System.out.println (n);
                    for (i in 0..9) {
                        coords = randomPoints(n, 1.0)
                        test(coords, null)
                    }
                    n += 10
                }
            }
            println(
                "Testing 20 to 200 random points in a sphere ..."
            )
            run {
                var n = 20
                while (n < 200) {
                    // System.out.println (n);
                    for (i in 0..9) {
                        coords = randomSphericalPoints(n, 1.0)
                        test(coords, null)
                    }
                    n += 10
                }
            }
            println(
                "Testing 20 to 200 random points clipped to a cube ..."
            )
            run {
                var n = 20
                while (n < 200) {
                    // System.out.println (n);
                    for (i in 0..9) {
                        coords = randomCubedPoints(n, 1.0, 0.5)
                        test(coords, null)
                    }
                    n += 10
                }
            }
            println(
                "Testing 8 to 1000 randomly shuffled points on a grid ..."
            )
            for (n in 2..10) { // System.out.println (n*n*n);
                for (i in 0..9) {
                    coords = randomGridPoints(n, 4.0)
                    test(coords, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            exitProcess(1)
        }
        println("\nPassed\n")
    }

    /**
     * Runs timing tests on QuickHull3D, and prints
     * the results to System.out.
     */
    fun timingTests() {
        var t0: Long
        var t1: Long
        var n = 10
        val hull = QuickHull3D()
        println("warming up ... ")
        for (i in 0..1) {
            val coords = randomSphericalPoints(10000, 1.0)
            hull.build(coords)
        }
        val cnt = 10
        for (i in 0..3) {
            n *= 10
            val coords = randomSphericalPoints(n, 1.0)
            t0 = System.currentTimeMillis()
            for (k in 0 until cnt) {
                hull.build(coords)
            }
            t1 = System.currentTimeMillis()
            println(
                n.toString() + " points: " + (t1 - t0) / cnt.toDouble() +
                    " msec"
            )
        }
    }

    companion object {
        private const val DOUBLE_PREC = 2.2204460492503131e-16
        var triangulate = false
        private var doTesting = true
        private var doTiming = false
        var debugEnable = false
        const val NO_DEGENERACY = 0
        const val EDGE_DEGENERACY = 1
        const val VERTEX_DEGENERACY = 2
        var testRotation = true
        var degeneracyTest = VERTEX_DEGENERACY
        var epsScale = 2.0

        /**
         * Runs a set of tests on the QuickHull3D class, and
         * prints `Passed` if all is well.
         * Otherwise, an error message and stack trace
         * are printed.
         *
         *
         * If the option `-timing` is supplied,
         * then timing information is produced instead.
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val tester = QuickHull3DTest()
            for (i in args.indices) {
                if (args[i] == "-timing") {
                    doTiming = true
                    doTesting = false
                } else {
                    println(
                        "Usage: java quickhull3d.QuickHull3DTest [-timing]"
                    )
                    exitProcess(1)
                }
            }
            if (doTesting) {
                tester.explicitAndRandomTests()
            }
            if (doTiming) {
                tester.timingTests()
            }
        }
    }

    /**
     * Creates a testing object.
     */
    init {
        rand.setSeed(0x1234)
    }
}

package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertFiniteMesh
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVolumeClose
import org.junit.Test

class CSGBooleanTest {
    @Test
    fun singleOverloadsProduceExpectedVolumes() {
        val a = cubeAtX(0.0)
        val b = cubeAtX(1.0)

        val union = a.union(b)
        val difference = a.difference(b)
        val intersection = a.intersect(b)

        assertFiniteMesh(union)
        assertFiniteMesh(difference)
        assertFiniteMesh(intersection)
        assertVolumeClose(12.0, union.computeVolume(), absoluteTolerance = 1e-6, relativeTolerance = 1e-4)
        assertVolumeClose(4.0, difference.computeVolume(), absoluteTolerance = 1e-6, relativeTolerance = 1e-4)
        assertVolumeClose(4.0, intersection.computeVolume(), absoluteTolerance = 1e-6, relativeTolerance = 1e-4)
    }

    @Test
    fun listOverloadsMatchSequentialEquivalentOperations() {
        val a = cubeAtX(0.0)
        val b = cubeAtX(1.0)
        val c = cubeAtX(2.0)

        val unionList = a.union(listOf(b, c))
        val unionExpected = a.union(b).union(c)
        assertVolumeClose(unionExpected.computeVolume(), unionList.computeVolume(), relativeTolerance = 1e-4)

        val differenceList = a.difference(listOf(b, c))
        val differenceExpected = a.difference(b.union(c))
        assertVolumeClose(differenceExpected.computeVolume(), differenceList.computeVolume(), relativeTolerance = 1e-4)

        val intersectList = a.intersect(listOf(b, c))
        val intersectExpected = a.intersect(b.union(c))
        assertVolumeClose(intersectExpected.computeVolume(), intersectList.computeVolume(), relativeTolerance = 1e-4)
    }

    @Test
    fun varargOverloadsMatchListOverloads() {
        val a = cubeAtX(0.0)
        val b = cubeAtX(1.0)
        val c = cubeAtX(2.0)

        val unionVararg = a.union(b, c)
        val unionList = a.union(listOf(b, c))
        assertVolumeClose(unionList.computeVolume(), unionVararg.computeVolume(), relativeTolerance = 1e-4)

        val differenceVararg = a.difference(b, c)
        val differenceList = a.difference(listOf(b, c))
        assertVolumeClose(differenceList.computeVolume(), differenceVararg.computeVolume(), relativeTolerance = 1e-4)

        val intersectVararg = a.intersect(b, c)
        val intersectList = a.intersect(listOf(b, c))
        assertVolumeClose(intersectList.computeVolume(), intersectVararg.computeVolume(), relativeTolerance = 1e-4)
    }

    private fun cubeAtX(centerX: Double): CSG {
        return Cube(
            center = Vector3d.xyz(centerX, 0.0, 0.0),
            dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
        ).toCSG()
    }
}

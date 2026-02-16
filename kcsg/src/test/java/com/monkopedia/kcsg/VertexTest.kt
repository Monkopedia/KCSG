package com.monkopedia.kcsg

import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

class VertexTest {
    @Test
    fun flipAndInterpolate() {
        val vertex = Vertex(
            pos = Vector3d.xyz(1.0, 2.0, 3.0),
            normal = Vector3d.xyz(0.0, 0.0, 1.0),
        )
        vertex.flip()
        assertVectorClose(Vector3d.xyz(0.0, 0.0, -1.0), vertex.normal, EPS)

        val other = Vertex(
            pos = Vector3d.xyz(5.0, 6.0, 7.0),
            normal = Vector3d.xyz(1.0, 0.0, 0.0),
        )
        val interpolated = vertex.interpolate(other, 0.25)
        assertVectorClose(Vector3d.xyz(2.0, 3.0, 4.0), interpolated.pos, EPS)
        assertVectorClose(Vector3d.xyz(0.25, 0.0, -0.75), interpolated.normal, EPS)
        assertEquals(1.0, interpolated.weight, EPS)
    }

    @Test
    fun stlAndObjFormattingOverloads() {
        val vertex = Vertex(
            pos = Vector3d.xyz(1.25, -2.5, 3.75),
            normal = Vector3d.Z_ONE,
        )

        val stlString = vertex.toStlString()
        val stlBuilder = vertex.toStlString(StringBuilder()).toString()
        assertEquals("vertex 1.25 -2.5 3.75", stlString)
        assertEquals(stlString, stlBuilder)

        val objString = vertex.toObjString()
        val objBuilder = vertex.toObjString(StringBuilder()).toString()
        assertEquals("v 1.25 -2.5 3.75\n", objString)
        assertEquals(objString, objBuilder)
    }

    @Test
    fun transformAndTransformedUseWeight() {
        val transform = Transform.unity().translate(10.0, 0.0, -4.0)
        val weighted = Vertex(
            pos = Vector3d.xyz(1.0, 2.0, 3.0),
            normal = Vector3d.Y_ONE,
            weight = 0.5,
        )

        val mutated = weighted.transform(transform)
        assertSame(weighted, mutated)
        assertVectorClose(Vector3d.xyz(6.0, 2.0, 1.0), weighted.pos, EPS)

        val original = Vertex(
            pos = Vector3d.xyz(1.0, 2.0, 3.0),
            normal = Vector3d.Y_ONE,
            weight = 0.5,
        )
        val transformed = original.transformed(transform)
        assertVectorClose(Vector3d.xyz(6.0, 2.0, 1.0), transformed.pos, EPS)
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 3.0), original.pos, EPS)
    }

    @Test
    fun equalityHashCodeAndToStringDependOnPosition() {
        val base = Vertex(
            pos = Vector3d.xyz(1.0, 2.0, 3.0),
            normal = Vector3d.X_ONE,
            weight = 1.0,
        )
        val samePosDifferentOtherFields = Vertex(
            pos = Vector3d.xyz(1.0, 2.0, 3.0),
            normal = Vector3d.Z_ONE,
            weight = 0.25,
        )
        val differentPos = Vertex(
            pos = Vector3d.xyz(1.0, 2.0, 4.0),
            normal = Vector3d.X_ONE,
            weight = 1.0,
        )

        assertEquals(base, samePosDifferentOtherFields)
        assertEquals(base.hashCode(), samePosDifferentOtherFields.hashCode())
        assertNotEquals(base, differentPos)
        assertEquals(base.pos.toString(), base.toString())
    }

    companion object {
        private const val EPS = 1e-9
    }
}

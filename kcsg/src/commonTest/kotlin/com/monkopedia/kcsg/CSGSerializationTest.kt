package com.monkopedia.kcsg

import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CSGSerializationTest {
    @Test
    fun toStlStringOverloadsMatch() {
        val csg = Cube(1.0).toCSG()

        val direct = csg.toStlString()
        val viaBuilder = csg.toStlString(StringBuilder()).toString()

        assertEquals(direct, viaBuilder)
        assertTrue(direct.startsWith("solid v3d.csg"))
        assertTrue(direct.endsWith("endsolid v3d.csg\n"))
    }

    @Test
    fun toObjProducesObjAndMtlOutput() {
        val csg = Cube(1.0).toCSG()
        val objFile = csg.toObj()
        val lines = objFile.obj.lineSequence().toList()

        assertEquals("mtllib ${ObjFile.MTL_NAME}", lines[0])
        assertEquals("# Group", lines[1])
        assertTrue(objFile.obj.contains("g v3d.csg"))
        assertTrue(objFile.obj.contains("\nv "))
        assertTrue(objFile.obj.contains("\nf "))

        assertTrue(objFile.mtl.contains("newmtl"))
        assertTrue(objFile.mtl.contains("Kd "))
        assertEquals(objFile.obj, objFile.objSource.readString())
        assertEquals(objFile.mtl, objFile.mtlSource.readString())
    }

    @Test
    fun toObjStringContainsGroupAndVertexSections() {
        val csg = Cube(1.0).toCSG()
        val objString = csg.toObjString()

        assertTrue(objString.contains("# Group"))
        assertTrue(objString.contains("g v3d.csg"))
        assertTrue(objString.contains("# Vertices"))
        assertTrue(objString.contains("\nv "))
        assertTrue(objString.contains("# Faces"))
        assertTrue(objString.contains("\nf "))
    }

    @Test
    fun toObjStringOmitsMaterialLibraryDirective() {
        val objString = Cube(1.0).toCSG().color(KcsgColor.BLUE).toObjString()

        assertEquals("# Group", objString.lineSequence().first())
        assertTrue(objString.lineSequence().none { it.startsWith("mtllib") })
    }

    @Test
    fun colorInfluencesMaterialOutputAndUnsupportedObjArgThrows() {
        val csg = Cube(1.0).toCSG()
        val colored = csg.color(KcsgColor.BLUE)

        assertSame(csg, colored)
        assertEquals(listOf(BLUE_MATERIAL), csg.toObj().mtl.materialColors())

        assertFailsWith<UnsupportedOperationException> {
            colored.toObj(maxNumberOfVerts = 4)
        }
    }

    @Test
    fun colorAppliesToBooleanOperationResult() {
        val union = cubeAtX(0.0).union(cubeAtX(1.0))

        union.color(KcsgColor.BLUE)

        assertEquals(listOf(BLUE_MATERIAL), union.toObj().mtl.materialColors())
    }

    @Test
    fun chainedColorCallsKeepTheLastColor() {
        val csg = Cube(1.0).toCSG()

        csg.color(KcsgColor.RED).color(KcsgColor.BLUE)

        assertEquals(listOf(BLUE_MATERIAL), csg.toObj().mtl.materialColors())
    }

    @Test
    fun coloringACopyLeavesTheSourceUntouched() {
        val csg = Cube(1.0).toCSG().color(KcsgColor.RED)

        val colored = csg.copy().color(KcsgColor.BLUE)

        assertEquals(listOf(BLUE_MATERIAL), colored.toObj().mtl.materialColors())
        assertEquals(listOf(RED_MATERIAL), csg.toObj().mtl.materialColors())
    }

    private fun cubeAtX(centerX: Double): CSG {
        return Cube(
            center = Vector3d.xyz(centerX, 0.0, 0.0),
            dimensions = Vector3d.xyz(2.0, 2.0, 2.0),
        ).toCSG()
    }

    private fun String.materialColors(): List<String> {
        return lineSequence().filter { it.startsWith("Kd ") }.toList()
    }

    private companion object {
        const val BLUE_MATERIAL = "Kd 0.0 0.0 1.0"
        const val RED_MATERIAL = "Kd 1.0 0.0 0.0"
    }
}

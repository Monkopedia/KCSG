package com.monkopedia.kcsg

import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
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

        assertTrue(objFile.obj.contains("mtllib"))
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
    fun colorInfluencesMaterialOutputAndUnsupportedObjArgThrows() {
        val csg = Cube(1.0).toCSG()
        val colored = csg.color(KcsgColor.BLUE)

        assertNotSame(csg, colored)
        val mtl = colored.toObj().mtl
        assertTrue(mtl.contains("0.0 0.0 1.0"))

        assertFailsWith<UnsupportedOperationException> {
            colored.toObj(maxNumberOfVerts = 4)
        }
    }
}

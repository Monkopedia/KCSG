package com.monkopedia.kcsg

import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
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

        assertEquals(objFile.obj, objFile.objSource.readString())
        assertEquals(objFile.mtl, objFile.mtlSource.readString())
    }

    @Test
    fun uncoloredToObjIsReproducibleAndHasNoMaterials() {
        // Regression for #46: every PropertyStorage used to get a random colour, so the .mtl of
        // the same uncoloured model differed between exports and usemtl was always emitted.
        val first = Cube(2.0).toCSG().toObj()
        val second = Cube(2.0).toCSG().toObj()

        assertEquals(first.obj, second.obj)
        assertEquals(first.mtl, second.mtl)
        assertEquals("", first.mtl)
        assertTrue(first.obj.lineSequence().none { it.startsWith("usemtl") })
    }

    @Test
    fun coloredToObjEmitsMaterialAndUsemtl() {
        val objFile = Cube(1.0).toCSG().color(KcsgColor.RED).toObj()

        assertTrue(objFile.mtl.contains("newmtl material-1"))
        assertEquals(listOf(RED_MATERIAL), objFile.mtl.materialColors())
        assertTrue(objFile.obj.lineSequence().any { it == "usemtl material-1" })
    }

    @Test
    fun mixedColoredAndUncoloredToObjGivesEveryFaceAMaterial() {
        // Regression for #46 review: usemtl persists until the next one, so skipping it for
        // uncoloured faces made them inherit the preceding colour.
        fun export() = Cube(1.0).toCSG().color(KcsgColor.RED)
            .union(
                Cube(
                    center = Vector3d.xyz(5.0, 0.0, 0.0),
                    dimensions = Vector3d.xyz(1.0, 1.0, 1.0),
                ).toCSG(),
            )
            .toObj()
        val first = export()
        val second = export()

        val kdByMaterial = mutableMapOf<String, String>()
        var current: String? = null
        first.mtl.lineSequence().forEach { line ->
            when {
                line.startsWith("newmtl ") -> current = line.removePrefix("newmtl ")
                line.startsWith("Kd ") -> kdByMaterial[assertNotNull(current)] = line
            }
        }
        var active: String? = null
        val facesByKd = mutableMapOf<String, Int>()
        first.obj.lineSequence().forEach { line ->
            when {
                line.startsWith("usemtl ") -> active = line.removePrefix("usemtl ")
                line.startsWith("f ") -> {
                    val kd = kdByMaterial[active]
                    assertNotNull(kd, "face '$line' uses material $active with no Kd")
                    facesByKd[kd] = (facesByKd[kd] ?: 0) + 1
                }
            }
        }
        // The plain cube's 12 faces must not inherit the red cube's material.
        assertEquals(mapOf(RED_MATERIAL to 12, DEFAULT_MATERIAL to 12), facesByKd)
        assertEquals(first.obj, second.obj)
        assertEquals(first.mtl, second.mtl)
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
        const val DEFAULT_MATERIAL = "Kd 0.8 0.8 0.8"
    }
}

package com.monkopedia.kcsg

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Mesh text must be byte-identical on every target and match the JVM (#41). Expected strings
 * are what `java.lang.Double.toString` prints; every value asserted through the public export
 * path prints the same on JDK 8 and JDK 19+, so these hold on the JVM test runtime as well.
 */
class DoubleFormattingTest {
    /** The measured per-target matrix from #41: value to the JVM (and linuxX64) rendering. */
    private val matrix = listOf(
        1.0 to "1.0",
        0.0 to "0.0",
        -0.0 to "-0.0",
        -5.0 to "-5.0",
        1.25 to "1.25",
        -2.5 to "-2.5",
        0.1 to "0.1",
        1.0 / 3.0 to "0.3333333333333333",
        0.1 + 0.2 to "0.30000000000000004",
        1e-7 to "1.0E-7",
        1e7 to "1.0E7",
        1e20 to "1.0E20",
        1e21 to "1.0E21",
        Double.MAX_VALUE to "1.7976931348623157E308",
        Double.MIN_VALUE to "4.9E-324",
    )

    @Test
    fun toJvmStringMatchesJvmForMeasuredMatrix() {
        for ((value, expected) in matrix) {
            assertEquals(expected, value.toJvmString(), "rendering of $expected")
        }
        // Kotlin/Native's own toString prints 5.0E-323 here; JDK 8 and JDK 19+ print 4.9E-323.
        assertEquals("4.9E-323", (Double.MIN_VALUE * 10).toJvmString())
        // Kotlin/Wasm's own toString prints 489.98447860564397, which is not the closest.
        assertEquals("489.984478605644", Double.fromBits(4647327514298117540L).toJvmString())
    }

    @Test
    fun vectorStlAndObjStringsMatchJvmForMeasuredMatrix() {
        for ((value, expected) in matrix) {
            val v = Vector3d.xyz(value, -value, value)
            val negated = if (expected.startsWith("-")) expected.substring(1) else "-$expected"
            val line = "$expected $negated $expected"
            assertEquals(line, v.toStlString(), "STL rendering of $expected")
            assertEquals(line, v.toObjString(), "OBJ rendering of $expected")
        }
        assertEquals("1.0 2.0 3.0", Vector3d.xyz(1.0, 2.0, 3.0).toStlString())
        assertEquals("1.0 2.0 3.0", Vector3d.xyz(1.0, 2.0, 3.0).toObjString())
    }

    @Test
    fun vertexAndCsgExportsUseJvmRendering() {
        assertEquals(
            "vertex 1.0 1.0E7 -0.0",
            Vertex(Vector3d.xyz(1.0, 1e7, -0.0), Vector3d.Z_ONE).toStlString(),
        )
        assertEquals(
            "v 1.0E-7 -5.0 0.0\n",
            Vertex(Vector3d.xyz(1e-7, -5.0, 0.0), Vector3d.Z_ONE).toObjString(),
        )

        val allowed = setOf("1.0", "-1.0", "0.0", "-0.0")
        val stl = Cube(2.0).toCSG().toStlString()
        assertTrue(stl.contains("vertex 1.0 1.0 1.0"), stl)
        stl.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("vertex ") || it.startsWith("facet normal ") }
            .flatMap { it.removePrefix("facet normal ").removePrefix("vertex ").split(' ') }
            .forEach { assertTrue(it in allowed, "unexpected STL number '$it'") }

        val obj = Cube(2.0).toCSG().toObjString()
        assertTrue(obj.contains("\nv -1.0 -1.0 -1.0\n"), obj)
    }

    @Test
    fun mtlColourUsesJvmRendering() {
        val mtl = Cube(1.0).toCSG().color(KcsgColor(1e-7, 0.5, 1.0)).toObj().mtl
        assertTrue(mtl.contains("Kd 1.0E-7 0.5 1.0\n"), mtl)
    }

    /**
     * The exact algorithm used on Wasm and Native, checked on every target against JDK 19+
     * `Double.toString`, including values where Kotlin/Wasm's own digits are not the closest,
     * Kotlin/Native's are not JDK 19+'s, or JDK 8 prints a non-shortest decimal.
     */
    @Test
    fun shortestJavaStringMatchesJdk19Specification() {
        val cases = listOf(
            1.0 to "1.0",
            -5.0 to "-5.0",
            1e-7 to "1.0E-7",
            1e7 to "1.0E7",
            1e20 to "1.0E20",
            1e21 to "1.0E21",
            1e23 to "1.0E23",
            2e23 to "2.0E23",
            0.001 to "0.001",
            9999999.0 to "9999999.0",
            0.1 + 0.2 to "0.30000000000000004",
            Double.MAX_VALUE to "1.7976931348623157E308",
            Double.MIN_VALUE to "4.9E-324",
            Double.MIN_VALUE * 2 to "9.9E-324",
            Double.MIN_VALUE * 10 to "4.9E-323",
            Double.MIN_VALUE * 32 to "1.6E-322",
            Double.fromBits(2048L) to "1.012E-320",
            Double.fromBits(705506098195459236L) to "1.6806711546222698E-261",
            Double.fromBits(4863202847240795136L) to "1.3315918296640717E17",
            Double.fromBits(0x0010000000000000L) to "2.2250738585072014E-308",
            Double.fromBits(0x000FFFFFFFFFFFFFL) to "2.225073858507201E-308",
        )
        for ((value, expected) in cases) {
            assertEquals(expected, shortestJavaString(value), "bits ${value.toRawBits()}")
        }
    }

    /** ECMAScript renderings, as Kotlin/JS produces them, re-laid-out on every target. */
    @Test
    fun formatJavaFromEcmaScriptConvertsJsRenderings() {
        val cases = listOf(
            Triple(1.0, "1", "1.0"),
            Triple(-5.0, "-5", "-5.0"),
            Triple(1.25, "1.25", "1.25"),
            Triple(0.001, "0.001", "0.001"),
            Triple(1e-7, "1e-7", "1.0E-7"),
            Triple(1e7, "10000000", "1.0E7"),
            Triple(1e20, "100000000000000000000", "1.0E20"),
            Triple(1e21, "1e+21", "1.0E21"),
            Triple(-1.5e300, "-1.5e+300", "-1.5E300"),
            Triple(Double.MAX_VALUE, "1.7976931348623157e+308", "1.7976931348623157E308"),
            Triple(Double.MIN_VALUE, "5e-324", "4.9E-324"),
            Triple(Double.MIN_VALUE * 2, "1e-323", "9.9E-324"),
            Triple(0.0001234, "0.0001234", "1.234E-4"),
        )
        for ((value, js, expected) in cases) {
            assertEquals(expected, formatJavaFromEcmaScript(value, js), "JS rendering $js")
        }
    }

    /** In the plain-decimal range meshes live in, the exact algorithm agrees with the target. */
    @Test
    fun exactAlgorithmAgreesWithTargetInMeshRange() {
        var seed = 0x2545F4914F6CDD1DL
        repeat(5_000) {
            seed = seed * 6364136223846793005L + 1442695040888963407L
            val unit = (seed ushr 11).toDouble() / (1L shl 53).toDouble()
            val value = (unit - 0.5) * 2000.0
            if (value != 0.0) {
                assertEquals(shortestJavaString(value), value.toJvmString(), "bits ${value.toRawBits()}")
            }
        }
    }
}

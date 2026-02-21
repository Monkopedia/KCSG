package com.monkopedia.kcsg.ext.org.poly2tri

import com.monkopedia.kcsg.Vector3d
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PolygonInputValidationTest {
    @Test
    fun constructorRejectsFewerThanThreePoints() {
        val p1 = PolygonPoint(Vector3d.xyz(0.0, 0.0, 0.0))
        val p2 = PolygonPoint(Vector3d.xyz(1.0, 0.0, 0.0))

        assertFailsWith<IllegalArgumentException> {
            Polygon(mutableListOf(p1, p2))
        }
    }

    @Test
    fun constructorRejectsDuplicateFirstAndLastWhenThatDropsBelowThreePoints() {
        val p1 = PolygonPoint(Vector3d.xyz(0.0, 0.0, 0.0))
        val p2 = PolygonPoint(Vector3d.xyz(1.0, 0.0, 0.0))

        assertFailsWith<IllegalArgumentException> {
            Polygon(mutableListOf(p1, p2, p1))
        }
    }
}

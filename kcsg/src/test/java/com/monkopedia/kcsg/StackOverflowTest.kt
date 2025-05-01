package com.monkopedia.kcsg

import com.monkopedia.kcsg.CSG.Companion.fromPolygons
import org.junit.Test

class StackOverflowTest  {

    @Test
    fun testOverflow() {

        val polys = listOf(Polygon(
            listOf(
        Vertex(Vector3d(1.23079, 0.35525, -0.39928), Vector3d(0.0, 0.0, 0.0)),
        Vertex(Vector3d(1.26563, 0.28906, -0.40625), Vector3d(0.0, 0.0, 0.0)),
        Vertex(Vector3d(1.24346, 0.44424, -0.41733), Vector3d(0.0, 0.0, 0.0))
            )
        ),
Polygon(
    listOf(
        Vertex(Vector3d(1.26216, 0.46369, -0.43822), Vector3d(0.0, 0.0, 0.0)),
        Vertex(Vector3d(1.36719, 0.29688, -0.50000), Vector3d(0.0, 0.0, 0.0)),
        Vertex(Vector3d(1.35204, 0.32133, -0.34461), Vector3d(0.0, 0.0, 0.0)),
        Vertex(Vector3d(1.25679, 0.47373, -0.34461), Vector3d(0.0, 0.0, 0.0)),
        )
        ))
        val res = fromPolygons(polys);

        var a = Node(res.polygons);
        var result = a.allPolygons()

    }
}

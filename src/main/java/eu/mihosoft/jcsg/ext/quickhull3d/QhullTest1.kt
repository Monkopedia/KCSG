package eu.mihosoft.jcsg.ext.quickhull3d

internal object QhullTest {
    var coords = doubleArrayOf()
    var faces = arrayOf<IntArray>()
    @JvmStatic
    fun main(args: Array<String>) {
        var hull = QuickHull3D()
        val tester = QuickHull3DTest()
        hull = QuickHull3D()
        for (i in 0..99) {
            var pnts = tester.randomCubedPoints(100, 1.0, 0.5)
            hull.setFromQhull(pnts, pnts.size / 3,  /*triangulated=*/false)
            pnts = tester.addDegeneracy(
                QuickHull3DTest.Companion.VERTEX_DEGENERACY, pnts, hull
            )

//	      hull = new QuickHull3D ();
            hull.setFromQhull(pnts, pnts.size / 3,  /*triangulated=*/true)
            if (!hull.check(System.out)) {
                println("failed for qhull triangulated")
            }

//	      hull = new QuickHull3D ();
            hull.setFromQhull(pnts, pnts.size / 3,  /*triangulated=*/false)
            if (!hull.check(System.out)) {
                println("failed for qhull regular")
            }

// 	      hull = new QuickHull3D ();
            hull.build(pnts, pnts.size / 3)
            hull.triangulate()
            if (!hull.check(System.out)) {
                println("failed for QuickHull3D triangulated")
            }

// 	      hull = new QuickHull3D ();
            hull.build(pnts, pnts.size / 3)
            if (!hull.check(System.out)) {
                println("failed for QuickHull3D regular")
            }
        }
    }
}
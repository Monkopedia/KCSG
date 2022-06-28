package eu.mihosoft.jcsg.ext.quickhull3d

/**
 * Simple example usage of QuickHull3D. Run as the command
 * <pre>
 * java quickhull3d.SimpleExample
</pre> *
 */
internal object SimpleExample {
    /**
     * Run for a simple demonstration of QuickHull3D.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // x y z coordinates of 6 points
        val points = arrayOf(
            Point3d(0.0, 0.0, 0.0),
            Point3d(1.0, 0.5, 0.0),
            Point3d(2.0, 0.0, 0.0),
            Point3d(0.5, 0.5, 0.5),
            Point3d(0.0, 0.0, 2.0),
            Point3d(0.1, 0.2, 0.3),
            Point3d(0.0, 2.0, 0.0)
        )
        val hull = QuickHull3D()
        hull.build(points)
        println("Vertices:")
        val vertices = hull.vertices
        for (i in vertices!!.indices) {
            val pnt = vertices!![i]
            println(pnt!!.x.toString() + " " + pnt!!.y + " " + pnt!!.z)
        }
        println("Faces:")
        val faceIndices = hull.getFaces()
        for (i in vertices!!.indices) {
            for (k in 0 until faceIndices[i]!!.size) {
                print(faceIndices[i]!![k].toString() + " ")
            }
            println("")
        }
    }
}
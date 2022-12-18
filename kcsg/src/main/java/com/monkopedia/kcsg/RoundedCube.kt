/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.monkopedia.kcsg

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
data class RoundedCube(
    var center: Vector3d = Vector3d.xyz(0.0, 0.0, 0.0),
    /**
     * Cube dimensions.
     */
    var dimensions: Vector3d = Vector3d.xyz(1.0, 1.0, 1.0),
    var cornerRadius: Double = 0.1,
    var resolution: Int = 8,
    var centered: Boolean = true
) : Primitive {
    private val properties = PropertyStorage()

    /**
     * Constructor. Creates a new rounded cube with center `[0,0,0]` and
     * dimensions `[size,size,size]`.
     *
     * @param size size
     */
    constructor(size: Double) : this(
        center = Vector3d.xyz(0.0, 0.0, 0.0),
        dimensions = Vector3d.xyz(size, size, size)
    )

    /**
     * Constructor. Creates a new rounded cuboid with center `[0,0,0]` and
     * with the specified dimensions.
     *
     * @param w width
     * @param h height
     * @param d depth
     */
    constructor(w: Double, h: Double, d: Double) : this(Vector3d.ZERO, Vector3d.xyz(w, h, d))

    override fun toPolygons(): List<Polygon> {
        val spherePrototype =
            Sphere(cornerRadius, resolution * 2, resolution).toCSG()
        val x = dimensions.x / 2.0 - cornerRadius
        val y = dimensions.y / 2.0 - cornerRadius
        val z = dimensions.z / 2.0 - cornerRadius
        val sphere1 = spherePrototype.transformed(Transform.unity().translate(-x, -y, -z))
        val sphere2 = spherePrototype.transformed(Transform.unity().translate(x, -y, -z))
        val sphere3 = spherePrototype.transformed(Transform.unity().translate(x, y, -z))
        val sphere4 = spherePrototype.transformed(Transform.unity().translate(-x, y, -z))
        val sphere5 = spherePrototype.transformed(Transform.unity().translate(-x, -y, z))
        val sphere6 = spherePrototype.transformed(Transform.unity().translate(x, -y, z))
        val sphere7 = spherePrototype.transformed(Transform.unity().translate(x, y, z))
        val sphere8 = spherePrototype.transformed(Transform.unity().translate(-x, y, z))
        val result = sphere1.union(
            sphere2, sphere3, sphere4,
            sphere5, sphere6, sphere7, sphere8
        ).hull().polygons
        val locTransform = Transform.unity().translate(center)
        for (p in result) {
            p.transform(locTransform)
        }
        if (!centered) {
            val centerTransform = Transform.unity().translate(
                dimensions.x / 2.0,
                dimensions.y / 2.0,
                dimensions.z / 2.0
            )
            for (p in result) {
                p.transform(centerTransform)
            }
        }
        return result
    }

    override fun getProperties(): PropertyStorage {
        return properties
    }

    /**
     * Defines that this cube will not be centered.
     *
     * @return this cube
     */
    fun noCenter(): RoundedCube {
        centered = false
        return this
    }
}
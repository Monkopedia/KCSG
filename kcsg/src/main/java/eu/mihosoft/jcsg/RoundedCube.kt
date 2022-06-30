/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class RoundedCube : Primitive {
    /**
     * Cube dimensions.
     */
    private var dimensions: Vector3d
    private var center: Vector3d
    private var centered = true
    private val properties = PropertyStorage()
    private var cornerRadius = 0.1
    private var resolution = 8

    /**
     * Constructor. Creates a new rounded cube with center `[0,0,0]` and
     * dimensions `[1,1,1]`.
     */
    constructor() {
        center = Vector3d.xyz(0.0, 0.0, 0.0)
        dimensions = Vector3d.xyz(1.0, 1.0, 1.0)
    }

    /**
     * Constructor. Creates a new rounded cube with center `[0,0,0]` and
     * dimensions `[size,size,size]`.
     *
     * @param size size
     */
    constructor(size: Double) {
        center = Vector3d.xyz(0.0, 0.0, 0.0)
        dimensions = Vector3d.xyz(size, size, size)
    }

    /**
     * Constructor. Creates a new rounded cuboid with the specified center and
     * dimensions.
     *
     * @param center center of the cuboid
     * @param dimensions cube dimensions
     */
    constructor(center: Vector3d, dimensions: Vector3d) {
        this.center = center
        this.dimensions = dimensions
    }

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
            Sphere(getCornerRadius(), getResolution() * 2, getResolution()).toCSG()
        val x = dimensions.x() / 2.0 - getCornerRadius()
        val y = dimensions.y() / 2.0 - getCornerRadius()
        val z = dimensions.z() / 2.0 - getCornerRadius()
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
                dimensions.x() / 2.0,
                dimensions.y() / 2.0,
                dimensions.z() / 2.0
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
     * @return the center
     */
    fun getCenter(): Vector3d {
        return center
    }

    /**
     * @param center the center to set
     */
    fun setCenter(center: Vector3d) {
        this.center = center
    }

    /**
     * @return the dimensions
     */
    fun getDimensions(): Vector3d {
        return dimensions
    }

    /**
     * @param dimensions the dimensions to set
     */
    fun setDimensions(dimensions: Vector3d) {
        this.dimensions = dimensions
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

    /**
     * @return the resolution
     */
    private fun getResolution(): Int {
        return resolution
    }

    /**
     * @param resolution the resolution to set
     */
    fun setResolution(resolution: Int) {
        this.resolution = resolution
    }

    /**
     * @param resolution the resolution to set
     * @return this cube
     */
    fun resolution(resolution: Int): RoundedCube {
        this.resolution = resolution
        return this
    }

    /**
     * @return the corner radius
     */
    private fun getCornerRadius(): Double {
        return cornerRadius
    }

    /**
     * @param cornerRadius the corner radius to set
     */
    fun setCornerRadius(cornerRadius: Double) {
        this.cornerRadius = cornerRadius
    }

    /**
     * @param cornerRadius the corner radius to set
     * @return this cube
     */
    fun cornerRadius(cornerRadius: Double): RoundedCube {
        this.cornerRadius = cornerRadius
        return this
    }
}
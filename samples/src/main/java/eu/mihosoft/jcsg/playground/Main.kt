/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.playground

import eu.mihosoft.jcsg.Bounds
import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Cube
import eu.mihosoft.jcsg.ObjFile
import eu.mihosoft.jcsg.Polygon
import eu.mihosoft.jcsg.STL
import eu.mihosoft.jcsg.Sphere
import eu.mihosoft.vvecmath.Plane
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.function.Predicate
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
object Main {
    private const val EPS = 1e-8

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        testCut()
        val c1 = Cube(Vector3d.zero(), Vector3d.xyz(1.0, 1.0, 1.0)).toCSG()
        var c2 = Cube(Vector3d.xyz(1.0, 1.0, 1.0), Vector3d.xyz(2.0, 2.0, 2.0)).toCSG()
            .transformed(Transform.unity().rot(Vector3d.ZERO, Vector3d.UNITY, 78.0))

//        Files.write(Paths.get("c1.stl"), c1.toStlString().getBytes());
//        Files.write(Paths.get("c2.stl"), c2.toStlString().getBytes());
//        c1 = STL.file(Paths.get("c1.stl"));
//        c2 = STL.file(Paths.get("c2.stl"));
//        c1 = new Sphere(Vector3d.x(0.), 0.5, 16, 16).toCSG();
//        c2 = new Sphere(Vector3d.x(0.6), 0.5, 16, 16).toCSG();
        c2 = Sphere(Vector3d.x(0.0), 0.65, 16, 16).toCSG()
        val result1 = splitPolygons(
            c1.polygons, c2.polygons,
            c1.bounds, c2.bounds
        )
        val result2 = splitPolygons(
            c2.polygons, c1.polygons,
            c2.bounds, c1.bounds
        )

        /*      result1 = splitPolygons(
                result2, c2.getPolygons(),
                c1.getBounds(), c2.getBounds());*/
        val splitted: MutableList<Polygon> = ArrayList()
        splitted.addAll(result1)
        splitted.addAll(result2)

//        CSG.fromPolygons(splitted).toObj(100).toFiles(Paths.get("test-split1.obj"));
//
        Files.write(
            Paths.get("test-split1.stl"),
            CSG.fromPolygons(splitted).toStlString().toByteArray()
        )
        val inC2: MutableList<Polygon> = ArrayList()
        val outC2: MutableList<Polygon> = ArrayList()
        val sameC2: MutableList<Polygon> = ArrayList()
        val oppositeC2: MutableList<Polygon> = ArrayList()
        val unknownOfC1: MutableList<Polygon> = ArrayList()
        for (p in result2) {
            val pT = classifyPolygon(p, c2.polygons, c2.bounds)
            if (pT == PolygonType.INSIDE) {
                inC2.add(p)
            }
            if (pT == PolygonType.SAME) {
                sameC2.add(p)
            }
            if (pT == PolygonType.OPPOSITE) {
                oppositeC2.add(p)
            }
            if (pT == PolygonType.OUTSIDE) {
                outC2.add(p)
            }
            if (pT == PolygonType.UNKNOWN) {
                unknownOfC1.add(p)
            }
        }
        val inC1: MutableList<Polygon> = ArrayList()
        val outC1: MutableList<Polygon> = ArrayList()
        val sameC1: MutableList<Polygon> = ArrayList()
        val oppositeC1: MutableList<Polygon> = ArrayList()
        val unknownOfC2: MutableList<Polygon> = ArrayList()
        for (p in result1) {
            val pT = classifyPolygon(p, c1.polygons, c1.bounds)
            if (pT == PolygonType.INSIDE) {
                inC1.add(p)
            }
            if (pT == PolygonType.OUTSIDE) {
                outC1.add(p)
            }
            if (pT == PolygonType.SAME) {
                sameC1.add(p)
            }
            if (pT == PolygonType.OPPOSITE) {
                oppositeC1.add(p)
            }
            if (pT == PolygonType.UNKNOWN) {
                unknownOfC2.add(p)
            }
        }
        val difference: MutableList<Polygon> = ArrayList()
        difference.addAll(outC2)
        difference.addAll(oppositeC2)
        for (p in inC1) {
            p.flip()
        }
        for (p in inC2) {
            p.flip()
        }
        difference.addAll(inC1)
        System.err.println(">> creating CSG")
        val result: CSG = CSG.fromPolygons(difference)
        System.err.println(">> unknown  polygons in C1: " + unknownOfC1.size)
        System.err.println(">> unknown  polygons in C2: " + unknownOfC2.size)
        System.err.println(">> opposite polygons in C1: " + oppositeC1.size)
        System.err.println(">> opposite polygons in C2: " + oppositeC2.size)
        System.err.println(">> inside   polygons in C1: " + inC1.size)
        System.err.println(">> inside   polygons in C2: " + inC2.size)
        Files.write(Paths.get("test.stl"), result.toStlString().toByteArray())
    }

    private fun classifyPolygon(p1: Polygon, polygons: List<Polygon>, b: Bounds): PolygonType {
        val tol = 1e-10

        // we are definitely outside if bounding boxes don't intersect
        if (!p1.bounds.intersects(b)) {
            return PolygonType.OUTSIDE
        }
        val rayCenter = p1.centroid()
        val rayDirection = p1.plane.normal
        val intersections = getPolygonsThatIntersectWithRay(
            rayCenter, rayDirection, polygons, tol
        )
        if (intersections.isEmpty()) {
            return PolygonType.OUTSIDE
        }

        // find the closest polygon to the centroid of p1 which intersects the
        // ray
        var min: RayIntersection? = null // intersections.get(0);
        var dist = 0.0
        var prevDist = Double.MAX_VALUE // min.polygon.centroid().minus(rayCenter).magnitude();
        var i = 0
        for (ri in intersections) {
            val frontOrBack = p1.plane.compare(ri.intersectionPoint, tol)
            if (frontOrBack < 0) {
                // System.out.println("  -> skipping intersection behind ray " + i);
                continue
            }

            // try {
            //    ObjFile objF = CSG.fromPolygons(ri.polygon).toObj(3);
            //    objF.toFiles(Paths.get("test-intersection-" + i + ".obj"));
            // } catch (IOException ex) {
            //    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            // }
            dist = ri.polygon.centroid().minus(rayCenter).magnitude()

            // System.out.println("dist-"+i+": " + dist);
            if (dist < tol && ri.polygon.plane.normal.dot(rayDirection) < tol) {
                // System.out.println("  -> skipping intersection " + i);
                continue
            }
            if (dist < prevDist) {
                prevDist = dist
                min = ri
            }
            i++
        }
        if (min == null) {
            return PolygonType.OUTSIDE
        }

        // try {
        //    ObjFile objF = CSG.fromPolygons(min.polygon).toObj();
        //    objF.toFiles(Paths.get("test-intersection-min.obj"));
        // } catch (IOException ex) {
        //    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        // }
        val frontOrBack = p1.plane.compare(min.intersectionPoint, tol)
        val planePoint = p1.plane.anchor
        val sameOrOpposite = p1.plane.compare(
            planePoint.plus(min.polygon.plane.normal), tol
        )
        if (frontOrBack > 0 && sameOrOpposite > 0) {
            return PolygonType.INSIDE
        }
        if (frontOrBack > 0 && sameOrOpposite < 0) {
            return PolygonType.OUTSIDE
        }
        if (frontOrBack < 0 && sameOrOpposite < 0) {
            return PolygonType.INSIDE
        }
        if (frontOrBack < 0 && sameOrOpposite > 0) {
            return PolygonType.OUTSIDE
        }
        if (frontOrBack == 0 && sameOrOpposite > 0) {
            return PolygonType.SAME
        }
        if (frontOrBack == 0 && sameOrOpposite < 0) {
            return PolygonType.OPPOSITE
        }
        System.err.println("I need help (2) !")
        return PolygonType.UNKNOWN
    }

    private fun getPolygonsThatIntersectWithRay(
        point: Vector3d,
        direction: Vector3d,
        polygons: List<Polygon>,
        TOL: Double
    ): List<RayIntersection> {
        val intersection: MutableList<RayIntersection> = ArrayList()
        for (p in polygons) {
            val res = computePlaneIntersection(p.plane, point, direction, TOL)
            if (res.point.isPresent) {
                if (p.contains(res.point.get())) {
                    intersection.add(RayIntersection(res.point.get(), p, res.type))
                }
            }
        }
        return intersection
    }

    private fun computePlaneIntersection(
        plane: Plane,
        point: Vector3d,
        direction: Vector3d,
        TOL: Double
    ): PlaneIntersection {

        // Ax + By + Cz + D = 0
        // x = x0 + t(x1  x0)
        // y = y0 + t(y1  y0)
        // z = z0 + t(z1  z0)
        // (x1 - x0) = dx, (y1 - y0) = dy, (z1 - z0) = dz
        // t = -(A*x0 + B*y0 + C*z0 )/(A*dx + B*dy + C*dz)
        val normal = plane.normal
        val planePoint = plane.anchor
        val a = normal.x
        val b = normal.y
        val c = normal.z
        val d =
            -(normal.x * planePoint.x + normal.y * planePoint.y + normal.z * planePoint.z)
        val numerator = a * point.x + b * point.y + c * point.z + d
        val denominator = a * direction.x + b * direction.y + c * direction.z

        // if line is paralel to the plane...
        return if (abs(denominator) < TOL) {
            // if line is contained in the plane...
            if (abs(numerator) < TOL) {
                PlaneIntersection(
                    PlaneIntersection.IntersectionType.ON,
                    Optional.of(point)
                )
            } else {
                PlaneIntersection(
                    PlaneIntersection.IntersectionType.PARALLEL,
                    Optional.empty()
                )
            }
        } // if line intercepts the plane...
        else {
            val t = -numerator / denominator
            val resultPoint = Vector3d.xyz(
                point.x + t * direction.x,
                point.y + t * direction.y,
                point.z + t * direction.z
            )
            PlaneIntersection(
                PlaneIntersection.IntersectionType.NON_PARALLEL,
                Optional.of(resultPoint)
            )
        }
    }

    /**
     * Splits polygons ps2 with planes from polygons ps1.
     *
     * @param ps1
     * @param ps2
     * @param b1
     * @param b2
     * @return
     */
    private fun splitPolygons(
        ps1: List<Polygon>,
        ps2: List<Polygon>,
        b1: Bounds,
        b2: Bounds
    ): List<Polygon> {
        println("#ps1: " + ps1.size + ", #ps2: " + ps2.size)
        if (ps1.isEmpty() || ps2.isEmpty()) return emptyList()
        val ps2WithCuts: MutableList<Polygon> = ArrayList(ps2)
        for (p1 in ps1) {

            // return early if polygon bounds do not intersect object bounds
            if (!p1.bounds.intersects(b2)) {
                continue
            }
            val cutsWithP1: MutableList<Polygon> = ArrayList()
            val p2ToDelete: MutableList<Polygon> = ArrayList()
            for (p2 in ps2WithCuts) {

                // return early if polygon bounds do not intersect other polygon bound
                if (!p1.bounds.intersects(p2.bounds)) {
                    continue
                }
                val cutsOfP2WithP1 = cutPolygonWithPlaneIf(
                    p2, p1.plane,
                    label@ Predicate { segments: List<Vector3d> ->

                        // if(true)return true;
                        if (segments.size != 2) return@Predicate true
                        val s1 = segments[0]
                        val s2 = segments[1]
                        var numIntersectionsPoly1 = 0
                        run {
                            var i = 0
                            while (i < p1.vertices.size - 1) {

                                // System.out.println("i,j : " + i + ", " + (i+1%p1.vertices.size()));
                                val e1 = p1.vertices[i].pos
                                val e2 = p1.vertices[i + 1 % p1.vertices.size].pos
                                val iRes = calculateLineLineIntersection(e1, e2, s1, s2)
                                if (iRes.type == LineIntersectionResult.IntersectionType.INTERSECTING &&
                                    p1.contains(iRes.segmentPoint1.get())
                                ) {
                                    numIntersectionsPoly1++
                                }
                                i++
                            }
                        }
                        var numIntersectionsPoly2 = 0
                        var i = 0
                        while (i < p2.vertices.size - 1) {
                            val e1 = p2.vertices[i].pos
                            val e2 = p2.vertices[i + 1 % p2.vertices.size].pos
                            val iRes = calculateLineLineIntersection(e1, e2, s1, s2)
                            if (iRes.type == LineIntersectionResult.IntersectionType.INTERSECTING &&
                                p2.contains(iRes.segmentPoint1.get())
                            ) {
                                numIntersectionsPoly2++
                            }
                            i++
                        }
                        numIntersectionsPoly1 > 0 && numIntersectionsPoly2 > 0
                    }
                )
                if (cutsOfP2WithP1.isNotEmpty()) {
                    cutsWithP1.addAll(cutsOfP2WithP1)
                    p2ToDelete.add(p2)
                }
            }
            ps2WithCuts.addAll(cutsWithP1)
            ps2WithCuts.removeAll(p2ToDelete.toSet())
        }
        return ps2WithCuts
    }

    private fun cutPolygonWithPlaneAndTypes(
        polygon: Polygon,
        cutPlane: Plane,
        vertexTypes: IntArray,
        frontPolygon: MutableList<Vector3d>,
        backPolygon: MutableList<Vector3d>,
        onPlane: MutableList<Vector3d>
    ) {

//        System.out.println("polygon: \n" + polygon.toStlString());
//        System.out.println("--------------------");
//        System.out.println("plane: \n -> p: " + cutPlane.getAnchor() + "\n -> n: " + cutPlane.getNormal());
//        System.out.println("--------------------");
        for (i in polygon.vertices.indices) {
            val j = (i + 1) % polygon.vertices.size
            val ti = vertexTypes[i]
            val tj = vertexTypes[j]
            val vi = polygon.vertices[i]
            val vj = polygon.vertices[j]
            if (ti == 1 /*front*/) {
                frontPolygon.add(vi.pos)
            }
            if (ti == -1 /*back*/) {
                backPolygon.add(vi.pos)
            }
            if (ti == 0) {
                frontPolygon.add(vi.pos)
                backPolygon.add(vi.pos)
                //                segmentPoints.add(vi.pos);
            }
            if (ti != tj && ti != 0 && tj != 0 /*spanning*/) {
                val pI = computePlaneIntersection(
                    cutPlane, vi.pos,
                    vj.pos.minus(
                        vi.pos
                    ),
                    EPS
                )
                if (pI.type != PlaneIntersection.IntersectionType.NON_PARALLEL) {
                    throw RuntimeException("I need help (3)!")
                }
                val intersectionPoint = pI.point.get()
                frontPolygon.add(intersectionPoint)
                backPolygon.add(intersectionPoint)
                onPlane.add(intersectionPoint)
            }
        }
    }

    private fun testCut() {
        var p: Polygon = Polygon.fromPoints(
            Vector3d.xyz(0.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(1.0, 0.0, 1.0),
            Vector3d.xyz(0.0, 0.0, 1.0)
        )
        try {
            val pCSG = STL.file(Paths.get("sphere-test-01.stl"))
            p = pCSG.polygons[0]
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        var cube = Cube(Vector3d.xyz(1.0, 1.0, 1.0), Vector3d.xyz(2.0, 2.0, 2.0)).toCSG()
            .transformed(Transform.unity().rot(Vector3d.ZERO, Vector3d.UNITY, 17.0))
        cube = Sphere(Vector3d.x(0.0), 0.5, 16, 16).toCSG()

//        CSG cube = new Cube(1).toCSG().transformed(
//                Transform.unity().translate(0.5,-0.55,0.5).rot(Vector3d.ZERO, Vector3d.UNITY, 0)
//        );
        val cubePolyFrom = 0
        val cubePolyTo = 6
        val cubePolys = cube.polygons.toMutableList() // .subList(cubePolyFrom, cubePolyTo);
        println("p: " + p.toStlString())
        println("p-centroid: " + p.centroid())
        val intersections = getPolygonsThatIntersectWithRay(
            p.centroid(),
            p.plane.normal,
            cubePolys, EPS
        )
        println("my normal: " + p.plane.normal)
        println("#intersections: " + intersections.size)
        for (ri in intersections) {
            println(ri)
        }
        val pType = classifyPolygon(p, cubePolys, cube.bounds)
        println("#pType:")
        println(" -> $pType")
        val cutsWithCube: MutableList<Polygon> = splitPolygons(
            cubePolys,
            listOf(p), p.bounds, cube.bounds
        ).toMutableList()
        cutsWithCube.addAll(cube.polygons /*.subList(cubePolyFrom, cubePolyTo)*/)
        try {
            val objF: ObjFile = CSG.fromPolygons(cutsWithCube).toObj(3)
            objF.toFiles(Paths.get("test-split1.obj"))
            //            Files.write(Paths.get("test-split1.stl"),
//                    CSG.fromPolygons(cutsWithP1).toStlString().getBytes());
        } catch (ex: IOException) {
            Logger.getLogger(Main::class.java.name).log(Level.SEVERE, null, ex)
        }
        val segmentP1 = Vector3d.zero().asModifiable()
        val segmentP2 = Vector3d.zero().asModifiable()
        val lineRes = calculateLineLineIntersection(
            Vector3d.xyz(-1.0, 0.0, 0.0), Vector3d.xyz(1.0, 0.0, 0.0),
            Vector3d.xyz(0.0, -1.0, 0.0), Vector3d.xyz(0.0, 1.0, 0.0)
        )
        println("l1 intersect l2: ")
        println(lineRes)

        // System.exit(0);
    }

    private fun cutPolygonWithPlaneIf(
        p: Polygon,
        plane: Plane,
        check: Predicate<List<Vector3d>>?
    ): List<Polygon> {
        var typesEqual = true
        val types = IntArray(p.vertices.size)
        for (i in p.vertices.indices) {
            types[i] = plane.compare(p.vertices[i].pos, EPS)
            //            System.out.println("type " + i + ": " + types[i]);
            if (i > 0 && typesEqual) {
                typesEqual = typesEqual && types[i] == types[i - 1]
            }
        }

        // planes are parallel, thus polygons do not intersect
        if (typesEqual) {
            return emptyList()
        }
        val front: MutableList<Vector3d> = ArrayList()
        val back: MutableList<Vector3d> = ArrayList()
        val on: MutableList<Vector3d> = ArrayList()
        cutPolygonWithPlaneAndTypes(p, plane, types, front, back, on)
        var checkResult = check == null
        if (check != null) {
            checkResult = check.test(on)
        }
        if (!checkResult) return emptyList()
        val cutsWithP1: MutableList<Polygon> = ArrayList()
        if (front.size > 2) {
            val frontCut: Polygon = Polygon.fromPoints(
                front
            )
            if (frontCut.isValid) {
                cutsWithP1.add(frontCut)
            }
        }
        if (back.size > 2) {
            val backCut: Polygon = Polygon.fromPoints(
                back
            )
            if (backCut.isValid) {
                cutsWithP1.add(backCut)
            }
        }
        return cutsWithP1
    }

    /**
     * Calculates the intersection line segment between two lines.
     *
     * @param line1Point1
     * @param line1Point2
     * @param line2Point1
     * @param line2Point2
     * @return `true` if the intersection line segment exists; `false` otherwise
     */
    private fun calculateLineLineIntersection(
        line1Point1: Vector3d,
        line1Point2: Vector3d,
        line2Point1: Vector3d,
        line2Point2: Vector3d
    ): LineIntersectionResult {
        // Algorithm is ported from the C algorithm of
        // Paul Bourke at http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline3d/
        val p13 = line1Point1.minus(line2Point1)
        val p43 = line2Point2.minus(line2Point1)
        if (p43.magnitudeSq() < EPS) {
            return LineIntersectionResult.PARALLEL
        }
        val p21 = line1Point2.minus(line1Point1)
        if (p21.magnitudeSq() < EPS) {
            return LineIntersectionResult.PARALLEL
        }
        val d1343 =
            p13.x * p43.x + p13.y * p43.y + p13.z * p43.z
        val d4321 =
            p43.x * p21.x + p43.y * p21.y + p43.z * p21.z
        val d1321 =
            p13.x * p21.x + p13.y * p21.y + p13.z * p21.z
        val d4343 =
            p43.x * p43.x + p43.y * p43.y + p43.z * p43.z
        val d2121 =
            p21.x * p21.x + p21.y * p21.y + p21.z * p21.z
        val denom = d2121 * d4343 - d4321 * d4321
        if (abs(denom) < EPS) {
            return LineIntersectionResult.PARALLEL
        }
        val numer = d1343 * d4321 - d1321 * d4343
        val mua = numer / denom
        val mub = (d1343 + d4321 * mua) / d4343
        val resultSegmentPoint1 = Vector3d.zero().asModifiable()
        val resultSegmentPoint2 = Vector3d.zero().asModifiable()
        resultSegmentPoint1.x = line1Point1.x + mua * p21.x
        resultSegmentPoint1.y = line1Point1.y + mua * p21.y
        resultSegmentPoint1.z = line1Point1.z + mua * p21.z
        resultSegmentPoint2.x = line2Point1.x + mub * p43.x
        resultSegmentPoint2.y = line2Point1.y + mub * p43.y
        resultSegmentPoint2.z = line2Point1.z + mub * p43.z
        return if (resultSegmentPoint1 == resultSegmentPoint2) {
            LineIntersectionResult(
                LineIntersectionResult.IntersectionType.INTERSECTING,
                resultSegmentPoint1, resultSegmentPoint2
            )
        } else {
            LineIntersectionResult(
                LineIntersectionResult.IntersectionType.NON_PARALLEL,
                resultSegmentPoint1, resultSegmentPoint2
            )
        }
    }

    class PlaneIntersection(
        val type: IntersectionType,
        var point: Optional<Vector3d>
    ) {
        enum class IntersectionType {
            ON, PARALLEL, NON_PARALLEL
        }
    }

    class RayIntersection(
        val intersectionPoint: Vector3d,
        val polygon: Polygon,
        val type: PlaneIntersection.IntersectionType
    ) {
        override fun toString(): String {
            return """[
 -> point:          $intersectionPoint
 -> polygon-normal: ${polygon.plane.normal}
 -> type:           $type
]"""
        }
    }

    internal enum class PolygonType {
        UNKNOWN, INSIDE, OUTSIDE, OPPOSITE, SAME
    }

    internal class LineIntersectionResult(
        val type: IntersectionType,
        segmentPoint1: Vector3d?,
        segmentPoint2: Vector3d?
    ) {
        val segmentPoint1: Optional<Vector3d>
        private val segmentPoint2: Optional<Vector3d>

        internal enum class IntersectionType {
            PARALLEL, NON_PARALLEL, INTERSECTING
        }

        override fun toString(): String {
            return """[
 -> type: $type
 -> segmentP1: ${if (segmentPoint1.isPresent) segmentPoint1.get() else "none"}
 -> segmentP2: ${if (segmentPoint2.isPresent) segmentPoint2.get() else "none"}
]"""
        }

        companion object {
            val PARALLEL = LineIntersectionResult(IntersectionType.PARALLEL, null, null)
        }

        init {
            this.segmentPoint1 = Optional.ofNullable(segmentPoint1)
            this.segmentPoint2 = Optional.ofNullable(segmentPoint2)
        }
    }
}

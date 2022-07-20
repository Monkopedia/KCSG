package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.CSG
import eu.mihosoft.jcsg.Polygon
import eu.mihosoft.vvecmath.Vector3d
import java.lang.Math.toDegrees
import java.lang.Math.toRadians

var count = 0

/**
 *
 * @author cpoliwoda
 */
class FractalStructure(
    groundCenter: Vector3d,
    topCenter: Vector3d,
    numberOfGroundEdges: Int,
    thickness: Double,
    level: Int,
    orthoVecToRotAxis1: Vector3d?,
    orthoVecToRotAxis2: Vector3d?
) {
    // decides which kind of polygon base should be created: triangle, hexagon, general n-polygon with n>2
    private var numberOfGroundEdges = 3

    //    double height = 1.0;
    var thickness = 1.0

    // divider 5 makes a good look for the structure
    // divider bigger 5 makes the structure thinner, lower than 5 makes it wider
    private var nextThicknessDivider = 6.0

    // the thickness of the child tubes in the next level
    private var nextThickness = thickness / nextThicknessDivider

    // decides who many connections there should be in the next level between
    // two subFractalStructures (position parent edge and center)
    private var crossConnectionsRate = 25 // percent

    // maxAngleForCrossConections dominates crossConnectionsRate
    private var maxAngleForCrossConections = 45 // degree

    // the distance between groundCenter and topCenter decides about the height of the tu
    // the center of the bottom polygon of the first FractalStructure (level=0) / tube
    private var groundCenter: Vector3d? = null

    // the center of the top polygon of the first FractalStructure (level=0) / tube
    private var topCenter: Vector3d? = null

    // collection of the bottom polygon points of a FractalStructure (edges & center)
    // used for the new bottom centers of the child FractalStructures
    private var groundPoints: MutableList<Vector3d>? = null

    // collection of the top polygon points of a FractalStructure (edges & center)
    private var topPoints: MutableList<Vector3d>? = null

    // collection of all child tubes, together they build the fractal structure we want
    private var subStructures: MutableList<CSG>? = null

    // how many recursion should be done before drawing (level 0), level 2 means draw after 2 refinments
    var level = 0

    companion object {
        // list which gives the user the controll of thickness in each level
        var thicknessList: ArrayList<Double>? = null

        init {
            thicknessList = ArrayList()
            thicknessList!!.add(0.01) // level 0
            thicknessList!!.add(0.1) // level 1
            thicknessList!!.add(4.0) // level 2
            thicknessList!!.add(80.0) // level 3
            thicknessList!!.add(160.0) // level 4
        }
    }

    // we need two vectors which span the plane where the circle lies in
    private var orthoVecToRotAxis1: Vector3d? = null
    private var orthoVecToRotAxis2: Vector3d? = null

    // if dot of two vectors is lower than threshhold we assume they are orthogonal
    private var orthoThreshhold = 1E-16

    /**
     * Helper methode which creates and draw structure into CSG.
     * Do NOT call this method by your self. This method is called by the constructor.
     *
     * @return a fractal structure as CSG
     */
    private fun createStructure(): CSG {
        val polygonList: ArrayList<Polygon> = ArrayList()
        var tmpList: ArrayList<Vector3d> = ArrayList()

        // all ground points without the center point
        for (i in 0 until groundPoints!!.size - 1) {
            tmpList.add(groundPoints!![i])
        }

        // add the ground polygon
        // flip is needed to set the normal int the right direction (out)
        polygonList.add(Polygon.fromPoints(tmpList).flip())
        var groundP1: Vector3d? = null
        var groundP2: Vector3d? = null
        var topP1: Vector3d? = null
        var topP2: Vector3d? = null

        // collect the points of the edge planes
        for (i in 0 until tmpList.size - 1) {
            groundP1 = groundPoints!![i]
            groundP2 = groundPoints!![i + 1]
            topP1 = topPoints!![i]
            topP2 = topPoints!![i + 1]

            // added in counter clockwise orientation: groundP1, groundP2, topP2, topP1
            polygonList.add(Polygon.fromPoints(groundP1, groundP2, topP2, topP1))
        }

        // collect the points of the last edge plane
        groundP1 = groundPoints!![tmpList.size - 1]
        groundP2 = groundPoints!![0]
        topP1 = topPoints!![tmpList.size - 1]
        topP2 = topPoints!![0]

        // added in counter clockwise orientation: groundP1, groundP2, topP2, topP1
        polygonList.add(Polygon.fromPoints(groundP1, groundP2, topP2, topP1))

        // clear tmp list
        tmpList = ArrayList()

        // all top points without the center point
        for (i in 0 until topPoints!!.size - 1) {
            tmpList.add(topPoints!![i])
        }

        // add the top polygon
        polygonList.add(Polygon.fromPoints(tmpList))
        return CSG.fromPolygons(polygonList)
    }

    /**
     * Helper methode which creates in the center and at the edges new smaller structures
     * with the same orientation in space as the parent structure (one level above) and cross
     * connections between them.
     * Do NOT call this method by your self. This method is called by the constructor.
     *
     * @return a list with smaller child structures
     */
    private fun createSubStructures(): ArrayList<FractalStructure> {

        //
        // PART 01 - creating subStructures parallel to rotation axis
        //
        var subGroundCenter: Vector3d? = null
        var subTopCenter: Vector3d? = null
        val subFractalStructures = ArrayList<FractalStructure>()
        var tmpGroundPoint: Vector3d? = null
        var tmpTopPoint: Vector3d? = null

        // is a bias the new center points needs to lie a bit more to the center
        // co the diameter of the fractal won't be increased.
        val correction = -1 * nextThickness / 2.0

        // create the edge subStructures
        for (i in 0 until numberOfGroundEdges) {
            tmpGroundPoint = groundPoints!![i]

            // one of the new a bit translated groundCenterpoint
            // subGc = groundEdge - (NextThickness / 2) * (groundCenter - groundEdge )
            subGroundCenter =
                tmpGroundPoint.minus(groundCenter!!.minus(tmpGroundPoint).times(correction))
            tmpTopPoint = topPoints!![i]

            // one of the new a bit translated topCenterpoint
            // subTc = topEdge - (NextThickness / 2) * (topCenter - topEdge )
            subTopCenter = tmpTopPoint.minus(topCenter!!.minus(tmpTopPoint).times(correction))

            // create the new subFractalStructure on the edge

            // create the new subFractalStructure on the edge
            subFractalStructures.add(
                FractalStructure(
                    subGroundCenter,
                    subTopCenter,
                    numberOfGroundEdges,
                    nextThickness,
                    level - 1,
                    orthoVecToRotAxis1,
                    orthoVecToRotAxis2
                )
            )
        }
        // create the subStructure in the center
        subFractalStructures.add(
            FractalStructure(
                groundCenter!!,
                topCenter!!,
                numberOfGroundEdges,
                nextThickness,
                level - 1,
                orthoVecToRotAxis1,
                orthoVecToRotAxis2
            )
        )

        //
        // PART 02 - creating stabilizing subStructures (cross connections)
        //
        /**
         *
         * ET  CT
         * |  /|hCP2
         * | / |
         * hEP2  |/  |
         * |\  |
         * | \ |
         * |  \|
         * |  /|hCP1
         * | / |
         * __  hEP1 |/  |
         * |           |\  |
         * | \ |
         * sSoCL      |  \|
         * |  /|hCP0
         * | / |
         * _|_ hEP0 |/  |
         * EG  CG
         *
         */
        val crossSubFractalStructures = ArrayList<FractalStructure>()
        val centerStructure = subFractalStructures[subFractalStructures.size - 1]
        var tmpStructure: FractalStructure? = null

        //
        // helper points for creating the cross connections to the center
        //
        // top and ground of the center subStructure
        val centerGroundPoint = centerStructure.groundCenter
        val centerTopPoint = centerStructure.topCenter
        // top and ground of the edge subStructures
        tmpGroundPoint = null
        tmpTopPoint = null
        // helper points on connection between edge ground and edge top
        var helpEdgePoint: Vector3d? = null
        // helper points on connection between center ground and center top
        var helpCenterPoint: Vector3d? = null

        // vector that shows / discribes the connection line from ground to top
        // the same for edge line and for center line because parallel and have the same lenght
        val connectionLineVector = centerTopPoint!!.minus(centerGroundPoint)
        val connectionLineVectorNormalized = connectionLineVector.normalized()

        // discribes where the help(Edge/Center)Points should lie on connection line of ground and top
        var stepSizeOnConnectionLine =
            10.0 / crossConnectionsRate * connectionLineVector.magnitude() // sSoCL
        var stepSizeOnConnectionLineHalf = stepSizeOnConnectionLine / 2.0

        // create cross connections from all edge subStructures to the center subStructure
        for (i in 0 until subFractalStructures.size - 1) {
            tmpStructure = subFractalStructures[i]
            tmpGroundPoint = tmpStructure.groundCenter

            // optional part , needed to reduce cross connections with big increase
            //
            // check maxAngleForCrossConections for angle a
            //
            //                |    / |hCP0
            //                |   /  |
            //         hEP0 | /a  |
            //              EG    CG
            //
            // in rectangular triangle EG, CG, hCP0
            // cos(a) = | ANKATHETE | / | HYPOTHENUSE |
            // cos(a) = | CG - EG | / | hCP0 - EG |
            //
            // hCP0
            helpCenterPoint = connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf)
                .plus(centerGroundPoint)
            // tmpGroundPoint = EG
            // centerGroundPoint = CG
            val ankathete = centerGroundPoint!!.minus(tmpGroundPoint).magnitude()
            var hypothenuse = helpCenterPoint.minus(tmpGroundPoint).magnitude()
            var angle = toDegrees(kotlin.math.acos(ankathete / hypothenuse))

            // check maxAngleForCrossConections for angle a and recalculate stepsize until angle
            while (angle >= maxAngleForCrossConections) {
                stepSizeOnConnectionLine = stepSizeOnConnectionLineHalf
                stepSizeOnConnectionLineHalf /= 2.0
                helpCenterPoint = connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf)
                    .plus(centerGroundPoint)
                hypothenuse = helpCenterPoint.minus(tmpGroundPoint).magnitude()
                angle = toDegrees(kotlin.math.acos(ankathete / hypothenuse))
            }

            // prevent that the cross connactions are to low in the bottom plane
            val correctionInRotationAxisDirection =
                connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf / 2.0)

            // help vector to reduce the calculations of second orthogonal vector in sub structures
            // and make the orientation of the cross connections 'north pole'
            var secondOrthoVec: Vector3d? = null

            // create multiple cross connections from ONE edge subStructure to the center subStructure
            var j = 0.0
            while (j < connectionLineVector.magnitude()) {

                // from bottom left to top right beginning at the ground point position
                // hEP0,2,4,....
                helpEdgePoint = connectionLineVectorNormalized.times(j).plus(tmpGroundPoint)
                    .plus(correctionInRotationAxisDirection)
                // hCP0,1,2,....
                helpCenterPoint = connectionLineVectorNormalized.times(j)
                    .plus(connectionLineVectorNormalized.times(stepSizeOnConnectionLineHalf))
                    .plus(centerGroundPoint).plus(correctionInRotationAxisDirection)
                if (secondOrthoVec == null) {
                    secondOrthoVec =
                        connectionLineVectorNormalized.crossed(helpCenterPoint.minus(helpEdgePoint))
                }

                // prevent that the last cross connactions from bottom left to top right has a to above end point in the top plane
                if (connectionLineVector.magnitude() > helpCenterPoint.minus(centerGroundPoint)
                    .magnitude()
                ) {
                    // collects the cross subStructure from bottom left to top right
                    crossSubFractalStructures.add(
                        FractalStructure(
                            helpEdgePoint,
                            helpCenterPoint,
                            numberOfGroundEdges,
                            nextThickness,
                            level - 1,
                            connectionLineVectorNormalized, secondOrthoVec
                        )
                    )
                }

                // from top left to bottom right beginning at the ground point position
                // hEP1,3,5,....
                helpEdgePoint = connectionLineVectorNormalized.times(j + stepSizeOnConnectionLine)
                    .plus(tmpGroundPoint).plus(correctionInRotationAxisDirection)

                // prevent that the last cross connactions from top left to bottom right has a to above end point in the top plane
                if (connectionLineVector.magnitude() > helpEdgePoint.minus(tmpGroundPoint)
                    .magnitude()
                ) {
//
                    // collects the cross subStructure from top left to bottom right
                    crossSubFractalStructures.add(
                        FractalStructure(
                            helpEdgePoint,
                            helpCenterPoint,
                            numberOfGroundEdges,
                            nextThickness,
                            level - 1,
                            connectionLineVectorNormalized, secondOrthoVec
                        )
                    )
                }
                j += stepSizeOnConnectionLine
            }
        } // for edges
        subFractalStructures.addAll(crossSubFractalStructures)

        // create cross connections from one edge subStructures to the neighbour edge subStructure
        return subFractalStructures
    }

    fun toCSG(): CSG {
        val polygons: MutableList<Polygon> = ArrayList()
        subStructures!!.stream().forEach { csg: CSG ->
            polygons.addAll(
                csg.polygons
            )
        }
        return CSG.fromPolygons(polygons)
    }

    /**
     *
     * EXAMPLE:
     * FractalStructure(Vector3d.ZERO, Vector3d.Z_ONE.times(2), 6, 1, 0) creates a tube with a
     * top and botton polygon consist of 6 point, a length of 2. The orintation in space is that kind that
     * z axis goes through the center of the bottom and top polygon. Level 0 means draw these tube.
     * A Level bigger than 0 means create new tubes with one level decresed, same Lenght and same
     * orintation in space in the edges and center.
     * 1 ____6
     * /          \
     * 2/      c      \5
     * \             /
     * \______/
     * 3      4
     * @param groundCenter the center point of the bottom polygon
     * @param topCenter the center point of the top polygon
     * @param numberOfGroundEdges  number which defines polygon should be created (circle divided in N equal parts)
     * @param thickness the distance between the center and all edge points of the bottom and/or top polygon
     * @param level is the number which defines how many recursion should be done
     * @param orthoVecToRotAxis1 is an orthogonal vector to the roation axis (connection line between
     * groundCenter and topCenter) and normalized, null is valid
     * @param orthoVecToRotAxis2 is an orthogonal vector to the roation axis (connection line between
     * groundCenter and topCenter) and the orthoVecToRotAxis1 vector and normalized, null is valid
     */
    init {
        println("Pos $groundCenter $topCenter $numberOfGroundEdges $thickness $level $orthoVecToRotAxis1 $orthoVecToRotAxis2")
        var numberOfGroundEdges = numberOfGroundEdges
        nextThickness = thickness / nextThicknessDivider
        if (numberOfGroundEdges < 3) {
            numberOfGroundEdges = 3
            System.err.println("numberOfGroundEdges need to be at least 3 and is set therefore to 3.")
        }
        this.numberOfGroundEdges = numberOfGroundEdges
        this.level = level

        // save the centers
        this.groundCenter = groundCenter
        this.topCenter = topCenter

        // create point lists
        groundPoints = ArrayList()
        topPoints = ArrayList()

        // Circle equation C_r_(x,y):  x^2 + y^2 = r^2
        // with x = r * cos(angle)
        //      y = r * sin(angle)
        //
        // Plane equation E(x,y) = S + P * x + Q * y
        // with vectors S, P, Q and  P othogonal to Q
        //
        val rotationAxis = Vector3d.xyz(
            topCenter.x() - groundCenter.x(),
            topCenter.y() - groundCenter.y(),
            topCenter.z() - groundCenter.z()
        ).normalized()

        //
        // we need two vectors which span the plane where the circle lies in
        //
        // if the user did not give us an orthogonal vector to the rotation axis we need to calculate one
        if (orthoVecToRotAxis1 != null) {

            // checking EQUAL to ZERO is a BAD IDEA
            if (kotlin.math.abs(orthoVecToRotAxis1.dot(rotationAxis)) < orthoThreshhold) {
                this.orthoVecToRotAxis1 = orthoVecToRotAxis1.normalized()
            } else {
                this.orthoVecToRotAxis1 = rotationAxis.orthogonal().normalized()
            }
        } else {
            this.orthoVecToRotAxis1 = rotationAxis.orthogonal().normalized()
        }

        // if the user did not give us an second orthogonal vector to the rotation axis and orthoVecToRotAxis1 we need to calculate one
        if (orthoVecToRotAxis2 != null) {
            // checking EQUAL to ZERO is a BAD IDEA
            if (kotlin.math.abs(orthoVecToRotAxis2.dot(this.orthoVecToRotAxis1)) < orthoThreshhold &&
                kotlin.math.abs(orthoVecToRotAxis2.dot(rotationAxis)) < orthoThreshhold
            ) {
                this.orthoVecToRotAxis2 = orthoVecToRotAxis2.normalized()
            } else {
                this.orthoVecToRotAxis2 = rotationAxis.crossed(this.orthoVecToRotAxis1).normalized()
            }
        } else {
            this.orthoVecToRotAxis2 = rotationAxis.crossed(this.orthoVecToRotAxis1).normalized()
        }

        // x, y, z
        // the first point is the most in the north in the x-y-plane
        var circlePoint: Vector3d? = null
        val angleStepSize = 360.0 / numberOfGroundEdges
        var angle = 0.0
        var radians = 0.0 // needed for cos & sin
        var radius =
            thickness / 2.0 // fallback rule if the user did not give a thickness for a level
        try {
            radius = thicknessList!![level]
        } catch (e: Exception) {
            println("no entry found in thicknessList for level = $level, therefore rule used: radius = thickness / 2.0")
        }
        var x = 0.0
        var y = 0.0

        // add/create the points around the ground and top center
        for (i in 0 until numberOfGroundEdges) {
            angle = i * angleStepSize
            radians = toRadians(angle)
            x = radius * kotlin.math.cos(radians)
            y = radius * kotlin.math.sin(radians)

            // Plane equation E(x,y) = S + P * x + Q * y
            // with P,Q orthogonal to the center rotation axis and
            // with x,y from the cirlce gives use the cirlce in 3d space
            // ground points
            circlePoint = groundCenter.plus(this.orthoVecToRotAxis1!!.times(x))
                .plus(this.orthoVecToRotAxis2!!.times(y))
            groundPoints!!.add(circlePoint)

            // top points
            circlePoint = topCenter.plus(this.orthoVecToRotAxis1!!.times(x))
                .plus(this.orthoVecToRotAxis2!!.times(y))
            topPoints!!.add(circlePoint)
        }

        // the last points in the list are the center points
        groundPoints!!.add(groundCenter)
        topPoints!!.add(topCenter)

        // here we want to save the substructures
        subStructures = ArrayList()
        if (level == 0) {
            subStructures!!.add(createStructure())
        } else {
            val subFractals = createSubStructures()
            for (i in subFractals.indices) {
                subStructures!!.add(subFractals[i].toCSG())
            }
        }
    }
}

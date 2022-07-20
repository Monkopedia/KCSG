/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg.samples

import eu.mihosoft.jcsg.*
import eu.mihosoft.jcsg.CSG.OptType
import eu.mihosoft.vvecmath.Plane
import eu.mihosoft.vvecmath.Transform
import eu.mihosoft.vvecmath.Vector3d
import java.io.IOException
import java.nio.file.Paths
import java.util.*
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.sqrt

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class QuadrocopterArm {
    private fun mainArm(
        numInnerStructures: Int,
        length: Double,
        armThickness: Double,
        innerTubeOffset: Double,
        armCubeThickness: Double
    ): CSG {
        val outerRadius = armThickness / 2.0
        val wallThickness = 0.8
        val structureRadius = 0.4
        val maxXRot = 180.0
        val maxYRot = 180.0
        val maxZRot = 180.0
        val innerRadius = 5.1
        val innerWallThickness = 0.8
        val numPlates = 0.0
        val plateThickness = 0.5
        val shrinkFactorX = 0.65
        val sideArmHight = length / 2.0
        val sideArmGroundDist = 30.0
        val sideArmRadius = armThickness / 6.0
        val sideArmShrinkFactor = 0.6
        var innerStructure: CSG? = null
        val random = Random(5)
        for (i in 0 until numInnerStructures) {
            var cyl = Cylinder(structureRadius, outerRadius * 10, 8).toCSG()
            cyl = cyl.transformed(
                Transform.unity().scale(
                    max(0.5, random.nextDouble() * 3),
                    max(0.5, random.nextDouble() * 3), 1.0
                )
            )
            cyl = cyl.transformed(
                Transform.unity().translateX(cyl.bounds.bounds.z() / 2.0).rotY(90.0)
            )
            cyl = cyl.transformed(
                Transform.unity().rot(
                    random.nextDouble() * maxXRot,
                    random.nextDouble() * maxYRot,
                    random.nextDouble() * maxZRot
                )
            )
            cyl = cyl.transformed(
                Transform.unity().translate(
                    -outerRadius / 2.0 + random.nextDouble() * outerRadius,
                    -outerRadius / 2.0 + random.nextDouble() * outerRadius,
                    innerTubeOffset + random.nextDouble() * (length - innerTubeOffset)
                )
            )
            innerStructure = innerStructure?.union(cyl) ?: cyl
        }
        if (innerStructure != null) {
            innerStructure = innerStructure.intersect(
                Cylinder(outerRadius, length - innerTubeOffset, 16).toCSG()
                    .transformed(Transform.unity().scaleX(0.5).translateZ(innerTubeOffset))
            )
        }
        var outerCyl = outerCyl(
            outerRadius, length, wallThickness,
            shrinkFactorX, shrinkFactorX * 0.95
        )
        if (innerStructure != null) {
            outerCyl = outerCyl.union(innerStructure)
        }
        val innerCyl = Cylinder(innerRadius, length - innerTubeOffset, 16).toCSG()
            .transformed(Transform.unity().translateZ(innerTubeOffset))
        var finalGeometry = outerCyl.union(innerCyl)
        val plate = Cylinder(outerRadius, plateThickness, 16).toCSG()
            .transformed(Transform.unity().scaleX(shrinkFactorX))
        val endPlate = plate.transformed(Transform.unity().translateZ(innerTubeOffset))
        finalGeometry = finalGeometry.union(endPlate)
        var plates: CSG? = null
        if (numPlates > 0) {
            val dt = (length - innerTubeOffset) / numPlates
            var i = 0
            while (i < numPlates) {
                val pl = plate.transformed(Transform.unity().translateZ(dt * i))
                plates = plates?.union(pl) ?: pl
                i++
            }
            finalGeometry = finalGeometry.union(plates!!)
        }
        val cube =
            Cube(outerRadius * 2, outerRadius * 2, armCubeThickness).toCSG().difference(innerCyl)
                .transformed(
                    Transform.unity().translateZ(length - armCubeThickness / 2.0)
                )
        finalGeometry = finalGeometry.union(cube)
        val sideArms = sideArms(
            sideArmGroundDist,
            sideArmHight,
            sideArmRadius,
            sideArmShrinkFactor,
            length,
            armCubeThickness,
            outerRadius
        )

        //finalGeometry = finalGeometry.union(sideArms);
        finalGeometry = finalGeometry.difference(
            Cylinder(
                innerRadius - innerWallThickness,
                length, 16
            ).toCSG()
        )
        return finalGeometry
    }

    //    private CSG outerCyl(double outerRadius, double length,
    //            double wallThickness, double scaleOuter, double scaleInner) {
    //
    //        // refine
    //        double l = length / 10;
    //
    //        CSG protoOuter = new Cylinder(outerRadius, l, 32).toCSG().
    //                transformed(unity().scaleX(scaleOuter));
    //        CSG protoInner = new Cylinder(outerRadius - wallThickness / scaleOuter,
    //                l, 32).toCSG().transformed(unity().scaleX(scaleInner));
    //
    //        CSG outerCylProto = protoOuter.difference(protoInner);
    //        
    //        CSG outerCyl = outerCylProto;
    //
    //        for (int i = 1; i < 10; i++) {
    //            outerCyl = outerCyl.union(protoOuter.transformed(Transform.unity().translateZ(i * l)));
    //        }
    //
    //        outerCyl = outerCyl.weighted(new ZModifier(true)).transformed(Transform.unity().scale(0.8, 0.8, 1)).weighted(new UnityModifier());
    //
    //        return outerCyl;
    //    }
    fun toCSG(): CSG {

        // optimization seems to cause problems
        CSG.setDefaultOptType(OptType.NONE)
        val engineRadius = 14.0
        val screwDistanceBig = 9.5
        val screwDistanceSmall = 8.0
        val screwRadius = 1.6
        val enginePlatformThickness = 2.0
        val mainHoleRadius = 4.0
        val washerWallThickness = 1.0
        val washerHeight = 2.0
        val armLength = 150.0
        val numInnerStructures = 60
        val armThickness = 18.0
        val armCubeThickness = 4.0
        val innerTubeOffset = engineRadius * 2 + 5
        var mainArm = mainArm(
            numInnerStructures,
            armLength,
            armThickness,
            innerTubeOffset,
            armCubeThickness
        ).transformed(
            Transform.unity().rotX(90.0).rotY(90.0)
        )
        val enginePlatformSphere = Sphere(engineRadius * 1.1, 64, 32).toCSG()
            .transformed(Transform.unity().scaleX(2.0).translateZ(armThickness * 0.5))
        val engineTransform =
            Transform.unity().translateX(-mainHoleRadius).translateZ(-armThickness * 0.28)
                .translateX(1.2)
        val mainHole = Cylinder(mainHoleRadius, enginePlatformThickness, 16).toCSG()
            .transformed(engineTransform)
        val enginePlatform = enginePlatform(
            engineRadius,
            enginePlatformThickness,
            mainHoleRadius,
            screwRadius,
            screwDistanceBig,
            screwDistanceSmall,
            washerWallThickness,
            washerHeight
        ).transformed(engineTransform)
        mainArm =
            mainArm.difference(enginePlatformSphere).union(enginePlatform).difference(mainHole)

        //double armHeight = mainArm.getBounds().getBounds().x;
        return mainArm.transformed(Transform.unity().rotX(90.0).rotZ(90.0))
    }

    private fun enginePlatform(
        engineRadius: Double,
        enginePlatformThickness: Double,
        mainHoleRadius: Double,
        screwRadius: Double,
        screwDistanceBig: Double,
        screwDistanceSmall: Double,
        washerWallThickness: Double,
        washerHeight: Double
    ): CSG {
        var enginePlatform = Cylinder(engineRadius, enginePlatformThickness, 32).toCSG()
        val secondCyl = Cylinder(engineRadius * 0.3, enginePlatformThickness, 3).toCSG()
            .transformed(Transform.unity().translateX(-engineRadius * 2.7))
        enginePlatform = enginePlatform.union(secondCyl).hull()
        val mainHole = Cylinder(mainHoleRadius, enginePlatformThickness * 5, 16).toCSG()
            .transformed(Transform.unity().translateZ(-enginePlatformThickness))
        val screwHolePrototype =
            Cylinder(screwRadius, enginePlatformThickness + washerHeight + 10, 16).toCSG()
                .transformed(Transform.unity().translateZ(-5.0))
        val screwHole1 =
            screwHolePrototype.transformed(Transform.unity().translateX(screwDistanceBig))
                .transformed(
                    Transform.unity().rotZ(-45.0)
                )
        val screwHole2 =
            screwHolePrototype.transformed(Transform.unity().translateX(screwDistanceBig))
                .transformed(
                    Transform.unity().rotZ(135.0)
                )
        val screwHole3 =
            screwHolePrototype.transformed(Transform.unity().translateX(screwDistanceSmall))
                .transformed(
                    Transform.unity().rotZ(45.0)
                )
        val screwHole4 =
            screwHolePrototype.transformed(Transform.unity().translateX(screwDistanceSmall))
                .transformed(
                    Transform.unity().rotZ(-135.0)
                )
        var washerPrototype = Cylinder(screwRadius + washerWallThickness, washerHeight, 16).toCSG()
        val washerHole = washerPrototype.clone()
        washerPrototype = washerPrototype.weighted(ZModifier())
            .transformed(Transform.unity().scale(1.35, 1.35, 1.0)).weighted(UnityModifier())
        washerPrototype = washerPrototype.difference(screwHolePrototype)
            .transformed(Transform.unity().translateZ(-washerHeight))
        val washer1 =
            washerPrototype.transformed(Transform.unity().translateX(screwDistanceBig)).transformed(
                Transform.unity().rotZ(-45.0)
            )
        val washer2 =
            washerPrototype.transformed(Transform.unity().translateX(screwDistanceBig)).transformed(
                Transform.unity().rotZ(135.0)
            )
        val washer3 = washerPrototype.transformed(Transform.unity().translateX(screwDistanceSmall))
            .transformed(
                Transform.unity().rotZ(45.0)
            )
        val washer4 = washerPrototype.transformed(Transform.unity().translateX(screwDistanceSmall))
            .transformed(
                Transform.unity().rotZ(-135.0)
            )
        val washerHole1 =
            washerHole.transformed(Transform.unity().translateX(screwDistanceBig)).transformed(
                Transform.unity().rotZ(-45.0)
            ).transformed(Transform.unity().translateZ(-washerHeight * 2))
        val washerHole2 =
            washerHole.transformed(Transform.unity().translateX(screwDistanceBig)).transformed(
                Transform.unity().rotZ(135.0)
            ).transformed(Transform.unity().translateZ(-washerHeight * 2))
        val washerHole3 =
            washerHole.transformed(Transform.unity().translateX(screwDistanceSmall)).transformed(
                Transform.unity().rotZ(45.0)
            ).transformed(Transform.unity().translateZ(-washerHeight * 2))
        val washerHole4 =
            washerHole.transformed(Transform.unity().translateX(screwDistanceSmall)).transformed(
                Transform.unity().rotZ(-135.0)
            ).transformed(Transform.unity().translateZ(-washerHeight * 2))

//        CSG hullCube = new RoundedCube(20,5,3.8).cornerRadius(1).toCSG().transformed(Transform.unity().translate(-10,-2.5,-3.8/2.0-enginePlatformThickness));
        val hullCube = Cylinder(3.0, 20.0, 16).toCSG()
            .transformed(Transform.unity().rotY(90.0))
            .transformed(Transform.unity().translate(0.0, 0.0, -2.0))
        enginePlatform = enginePlatform.union(hullCube).hull()
            .difference(washerHole1, washerHole2, washerHole3, washerHole4)
        enginePlatform =
            enginePlatform.difference(mainHole, screwHole1, screwHole2, screwHole3, screwHole4)
                .union(washer1, washer2, washer3, washer4)
        return enginePlatform
    }

    companion object {
        fun sideArms(
            sideArmGroundDist: Double,
            sideArmHight: Double,
            sideArmRadius: Double,
            sideArmShrinkFactor: Double,
            length: Double,
            armCubeThickness: Double,
            outerRadius: Double
        ): CSG {
            val sideArmLength =
                sqrt(sideArmGroundDist * sideArmGroundDist + sideArmHight * sideArmHight)
            val alpha =
                atan(sideArmGroundDist / sideArmHight) * 180 / Math.PI
            var subCylinder =
                Cylinder(sideArmRadius, sideArmLength + sideArmRadius, 16)
                    .toCSG()
                    .transformed(
                        Transform.unity().rotY(90.0)
                            .scaleX(sideArmShrinkFactor)
                    ).transformed(Transform.unity().rotZ(alpha))
                    .transformed(
                        Transform.unity().translateX(-length + sideArmHight)
                    )
            subCylinder = subCylinder.difference(
                Cube(
                    Vector3d.xy(
                        -length - sideArmRadius * 2,
                        sideArmGroundDist
                    ),
                    Vector3d.xyz(
                        sideArmRadius * 4,
                        sideArmRadius * 4,
                        sideArmRadius * 4
                    )
                ).toCSG()
            )
            subCylinder = subCylinder.union(
                Cube(
                    Vector3d.xyz(
                        -length + armCubeThickness / 2.0,
                        sideArmGroundDist,
                        0.0
                    ),
                    Vector3d.xyz(
                        armCubeThickness,
                        outerRadius * 2,
                        outerRadius * 2
                    )
                ).toCSG()
            )
            return subCylinder.union(
                subCylinder.transformed(
                    Transform.unity().mirror(Plane.XZ_PLANE)
                )
            ).transformed(Transform.unity().rotY(90.0).rotZ(180.0).rotX(90.0))
        }

        fun sideArms(
            sideArmGroundDist: Double,
            sideArmHight: Double,
            sideArmRadius: Double,
            sideArmShrinkFactor: Double,
            armCubeThickness: Double,
            outerRadius: Double
        ): CSG {
            val sideArmLength =
                sqrt(sideArmGroundDist * sideArmGroundDist + sideArmHight * sideArmHight)
            val alpha =
                atan(sideArmGroundDist / sideArmHight) * 180 / Math.PI
            var subCylinder =
                Cylinder(sideArmRadius, sideArmLength + sideArmRadius, 16)
                    .toCSG()
                    .transformed(
                        Transform.unity().rotY(90.0)
                            .scaleX(sideArmShrinkFactor)
                    ).transformed(Transform.unity().rotZ(alpha))
                    .transformed(Transform.unity().translateX(sideArmHight))
            subCylinder = subCylinder.difference(
                Cube(
                    Vector3d.xy(
                        0 - sideArmRadius * 2,
                        sideArmGroundDist
                    ),
                    Vector3d.xyz(
                        sideArmRadius * 4,
                        sideArmRadius * 4,
                        sideArmRadius * 4
                    )
                ).toCSG()
            )
            subCylinder = subCylinder.union(
                Cube(
                    Vector3d.xyz(
                        0 + armCubeThickness / 2.0,
                        sideArmGroundDist,
                        0.0
                    ),
                    Vector3d.xyz(
                        armCubeThickness,
                        outerRadius * 2,
                        outerRadius * 2
                    )
                ).toCSG()
            )
            return subCylinder.union(
                subCylinder.transformed(
                    Transform.unity().mirror(Plane.XZ_PLANE)
                )
            )
        }

        @JvmOverloads
        fun outerCyl(
            outerRadius: Double, length: Double,
            wallThickness: Double, scaleOuter: Double, scaleInner: Double, filled: Boolean = false
        ): CSG {
            var outerCyl = Cylinder(outerRadius, length, 32).toCSG()
                .transformed(Transform.unity().scaleX(scaleOuter))
            if (!filled) {
                val outerCylInner = Cylinder(
                    outerRadius - wallThickness / scaleOuter,
                    length, 32
                ).toCSG().transformed(Transform.unity().scaleX(scaleInner))
                outerCyl = outerCyl.difference(outerCylInner)
            }
            return outerCyl
        }

        //    private CSG enginePlatform(double engineRadius, double enginePlatformThickness, double mainHoleRadius, double screwRadius, double screwDistance) {
        //        CSG enginePlatform = new Cylinder(engineRadius, enginePlatformThickness, 32).toCSG();
        //
        //        CSG secondCyl = new Cylinder(engineRadius * 0.3, enginePlatformThickness, 3).toCSG().transformed(unity().translateX(-engineRadius * 2));
        //
        //        enginePlatform = enginePlatform.union(secondCyl).hull();
        //
        //        CSG mainHole = new Cylinder(mainHoleRadius, enginePlatformThickness, 16).toCSG();
        //
        //        CSG screwHolePrototype = new Cylinder(screwRadius, enginePlatformThickness, 16).toCSG();
        //
        //        double screwDistFromOrigin = screwDistance / 2.0;
        //        double upperScrewDistFromOrigin1 = screwDistFromOrigin* 0.9;
        //        double upperScrewDistFromOrigin2 = screwDistFromOrigin* 0.95;
        //
        //        CSG screwHole1 = screwHolePrototype.transformed(unity().translate(-screwDistFromOrigin, -screwDistFromOrigin, 0));
        //        CSG screwHole2 = screwHolePrototype.transformed(unity().translate(screwDistFromOrigin, -screwDistFromOrigin, 0));
        //        CSG screwHole3 = screwHolePrototype.transformed(unity().translate(screwDistFromOrigin, screwDistFromOrigin, 0));
        //        CSG screwHole4 = screwHolePrototype.transformed(unity().translate(-screwDistFromOrigin, screwDistFromOrigin, 0));
        //        
        //        screwHole1 = screwHole1.union(screwHolePrototype.transformed(unity().translate(-upperScrewDistFromOrigin1, -upperScrewDistFromOrigin2, 0))).hull();
        //        screwHole4 = screwHole4.union(screwHolePrototype.transformed(unity().translate(-upperScrewDistFromOrigin1, upperScrewDistFromOrigin2, 0))).hull();
        //        
        //        screwHole2 = screwHole2.union(screwHolePrototype.transformed(unity().translate(upperScrewDistFromOrigin1, -upperScrewDistFromOrigin1, 0))).hull();
        //        screwHole3 = screwHole3.union(screwHolePrototype.transformed(unity().translate(upperScrewDistFromOrigin1, upperScrewDistFromOrigin1, 0))).hull();
        //
        //        enginePlatform = enginePlatform.difference(mainHole, screwHole1, screwHole2, screwHole3, screwHole4);
        //
        //        return enginePlatform;
        //    }
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val result = QuadrocopterArm().toCSG()
            FileUtil.write(Paths.get("quadrocopter-arm.stl"), result.toStlString())
            result.toObj().toFiles(Paths.get("quadrocopter-arm.obj"))

//        CSG resultNoStructure = new QuadrocopterArm().toCSG();
//
//        FileUtil.write(Paths.get("quadrocopter-arm-no-structure.stl"), resultNoStructure.toStlString());
//        resultNoStructure.toObj().toFiles(Paths.get("quadrocopter-arm-no-structure.obj"));
        }
    }
}
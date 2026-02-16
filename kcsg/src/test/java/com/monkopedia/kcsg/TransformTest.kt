package com.monkopedia.kcsg

import com.monkopedia.kcsg.ext.vvecmath.Plane
import com.monkopedia.kcsg.testutil.GeometryAssertions.assertVectorClose
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TransformTest {
    @Test
    fun unityFromAndToMatrixValues() {
        val values = DoubleArray(16) { -1.0 }
        val identity = Transform.unity()
        val returned = identity.to(values)

        assertSame(values, returned)
        assertArrayEquals(
            doubleArrayOf(
                1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0,
            ),
            values,
            EPS,
        )

        val from = Transform.from(
            1.0, 0.0, 0.0, 4.0,
            0.0, 1.0, 0.0, -3.0,
            0.0, 0.0, 1.0, 2.0,
            0.0, 0.0, 0.0, 1.0,
        )
        assertVectorClose(Vector3d.xyz(5.0, -2.0, 3.0), from.transform(Vector3d.xyz(1.0, 1.0, 1.0)), EPS)
    }

    @Test
    fun axisRotationOverloads() {
        assertVectorClose(
            Vector3d.xyz(0.0, 0.0, -1.0),
            Transform.unity().rotX(90.0).transform(Vector3d.Y_ONE),
            ROTATION_EPS,
        )
        assertVectorClose(
            Vector3d.xyz(0.0, 0.0, 1.0),
            Transform.unity().rotY(90.0).transform(Vector3d.X_ONE),
            ROTATION_EPS,
        )
        assertVectorClose(
            Vector3d.xyz(0.0, -1.0, 0.0),
            Transform.unity().rotZ(90.0).transform(Vector3d.X_ONE),
            ROTATION_EPS,
        )

        val probe = Vector3d.xyz(0.7, -2.0, 5.0)
        assertVectorClose(
            Transform.unity().rot(15.0, 30.0, -45.0).transform(probe),
            Transform.unity().rot(Vector3d.xyz(15.0, 30.0, -45.0)).transform(probe),
            ROTATION_EPS,
        )
    }

    @Test
    fun vectorAndAxisBasedRotationOverloads() {
        val rotatedFromTo = Transform.unity().rot(Vector3d.X_ONE, Vector3d.Y_ONE)
        assertVectorClose(
            Vector3d.Y_ONE,
            rotatedFromTo.transform(Vector3d.X_ONE).normalized(),
            ROTATION_EPS,
        )

        val noOpFromTo = Transform.unity().rot(Vector3d.X_ONE, Vector3d.X_ONE)
        val original = Vector3d.xyz(2.0, -3.0, 4.0)
        assertVectorClose(original, noOpFromTo.transform(original), EPS)

        val axisRotation = Transform.unity().rot(Vector3d.ZERO, Vector3d.Z_ONE, 90.0)
        assertVectorClose(Vector3d.xyz(0.0, 1.0, 0.0), axisRotation.transform(Vector3d.X_ONE), ROTATION_EPS)
    }

    @Test
    fun translateAndScaleOverloads() {
        val probe = Vector3d.xyz(1.0, 2.0, 3.0)
        assertVectorClose(
            Vector3d.xyz(4.0, 6.0, 8.0),
            Transform.unity().translate(Vector3d.xyz(3.0, 4.0, 5.0)).transform(probe),
            EPS,
        )
        assertVectorClose(
            Vector3d.xyz(4.0, 6.0, 8.0),
            Transform.unity().translate(3.0, 4.0, 5.0).transform(probe),
            EPS,
        )
        assertVectorClose(Vector3d.xyz(4.0, 2.0, 3.0), Transform.unity().translateX(3.0).transform(probe), EPS)
        assertVectorClose(Vector3d.xyz(1.0, 5.0, 3.0), Transform.unity().translateY(3.0).transform(probe), EPS)
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 6.0), Transform.unity().translateZ(3.0).transform(probe), EPS)

        assertVectorClose(
            Vector3d.xyz(2.0, 6.0, 12.0),
            Transform.unity().scale(Vector3d.xyz(2.0, 3.0, 4.0)).transform(probe),
            EPS,
        )
        assertVectorClose(
            Vector3d.xyz(2.0, 6.0, 12.0),
            Transform.unity().scale(2.0, 3.0, 4.0).transform(probe),
            EPS,
        )
        assertVectorClose(Vector3d.xyz(2.0, 4.0, 6.0), Transform.unity().scale(2.0).transform(probe), EPS)
        assertVectorClose(Vector3d.xyz(2.0, 2.0, 3.0), Transform.unity().scaleX(2.0).transform(probe), EPS)
        assertVectorClose(Vector3d.xyz(1.0, 4.0, 3.0), Transform.unity().scaleY(2.0).transform(probe), EPS)
        assertVectorClose(Vector3d.xyz(1.0, 2.0, 6.0), Transform.unity().scaleZ(2.0).transform(probe), EPS)
    }

    @Test
    fun mirrorTransformApplyAndAmountInterpolation() {
        val mirrored = Transform.unity().mirror(Plane.XY_PLANE)
        assertTrue(mirrored.isMirror)
        assertVectorClose(Vector3d.xyz(1.0, 2.0, -3.0), mirrored.transform(Vector3d.xyz(1.0, 2.0, 3.0)), EPS)
        assertFalse(Transform.unity().scale(2.0).isMirror)

        val base = Vector3d.xyz(1.0, -2.0, 3.0)
        val translation = Transform.unity().translate(10.0, 0.0, -4.0)
        assertVectorClose(Vector3d.xyz(11.0, -2.0, -1.0), translation.transform(base), EPS)
        assertVectorClose(Vector3d.xyz(6.0, -2.0, 1.0), translation.transform(base, 0.5), EPS)

        val viaApply = Transform.unity()
            .apply(Transform.unity().translate(2.0, 3.0, 4.0))
            .apply(Transform.unity().scale(2.0))
        val viaChain = Transform.unity().translate(2.0, 3.0, 4.0).scale(2.0)
        assertVectorClose(viaChain.transform(base), viaApply.transform(base), EPS)
    }

    @Test
    fun scaleByZeroIsRejectedForEveryOverload() {
        assertThrows(IllegalArgumentException::class.java) {
            Transform.unity().scale(Vector3d.xyz(0.0, 1.0, 1.0))
        }
        assertThrows(IllegalArgumentException::class.java) {
            Transform.unity().scale(1.0, 0.0, 1.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Transform.unity().scale(0.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Transform.unity().scaleX(0.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Transform.unity().scaleY(0.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Transform.unity().scaleZ(0.0)
        }
    }

    companion object {
        private const val EPS = 1e-9
        private const val ROTATION_EPS = 1e-6
    }
}

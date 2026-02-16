package com.monkopedia.kcsg

import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.CullFace
import javafx.scene.shape.Mesh
import javafx.scene.shape.TriangleMesh
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MeshContainerTest {
    @Test
    fun dimensionsBoundsAccessorsAndMeshViews() {
        val mesh1 = simpleTriangleMesh()
        val mesh2 = simpleTriangleMesh()
        val min = Vector3d.xyz(-1.0, -2.0, -3.0)
        val max = Vector3d.xyz(3.0, 4.0, 5.0)

        val container = MeshContainer(min, max, mesh1, mesh2)
        assertEquals(4.0, container.getWidth(), 1e-9)
        assertEquals(6.0, container.getHeight(), 1e-9)
        assertEquals(8.0, container.getDepth(), 1e-9)
        assertEquals(Bounds(min, max), container.getBounds())
        assertEquals(2, container.getMeshes().size)
        assertEquals(2, container.getMaterials().size)
        assertTrue(container.toString().contains("bounds"))

        val meshViews = container.getAsMeshViews()
        assertEquals(2, meshViews.size)
        meshViews.forEachIndexed { index, meshView ->
            assertEquals(container.getMeshes()[index], meshView.mesh)
            assertEquals(container.getMaterials()[index], meshView.material)
            assertEquals(CullFace.NONE, meshView.cullFace)
        }
    }

    @Test
    fun constructorRejectsMismatchedMeshAndMaterialCounts() {
        val min = Vector3d.ZERO
        val max = Vector3d.UNITY
        val meshes: List<Mesh> = listOf(simpleTriangleMesh())
        val materials: MutableList<Material> = mutableListOf(
            PhongMaterial(Color.RED),
            PhongMaterial(Color.BLUE),
        )

        assertThrows(IllegalArgumentException::class.java) {
            MeshContainer(min, max, meshes, materials)
        }
    }

    private fun simpleTriangleMesh(): TriangleMesh {
        val mesh = TriangleMesh()
        mesh.points.addAll(
            0f, 0f, 0f,
            1f, 0f, 0f,
            0f, 1f, 0f,
        )
        mesh.texCoords.addAll(0f, 0f)
        mesh.faces.addAll(0, 0, 1, 0, 2, 0)
        return mesh
    }
}

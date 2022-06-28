/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.jcsg

import eu.mihosoft.vvecmath.Vector3d
import javafx.scene.Group
import javafx.scene.SubScene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.paint.Material
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.CullFace
import javafx.scene.shape.Mesh
import javafx.scene.shape.MeshView
import java.util.*

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class MeshContainer {
    private val meshes: List<Mesh>
    private val materials: MutableList<Material>
    private val width: Double
    private val height: Double
    private val depth: Double
    private val bounds: Bounds
    private val root = Group()
    private val viewContainer: Pane? = null
    private val subScene: SubScene? = null

    internal constructor(min: Vector3d, max: Vector3d, vararg meshes: Mesh) : this(
        min,
        max,
        listOf<Mesh>(*meshes)
    )

    internal constructor(min: Vector3d, max: Vector3d, meshes: List<Mesh>) {
        this.meshes = meshes
        materials = ArrayList()
        bounds = Bounds(min, max)
        width = bounds.bounds.x()
        height = bounds.bounds.y()
        depth = bounds.bounds.z()
        val material = PhongMaterial(Color.RED)
        for (mesh in meshes) {
            materials.add(material)
        }
    }

    internal constructor(
        min: Vector3d,
        max: Vector3d,
        meshes: List<Mesh>,
        materials: MutableList<Material>
    ) {
        this.meshes = meshes
        this.materials = materials
        bounds = Bounds(min, max)
        width = bounds.bounds.x()
        height = bounds.bounds.y()
        depth = bounds.bounds.z()
        require(materials.size == meshes.size) { "Mesh list and Material list must not differ in size!" }
    }

    /**
     * @return the width
     */
    fun getWidth(): Double {
        return width
    }

    /**
     * @return the height
     */
    fun getHeight(): Double {
        return height
    }

    /**
     * @return the depth
     */
    fun getDepth(): Double {
        return depth
    }

    /**
     * @return the mesh
     */
    fun getMeshes(): List<Mesh> {
        return meshes
    }

    override fun toString(): String {
        return bounds.toString()
    }

    /**
     * @return the bounds
     */
    fun getBounds(): Bounds {
        return bounds
    }

    /**
     * @return the materials
     */
    fun getMaterials(): List<Material> {
        return materials
    }

    fun getAsMeshViews(): List<MeshView> {
        val result: MutableList<MeshView> = ArrayList(meshes.size)
        for (i in meshes.indices) {
            val mesh = meshes[i]
            val mat = materials[i]
            val view = MeshView(mesh)
            view.material = mat
            view.cullFace = CullFace.NONE
            result.add(view)
        }
        return result
    } //    public javafx.scene.Node getAsInteractiveSubSceneNode() {
    //
    //        if (viewContainer != null) {
    //            return viewContainer;
    //        }
    //
    //        viewContainer = new Pane();
    //
    //        SubScene subScene = new SubScene(getRoot(), 100, 100, true, SceneAntialiasing.BALANCED);
    ////        subScene.setFill(Color.BLACK);
    //
    //        subScene.widthProperty().bind(viewContainer.widthProperty());
    //        subScene.heightProperty().bind(viewContainer.heightProperty());
    //
    //        PerspectiveCamera subSceneCamera = new PerspectiveCamera(false);
    //        subScene.setCamera(subSceneCamera);
    //
    //        viewContainer.getChildren().add(subScene);
    //
    //        getRoot().layoutXProperty().bind(viewContainer.widthProperty().divide(2));
    //        getRoot().layoutYProperty().bind(viewContainer.heightProperty().divide(2));
    //
    //        viewContainer.boundsInLocalProperty().addListener(
    //                (ObservableValue<? extends javafx.geometry.Bounds> ov, javafx.geometry.Bounds t, javafx.geometry.Bounds t1) -> {
    //                    setMeshScale(this, t1, getRoot());
    //                });
    //
    //        VFX3DUtil.addMouseBehavior(getRoot(), viewContainer, MouseButton.PRIMARY);
    //
    //        return viewContainer;
    //    }
    //
    //    private void setMeshScale(MeshContainer meshContainer, javafx.geometry.Bounds t1, final Group meshView) {
    //        double maxDim
    //                = Math.max(meshContainer.getWidth(),
    //                        Math.max(meshContainer.getHeight(), meshContainer.getDepth()));
    //
    //        double minContDim = Math.min(t1.getWidth(), t1.getHeight());
    //
    //        double scale = minContDim / (maxDim * 2);
    //
    //        //System.out.println("scale: " + scale + ", maxDim: " + maxDim + ", " + meshContainer);
    //        meshView.setScaleX(scale);
    //        meshView.setScaleY(scale);
    //        meshView.setScaleZ(scale);
    //    }
}
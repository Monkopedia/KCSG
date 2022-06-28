/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package eu.mihosoft.jcsg.ext.openjfx.importers

import javafx.collections.ObservableIntegerArray
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.shape.Mesh
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh

/**
 * Mesh data validator
 */
class Validator {
    fun validate(node: Node?) {
        if (node is MeshView) {
            validate(node.mesh)
        } else if (node is Parent) {
            for (child in node.childrenUnmodifiable) {
                validate(child)
            }
        }
    }

    fun validate(mesh: Mesh) {
        if (mesh !is TriangleMesh) {
            throw AssertionError("Mesh is not TriangleMesh: " + mesh.javaClass + ", mesh = " + mesh)
        }
        val tMesh = mesh
        val numPoints = tMesh.points.size() / tMesh.pointElementSize
        val numTexCoords = tMesh.texCoords.size() / tMesh.texCoordElementSize
        val numFaces = tMesh.faces.size() / tMesh.faceElementSize
        if (numPoints == 0 || numPoints * tMesh.pointElementSize != tMesh.points.size()) {
            throw AssertionError("Points array size is not correct: " + tMesh.points.size())
        }
        if (numTexCoords == 0 || numTexCoords * tMesh.texCoordElementSize != tMesh.texCoords.size()) {
            throw AssertionError("TexCoords array size is not correct: " + tMesh.points.size())
        }
        if (numFaces == 0 || numFaces * tMesh.faceElementSize != tMesh.faces.size()) {
            throw AssertionError("Faces array size is not correct: " + tMesh.points.size())
        }
        if (numFaces != tMesh.faceSmoothingGroups.size() && tMesh.faceSmoothingGroups.size() > 0) {
            throw AssertionError("FaceSmoothingGroups array size is not correct: " + tMesh.points.size() + ", numFaces = " + numFaces)
        }
        val faces: ObservableIntegerArray = tMesh.faces
        var i = 0
        while (i < faces.size()) {
            val pIndex = faces[i]
            if (pIndex < 0 || pIndex > numPoints) {
                throw AssertionError("Incorrect point index: $pIndex, numPoints = $numPoints")
            }
            val tcIndex = faces[i + 1]
            if (tcIndex < 0 || tcIndex > numTexCoords) {
                throw AssertionError("Incorrect texCoord index: $tcIndex, numTexCoords = $numTexCoords")
            }
            i += 2
        }
        //        System.out.println("Validation successfull of " + mesh);
    }
}
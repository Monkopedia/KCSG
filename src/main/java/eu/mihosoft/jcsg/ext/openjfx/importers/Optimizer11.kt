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

import javafx.animation.Interpolator
import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.beans.property.Property
import javafx.beans.value.WritableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableIntegerArray
import javafx.collections.ObservableList
import javafx.geometry.Point2D
import javafx.geometry.Point3D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.shape.MeshView
import javafx.scene.shape.TriangleMesh
import javafx.scene.transform.Transform
import javafx.util.Duration

/**
 * Optimizer to take 3D model and timeline loaded by one of the importers and do as much optimization on
 * the scene graph that was create as we can while still being able to play the given animation.
 */
class Optimizer @JvmOverloads constructor(
    private val timeline: Timeline?,
    private val root: Node,
    convertToDiscrete: Boolean = false
) {
    private val bound: MutableSet<Transform> = HashSet()
    private val emptyParents: MutableList<Parent> = ArrayList()
    private val meshViews: MutableList<MeshView> = ArrayList()
    private val convertToDiscrete = convertToDiscrete
    private var trRemoved = 0
    private var trTotal = 0
    private var groupsTotal = 0
    private var trCandidate = 0
    private var trEmpty = 0
    fun optimize() {
        trRemoved = 0
        trTotal = 0
        trCandidate = 0
        trEmpty = 0
        groupsTotal = 0
        emptyParents.clear()
        parseTimeline()
        optimize(root)
        removeEmptyGroups()
        optimizeMeshes()
        System.out.printf(
            "removed %d (%.2f%%) out of total %d transforms\n",
            trRemoved,
            100.0 * trRemoved / trTotal,
            trTotal
        )
        System.out.printf(
            "there are %d more multiplications that can be done of matrices that never change\n",
            trCandidate
        )
        System.out.printf(
            "there are %d (%.2f%%) out of total %d groups with no transforms in them\n",
            trEmpty,
            100.0 * trEmpty / groupsTotal,
            groupsTotal
        )
    }

    private fun optimize(node: Node) {
        val transforms = node.transforms
        val iterator = transforms.iterator()
        var prevIsStatic = false
        while (iterator.hasNext()) {
            val transform = iterator.next()
            trTotal++
            if (transform.isIdentity) {
                if (timeline == null || !bound.contains(transform)) {
                    iterator.remove()
                    trRemoved++
                }
            } else {
                prevIsStatic = if (timeline == null || !bound.contains(transform)) {
                    if (prevIsStatic) {
                        trCandidate++
                    }
                    true
                } else {
                    false
                }
            }
        }
        if (node is Parent) {
            groupsTotal++
            for (n in node.childrenUnmodifiable) {
                optimize(n)
            }
            if (transforms.isEmpty()) {
                val parent = node.parent
                if (parent is Group) {
                    trEmpty++
                    //                    System.out.println("Empty group = " + node.getId());
                    emptyParents.add(node)
                } else {
//                    System.err.println("parent is not group = " + parent);
                }
            }
        }
        if (node is MeshView) {
            meshViews.add(node)
        }
    }

    private fun optimizeMeshes() {
        optimizePoints()
        optimizeTexCoords()
        optimizeFaces()
    }

    private fun optimizeFaces() {
        var total = 0
        var sameIndexes = 0
        var samePoints = 0
        var smallArea = 0
        val newFaces = FXCollections.observableIntegerArray()
        val newFaceSmoothingGroups = FXCollections.observableIntegerArray()
        for (meshView in meshViews) {
            val mesh = meshView.mesh as TriangleMesh
            val faces: ObservableIntegerArray = mesh.faces
            val faceSmoothingGroups = mesh.faceSmoothingGroups
            val points = mesh.points
            newFaces.clear()
            newFaces.ensureCapacity(faces.size())
            newFaceSmoothingGroups.clear()
            newFaceSmoothingGroups.ensureCapacity(faceSmoothingGroups.size())
            val pointElementSize = mesh.pointElementSize
            val faceElementSize = mesh.faceElementSize
            var i = 0
            while (i < faces.size()) {
                total++
                val i1 = faces[i] * pointElementSize
                val i2 = faces[i + 2] * pointElementSize
                val i3 = faces[i + 4] * pointElementSize
                if (i1 == i2 || i1 == i3 || i2 == i3) {
                    sameIndexes++
                    i += faceElementSize
                    continue
                }
                val p1 = Point3D(
                    points[i1].toDouble(), points[i1 + 1].toDouble(), points[i1 + 2].toDouble()
                )
                val p2 = Point3D(
                    points[i2].toDouble(), points[i2 + 1].toDouble(), points[i2 + 2].toDouble()
                )
                val p3 = Point3D(
                    points[i3].toDouble(), points[i3 + 1].toDouble(), points[i3 + 2].toDouble()
                )
                if (p1 == p2 || p1 == p3 || p2 == p3) {
                    samePoints++
                    i += faceElementSize
                    continue
                }
                val a = p1.distance(p2)
                val b = p2.distance(p3)
                val c = p3.distance(p1)
                val p = (a + b + c) / 2
                val sqarea = p * (p - a) * (p - b) * (p - c)
                val DEAD_FACE = 1f / 1024 / 1024 / 1024 / 1024 // taken from MeshNormal code
                if (sqarea < DEAD_FACE) {
                    smallArea++
                    i += faceElementSize
                    //                    System.out.printf("a = %e, b = %e, c = %e, sqarea = %e\n"
//                            + "p1 = %s\np2 = %s\np3 = %s\n", a, b, c, sqarea, p1.toString(), p2.toString(), p3.toString());
                    continue
                }
                newFaces.addAll(faces, i, faceElementSize)
                val fIndex = i / faceElementSize
                if (fIndex < faceSmoothingGroups.size()) {
                    newFaceSmoothingGroups.addAll(faceSmoothingGroups[fIndex])
                }
                i += faceElementSize
            }
            faces.setAll(newFaces)
            faceSmoothingGroups.setAll(newFaceSmoothingGroups)
            faces.trimToSize()
            faceSmoothingGroups.trimToSize()
        }
        val badTotal = sameIndexes + samePoints + smallArea
        System.out.printf(
            """
    Removed %d (%.2f%%) faces with same point indexes, %d (%.2f%%) faces with same points, %d (%.2f%%) faces with small area. Total %d (%.2f%%) bad faces out of %d total.
    
    """.trimIndent(),
            sameIndexes, 100.0 * sameIndexes / total,
            samePoints, 100.0 * samePoints / total,
            smallArea, 100.0 * smallArea / total,
            badTotal, 100.0 * badTotal / total, total
        )
    }

    private fun optimizePoints() {
        var total = 0
        var duplicates = 0
        var check = 0
        val pp: MutableMap<Point3D, Int> = HashMap()
        val reindex = FXCollections.observableIntegerArray()
        val newPoints = FXCollections.observableFloatArray()
        for (meshView in meshViews) {
            val mesh = meshView.mesh as TriangleMesh
            val points = mesh.points
            val pointElementSize = mesh.pointElementSize
            val os = points.size() / pointElementSize
            pp.clear()
            newPoints.clear()
            newPoints.ensureCapacity(points.size())
            reindex.clear()
            reindex.resize(os)
            run {
                var i = 0
                var oi = 0
                var ni = 0
                while (i < points.size()) {
                    val x = points[i]
                    val y = points[i + 1]
                    val z = points[i + 2]
                    val p = Point3D(x.toDouble(), y.toDouble(), z.toDouble())
                    val index = pp[p]
                    if (index == null) {
                        pp[p] = ni
                        reindex[oi] = ni
                        newPoints.addAll(x, y, z)
                        ni++
                    } else {
                        reindex[oi] = index
                    }
                    i += pointElementSize
                    oi++
                }
            }
            val ns = newPoints.size() / pointElementSize
            val d = os - ns
            duplicates += d
            total += os
            points.setAll(newPoints)
            points.trimToSize()
            val faces: ObservableIntegerArray = mesh.faces
            var i = 0
            while (i < faces.size()) {
                faces[i] = reindex[faces[i]]
                i += 2
            }

//            System.out.printf("There are %d (%.2f%%) duplicate points out of %d total for mesh '%s'.\n",
//                    d, 100d * d / os, os, meshView.getId());
            check += mesh.points.size() / pointElementSize
        }
        System.out.printf(
            "There are %d (%.2f%%) duplicate points out of %d total.\n",
            duplicates, 100.0 * duplicates / total, total
        )
        System.out.printf("Now we have %d points.\n", check)
    }

    private fun optimizeTexCoords() {
        var total = 0
        var duplicates = 0
        var check = 0
        val pp: MutableMap<Point2D, Int> = HashMap()
        val reindex = FXCollections.observableIntegerArray()
        val newTexCoords = FXCollections.observableFloatArray()
        for (meshView in meshViews) {
            val mesh = meshView.mesh as TriangleMesh
            val texcoords = mesh.texCoords
            val texcoordElementSize = mesh.texCoordElementSize
            val os = texcoords.size() / texcoordElementSize
            pp.clear()
            newTexCoords.clear()
            newTexCoords.ensureCapacity(texcoords.size())
            reindex.clear()
            reindex.resize(os)
            run {
                var i = 0
                var oi = 0
                var ni = 0
                while (i < texcoords.size()) {
                    val x = texcoords[i]
                    val y = texcoords[i + 1]
                    val p = Point2D(x.toDouble(), y.toDouble())
                    val index = pp[p]
                    if (index == null) {
                        pp[p] = ni
                        reindex[oi] = ni
                        newTexCoords.addAll(x, y)
                        ni++
                    } else {
                        reindex[oi] = index
                    }
                    i += texcoordElementSize
                    oi++
                }
            }
            val ns = newTexCoords.size() / texcoordElementSize
            val d = os - ns
            duplicates += d
            total += os
            texcoords.setAll(newTexCoords)
            texcoords.trimToSize()
            val faces: ObservableIntegerArray = mesh.faces
            var i = 1
            while (i < faces.size()) {
                faces[i] = reindex[faces[i]]
                i += 2
            }

//            System.out.printf("There are %d (%.2f%%) duplicate texcoords out of %d total for mesh '%s'.\n",
//                    d, 100d * d / os, os, meshView.getId());
            check += mesh.texCoords.size() / texcoordElementSize
        }
        System.out.printf(
            "There are %d (%.2f%%) duplicate texcoords out of %d total.\n",
            duplicates, 100.0 * duplicates / total, total
        )
        System.out.printf("Now we have %d texcoords.\n", check)
    }

    private fun cleanUpRepeatingFramesAndValues() {
        val timelineKeyFrames: ObservableList<KeyFrame?> =
            timeline!!.keyFrames.sorted(KeyFrameComparator())
        //        Timeline timeline;
        val kfTotal = timelineKeyFrames.size
        var kfRemoved = 0
        var kvTotal = 0
        var kvRemoved = 0
        val kfUnique: MutableMap<Duration, KeyFrame?> = HashMap()
        val kvUnique: MutableMap<WritableValue<*>, KeyValue> = HashMap()
        val duplicates = MapOfLists<KeyFrame?, KeyFrame?>()
        val iterator = timelineKeyFrames.iterator()
        while (iterator.hasNext()) {
            val duplicate = iterator.next()
            val original = kfUnique.put(duplicate!!.time, duplicate)
            if (original != null) {
                kfRemoved++
                iterator.remove() // removing duplicate keyFrame
                duplicates.add(original, duplicate)
                kfUnique[duplicate.time] = original
            }
            kvUnique.clear()
            for (kvDup in duplicate.values) {
                kvTotal++
                val kvOrig = kvUnique.put(kvDup.target, kvDup)
                if (kvOrig != null) {
                    kvRemoved++
                    if (kvOrig.endValue != kvDup.endValue && kvOrig.target === kvDup.target) {
                        System.err.println(
                            """KeyValues set different values for KeyFrame ${duplicate.time}:
 kvOrig = $kvOrig, 
kvDup = $kvDup"""
                        )
                    }
                }
            }
        }
        for (orig in duplicates.keys) {
            val keyValues: MutableList<KeyValue> = ArrayList()
            for (dup in duplicates[orig]!!) {
                keyValues.addAll(dup!!.values)
            }
            timelineKeyFrames[timelineKeyFrames.indexOf(orig)] =
                KeyFrame(orig!!.time, *keyValues.toTypedArray())
        }
        System.out.printf(
            "Removed %d (%.2f%%) duplicate KeyFrames out of total %d.\n",
            kfRemoved, 100.0 * kfRemoved / kfTotal, kfTotal
        )
        System.out.printf(
            "Identified %d (%.2f%%) duplicate KeyValues out of total %d.\n",
            kvRemoved, 100.0 * kvRemoved / kvTotal, kvTotal
        )
    }

    private class KeyInfo {
        var keyFrame: KeyFrame
        var keyValue: KeyValue
        var first: Boolean

        constructor(keyFrame: KeyFrame, keyValue: KeyValue) {
            this.keyFrame = keyFrame
            this.keyValue = keyValue
            first = false
        }

        constructor(keyFrame: KeyFrame, keyValue: KeyValue, first: Boolean) {
            this.keyFrame = keyFrame
            this.keyValue = keyValue
            this.first = first
        }
    }

    private class MapOfLists<K, V> : HashMap<K, MutableList<V>>() {
        fun add(key: K, value: V) {
            var p: MutableList<V>? = get(key)
            if (p == null) {
                p = ArrayList()
                put(key, p)
            }
            p.add(value)
        }
    }

    private fun parseTimeline() {
        bound.clear()
        if (timeline == null) {
            return
        }
        //        cleanUpRepeatingFramesAndValues(); // we don't need it usually as timeline is initially correct
        val sortedKeyFrames = timeline.keyFrames.sorted(KeyFrameComparator())
        val toRemove = MapOfLists<KeyFrame?, KeyValue?>()
        val prevValues: MutableMap<WritableValue<*>, KeyInfo> = HashMap()
        val prevPrevValues: MutableMap<WritableValue<*>, KeyInfo> = HashMap()
        var kvTotal = 0
        for (keyFrame in sortedKeyFrames) {
            for (keyValue in keyFrame.values) {
                val target = keyValue.target
                val prev = prevValues[target]
                kvTotal++
                if (prev != null && prev.keyValue.endValue == keyValue.endValue) {
//                if (prev != null && (prev.keyValue.equals(keyValue) || (prev.first && prev.keyValue.getEndValue().equals(keyValue.getEndValue())))) {
                    val prevPrev = prevPrevValues[target]
                    if (prevPrev != null && prevPrev.keyValue.endValue == keyValue.endValue
                        || prev.first && target.value == prev.keyValue.endValue
                    ) {
                        // All prevPrev, prev and current match, so prev can be removed
                        // or prev is first and its value equals to the property existing value, so prev can be removed
                        toRemove.add(prev.keyFrame, prev.keyValue)
                    } else {
                        prevPrevValues[target] = prev
                        //                        KeyInfo oldKeyInfo = prevPrevValues.put(target, prev);
//                        if (oldKeyInfo != null && oldKeyInfo.keyFrame.getTime().equals(prev.keyFrame.getTime())) {
//                            System.err.println("prevPrev replaced more than once per keyFrame on " + target + "\n"
//                                    + "old = " + oldKeyInfo.keyFrame.getTime() + ", " + oldKeyInfo.keyValue + "\n"
//                                    + "new = " + prev.keyFrame.getTime() + ", " + prev.keyValue
//                                    );
//                        }
                    }
                }
                val oldPrev = prevValues.put(target, KeyInfo(keyFrame, keyValue, prev == null))
                if (oldPrev != null) prevPrevValues[target] = oldPrev
            }
        }
        // Deal with ending keyValues
        for (target in prevValues.keys) {
            val prev = prevValues[target]
            val prevPrev = prevPrevValues[target]
            if (prevPrev != null && prevPrev.keyValue.endValue == prev!!.keyValue.endValue) {
                // prevPrev and prev match, so prev can be removed
                toRemove.add(prev.keyFrame, prev.keyValue)
            }
        }
        var kvRemoved = 0
        var kfRemoved = 0
        val kfTotal = timeline.keyFrames.size
        var kfSimplified = 0
        var kfNotRemoved = 0
        // Removing unnecessary KeyValues and KeyFrames
        val newKeyValues: MutableList<KeyValue> = ArrayList()
        var i = 0
        while (i < timeline.keyFrames.size) {
            var keyFrame = timeline.keyFrames[i]
            val keyValuesToRemove: MutableList<KeyValue?>? = toRemove[keyFrame]
            if (keyValuesToRemove != null) {
                newKeyValues.clear()
                for (keyValue in keyFrame.values) {
                    if (keyValuesToRemove.remove(keyValue)) {
                        kvRemoved++
                    } else {
                        if (convertToDiscrete) {
                            newKeyValues.add(
                                KeyValue(
                                    keyValue.target as WritableValue<Any>,
                                    keyValue.endValue as Any,
                                    Interpolator.DISCRETE
                                )
                            )
                        } else {
                            newKeyValues.add(keyValue)
                        }
                    }
                }
            } else if (convertToDiscrete) {
                newKeyValues.clear()
                for (keyValue in keyFrame.values) {
                    newKeyValues.add(
                        KeyValue(
                            keyValue.target as WritableValue<Any>,
                            keyValue.endValue as Any,
                            Interpolator.DISCRETE
                        )
                    )
                }
            }
            if (keyValuesToRemove != null || convertToDiscrete) {
                if (newKeyValues.isEmpty()) {
                    if (keyFrame.onFinished == null) {
                        if (keyFrame.name != null) {
                            System.err.println("Removed KeyFrame with name = " + keyFrame.name)
                        }
                        timeline.keyFrames.removeAt(i)
                        i--
                        kfRemoved++
                        i++
                        continue  // for i
                    } else {
                        kfNotRemoved++
                    }
                } else {
                    keyFrame =
                        KeyFrame(keyFrame.time, keyFrame.name, keyFrame.onFinished, newKeyValues)
                    timeline.keyFrames[i] = keyFrame
                    kfSimplified++
                }
            }
            // collecting bound targets
            for (keyValue in keyFrame.values) {
                val target = keyValue.target
                if (target is Property<*>) {
                    val bean = target.bean
                    if (bean is Transform) {
                        bound.add(bean)
                    } else {
                        throw UnsupportedOperationException("Bean is not transform, bean = $bean")
                    }
                } else {
                    throw UnsupportedOperationException("WritableValue is not property, can't identify what it changes, target = $target")
                }
            }
            i++
        }
        //        System.out.println("bound.size() = " + bound.size());
        System.out.printf(
            "Removed %d (%.2f%%) repeating KeyValues out of total %d.\n",
            kvRemoved,
            100.0 * kvRemoved / kvTotal,
            kvTotal
        )
        System.out.printf(
            "Removed %d (%.2f%%) and simplified %d (%.2f%%) KeyFrames out of total %d. %d (%.2f%%) were not removed due to event handler attached.\n",
            kfRemoved,
            100.0 * kfRemoved / kfTotal,
            kfSimplified,
            100.0 * kfSimplified / kfTotal,
            kfTotal,
            kfNotRemoved,
            100.0 * kfNotRemoved / kfTotal
        )
        var check = 0
        for (keyFrame in timeline.keyFrames) {
            check += keyFrame.values.size
            //            for (KeyValue keyValue : keyFrame.getValues()) {
//                if (keyValue.getInterpolator() != Interpolator.DISCRETE) {
//                    throw new IllegalStateException();
//                }
//            }
        }
        System.out.printf(
            "Now there are %d KeyValues and %d KeyFrames.\n",
            check,
            timeline.keyFrames.size
        )
    }

    private fun removeEmptyGroups() {
        for (p in emptyParents) {
            val parent = p.parent
            val g = parent as Group
            g.children.addAll(p.childrenUnmodifiable)
            g.children.remove(p)
        }
    }

    private class KeyFrameComparator : Comparator<KeyFrame> {
        override fun compare(o1: KeyFrame, o2: KeyFrame): Int {
//            int compareTo = o1.getTime().compareTo(o2.getTime());
//            if (compareTo == 0 && o1 != o2) {
//                System.err.println("those two KeyFrames are equal: o1 = " + o1.getTime() + " and o2 = " + o2.getTime());
//            }
            return o1.time.compareTo(o2.time)
        }
    }
}
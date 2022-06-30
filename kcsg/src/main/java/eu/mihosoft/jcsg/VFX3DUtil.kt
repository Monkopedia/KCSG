/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package eu.mihosoft.jcsg

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.transform.Rotate

/**
 * Utility class that allows to visualize meshes created with null [ ][MathUtil.evaluateFunction].
 *
 * @author Michael Hoffer <info></info>@michaelhoffer.de>
 */
class VFX3DUtil private constructor() {
    companion object {
        /**
         * Adds rotation behavior to the specified node.
         *
         * @param n node
         * @param eventReceiver receiver of the event
         * @param btn mouse button that shall be used for this behavior
         */
        fun addMouseBehavior(
            n: Node, eventReceiver: Scene, btn: MouseButton?
        ) {
            eventReceiver.addEventHandler(
                MouseEvent.ANY,
                MouseBehaviorImpl1(n, btn)
            )
        }

        /**
         * Adds rotation behavior to the specified node.
         *
         * @param n node
         * @param eventReceiver receiver of the event
         * @param btn mouse button that shall be used for this behavior
         */
        fun addMouseBehavior(
            n: Node, eventReceiver: Node, btn: MouseButton?
        ) {
            eventReceiver.addEventHandler(
                MouseEvent.ANY,
                MouseBehaviorImpl1(n, btn)
            )
        }
    }

    init {
        throw AssertionError("don't instanciate me!")
    }
} // rotation behavior implementation

internal class MouseBehaviorImpl1(n: Node, btn: MouseButton?) : EventHandler<MouseEvent> {
    private var anchorAngleX = 0.0
    private var anchorAngleY = 0.0
    private var anchorX = 0.0
    private var anchorY = 0.0
    private val rotateX = Rotate(0.0, 0.0, 0.0, 0.0, Rotate.X_AXIS)
    private val rotateZ = Rotate(0.0, 0.0, 0.0, 0.0, Rotate.Z_AXIS)
    private val btn: MouseButton?
    override fun handle(t: MouseEvent) {
        if (btn != t.button) {
            return
        }
        t.consume()
        if (MouseEvent.MOUSE_PRESSED == t.eventType) {
            anchorX = t.sceneX
            anchorY = t.sceneY
            anchorAngleX = rotateX.angle
            anchorAngleY = rotateZ.angle
            t.consume()
        } else if (MouseEvent.MOUSE_DRAGGED == t.eventType) {
            rotateZ.angle = anchorAngleY + (anchorX - t.sceneX) * 0.7
            rotateX.angle = anchorAngleX - (anchorY - t.sceneY) * 0.7
        }
    }

    init {
        n.transforms.addAll(rotateX, rotateZ)
        this.btn = btn ?:  MouseButton.MIDDLE
    }
}
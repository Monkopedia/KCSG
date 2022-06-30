/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
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
package eu.mihosoft.jcsg.ext.openjfx.utils3d

import javafx.beans.property.Property
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

/**
 * Utility class that binds simple mouse gestures to number properties so that
 * their values can be controlled with mouse drag events.
 */
internal class DragSupport @JvmOverloads constructor(
    private val target: Scene,
    modifier: KeyCode,
    mouseButton: MouseButton,
    orientation: Orientation,
    property: Property<Number>,
    factor: Double = 1.0
) {
    private var keyboardEventHandler: EventHandler<KeyEvent>
    private var mouseEventHandler: EventHandler<MouseEvent>
    private var anchor: Number? = null
    private var dragAnchor = 0.0
    private var lastMouseEvent: MouseEvent? = null

    /**
     * Creates DragSupport instance that attaches EventHandlers to the given scene
     * and responds to mouse and keyboard events in order to change given
     * property values according to mouse drag events of given orientation
     * @param target scene
     * @param modifier null if no modifier needed
     * @param orientation vertical or horizontal
     * @param property number property to control
     * @see .DragSupport
     */
    constructor(
        target: Scene,
        modifier: KeyCode,
        orientation: Orientation,
        property: Property<Number>
    ) : this(target, modifier, MouseButton.PRIMARY, orientation, property, 1.0)

    /**
     * Removes event handlers of this DragSupport instance from the target scene
     */
    fun detach() {
        target.removeEventHandler(MouseEvent.ANY, mouseEventHandler)
        target.removeEventHandler(KeyEvent.ANY, keyboardEventHandler)
    }

    /**
     * Creates DragSupport instance that attaches EventHandlers to the given scene
     * and responds to mouse and keyboard events in order to change given
     * property values according to mouse drag events of given orientation.
     * Mouse movement amount is multiplied by given factor.
     * @param target scene
     * @param modifier null if no modifier needed
     * @param orientation vertical or horizontal
     * @param property number property to control
     * @param factor multiplier for mouse movement amount
     */
    constructor(
        target: Scene,
        modifier: KeyCode,
        orientation: Orientation,
        property: Property<Number>,
        factor: Double
    ) : this(target, modifier, MouseButton.PRIMARY, orientation, property, factor)

    private fun isModifierCorrect(t: KeyEvent, keyCode: KeyCode): Boolean {
        return ((keyCode != KeyCode.ALT)  xor t.isAltDown
            && (keyCode != KeyCode.CONTROL)  xor t.isControlDown
            && (keyCode != KeyCode.SHIFT) xor t.isShiftDown
            && (keyCode != KeyCode.META) xor t.isMetaDown)
    }

    private fun isModifierCorrect(t: MouseEvent, keyCode: KeyCode): Boolean {
        return ((keyCode != KeyCode.ALT) xor t.isAltDown
            && (keyCode != KeyCode.CONTROL) xor t.isControlDown
            && (keyCode != KeyCode.SHIFT) xor t.isShiftDown
            && (keyCode != KeyCode.META) xor t.isMetaDown)
    }

    private fun getCoord(t: MouseEvent, orientation: Orientation): Double {
        return when (orientation) {
            Orientation.HORIZONTAL -> t.screenX
            Orientation.VERTICAL -> t.screenY
            else -> throw IllegalArgumentException("This orientation is not supported: $orientation")
        }
    }

    init {
        mouseEventHandler = EventHandler { t: MouseEvent ->
            if (t.eventType != MouseEvent.MOUSE_ENTERED_TARGET
                && t.eventType != MouseEvent.MOUSE_EXITED_TARGET
            ) {
                lastMouseEvent = t
            }
            if (t.eventType == MouseEvent.MOUSE_PRESSED) {
                if (t.button == mouseButton
                    && isModifierCorrect(t, modifier)
                ) {
                    anchor = property.value
                    dragAnchor = getCoord(t, orientation)
                    t.consume()
                }
            } else if (t.eventType == MouseEvent.MOUSE_DRAGGED) {
                if (t.button == mouseButton
                    && isModifierCorrect(t, modifier)
                ) {
                    property.value = (anchor!!.toDouble()
                        + (getCoord(t, orientation) - dragAnchor) * factor)
                    t.consume()
                }
            }
        }
        keyboardEventHandler = EventHandler { t: KeyEvent ->
            if (t.eventType == KeyEvent.KEY_PRESSED) {
                if (t.code == modifier) {
                    anchor = property.value
                    if (lastMouseEvent != null) {
                        dragAnchor = getCoord(lastMouseEvent!!, orientation)
                    }
                    t.consume()
                }
            } else if (t.eventType == KeyEvent.KEY_RELEASED) {
                if (t.code != modifier && isModifierCorrect(t, modifier)) {
                    anchor = property.value
                    if (lastMouseEvent != null) {
                        dragAnchor = getCoord(lastMouseEvent!!, orientation)
                    }
                    t.consume()
                }
            }
        }
        target.addEventHandler(MouseEvent.ANY, mouseEventHandler)
        target.addEventHandler(KeyEvent.ANY, keyboardEventHandler)
    }
}
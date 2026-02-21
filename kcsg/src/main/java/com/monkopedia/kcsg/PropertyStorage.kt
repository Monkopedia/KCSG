/**
 * PropertyStorage.java
 *
 * Copyright 2014-2014 Michael Hoffer <info></info>@michaelhoffer.de>. All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info></info>@michaelhoffer.de> "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info></info>@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Michael Hoffer
 * <info></info>@michaelhoffer.de>.
 */
package com.monkopedia.kcsg

import kotlin.random.Random

/**
 * A simple property storage.
 */
class PropertyStorage {
    private val map: MutableMap<String, Any> = mutableMapOf()

    /**
     * Sets a property. Existing properties are overwritten.
     *
     * @param key key
     * @param property property
     */
    operator fun set(key: String, property: Any) {
        map[key] = property
    }

    /**
     * Returns a property.
     *
     * @param <T> property type
     * @param key key
     * @return the property; null will be returned if the property does not exist or the type does
     * not match
    </T> */
    fun <T> getValue(key: String): T? {
        val value = map[key]
        return value as? T
    }

    /**
     * Deletes the requested property if present. Does nothing otherwise.
     *
     * @param key key
     */
    fun delete(key: String) {
        map.remove(key)
    }

    /**
     * Indicates whether this storage contains the requested property.
     *
     * @param key key
     * @return `true` if this storage contains the requested property;
     * `false`
     */
    operator fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    companion object {
        private val colors = arrayOf(
            KcsgColor.RED, KcsgColor.YELLOW, KcsgColor.GREEN, KcsgColor.BLUE, KcsgColor.MAGENTA,
            KcsgColor.WHITE, KcsgColor.BLACK, KcsgColor.GRAY, KcsgColor.ORANGE,
        )

        fun randomColor(storage: PropertyStorage) {
            val c = colors[Random.nextInt(colors.size)]
            storage["material:color"] = listOf(c.red, c.green, c.blue).joinToString(" ") {
                it.toJvmString()
            }
        }
    }

    /**
     * Constructor. Creates a new property storage.
     */
    init {
        randomColor(this)
    }
}

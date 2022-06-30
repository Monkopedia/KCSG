/*
 * Copyright (c) 2014, Oracle and/or its affiliates.
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

import java.io.File
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.net.URLClassLoader

internal class ImporterFinder {
    fun addUrlToClassPath(): URLClassLoader {
        val referenceClass: Class<*> = ImporterFinder::class.java
        val url = referenceClass.protectionDomain.codeSource.location
        var libDir: File? = null
        libDir = try {
            val currentDir = File(url.toURI()).parentFile
            File(currentDir, "lib")
        } catch (ue: URISyntaxException) {
            ue.printStackTrace()
            throw RuntimeException("Could not import library. Failed to determine library location. URL = " + url.path)
        }
        return if (libDir != null) {
            val files = libDir.listFiles()
            val urlList: MutableList<URL> =
                ArrayList()
            if (files != null) {
                for (file in files) {
                    try {
                        urlList.add(file.toURI().toURL())
                    } catch (me: MalformedURLException) {
                        me.printStackTrace()
                    }
                }
            }
            URLClassLoader(urlList.toTypedArray(), javaClass.classLoader)
        } else {
            throw RuntimeException("Could not import library. Failed to determine importer library location ")
        }
    }
}
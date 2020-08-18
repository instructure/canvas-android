/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.android.buildtools.transform

import javassist.ClassPool
import javassist.CtClass

class FlutterTextureDisconnectFix : ClassTransformer() {

    override val transformName = "FlutterTextureDisconnectFix"

    override val counter = TransformCounter.Exactly(1)

    override val includeExternalLibs: List<String> = listOf("io.flutter:flutter_embedding")

    private lateinit var exceptionClass: CtClass

    override fun createFilter() = NameEquals("io.flutter.embedding.android.FlutterTextureView")

    override fun onClassPoolReady(classPool: ClassPool) {
        exceptionClass = classPool["java.lang.NullPointerException"]
    }

    override fun transform(cc: CtClass, classPool: ClassPool): Boolean = with(cc){
        // Adds a NullPointerException catch to the disconnectSurfaceFromRenderer method and logs the incident
        val method = declaredMethods.single { it.name == "disconnectSurfaceFromRenderer" }
        val message = "FlutterTextureView.disconnectSurfaceFromRenderer called with null renderSurface"
        method.addCatch("""
            com.instructure.student.util.LoggingUtility.INSTANCE.log(android.util.Log.DEBUG, "$message");
            return;""".trimIndent(), exceptionClass)
        println("    :FlutterTextureView patched")
        true
    }
}

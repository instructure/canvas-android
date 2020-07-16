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
 *
 */
@file:Suppress("unused")

package com.instructure.android.buildtools.transform

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.bytecode.CodeAttribute
import javassist.bytecode.LineNumberAttribute

/** Works around a bug in Flutter 1.17 where FlutterView will crash when using RenderMode.texture. */
class FlutterTextureRenderModeFix : ClassTransformer() {
    override val transformName = "FlutterTextureRenderModeFix"

    override val includeExternalLibs: List<String> = listOf("io.flutter:flutter_embedding")

    private lateinit var contextClass: CtClass
    private lateinit var attributeSetClass: CtClass
    private lateinit var flutterTextureViewClass: CtClass

    override fun onClassPoolReady(classPool: ClassPool) {
        contextClass = classPool["android.content.Context"]
        attributeSetClass = classPool["android.util.AttributeSet"]
        flutterTextureViewClass = classPool["io.flutter.embedding.android.FlutterTextureView"]
    }

    override fun createFilter() = NameEquals("io.flutter.embedding.android.FlutterView")

    override fun transform(cc: CtClass, classPool: ClassPool): Boolean {
        cc.transformFlutterViewConstructor()
        return true
    }

    private fun CtClass.transformFlutterViewConstructor() {
        val constructor = getDeclaredConstructor(arrayOf(contextClass, attributeSetClass, flutterTextureViewClass))
        if (constructor != null) {
            // Replaces the line "this.renderSurface = flutterSurfaceView;" in the constructor that takes a FlutterTextureView param.
            // The exact line number(s) may change in newer Flutter versions.
            constructor.removeLines(267..268)
            constructor.insertAt(266, """renderSurface = $3;""")
            println("    :FlutterView patched")
        } else {
            throw IllegalStateException("Could not find correct constructor for $transformName")
        }
    }

    private fun CtConstructor.removeLines(range: IntRange) {
        val codeAttribute = methodInfo.codeAttribute
        val lineNumbers = codeAttribute.getAttribute(LineNumberAttribute.tag) as LineNumberAttribute
        val programCounterRange = lineNumbers.toStartPc(range.first) until lineNumbers.toStartPc(range.last + 1)
        programCounterRange.forEach { codeAttribute.code[it] = CodeAttribute.NOP.toByte() }
    }
}

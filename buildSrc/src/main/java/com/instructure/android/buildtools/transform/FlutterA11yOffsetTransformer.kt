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
import java.lang.IllegalStateException

/**
 * A [ClassTransformer] that works around a bug in the flutter_embedding dependency which causes the accessibility
 * borders for Flutter elements to be drawn with an incorrect offset.
 *
 * To interface with Android's accessibility layer and replicate the interaction behaviors that users expect, Flutter
 * internally tracks a hierarchy of virtual views (via the AccessibilityBridge class) that acts as a middle man between
 * Flutter's widget tree and Android's accessibility APIs. However, in its current state it does not seem to account
 * for non-fullscreen use cases of FlutterView (the Android View that attaches to the Flutter engine), causing the
 * virtual view bounds to be reported relative to the FlutterView bounds rather than to the screen bounds. This
 * ultimately results in the system drawing the accessibility 'highlight' borders for Flutter elements with an
 * incorrect offset.
 *
 * The one exception to this behavior happens in 'AccessibilityBridge.updateSemantics', where a transformation is
 * applied to the virtual views to match the window's left inset, covering a case where Android displays the
 * navigation bar on the left side of the screen in landscape mode.
 *
 * Fortunately, because our embedded usage of Flutter also aligns with the top window inset, we are able to patch
 * the flutter_embedding jar in order to hijack that transformation, modifying it to include the top inset in
 * addition to the left inset.
 *
 * Note that this patch will not account for additional offsets of the FlutterView (due to padding/margin) or for cases
 * where the FlutterView is otherwise misaligned with the window insets.
 *
 * As with all jar patches, this transformer is designed for a specific dependency version and may cease working
 * if the dependency is updated.
 */
class FlutterA11yOffsetTransformer() : ClassTransformer() {
    override val transformName = "FlutterA11yOffsetTransformer"

    override val includeExternalLibs: List<String> = listOf("io.flutter:flutter_embedding")

    override fun onClassPoolReady(classPool: ClassPool) {
        classPool.importPackage("android.view.WindowInsets")
        classPool.importPackage("android.opengl.Matrix")
    }

    override fun createFilter() = NameEquals("io.flutter.view.AccessibilityBridge")

    override fun transform(cc: CtClass, classPool: ClassPool): Boolean {
        cc.transformAccessibilityBridge()
        return true
    }

    private fun CtClass.transformAccessibilityBridge() {
        val method = declaredMethods.find { it.name == "updateSemantics" }
        if (method != null) {
            // Insert at line "lastLeftFrameInset = insets.getSystemWindowInsetLeft();"
            method.insertAt(1534, """
                float topOffset = (float) insets.getSystemWindowInsetTop();
                android.opengl.Matrix.translateM(identity, 0, 0f, topOffset, 0f);
            """.trimIndent())
            println("    :Flutter AccessibilityBridge patched")
        } else {
            throw IllegalStateException("Method 'updateSemantics' is null for transformer $transformName")
        }
    }
}

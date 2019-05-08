/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

class PDFKitCrashFixTransformer : ClassTransformer() {

    override val transformName = "PSPDFKitYUDoDis"

    override val counter = TransformCounter.Exactly(1)

    override val includeExternalLibs = listOf("pspdfkit:4.8.1")

    private val problemClassName = "com.pspdfkit.ui.PdfThumbnailGrid"

    private lateinit var exceptionClass: CtClass

    override fun onClassPoolReady(classPool: ClassPool) {
        exceptionClass = classPool["java.lang.IllegalStateException"]
    }

    override fun createFilter() = NameEquals(problemClassName)

    override fun transform(cc: CtClass, classPool: ClassPool): Boolean = with(cc){
        val method = declaredMethods.single { it.name == "onSaveInstanceState" }
        method.addCatch("""return super.onSaveInstanceState();""", exceptionClass)
        println("    :PSPDFKit patched")
        true
    }

}

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
 */
package com.instructure.android.buildtools.transform

import javassist.*
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.StringMemberValue

abstract class ClassTransformer {

    /** The name of this transformer */
    abstract val transformName: String

    /**
     * Called once the [ClassPool] has been populated. At this point all source files have been scanned and it should be
     * safe to obtain [CtClass]es, add package imports, append the classpath, or otherwise interact with the [ClassPool].
     * Note that this will be called before [createFilter].
     */
    abstract fun onClassPoolReady(classPool: ClassPool)

    /**
     * Should return a filter that determines which classes will be passed to the [transform] function. See [ClassFilter]
     * for filter options. Note that this will be called after [onClassPoolReady].
     */
    protected abstract fun createFilter(): ClassFilter

    /**
     * Called when a class is ready to be transformed. The provided [CtClass] may be sourced from either a class file
     * or a jar file, and may also be transformed by other [ClassTransformer]s in the same [ProjectTransformer]. As such,
     * implementations of this function should avoid locking the [CtClass] and should not perform the task of saving
     * the class to disk. This function should return true if the [CtClass] was transformed in any way, or false if
     * no transformation was made.
     */
    abstract fun transform(cc: CtClass, classPool: ClassPool): Boolean

    /** Invokes [transform] and updates the [counter] if the transform was successful */
    fun performTransform(cc: CtClass, classPool: ClassPool): Boolean {
        val transformed = transform(cc, classPool)
        return transformed
    }

    val filter by lazy { createFilter() }

    open val includeExternalLibs: List<String> = emptyList()

    protected fun CtClass.getAnnotation(name: String): Annotation? {
        return (classFile.getAttribute(AnnotationsAttribute.invisibleTag) as? AnnotationsAttribute)?.getAnnotation(name)
    }

    protected fun CtMethod.getAnnotation(name: String): Annotation? {
        return (methodInfo.getAttribute(AnnotationsAttribute.invisibleTag) as? AnnotationsAttribute)?.getAnnotation(name)
    }

    protected fun CtField.getAnnotation(name: String): Annotation? {
        return (fieldInfo.getAttribute(AnnotationsAttribute.invisibleTag) as? AnnotationsAttribute)?.getAnnotation(name)
    }

    protected fun Annotation.getString(memberName: String): String? {
        return (getMemberValue(memberName) as? StringMemberValue)?.value
    }

    protected fun CtClass.addOrUpdateDeclaredMethod(methodName: String, content: String, defaultMethodBody: String, before: Boolean = true) {
        val method = declaredMethods.find { it.name == methodName }
                ?: CtNewMethod.make(defaultMethodBody, this).also { addMethod(it) }
        if (before) method.insertBefore(content) else method.insertAfter(content)
    }

}


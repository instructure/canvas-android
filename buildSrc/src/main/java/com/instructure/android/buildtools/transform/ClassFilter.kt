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
@file:Suppress("unused")

package com.instructure.android.buildtools.transform

import javassist.CtClass

interface ClassFilter {
    fun matches(cc: CtClass): Boolean
}

class HasAnnotation(private val annotationName: String) : ClassFilter {
    override fun matches(cc: CtClass) = cc.hasAnnotation(annotationName)
}

class NameContains(private val name: String) : ClassFilter {
    override fun matches(cc: CtClass) = name in cc.name
}

class NameEquals(private val name: String) : ClassFilter {
    override fun matches(cc: CtClass) = name == cc.name
}

class SubclassesAny(private vararg val supers: CtClass) : ClassFilter {
    override fun matches(cc: CtClass) = supers.any { cc.subclassOf(it) }
}

class AllOf(private vararg val filters: ClassFilter) :
        ClassFilter {
    override fun matches(cc: CtClass) = filters.all { it.matches(cc) }
}

class AnyOf(private vararg val filters: ClassFilter) :
        ClassFilter {
    override fun matches(cc: CtClass) = filters.any { it.matches(cc) }
}

operator fun ClassFilter.plus(other: ClassFilter): ClassFilter =
        AllOf(this, other)

infix fun ClassFilter.or(other: ClassFilter): ClassFilter = AnyOf(this, other)

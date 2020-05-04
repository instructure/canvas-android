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

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtNewMethod
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import java.io.File
import kotlin.system.exitProcess

class LocaleTransformer : ClassTransformer() {

    override val transformName = "LocaleTransformer"

    private val localeUtilsClassName = "com.instructure.canvasapi2.utils.LocaleUtils"

    private lateinit var activityClass: CtClass

    override fun onClassPoolReady(classPool: ClassPool) {
        activityClass = classPool["android.app.Activity"]
        classPool.importPackage("android.content.Context")
        classPool.importPackage(localeUtilsClassName)
    }

    override fun createFilter() = NameEquals(localeUtilsClassName) or (NameContains("instructure") + SubclassesAny(activityClass))

    override fun transform(cc: CtClass, classPool: ClassPool): Boolean {
        when {
            cc.subclassOf(activityClass) -> return cc.transformActivity()
            cc.name == localeUtilsClassName -> cc.transformLocaleUtils()
            else -> throw UnsupportedOperationException("Transforming classes of type ${cc.superclass.name} is unsupported.")
        }
        return true
    }

    /**
     * Transforms this class to override attachBaseContext in order to set the locale.
     * This only works for subclasses of android.app.Activity.
     */
    private fun CtClass.transformActivity(): Boolean {
        // Skip if this Activity inherits from another Activity that we own
        if (filter.matches(superclass)) return false

        val method = declaredMethods.find { it.name == "attachBaseContext" }
        if (method != null) {
            // If method is overridden, ensure that the context is being wrapped
            var callsWrap = false
            method.instrument(object : ExprEditor() {
                val expectedCall = "com.instructure.canvasapi2.utils.LocaleUtils.wrapContext(android.content.Context)"
                override fun edit(m: MethodCall) {
                    if (m.method.longName == expectedCall) callsWrap = true
                }
            })
            if (!callsWrap) throw IllegalStateException("$simpleName overrides attachBaseContext but does not call LocaleUtils.wrapContext()")
        } else {
            val newMethod = CtNewMethod.make(
                    """
                protected void attachBaseContext(android.content.Context base) {
                    android.content.Context newBase = com.instructure.canvasapi2.utils.LocaleUtils.wrapContext(base);
                    super.attachBaseContext(newBase);
                }
                """.trimIndent(), this)
            addMethod(newMethod)
        }
        return true
    }

    /**
     * Scans for available translations and adds supported language tags to LocaleUtils
     */
    private fun CtClass.transformLocaleUtils() {
        val configLocations = listOf(
            File("translations/projects.json").canonicalFile,
            File("../translations/projects.json").canonicalFile,
            File("../../translations/projects.json").canonicalFile
        )
        val translationsFile = configLocations.find { it.exists() }
        if (translationsFile == null) {
            println("\nUnable to find translation project config file (translations/projects.json)")
            println("Exiting...")
            Thread.sleep(1000) // Allow time to print message
            exitProcess(1)
        }
        val root = translationsFile.parentFile.parentFile
        val translationsJson = translationsFile.readText()
        val type = object : TypeToken<List<TranslationProject>>() {}.type
        val projects = Gson().fromJson<List<TranslationProject>>(translationsJson, type)
        val resDirs = projects.map { it.resourceDir }
        val resNames = resDirs.asSequence()
            .map { File(root, it) }
            .flatMap { dir ->
                dir.walkTopDown()
                    .filter { it.name == "strings.xml" && it.parentFile.name != "values" }
                    .map { it.parentFile }
            }
            .map { dir ->
                val name = dir.name.substringAfter("values-").replace("-r", "-")
                name.takeUnless { it.startsWith("b+") } ?: name.drop(2).replace("+", "-")
            }
            .distinct()
            .sorted()
            .toList()
        println("    :LocaleTransformer found ${resNames.size} translations")

        val fieldName = "__languageTags"
        val fieldBody = "private static final String[] $fieldName = new String[]{${resNames.joinToString { "\"$it\"" }}};"
        addField(CtField.make(fieldBody, this))

        declaredMethods.single { it.name == "getSupportedLanguageTags" }.setBody("{ return $fieldName; }")
    }

}

private data class TranslationProject(
    val name: String,
    @SerializedName("source_path")
    val sourcePath: String,
    @SerializedName("resource_dir")
    val resourceDir: String
)

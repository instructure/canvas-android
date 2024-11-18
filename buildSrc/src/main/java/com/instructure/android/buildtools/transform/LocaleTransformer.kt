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
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

object LocaleTransformer {

    /**
     * Scans for available translations and returns supported language tags
     */
    @JvmStatic
    fun getAvailableLanguageTags(project: Project): String {
        val translationsFile = try {
            File(project.projectDir, "../../translations/projects.json").canonicalFile
        } catch (e: IOException) {
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
        return resNames.toTypedArray().joinToString(";")
    }

}

private data class TranslationProject(
    val name: String,
    @SerializedName("source_path")
    val sourcePath: String,
    @SerializedName("resource_dir")
    val resourceDir: String
)

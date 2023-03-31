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
package com.instructure.android.buildtools.transform

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import javassist.ClassPool
import javassist.CtClass
import java.io.File
import java.lang.IllegalStateException
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

@Suppress("unused")
class ProjectTransformer(
        private val app: AppExtension,
        private vararg val transformers: ClassTransformer
) : Transform() {

    override fun getName() = "ProjectTransform"

    override fun isIncremental() = false

    override fun getInputTypes() = mutableSetOf(QualifiedContent.DefaultContentType.CLASSES)

    override fun getScopes() = mutableSetOf(
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.SUB_PROJECTS
    )

    override fun transform(transformInvocation: TransformInvocation) = with(transformInvocation) {
        val start = System.currentTimeMillis()

        // Don't transform anything if this is a test APK; just copy inputs and return
        if (context.variantName.endsWith("AndroidTest")) {
            inputs.forEach { input ->
                input.jarInputs.forEach {
                    val dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                    when (it.status) {
                        Status.REMOVED -> dest.delete()
                        else -> it.file.copyTo(dest, true)
                    }
                }
                input.directoryInputs.forEach {
                    val dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                    it.file.copyRecursively(dest, overwrite = true)
                }
            }
            return
        }

        println(transformers.joinToString(prefix = "    :", separator = " :") { it.transformName })

        val includedExternalLibs = transformers.flatMap { it.includeExternalLibs }

        // Create class pool to hold the project's class paths. This must be populated prior to transforming any classes.
        val classPool = ClassPool()

        // A list of jar files, limited to sub-projects
        val candidateJars = mutableListOf<Pair<File, File>>()

        // A list of project classes and their associated File objects
        val candidateFiles = mutableListOf<Pair<CtClass, File>>()

        // Copy inputs to their destinations
        inputs.forEach { input ->

            // Add jars to the ClassPool, and add sub-project jars to the list of candidates
            input.jarInputs.forEach { jarInput ->
                val dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                when (jarInput.status) {
                    Status.REMOVED -> dest.delete()
                    else -> {
                        jarInput.file.copyTo(dest, true)
                        classPool.insertClassPath(jarInput.file.absolutePath)
                        if (jarInput.scopes.contains(QualifiedContent.Scope.SUB_PROJECTS)
                            || includedExternalLibs.any { it in jarInput.name }) {
                            candidateJars += jarInput.file to dest
                        }
                    }
                }
            }

            // Copy input directories, add classes to ClassPool, and extract candidate files
            input.directoryInputs.forEach {
                val output = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                it.file.copyRecursively(output, overwrite = true)
                output.extractClasses(classPool, candidateFiles)
            }
        }

        // android.jar is a compile-only dependency so we must manually add it to the class pool
        classPool.insertClassPath(File(app.sdkDirectory, "platforms/${app.compileSdkVersion}/android.jar").absolutePath)

        // At this point the ClassPool should be fully populated
        transformers.forEach { it.onClassPoolReady(classPool) }

        // Transform files
        candidateFiles.forEach { (cc, src) ->
            try {
                val modCount = transformers.count { it.filter.matches(cc) && it.performTransform(cc, classPool) }
                if (modCount > 0) src.writeBytes(cc.toBytecode())
            } catch (e: Throwable) {
                println("Error transforming file ${src.nameWithoutExtension}")
                e.printStackTrace()
                throw e
            }
        }

        // Transform Jars
        candidateJars.forEach { (src, dest) ->
            val input = JarFile(src)
            val output = JarOutputStream(dest.outputStream())
            input.entries().asSequence().forEach { entry ->
                try {
                    if (entry.name.endsWith(".class")) {
                        val cc = classPool[entry.name.substringBeforeLast(".class").replace("/", ".")]
                        if (cc.isFrozen) cc.defrost()
                        if (transformers.count { it.filter.matches(cc) && it.performTransform(cc, classPool) } > 0) {
                            output.putNextEntry(JarEntry(entry.name))
                            output.write(cc.toBytecode())
                        } else {
                            copyJarEntry(input, entry, output)
                        }
                    } else {
                        copyJarEntry(input, entry, output)
                    }
                } catch (e: Throwable) {
                    println("Error transforming jar entry: ${e.message}")
                    e.printStackTrace()
                    throw e
                }
            }
            output.close()
            input.close()
        }

        val end = System.currentTimeMillis()
        println("    :Transform took ${(end - start) / 1000.0} seconds")
    }

    /** Copies [entry] from the [input] jar to the [output] jar without modification */
    private fun copyJarEntry(input: JarFile, entry: JarEntry?, output: JarOutputStream) {
        input.getInputStream(entry).use {
            output.putNextEntry(entry)
            it.copyTo(output)
        }
    }

    /**
     * Extracts classes to [projectClasses] and adds them to the [classPool]
     */
    private fun File.extractClasses(classPool: ClassPool, projectClasses: MutableList<Pair<CtClass, File>>) {
        if (isDirectory) {
            listFiles().forEach { it.extractClasses(classPool, projectClasses) }
        } else if (isFile && extension == "class") {
            projectClasses += classPool.makeClass(this.inputStream(), false) to this
        }
    }

}

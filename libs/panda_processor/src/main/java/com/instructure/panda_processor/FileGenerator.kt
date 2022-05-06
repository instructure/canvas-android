/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.panda_processor

import com.google.auto.service.AutoService
import com.instructure.panda_annotations.*
import java.awt.AWTError
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor(){

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(TestMetaData::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        val annotationList = roundEnvironment?.getElementsAnnotatedWith(TestMetaData::class.java)?.map { AnnotationWrapper(it.getAnnotation(TestMetaData::class.java), it) }
        if(annotationList?.isNotEmpty() == true) generateFile(annotationList)
        return true
    }

    private fun generateFile(annotationWrapperList: List<AnnotationWrapper>){
        val fileName = "Generated_Test_data"
        val fileContent = generateFileContent(annotationWrapperList)

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "$fileName.txt")

        file.writeText(fileContent)
    }

    private fun generateFileContent(annotationWrapperList: List<AnnotationWrapper>): String {
        var totalWritten = 0
        var totalStubbed = 0
        val testCounts = HashMap<TestCategory, TestCount>()
        val priorityCounts = HashMap<Priority, TestCount>()
        val featureCounts = HashMap<FeatureCategory, TestCount>()
        val manualOnly = ArrayList<String>()

        annotationWrapperList.forEach { annotationWrapper ->
            val stubbedCount = if(annotationWrapper.annotation.stubbed) 1 else 0
            val writtenCount = if(!annotationWrapper.annotation.stubbed) 1 else 0
            totalStubbed += stubbedCount
            totalWritten += writtenCount
            val testCount = testCounts[annotationWrapper.annotation.testCategory] ?: TestCount()
            val priorityCount = priorityCounts[annotationWrapper.annotation.priority] ?: TestCount()
            val featureCount = featureCounts[annotationWrapper.annotation.featureCategory] ?: TestCount()
            testCounts[annotationWrapper.annotation.testCategory] = TestCount(testCount.stubbed + stubbedCount, testCount.written + writtenCount)
            priorityCounts[annotationWrapper.annotation.priority] = TestCount(priorityCount.stubbed + stubbedCount, priorityCount.written + writtenCount)
            featureCounts[annotationWrapper.annotation.featureCategory] = TestCount(featureCount.stubbed + stubbedCount, featureCount.written + writtenCount)
        }

        return CustomFileBuilder(
                totalWritten,
                totalStubbed,
                testCounts,
                priorityCounts,
                featureCounts
        ).getContent()
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

}

data class AnnotationWrapper(val annotation: TestMetaData, val annotatedElement: Element)
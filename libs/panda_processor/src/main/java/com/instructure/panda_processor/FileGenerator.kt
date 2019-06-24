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
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor(){

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(TestMetaData::class.java.name, GreetingGenerator::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        val annotationList = roundEnvironment?.getElementsAnnotatedWith(TestMetaData::class.java)?.map { it.getAnnotation(TestMetaData::class.java) }
        if(annotationList?.isNotEmpty() == true) generateFile(annotationList)
        return true
    }

    private fun generateFile(annotationList: List<TestMetaData>){
        val fileName = "Generated_Test_data"
        val fileContent =
                CustomFileBuilder(
                        calculateTestCounts(annotationList),
                        calculatePriorityCounts(annotationList),
                        calculateFeatureCounts(annotationList)
                ).getContent()

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "$fileName.txt")

        file.writeText(fileContent)
    }

    private fun calculateTestCounts(annotationList: List<TestMetaData>) = TestCounts(
            annotationList.filter { it.stubbed }.size,
            annotationList.filter { it.testCategory == TestCategory.E2E && it.stubbed}.size,
            annotationList.filter { it.testCategory == TestCategory.INTERACTION && it.stubbed}.size,
            annotationList.filter { it.testCategory == TestCategory.RENDER && it.stubbed}.size,

            annotationList.filter { !it.stubbed }.size,
            annotationList.filter { it.testCategory == TestCategory.E2E && !it.stubbed}.size,
            annotationList.filter { it.testCategory == TestCategory.INTERACTION && !it.stubbed}.size,
            annotationList.filter { it.testCategory == TestCategory.RENDER && !it.stubbed}.size
    )

    private fun calculatePriorityCounts(annotationList: List<TestMetaData>) = PriorityCounts(
            annotationList.filter { it.priority == Priority.P0 && it.stubbed }.size,
            annotationList.filter { it.priority == Priority.P1 && it.stubbed }.size,
            annotationList.filter { it.priority == Priority.P2 && it.stubbed }.size,
            annotationList.filter { it.priority == Priority.P3 && it.stubbed }.size,

            annotationList.filter { it.priority == Priority.P0 && !it.stubbed }.size,
            annotationList.filter { it.priority == Priority.P1 && !it.stubbed }.size,
            annotationList.filter { it.priority == Priority.P2 && !it.stubbed }.size,
            annotationList.filter { it.priority == Priority.P3 && !it.stubbed }.size
    )

    private fun calculateFeatureCounts(annotationList: List<TestMetaData>) = FeatureCounts(
            annotationList.filter { FeatureCategory.ASSIGNMENTS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.SUBMISSIONS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.LOGIN in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.COURSE in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.DASHBOARD in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.SETTINGS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.PAGES in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.DISCUSSIONS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.MODULES in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.INBOX in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.GRADES in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.FILES in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.EVENTS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.PEOPLE in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.CONFERENCES in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.COLLABORATIONS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.SYLLABUS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,
            annotationList.filter { FeatureCategory.TODOS in listOf(it.featureCategory, it.secondaryFeature) && it.stubbed }.size,

            annotationList.filter { FeatureCategory.ASSIGNMENTS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.SUBMISSIONS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.LOGIN in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.COURSE in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.DASHBOARD in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.SETTINGS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.PAGES in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.DISCUSSIONS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.MODULES in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.INBOX in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.GRADES in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.FILES in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.EVENTS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.PEOPLE in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.CONFERENCES in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.COLLABORATIONS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.SYLLABUS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size,
            annotationList.filter { FeatureCategory.TODOS in listOf(it.featureCategory, it.secondaryFeature) && !it.stubbed }.size
    )

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

}
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

import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory

class CustomFileBuilder(
        totalWritten: Int,
        totalStubbed: Int,
        testCounts: HashMap<TestCategory, TestCount>,
        priorityCounts: HashMap<Priority, TestCount>,
        featureCounts: HashMap<FeatureCategory, TestCount>) {

    private val contentTemplate = """
        *** Total Test Counts ***
        Total Tests = ${totalWritten + totalStubbed}
        Tests Written = $totalWritten
        Tests Stubbed = $totalStubbed

        *** Test Types ***
        E2E Test Count = stubbed(${testCounts[TestCategory.E2E]?.stubbed}) / written(${testCounts[TestCategory.E2E]?.written})
        Interaction Test Count = stubbed(${testCounts[TestCategory.INTERACTION]?.stubbed}) / written(${testCounts[TestCategory.INTERACTION]?.written})
        Render Test Count = stubbed(${testCounts[TestCategory.RENDER]?.stubbed}) / written(${testCounts[TestCategory.RENDER]?.written})


        *** Test Priority ***
        P0 Test Count = stubbed(${priorityCounts[Priority.P0]?.stubbed}) / written(${priorityCounts[Priority.P0]?.written})
        P1 Test Count = stubbed(${priorityCounts[Priority.P1]?.stubbed}) / written(${priorityCounts[Priority.P1]?.written})
        P2 Test Count = stubbed(${priorityCounts[Priority.P2]?.stubbed}) / written(${priorityCounts[Priority.P2]?.written})
        P3 Test Count = stubbed(${priorityCounts[Priority.P3]?.stubbed}) / written(${priorityCounts[Priority.P3]?.written})

        *** Test Feature Coverage ***
        Assignments = stubbed(${featureCounts[FeatureCategory.ASSIGNMENTS]?.stubbed}) / written(${featureCounts[FeatureCategory.ASSIGNMENTS]?.written})
        Submissions = stubbed(${featureCounts[FeatureCategory.SUBMISSIONS]?.stubbed}) / written(${featureCounts[FeatureCategory.SUBMISSIONS]?.written})
        Login = stubbed(${featureCounts[FeatureCategory.LOGIN]?.stubbed}) / written(${featureCounts[FeatureCategory.LOGIN]?.written})
        Course = stubbed(${featureCounts[FeatureCategory.COURSE]?.stubbed}) / written(${featureCounts[FeatureCategory.COURSE]?.written})
        Dashboard = stubbed(${featureCounts[FeatureCategory.DASHBOARD]?.stubbed}) / written(${featureCounts[FeatureCategory.DASHBOARD]?.written})
        Settings = stubbed(${featureCounts[FeatureCategory.SETTINGS]?.stubbed}) / written(${featureCounts[FeatureCategory.SETTINGS]?.written})
        Pages = stubbed(${featureCounts[FeatureCategory.PAGES]?.stubbed}) / written(${featureCounts[FeatureCategory.PAGES]?.written})
        Discussions = stubbed(${featureCounts[FeatureCategory.DISCUSSIONS]?.stubbed}) / written(${featureCounts[FeatureCategory.DISCUSSIONS]?.written})
        Modules = stubbed(${featureCounts[FeatureCategory.MODULES]?.stubbed}) / written(${featureCounts[FeatureCategory.MODULES]?.written})
        Inbox = stubbed(${featureCounts[FeatureCategory.INBOX]?.stubbed}) / written(${featureCounts[FeatureCategory.INBOX]?.written})
        Grades = stubbed(${featureCounts[FeatureCategory.GRADES]?.stubbed}) / written(${featureCounts[FeatureCategory.GRADES]?.written})
        Files = stubbed(${featureCounts[FeatureCategory.FILES]?.stubbed}) / written(${featureCounts[FeatureCategory.FILES]?.written})
        Events = stubbed(${featureCounts[FeatureCategory.EVENTS]?.stubbed}) / written(${featureCounts[FeatureCategory.EVENTS]?.written})
        People = stubbed(${featureCounts[FeatureCategory.PEOPLE]?.stubbed}) / written(${featureCounts[FeatureCategory.PEOPLE]?.written})
        Conferences = stubbed(${featureCounts[FeatureCategory.CONFERENCES]?.stubbed}) / written(${featureCounts[FeatureCategory.CONFERENCES]?.written})
        Collaborations = stubbed(${featureCounts[FeatureCategory.COLLABORATIONS]?.stubbed}) / written(${featureCounts[FeatureCategory.COLLABORATIONS]?.written})
        Syllabus = stubbed(${featureCounts[FeatureCategory.SYLLABUS]?.stubbed}) / written(${featureCounts[FeatureCategory.SYLLABUS]?.written})
        Todos = stubbed(${featureCounts[FeatureCategory.TODOS]?.stubbed}) / written(${featureCounts[FeatureCategory.TODOS]?.written})
    """.trimIndent()
    fun getContent() : String{
        return contentTemplate
    }

}

data class TestCount(val stubbed: Int = 0, val written: Int = 0)
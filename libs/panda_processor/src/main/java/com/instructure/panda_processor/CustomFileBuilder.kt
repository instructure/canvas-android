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

class CustomFileBuilder(testCounts: TestCounts, priorityCounts: PriorityCounts, featureCounts: FeatureCounts) {

    private val contentTemplate = """
        *** Total Test Counts ***
        Tests Written = ${testCounts.totalWritten}
        Tests Stubbed = ${testCounts.totalStubbed}

        *** Test Types ***
        E2E Test Count = stubbed(${testCounts.e2eStubbed}) / written(${testCounts.e2eWritten})
        Interaction Test Count = stubbed(${testCounts.interactionStubbed}) / written(${testCounts.interactionWritten})
        Render Test Count = stubbed(${testCounts.renderStubbed}) / written(${testCounts.renderWritten})


        *** Test Priority ***
        P0 Test Count = stubbed(${priorityCounts.p0Stubbed}) / written(${priorityCounts.p0Written})
        P1 Test Count = stubbed(${priorityCounts.p1Stubbed}) / written(${priorityCounts.p1Written})
        P2 Test Count = stubbed(${priorityCounts.p2Stubbed}) / written(${priorityCounts.p2Written})
        P3 Test Count = stubbed(${priorityCounts.p2Stubbed}) / written(${priorityCounts.p3Written})

        *** Test Feature Coverage ***
        Assignments = stubbed(${featureCounts.assignmentsStubbed}) / written(${featureCounts.assignmentsWritten})
        Submissions = stubbed(${featureCounts.submissionsStubbed}) / written(${featureCounts.submissionsWritten})
        Login = stubbed(${featureCounts.loginStubbed}) / written(${featureCounts.loginWritten})
        Course = stubbed(${featureCounts.courseStubbed}) / written(${featureCounts.courseWritten})
        Dashboard = stubbed(${featureCounts.dashboardStubbed}) / written(${featureCounts.dashboardWritten})
        Settings = stubbed(${featureCounts.settingsStubbed}) / written(${featureCounts.settingsWritten})
        Pages = stubbed(${featureCounts.pagesStubbed}) / written(${featureCounts.pagesWritten})
        Discussions = stubbed(${featureCounts.discussionsStubbed}) / written(${featureCounts.discussionsWritten})
        Modules = stubbed(${featureCounts.modulesStubbed}) / written(${featureCounts.modulesWritten})
        Inbox = stubbed(${featureCounts.inboxStubbed}) / written(${featureCounts.inboxWritten})
        Grades = stubbed(${featureCounts.gradesStubbed}) / written(${featureCounts.gradesWritten})
        Files = stubbed(${featureCounts.filesStubbed}) / written(${featureCounts.filesWritten})
        Events = stubbed(${featureCounts.eventsStubbed}) / written(${featureCounts.eventsWritten})
        People = stubbed(${featureCounts.peopleStubbed}) / written(${featureCounts.peopleWritten})
        Conferences = stubbed(${featureCounts.conferencesStubbed}) / written(${featureCounts.conferencesWritten})
        Collaborations = stubbed(${featureCounts.collaborationsStubbed}) / written(${featureCounts.collaborationsWritten})
        Syllabus = stubbed(${featureCounts.syllabusStubbed}) / written(${featureCounts.syllabusWritten})
        Todos = stubbed(${featureCounts.todosStubbed}) / written(${featureCounts.todosWritten})
    """.trimIndent()
    fun getContent() : String{
        return contentTemplate
    }

}

data class TestCounts(
    val total: Int,
    val totalWritten: Int,
    val e2eWritten: Int,
    val interactionWritten: Int,
    val renderWritten: Int,

    val totalStubbed: Int,
    val e2eStubbed: Int,
    val interactionStubbed: Int,
    val renderStubbed: Int
)

data class PriorityCounts(
    val p0Stubbed: Int,
    val p1Stubbed: Int,
    val p2Stubbed: Int,
    val p3Stubbed: Int,

    val p0Written: Int,
    val p1Written: Int,
    val p2Written: Int,
    val p3Written: Int
)

data class FeatureCounts(
    val assignmentsStubbed: Int,
    val submissionsStubbed: Int,
    val loginStubbed: Int,
    val courseStubbed: Int,
    val dashboardStubbed: Int,
    val settingsStubbed: Int,
    val pagesStubbed: Int,
    val discussionsStubbed: Int,
    val modulesStubbed: Int,
    val inboxStubbed: Int,
    val gradesStubbed: Int,
    val filesStubbed: Int,
    val eventsStubbed: Int,
    val peopleStubbed: Int,
    val conferencesStubbed: Int,
    val collaborationsStubbed: Int,
    val syllabusStubbed: Int,
    val todosStubbed: Int,

    val assignmentsWritten: Int,
    val submissionsWritten: Int,
    val loginWritten: Int,
    val courseWritten: Int,
    val dashboardWritten: Int,
    val settingsWritten: Int,
    val pagesWritten: Int,
    val discussionsWritten: Int,
    val modulesWritten: Int,
    val inboxWritten: Int,
    val gradesWritten: Int,
    val filesWritten: Int,
    val eventsWritten: Int,
    val peopleWritten: Int,
    val conferencesWritten: Int,
    val collaborationsWritten: Int,
    val syllabusWritten: Int,
    val todosWritten: Int
)
/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e

import android.util.Log
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.CoursesApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import com.instructure.parentapp.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SyllabusE2ETest : ParentComposeTest() {

    @Test
    fun testSyllabusE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(
            students = 1,
            teachers = 1,
            parents = 1,
            courses = 1,
            syllabusBody = "dummy syllabus body"
        )
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]
        val parent = data.parentsList[0]

        Log.d(PREPARATION_TAG, "Seed assignment and update syllabus body.")
        val assignment = AssignmentsApi.createAssignment(
            course.id, teacher.token, submissionTypes = listOf(
                SubmissionType.ON_PAPER
            ), withDescription = true, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601
        )

        val url = "https://mobileqa.beta.instructure.com/courses/${course.id}/assignments/${assignment.id}"
        val syllabusBody = "this is the syllabus body <a id=\"assignmentLink\" href=\"$url\">Assignment</a>"

        CoursesApi.updateCourse(course.id, syllabusBody = syllabusBody)

        Log.d(STEP_TAG, "Login with user: ${parent.name}, login id: ${parent.loginId}.")
        tokenLogin(parent)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select '${course.name}' course.")
        dashboardPage.waitForRender()
        coursesPage.clickCourseItem(course.name)

        Log.d(STEP_TAG,"Navigate to Syllabus Page. Assert that the syllabus body string is displayed and click the link for the assignment.")
        courseDetailsPage.selectTab("SYLLABUS")
        courseDetailsPage.assertTabSelected("SYLLABUS")
        syllabusPage.assertSyllabusBody("this is the syllabus body Assignment")
        syllabusPage.clickLink("assignmentLink")

        Log.d(STEP_TAG, "Assert that the Assignment Details Page is loaded successfully.")
        assignmentDetailsPage.assertAssignmentDetails(assignment)

    }

    override fun displaysPageObjects() = Unit

}
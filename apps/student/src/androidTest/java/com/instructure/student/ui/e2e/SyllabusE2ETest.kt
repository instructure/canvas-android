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
 *
 */
package com.instructure.student.ui.e2e

import android.util.Log
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.QuizzesApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SyllabusE2ETest: StudentTest() {
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.E2E)
    fun testSyllabusE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select ${course.name} course.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to Syllabus Page. Assert that Empty View is displayed, because there is no syllabus yet.")
        courseBrowserPage.selectSyllabus()
        syllabusPage.assertEmptyView()

        Log.d(PREPARATION_TAG,"Seed an assignment for ${course.name} course.")
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                teacherToken = teacher.token,
                submissionTypes = listOf(SubmissionType.ON_PAPER),
                dueAt = 1.days.fromNow.iso8601,
                withDescription = true
        ))

        Log.d(PREPARATION_TAG,"Seed a quiz for ${course.name} course.")
        val quiz = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                published = true,
                token = teacher.token,
                dueAt = 2.days.fromNow.iso8601
        ))

        // TODO: Seed a generic calendar event

        Log.d(STEP_TAG,"Refresh the page. Assert that all of the items, so ${assignment.name} assignment and ${quiz.title} quiz are displayed.")
        syllabusPage.refresh()
        syllabusPage.assertItemDisplayed(assignment.name)
        syllabusPage.assertItemDisplayed(quiz.title)
    }
}
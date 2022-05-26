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

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SYLLABUS, TestCategory.E2E)
    fun testSyllabusE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Sign in and navigate to the course.  The course syllabus should be empty
        tokenLogin(student)
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectSyllabus()
        syllabusPage.assertEmptyView()


        // Create/publish our syllabus items

        // Seed an assignment
        val assignment = AssignmentsApi.createAssignment(AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                teacherToken = teacher.token,
                submissionTypes = listOf(SubmissionType.ON_PAPER),
                dueAt = 1.days.fromNow.iso8601,
                withDescription = true
        ))

        // Seed a quiz
        val quiz = QuizzesApi.createQuiz(QuizzesApi.CreateQuizRequest(
                courseId = course.id,
                withDescription = true,
                published = true,
                token = teacher.token,
                dueAt = 2.days.fromNow.iso8601
        ))

        // TODO: Seed a generic calendar event

        // Now refresh our syllabus page and verify that our assignment and quiz are showing
        syllabusPage.refresh()
        syllabusPage.assertItemDisplayed(assignment.name)
        syllabusPage.assertItemDisplayed(quiz.title)
    }
}
/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.ui.e2e

import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.util.*

@HiltAndroidTest
class TodoE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.TODOS, TestCategory.E2E)
    fun testTodoE2E() {
        // Inherited from student todo tests, may check this out later
        // Don't attempt this test on a Friday, Saturday or Sunday.
        // The TODO tab doesn't seem to behave correctly on Fridays (or presumably weekends).
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if(dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            println("We don't run the TODO E2E test on weekends")
            return
        }

        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = student.token
        )

        tokenLogin(teacher)

        dashboardPage.waitForRender()
        dashboardPage.openTodo()
        todoPage.waitForRender()
        todoPage.assertTodoElementIsDisplayed(courseName = course.name)

        SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = assignment[0].id,
                studentId = student.id,
                postedGrade = "15",
                excused = false
        )

        todoPage.refresh()
        todoPage.assertEmptyView()
    }
}
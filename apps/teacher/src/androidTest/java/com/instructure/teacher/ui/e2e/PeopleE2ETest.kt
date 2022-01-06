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

import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PeopleE2ETest: TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }
    
    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.PEOPLE, TestCategory.E2E)
    fun testPeopleE2E() {

        val data = seedData(teachers = 1, students = 2, courses = 1)
        val teacher = data.teachersList[0]
        val notGradedStudent = data.studentsList[0]
        val gradedStudent = data.studentsList[1]
        val course = data.coursesList[0]

        val assignments = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 10.0
        )

        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignments[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = assignments[0].id,
                studentId = gradedStudent.id,
                postedGrade = "10",
                excused = false
        )


        tokenLogin(teacher)

        dashboardPage.openCourse(course.name)
        courseBrowserPage.openPeopleTab()
        peopleListPage.clickPerson(teacher)
        studentContextPage.assertDisplaysCourseInfo(course)
        Espresso.pressBack()

        peopleListPage.clickPerson(notGradedStudent)
        studentContextPage.assertDisplaysStudentInfo(notGradedStudent)
        studentContextPage.assertDisplaysCourseInfo(course)
        studentContextPage.assertStudentGrade("--")
        studentContextPage.assertStudentSubmission("--")
        Espresso.pressBack()

        peopleListPage.clickPerson(gradedStudent)
        studentContextPage.assertDisplaysStudentInfo(gradedStudent)
        studentContextPage.assertDisplaysCourseInfo(course)
        studentContextPage.assertStudentGrade("100.0")
        studentContextPage.assertStudentSubmission("1")
    }
}
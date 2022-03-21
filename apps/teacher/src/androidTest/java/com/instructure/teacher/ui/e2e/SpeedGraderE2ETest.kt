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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.refresh
import com.instructure.canvas.espresso.withCustomConstraints
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.teacher.R
import com.instructure.teacher.ui.utils.*
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SpeedGraderE2ETest : TeacherTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We dont want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GRADES, TestCategory.E2E)
    fun testSpeedGraderE2E() {

        val data = seedData(teachers = 1, courses = 1, students = 3, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]
        val gradedStudent = data.studentsList[1]
        val noSubStudent = data.studentsList[2]

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

        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = assignment[0].id,
                studentId = gradedStudent.id,
                postedGrade = "15",
                excused = false
        )

        tokenLogin(teacher)

        coursesListPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()

        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.assertNeedsGrading(actual = 1, outOf = 3)
        assignmentDetailsPage.assertNotSubmitted(actual = 1, outOf = 3)
        assignmentDetailsPage.openNotSubmittedSubmissions()
        assignmentSubmissionListPage.assertHasStudentSubmission(canvasUser = noSubStudent)
        Espresso.pressBack()

        assignmentDetailsPage.openGradedSubmissions()
        assignmentSubmissionListPage.assertHasStudentSubmission(canvasUser = gradedStudent)
        Espresso.pressBack()

        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.assertDisplaysTextSubmissionViewWithStudentName(studentName = student.name)
        speedGraderPage.selectGradesTab()
        speedGraderGradePage.openGradeDialog()
        val grade = "10"
        speedGraderGradePage.enterNewGrade(grade)
        speedGraderGradePage.assertHasGrade(grade)
        Espresso.pressBack()
        refresh()

        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmissions()
        assignmentSubmissionListPage.clickFilterUngraded()
        assignmentSubmissionListPage.clickFilterDialogOk()
        assignmentSubmissionListPage.assertHasNoSubmission()

        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmissions()
        onView(withText(R.string.not_submitted)).perform(withCustomConstraints(click(), isDisplayingAtLeast(10)))
        assignmentSubmissionListPage.clickFilterDialogOk()
        assignmentSubmissionListPage.assertHasSubmission(1)
    }
}
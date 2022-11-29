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

import android.util.Log
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

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GRADES, TestCategory.E2E)
    fun testSpeedGraderE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(teachers = 1, courses = 1, students = 3, favoriteCourses = 1)
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val student = data.studentsList[0]
        val gradedStudent = data.studentsList[1]
        val noSubStudent = data.studentsList[2]

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for ${course.name} course.")
        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        Log.d(PREPARATION_TAG,"Seed a submission for ${assignment[0].name} assignment with ${student.name} student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = student.token
        )

        Log.d(PREPARATION_TAG,"Seed a submission for ${assignment[0].name} assignment with ${gradedStudent.name} student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        Log.d(PREPARATION_TAG,"Grade the previously seeded submission for ${gradedStudent.name} student.")
        SubmissionsApi.gradeSubmission(
                teacherToken = teacher.token,
                courseId = course.id,
                assignmentId = assignment[0].id,
                studentId = gradedStudent.id,
                postedGrade = "15",
                excused = false
        )

        Log.d(STEP_TAG, "Login with user: ${teacher.name}, login id: ${teacher.loginId}.")
        tokenLogin(teacher)

        Log.d(STEP_TAG,"Open ${course.name} course and navigate to Assignments Page.")
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG,"Click on ${assignment[0].name} assignment and assert that that there is one 'Needs Grading' submission (for ${noSubStudent.name} student) and one 'Not Submitted' submission (for ${student.name} student. ")
        assignmentListPage.clickAssignment(assignment[0])
        assignmentDetailsPage.assertNeedsGrading(actual = 1, outOf = 3)
        assignmentDetailsPage.assertNotSubmitted(actual = 1, outOf = 3)

        Log.d(STEP_TAG,"Open 'Not Submitted' submissions and assert that the submission of ${noSubStudent.name} student is displayed. Navigate back.")
        assignmentDetailsPage.openNotSubmittedSubmissions()
        assignmentSubmissionListPage.assertHasStudentSubmission(noSubStudent)
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open 'Graded' submissions and assert that the submission of ${gradedStudent.name} student is displayed. Navigate back.")
        assignmentDetailsPage.openGradedSubmissions()
        assignmentSubmissionListPage.assertHasStudentSubmission(gradedStudent)
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open (all) submissions and assert that the submission of ${student.name} student is displayed.")
        assignmentDetailsPage.openSubmissionsPage()
        assignmentSubmissionListPage.clickSubmission(student)
        speedGraderPage.assertDisplaysTextSubmissionViewWithStudentName(student.name)

        Log.d(STEP_TAG,"Select 'Grades' Tab and open the grade dialog.")
        speedGraderPage.selectGradesTab()
        speedGraderGradePage.openGradeDialog()
        val grade = "10"

        Log.d(STEP_TAG,"Enter $grade as the new grade and assert that it has applied. Navigate back and refresh the page.")
        speedGraderGradePage.enterNewGrade(grade)
        speedGraderGradePage.assertHasGrade(grade)
        Espresso.pressBack()
        refresh()

        Log.d(STEP_TAG,"Click on filter button and click on 'Filter submissions'.")
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmissions()

        Log.d(STEP_TAG,"Select 'Not Graded' and click on 'OK'.")
        assignmentSubmissionListPage.clickFilterUngraded()
        assignmentSubmissionListPage.clickFilterDialogOk()

        Log.d(STEP_TAG,"Assert that there isn't any submission displayed.")
        assignmentSubmissionListPage.assertHasNoSubmission()

        Log.d(STEP_TAG,"Click on filter button and click on 'Filter submissions'.")
        assignmentSubmissionListPage.clickFilterButton()
        assignmentSubmissionListPage.clickFilterSubmissions()

        Log.d(STEP_TAG,"Select 'Not Submitted' and click on 'OK'.")
        onView(withText(R.string.not_submitted)).perform(withCustomConstraints(click(), isDisplayingAtLeast(10)))
        assignmentSubmissionListPage.clickFilterDialogOk()

        Log.d(STEP_TAG,"Assert that there is one submission displayed.")
        assignmentSubmissionListPage.assertHasSubmission(1)

        Log.d(STEP_TAG, "Navigate back assignment's details page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Open (all) submissions and assert that the submission of ${student.name} student is displayed.")
        assignmentDetailsPage.openSubmissionsPage()
        
        Log.d(STEP_TAG, "Click on 'Post Policies' (eye) icon.")
        assignmentSubmissionListPage.clickOnPostPolicies()

        Log.d(STEP_TAG, "Assert that there is 1 grade which is hidden at this moment.")
        postSettingsPage.assertPostPolicyStatusCount(1, true)

        Log.d(STEP_TAG, "Click on 'Post Grades' button, navigate back to the Post Policies page." +
                "Assert that the empty view is displayed on the 'Post Grades' tab.")
        postSettingsPage.clickOnPostGradesButton()
        assignmentSubmissionListPage.clickOnPostPolicies()
        postSettingsPage.assertEmptyView()

        Log.d(STEP_TAG, "Click on 'Hide Grades' tab. Assert that there are 3 posted grades at this moment.")
        postSettingsPage.clickOnTab(R.string.hideGradesTab)
        postSettingsPage.assertPostPolicyStatusCount(3, false)

        Log.d(STEP_TAG, "Click on 'Hide Grades' button. It will navigate back to the Assignment Submission List Page." +
                "Assert that the hide grades (eye) icon is displayed next to the corresponding (graded) students.")
        postSettingsPage.clickOnHideGradesButton()
        assignmentSubmissionListPage.assertGradesHidden(gradedStudent.name)
        assignmentSubmissionListPage.assertGradesHidden(student.name)
    }
}
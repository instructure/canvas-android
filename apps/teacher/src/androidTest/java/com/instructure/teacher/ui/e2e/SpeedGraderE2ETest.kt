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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ViewUtils
import com.instructure.espresso.retry
import com.instructure.teacher.R
import com.instructure.teacher.ui.pages.PersonContextPage
import com.instructure.teacher.ui.utils.TeacherComposeTest
import com.instructure.teacher.ui.utils.seedAssignmentSubmission
import com.instructure.teacher.ui.utils.seedAssignments
import com.instructure.teacher.ui.utils.seedData
import com.instructure.teacher.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SpeedGraderE2ETest : TeacherComposeTest() {

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

        Log.d(PREPARATION_TAG, "Seeding 'Text Entry' assignment for '${course.name}' course.")
        val assignment = seedAssignments(
                courseId = course.id,
                dueAt = 1.days.fromNow.iso8601,
                submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY),
                teacherToken = teacher.token,
                pointsPossible = 15.0
        )

        Log.d(PREPARATION_TAG, "Seed a submission for '${assignment[0].name}' assignment with '${student.name}' student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = student.token
        )

        Log.d(PREPARATION_TAG, "Seed a submission for '${assignment[0].name}' assignment with '${gradedStudent.name}' student.")
        seedAssignmentSubmission(
                submissionSeeds = listOf(SubmissionsApi.SubmissionSeedInfo(
                        amount = 1,
                        submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )),
                assignmentId = assignment[0].id,
                courseId = course.id,
                studentToken = gradedStudent.token
        )

        Log.d(PREPARATION_TAG, "Grade the previously seeded submission for '${gradedStudent.name}' student.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, assignment[0].id, gradedStudent.id, postedGrade = "15")

        Log.d(STEP_TAG, "Login with user: '${teacher.name}', login id: '${teacher.loginId}'.")
        tokenLogin(teacher)

        Log.d(STEP_TAG, "Open '${course.name}' course and navigate to Assignments Page.")
        dashboardPage.openCourse(course)
        courseBrowserPage.openAssignmentsTab()

        Log.d(STEP_TAG, "Click on '${assignment[0].name}' assignment.")
        assignmentListPage.clickAssignment(assignment[0])

        Log.d(ASSERTION_TAG, "Assert that that there is one 'Needs Grading' submission for '${noSubStudent.name}' student and one 'Not Submitted' submission for '${student.name}' student, and one 'Graded' submission for '${gradedStudent.name}' student.")
        assignmentDetailsPage.assertNeedsGrading(actual = 1, outOf = 3)
        assignmentDetailsPage.assertNotSubmitted(actual = 1, outOf = 3)
        assignmentDetailsPage.assertHasGraded(actual = 1, outOf = 3)

        Log.d(ASSERTION_TAG, "Assert that the 'Submission Types' is 'Text Entry' at this assignment and the 'Submissions' and 'All' labels are displayed on the 'Submissions' card.")
        assignmentDetailsPage.assertSubmissionTypes(R.string.canvasAPI_onlineTextEntry  )
        assignmentDetailsPage.assertSubmissionsLabel()
        assignmentDetailsPage.assertAllSubmissionsLabel()

        Log.d(STEP_TAG, "Open 'Not Submitted' submissions.")
        assignmentDetailsPage.clickNotSubmittedSubmissions()

        Log.d(ASSERTION_TAG, "Assert that the 'Haven't Submitted Yet' label is displayed (as we filtered for only 'Not Submitted') and the submission of '${noSubStudent.name}' student is displayed.")
        assignmentSubmissionListPage.assertFilterLabelNotSubmittedSubmissions()
        assignmentSubmissionListPage.assertHasStudentSubmission(noSubStudent)

        composeTestRule.onRoot().printToLog("SEMANTIC_TREE")

        Log.d(ASSERTION_TAG, "Assert that the '${noSubStudent.name}' student has '-' as score as it's submission is not submitted yet.")
        assignmentSubmissionListPage.assertStudentScoreText(noSubStudent.name, "-")

        Log.d(STEP_TAG, "Navigate back to the Assignment Details Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on 'View All Submission' arrow icon.")
        assignmentDetailsPage.clickAllSubmissions()

        Log.d(STEP_TAG, "Click on '${student.name}' student's avatar.")
        assignmentSubmissionListPage.clickOnStudentAvatar(student.name)

        Log.d(ASSERTION_TAG, "Assert if it's navigating to the Student Context Page.")
        studentContextPage.assertDisplaysStudentInfo(student.shortName, student.loginId)
        studentContextPage.assertDisplaysCourseInfo(course)
        studentContextPage.assertStudentGrade("--")
        studentContextPage.assertStudentSubmission("1")
        studentContextPage.assertSectionNameView(PersonContextPage.UserRole.STUDENT)

        Log.d(STEP_TAG, "Navigate back to the Assignment Details Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Open 'Graded' submissions.")
        assignmentDetailsPage.clickGradedSubmissions()

        Log.d(ASSERTION_TAG, "Assert that the the submission of '${gradedStudent.name}' student is displayed with the corresponding grade (15).")
        assignmentSubmissionListPage.assertHasStudentSubmission(gradedStudent)
        assignmentSubmissionListPage.assertStudentScoreText(gradedStudent.name, "15")

        Log.d(STEP_TAG, "Navigate back to the Assignment Details Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open (all) submissions.")
        assignmentDetailsPage.clickAllSubmissions()
        assignmentSubmissionListPage.clickSubmission(student)

        Log.d(ASSERTION_TAG, "Assert that the submission of '${student.name}' student is displayed.")
        speedGraderPage.assertDisplaysTextSubmissionViewWithStudentName(student.name)

        Log.d(STEP_TAG, "Select 'Grades' Tab and open the grade dialog.")
        speedGraderPage.selectGradesTab()
        speedGraderGradePage.openGradeDialog()

        val grade = "10"
        Log.d(STEP_TAG, "Enter '$grade' as the new grade.")
        speedGraderGradePage.enterNewGrade(grade)

        Log.d(ASSERTION_TAG, "Assert that it has applied.")
        speedGraderGradePage.assertHasGrade(grade)

        Log.d(STEP_TAG, "Navigate back to the Assignment Submission List Page and refresh the page to apply the new grade changes.")
        Espresso.pressBack()
        assignmentSubmissionListPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the 'All Submissions' filter text is displayed.")
        assignmentSubmissionListPage.assertFilterLabelAllSubmissions()

        Log.d(STEP_TAG, "Click on filter button and click on 'Filter submissions'.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(ASSERTION_TAG, "Assert that all the corresponding filter texts are displayed.")
        assignmentSubmissionListPage.assertSubmissionFilterOption("All Submissions")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Submitted Late")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Needs Grading")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Not Submitted")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Graded")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Scored Less Than…")
        assignmentSubmissionListPage.assertSubmissionFilterOption("Scored More Than…")

        Log.d(STEP_TAG, "Select 'Not Graded' and click on 'OK'.")
        assignmentSubmissionListPage.clickFilterUngraded()
        assignmentSubmissionListPage.clickFilterDialogOk()

        Log.d(ASSERTION_TAG, "Assert that there isn't any submission displayed.")
        assignmentSubmissionListPage.assertHasNoSubmission()

        Log.d(STEP_TAG, "Click on filter button and click on 'Filter submissions'.")
        assignmentSubmissionListPage.clickFilterButton()

        Log.d(STEP_TAG, "Select 'Not Submitted' and click on 'OK'.")
        assignmentSubmissionListPage.clickFilterNotSubmitted()
        assignmentSubmissionListPage.clickFilterDialogOk()

        Log.d(ASSERTION_TAG, "Assert that there is one submission displayed.")
        retry(times  = 5, delay = 3000, catchBlock =  { refresh() }) {
            assignmentSubmissionListPage.assertHasSubmission(1)
        }

        Log.d(STEP_TAG, "Navigate back assignment's details page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Open (all) submissions and assert that the submission of '${student.name}' student is displayed.")
        assignmentDetailsPage.clickAllSubmissions()

        Log.d(STEP_TAG, "Type the '${gradedStudent.name}' student's name into the search input field.")
        assignmentSubmissionListPage.searchSubmission(gradedStudent.name)

        Log.d(ASSERTION_TAG, "Assert that there is one submission displayed and that is for '${gradedStudent.name}' student.")
        assignmentSubmissionListPage.assertHasSubmission(1)
        assignmentSubmissionListPage.assertHasStudentSubmission(gradedStudent)

        Log.d(STEP_TAG, "Clear the search field.")
        assignmentSubmissionListPage.clearSearch()

        Log.d(STEP_TAG, "Click on 'Post Policies' (eye) icon.")
        assignmentSubmissionListPage.clickOnPostPolicies()

        Log.d(ASSERTION_TAG, "Assert that there is 1 grade which is hidden at this moment.")
        postSettingsPage.assertPostPolicyStatusCount(1, true)

        Log.d(STEP_TAG, "Click on 'Post Grades' button, navigate back to the Post Policies page.")
        postSettingsPage.clickOnPostGradesButton()
        assignmentSubmissionListPage.clickOnPostPolicies()

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed on the 'Post Grades' tab.")
        postSettingsPage.assertEmptyView()

        Log.d(STEP_TAG, "Click on 'Hide Grades' tab.")
        postSettingsPage.clickOnTab(R.string.hideGradesTab)

        Log.d(ASSERTION_TAG, "Assert that there are 3 posted grades at this moment.")
        postSettingsPage.assertPostPolicyStatusCount(3, false)

        Log.d(STEP_TAG, "Click on 'Hide Grades' button. It will navigate back to the Assignment Submission List Page.")
        postSettingsPage.clickOnHideGradesButton()

        Log.d(ASSERTION_TAG, "Assert that the hide grades (eye) icon is displayed next to the corresponding (graded) students.")
        assignmentSubmissionListPage.assertGradesHidden(gradedStudent.name)
        assignmentSubmissionListPage.assertGradesHidden(student.name)
    }

}
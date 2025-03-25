/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.offline

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.common.pages.compose.AssignmentListPage
import com.instructure.dataseeding.api.AssignmentGroupsApi
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.ViewUtils
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineAssignmentsE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Stub
    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ASSIGNMENTS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineAssignmentsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding a NOT SUBMITTED assignment for '${course.name}' course.")
        val notSubmittedAssignment = AssignmentsApi.createAssignment(
            courseId = course.id,
            teacherToken = teacher.token,
            gradingType = GradingType.PERCENT,
            dueAt = 10.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG,"Seeding a SUBMITTED assignment for '${course.name}' course.")
        val submittedAssignment = AssignmentsApi.createAssignment(
            courseId = course.id,
            teacherToken = teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG,"Submit assignment: '${submittedAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, submittedAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG,"Seeding a GRADED assignment for '${course.name}' course.")
        val gradedAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(PREPARATION_TAG,"Submit assignment: '${gradedAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(course.id, student.token, gradedAssignment.id, submissionSeedsList = listOf(SubmissionsApi.SubmissionSeedInfo(amount = 1, submissionType = SubmissionType.ONLINE_TEXT_ENTRY)))

        Log.d(PREPARATION_TAG,"Grade submission: '${gradedAssignment.name}' with 13 points.")
        SubmissionsApi.gradeSubmission(teacher.token, course.id, gradedAssignment.id, student.id, postedGrade = "13")

        Log.d(PREPARATION_TAG,"Create an Assignment Group for '${course.name}' course.")
        val assignmentGroup = AssignmentGroupsApi.createAssignmentGroup(teacher.token, course.id, name = "Discussions")

        Log.d(PREPARATION_TAG,"Seeding assignment for '${course.name}' course.")
        val otherTypeAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.LETTER_GRADE, pointsPossible = 20.0, assignmentGroupId = assignmentGroup.id, submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY))

        Log.d(STEP_TAG,"Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Expand '${course.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course.name)

        Log.d(STEP_TAG, "Select the 'People' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState("Assignments")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(STEP_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG,"Select course: ${course.name}.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to course Assignments Page.")
        courseBrowserPage.selectAssignments()

        Log.d(ASSERTION_TAG, "Assert that the grading period label is 'Grading Period: All'.")
        assignmentListPage.assertGradingPeriodLabel()

        Log.d(ASSERTION_TAG, "Assert that all the previously seeded (4) assignments are displayed on the Assignment List Page.")
        assignmentListPage.assertHasAssignment(notSubmittedAssignment)
        assignmentListPage.assertHasAssignment(submittedAssignment)
        assignmentListPage.assertHasAssignment(gradedAssignment)
        assignmentListPage.assertHasAssignment(otherTypeAssignment)

        Log.d(ASSERTION_TAG, "Assert that the '${gradedAssignment.name}' assignment's grade is: '13/20 (D)'.")
        assignmentListPage.assertHasAssignment(gradedAssignment, expectedGrade = "13/20 (D)")

        Log.d(ASSERTION_TAG, "Assert that that the 'Upcoming Assignments' and 'Undated Assignments' filter groups are displayed and the sorting is 'Sort by Time'.")
        assignmentListPage.assertAssignmentGroupDisplayed("Upcoming Assignments") //Because 2 of our assignments has 1 and 10 days due date from today
        assignmentListPage.assertAssignmentGroupDisplayed("Undated Assignments") //Because one of our assignments has no due date

        Log.d(ASSERTION_TAG, "Assert that all the seeded (4) assignments are displayed on the Assignment List Page.")
        assignmentListPage.assertHasAssignment(notSubmittedAssignment)
        assignmentListPage.assertHasAssignment(submittedAssignment)
        assignmentListPage.assertHasAssignment(gradedAssignment)
        assignmentListPage.assertHasAssignment(otherTypeAssignment)

        Log.d(ASSERTION_TAG, "Assert that the 'Assignments' (type) filter group is displayed and the sorting is 'Sort by Type'.")
        assignmentListPage.groupByAssignments(AssignmentListPage.GroupByOption.AssignmentGroup)
        assignmentListPage.assertAssignmentGroupDisplayed("Assignments")
        assignmentListPage.assertAssignmentGroupDisplayed("Discussions") //Because one of our seeded data is actually a discussion.

        Log.d(STEP_TAG, "Filter the 'Not Yet Submitted' assignments.")
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.ToBeGraded)
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.Graded)
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.Other)
        assignmentListPage.assertHasAssignment(notSubmittedAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(submittedAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(gradedAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)

        Log.d(STEP_TAG, "Filter the 'To Be Graded' assignments.")
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.NotYetSubmitted)
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.ToBeGraded)
        assignmentListPage.assertHasAssignment(submittedAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(notSubmittedAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(gradedAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)

        Log.d(STEP_TAG, "Filter the 'GRADED' assignments.")
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.ToBeGraded)
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.Graded)
        assignmentListPage.assertHasAssignment(gradedAssignment)
        assignmentListPage.assertAssignmentNotDisplayed(notSubmittedAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(submittedAssignment.name)
        assignmentListPage.assertAssignmentNotDisplayed(otherTypeAssignment.name)

        Log.d(STEP_TAG, "Filter the 'ALL' assignments.")
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.NotYetSubmitted)
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.ToBeGraded)
        assignmentListPage.filterAssignments("Assignment Filter", AssignmentListPage.FilterOption.Other)

        Log.d(ASSERTION_TAG, "Assert that all the seeded (5) assignments are displayed on the Assignment List Page.")
        assignmentListPage.assertHasAssignment(notSubmittedAssignment)
        assignmentListPage.assertHasAssignment(submittedAssignment)
        assignmentListPage.assertHasAssignment(gradedAssignment)
        assignmentListPage.assertHasAssignment(otherTypeAssignment)

        Log.d(STEP_TAG, "Click on the '${submittedAssignment.name}' submitted assignment.")
        assignmentListPage.clickAssignment(submittedAssignment)

        Log.d(ASSERTION_TAG, "Assert that the corresponding views are displayed on the Assignment Details Page, and there IS a submission for it. Navigate back to Assignment List Page.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusSubmitted()
        assignmentDetailsPage.assertSubmissionAndRubricLabel()
        assignmentDetailsPage.assertStatusSubmitted()

        Log.d(ASSERTION_TAG, "Assert that the (Re)submit Assignment button is not enabled as submitting assignments is not supported in offline mode.")
        assignmentDetailsPage.assertSubmitButtonDisabled()

        Log.d(STEP_TAG, "Navigate to Submission Details Page by clicking on the submission and open the 'Comments' tab.")
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openComments()

        Log.d(ASSERTION_TAG, "Assert that the text submission is displayed as a comment.")
        submissionDetailsPage.assertTextSubmissionDisplayedAsComment()

        Log.d(STEP_TAG, "Click on the (+), add attachment button.")
        submissionDetailsPage.clickOnAddAttachmentButton()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog is displayed. Dismiss the dialog.")
        OfflineTestUtils.assertNoInternetConnectionDialog()
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Navigate back to the Assignment List Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Click on the '${notSubmittedAssignment.name}' NOT submitted assignment.")
        assignmentListPage.clickAssignment(notSubmittedAssignment)

        Log.d(STEP_TAG, "Assert that the corresponding views are displayed on the Assignment Details Page, and there is no submission yet. Navigate back to Assignment List Page.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusNotSubmitted()

        Log.d(ASSERTION_TAG, "Assert that 'Submission & Rubric' label is displayed.")
        assignmentDetailsPage.assertSubmissionAndRubricLabel()

        Log.d(STEP_TAG, "Navigate to Submission Details Page by clicking on the submission.")
        assignmentDetailsPage.goToSubmissionDetails()

        Log.d(ASSERTION_TAG, "Assert that there is no submission yet for the '${submittedAssignment.name}' assignment.")
        submissionDetailsPage.assertNoSubmissionEmptyView()

        Log.d(STEP_TAG, "Navigate back to the Assignment List Page.")
        ViewUtils.pressBackButton(2)

        Log.d(STEP_TAG, "Click on the '${gradedAssignment.name}' GRADED submitted assignment.")
        assignmentListPage.clickAssignment(gradedAssignment)

        Log.d(STEP_TAG, "Assert that the corresponding views are displayed on the Assignment Details Page, and there is no submission yet. Navigate back to Assignment List Page.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertStatusGraded()

        Log.d(STEP_TAG,"Refresh the page. Assert that the assignment '${submittedAssignment.name}' has been graded with 13 points out of 20 points.")
        assignmentDetailsPage.assertAssignmentGraded("13")
        assignmentDetailsPage.assertOutOfTextDisplayed("Out of 20 pts")
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }

}
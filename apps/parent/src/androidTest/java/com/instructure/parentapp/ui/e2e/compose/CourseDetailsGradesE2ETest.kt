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
 *
 */
package com.instructure.parentapp.ui.e2e.compose

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.annotations.ReleaseExclude
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.ago
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.espresso.getCustomDateCalendar
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.seedData
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@HiltAndroidTest
class CourseDetailsGradesE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GRADES, TestCategory.E2E)
    @ReleaseExclude
    fun testGradesListE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val gradedAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Submit assignment: '${gradedAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(
            course.id, student.token, gradedAssignment.id, submissionSeedsList = listOf(
                SubmissionsApi.SubmissionSeedInfo(
                    amount = 1,
                    submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )
            )
        )

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course submitted by the observed student but not graded by the teacher.")
        val submittedAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 30.0,
            dueAt = 2.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Grade submission: '${gradedAssignment.name}' with 13 points.")
        SubmissionsApi.gradeSubmission(
            teacher.token,
            course.id,
            gradedAssignment.id,
            student.id,
            postedGrade = "13"
        )

        Log.d(PREPARATION_TAG, "Submit assignment: '${submittedAssignment.name}' for student: '${student.name}'.")
        SubmissionsApi.seedAssignmentSubmission(
            course.id, student.token, submittedAssignment.id, submissionSeedsList = listOf(
                SubmissionsApi.SubmissionSeedInfo(
                    amount = 1,
                    submissionType = SubmissionType.ONLINE_TEXT_ENTRY
                )
            )
        )

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course which is not submitted by the observed student.")
        val notSubmittedAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        composeTestRule.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on the '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert that the details of the course has opened.")
        courseDetailsPage.assertCourseNameDisplayed(course) //Course Details Page is actually the Grades page by default when there are no tabs.

        Log.d(ASSERTION_TAG, "Assert that the Grades Card text is 'Total' by default and the 'Based on graded assignments' label is displayed.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertCardText("Total")
            gradesPage.assertBasedOnGradedAssignmentsLabel()
        }

        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            Log.d(ASSERTION_TAG, "Assert that the group header 'Upcoming Assignments' is displayed since the '${gradedAssignment.name}' assignment's due date is in the future and it's already graded.")
            gradesPage.assertGroupHeaderIsDisplayed("Upcoming Assignments")

            Log.d(ASSERTION_TAG, "Assert that the group header 'Past Assignments' is displayed since the '${submittedAssignment.name}' assignment's due date is in the past and it's already submitted but hasn't graded yet.")
            gradesPage.assertGroupHeaderIsDisplayed("Past Assignments")

            Log.d(ASSERTION_TAG, "Assert that the group header 'Overdue Assignments' is displayed since the '${notSubmittedAssignment.name}' assignment's due date is in the past and there is no submission for '${student.name}' student.")
            gradesPage.assertGroupHeaderIsDisplayed("Overdue Assignments")
        }

        Log.d(ASSERTION_TAG, "Assert that all the three seeded assignments are displayed.")
        gradesPage.assertAssignmentIsDisplayed(gradedAssignment.name)
        gradesPage.assertAssignmentIsDisplayed(submittedAssignment.name)
        gradesPage.assertAssignmentIsDisplayed(notSubmittedAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${gradedAssignment.name}' graded assignment's score (aka. grade text) is 13/15 and the Total grade is 86.67%.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000, catchBlock = {
            composeTestRule.waitForIdle()
            gradesPage.refresh()
        })
        {
            gradesPage.assertAssignmentGradeText(gradedAssignment.name, "13/15")
            gradesPage.assertTotalGradeText("86.67%") //Sometimes the Total grade cannot be displayed because of API issues and even the retry logic does not solve it.
        }

        Log.d(ASSERTION_TAG, "Assert that none of the '${submittedAssignment.name}' and '${notSubmittedAssignment.name}' has valid score (aka. grade text) since one of the is submitted but not graded yet, the other not even submitted.")
        gradesPage.assertAssignmentGradeText(submittedAssignment.name, "-/30")
        gradesPage.assertAssignmentGradeText(notSubmittedAssignment.name, "-/15")

        Log.d(STEP_TAG, "Click on the '${gradedAssignment.name}' assignment to open it's details.")
        courseDetailsPage.clickAssignment(gradedAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the '${gradedAssignment.name}' assignment's title and details are displayed and the proper score (13) is displayed as well. Assert that the selected attempt is 'Attempt 1'.")
        assignmentDetailsPage.assertAssignmentTitle(gradedAssignment.name)
        assignmentDetailsPage.assertAssignmentDetails(gradedAssignment)
        assignmentDetailsPage.assertAssignmentGraded("13")

        Log.d(STEP_TAG, "Navigate back to the Assignment (Grades) List page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that there is no filter applied by default.")
        gradesPage.assertFilterNotApplied()

        Log.d(STEP_TAG, "Click on the 'Filter' icon (next to the total grade) and choose 'Group' filter option.")
        gradesPage.clickFilterButton()
        gradesPage.clickFilterOption("Group")

        Log.d(ASSERTION_TAG, "Assert the Grades Preferences (aka. Filter) screen labels and toolbar title.")
        gradesPage.assertGradesPreferencesFilterScreenLabels()

        Log.d(STEP_TAG, "Save the previously set filter.")
        gradesPage.clickSaveButton()

        Log.d(ASSERTION_TAG, "Assert that there is filter applied se we just set it recently.")
        gradesPage.assertFilterApplied()

        Log.d(ASSERTION_TAG, "Assert that only the 'Assignments' group header is displayed since we are filtering based on groups, and all of our items are Assignments.")
        gradesPage.assertGroupHeaderIsDisplayed("Assignments")

        Log.d(ASSERTION_TAG, "Assert that none of the 'Due Date' filter group headers are displayed since we are filtering based on 'Group' now.")
        gradesPage.assertGroupHeaderIsNotDisplayed("Upcoming Assignments")
        gradesPage.assertGroupHeaderIsNotDisplayed("Overdue Assignments")
        gradesPage.assertGroupHeaderIsNotDisplayed("Past Assignments")

        Log.d(STEP_TAG, "Turn off 'Based on graded assignments' toggle.")
        gradesPage.clickBasedOnGradedAssignments()

        Log.d(ASSERTION_TAG, "Assert that the Total grade is 21.67%, since we are counting missing and not graded assignments as well when calculating the Total Grade.")
        composeTestRule.waitForIdle()
        gradesPage.assertTotalGradeText("21.67%")

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course which is not submitted by the observed student.")
        AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course which is not submitted by the observed student.")
        AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course which is not submitted by the observed student.")
        AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Thread.sleep(5000) // Allow assignments' creation to propagate

        Log.d(ASSERTION_TAG, "Assert the 'Card' label is 'Total'.")
        gradesPage.assertCardText("Total")

        Log.d(STEP_TAG, "Turn on 'Based on graded assignments' toggle.")
        gradesPage.clickBasedOnGradedAssignments()

        Log.d(STEP_TAG, "Refresh the Grades page and scroll to the down the assignment list.")
        gradesPage.refresh()
        gradesPage.scrollDownScreen()

        Log.d(ASSERTION_TAG, "Assert the 'Card' label is 'Total'.")
        gradesPage.assertCardText("Total")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.E2E)
    fun testGradesListSearchE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, courses = 1, teachers = 1, parents = 1)
        val course = data.coursesList[0]
        val parent = data.parentsList[0]
        val teacher = data.teachersList[0]

        val discussionWithCheckpointsTitle = "Test Discussion with Checkpoints"
        val assignmentName = "Test Assignment with Checkpoints"

        Log.d(PREPARATION_TAG, "Convert dates to match with different formats in different screens (Calendar, Assignment Details)")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val replyToTopicCalendar = getCustomDateCalendar(2)
        val replyToEntryCalendar = getCustomDateCalendar(4)
        val replyToTopicDueDate = dateFormat.format(replyToTopicCalendar.time)
        val replyToEntryDueDate = dateFormat.format(replyToEntryCalendar.time)

        Log.d(PREPARATION_TAG, "Seed a discussion topic with checkpoints for '${course.name}' course.")
        DiscussionTopicsApi.createDiscussionTopicWithCheckpoints(course.id, teacher.token, discussionWithCheckpointsTitle, assignmentName, replyToTopicDueDate, replyToEntryDueDate)

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course.")
        val pointsAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            assignmentName = "Algebra Homework",
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course submitted by the observed student but not graded by the teacher.")
        val percentageAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            assignmentName = "Biology Lab Report",
            gradingType = GradingType.PERCENT,
            pointsPossible = 100.0,
            dueAt = 2.days.ago.iso8601,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Log.d(PREPARATION_TAG, "Seeding assignment for '${course.name}' course which is not submitted by the observed student.")
        val undatedAssignment = AssignmentsApi.createAssignment(
            course.id,
            teacher.token,
            assignmentName = "Chemistry Project",
            gradingType = GradingType.POINTS,
            pointsPossible = 15.0,
            submissionTypes = listOf(SubmissionType.ONLINE_TEXT_ENTRY)
        )

        Thread.sleep(5000) // Allow the grading to propagate

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        composeTestRule.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on the '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert that the details of the course has opened.")
        courseDetailsPage.assertCourseNameDisplayed(course) //Course Details Page is actually the Grades page by default when there are no tabs.

        Log.d(ASSERTION_TAG, "Assert that all the three seeded assignments are displayed before searching.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertAssignmentIsDisplayed(pointsAssignment.name)
            gradesPage.assertAssignmentIsDisplayed(percentageAssignment.name)
            gradesPage.assertAssignmentIsDisplayed(undatedAssignment.name)
        }

        Log.d(STEP_TAG, "Click on the 'Search' (magnifying glass) icon to expand the search bar.")
        gradesPage.clickSearchButton()

        Log.d(ASSERTION_TAG, "Assert that the search field has expanded and is displayed.")
        gradesPage.assertSearchFieldIsDisplayed()

        Log.d(STEP_TAG, "Type 'Algebra' into the search field to match only the '${pointsAssignment.name}' assignment.")
        gradesPage.typeSearchQuery("Algebra")

        Log.d(ASSERTION_TAG, "Assert that the matching '${pointsAssignment.name}' assignment is displayed and the non-matching assignments are not displayed.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertAssignmentIsDisplayed(pointsAssignment.name)
            gradesPage.assertAssignmentIsNotDisplayed(percentageAssignment.name)
            gradesPage.assertAssignmentIsNotDisplayed(undatedAssignment.name)
        }

        Log.d(STEP_TAG, "Clear the search query by clicking on the clear (X) button.")
        gradesPage.clearSearchQuery()

        Log.d(ASSERTION_TAG, "Assert that all the three assignments are displayed again after clearing the search query.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertAssignmentIsDisplayed(pointsAssignment.name)
            gradesPage.assertAssignmentIsDisplayed(percentageAssignment.name)
            gradesPage.assertAssignmentIsDisplayed(undatedAssignment.name)
        }

        Log.d(STEP_TAG, "Type a search query ('Geography') that does not match any of the seeded assignments.")
        gradesPage.typeSearchQuery("Geography")
        Espresso.closeSoftKeyboard()

        Log.d(ASSERTION_TAG, "Assert that the 'No Matching Assignments' empty state is displayed and none of the assignments are displayed.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertNoMatchingAssignmentsDisplayed()
            gradesPage.assertAssignmentIsNotDisplayed(pointsAssignment.name)
            gradesPage.assertAssignmentIsNotDisplayed(percentageAssignment.name)
            gradesPage.assertAssignmentIsNotDisplayed(undatedAssignment.name)
        }

        Log.d(STEP_TAG, "Close the search bar by clicking on the close (X) button.")
        gradesPage.closeSearchBar()

        Log.d(ASSERTION_TAG, "Assert that the search bar has collapsed (the 'Search' icon is displayed again) and all the three assignments are displayed.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertSearchButtonIsDisplayed()
            gradesPage.assertAssignmentIsDisplayed(pointsAssignment.name)
            gradesPage.assertAssignmentIsDisplayed(percentageAssignment.name)
            gradesPage.assertAssignmentIsDisplayed(undatedAssignment.name)
        }

        Log.d(ASSERTION_TAG, "Assert that there is no filter applied by default.")
        gradesPage.assertFilterNotApplied()

        Log.d(ASSERTION_TAG, "Assert that the assignments are grouped by due date by default so the 'Upcoming Assignments', 'Overdue Assignments' and 'Undated Assignments' group headers are displayed.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertGroupHeaderIsDisplayed("Upcoming Assignments")
            gradesPage.assertGroupHeaderIsDisplayed("Overdue Assignments")
            gradesPage.assertGroupHeaderIsDisplayed("Undated Assignments")
        }

        Log.d(STEP_TAG, "Click on the 'Filter' icon (next to the total grade) and choose the 'Group' filter option.")
        gradesPage.clickFilterButton()
        gradesPage.clickFilterOption("Group")

        Log.d(ASSERTION_TAG, "Assert the Grades Preferences (aka. Filter) screen labels and toolbar title.")
        gradesPage.assertGradesPreferencesFilterScreenLabels()

        Log.d(STEP_TAG, "Save the previously set 'Group' filter.")
        gradesPage.clickSaveButton()

        Log.d(ASSERTION_TAG, "Assert that the filter is still not applied because the grouping does not count as filtering as it won't change the count of the result items.")
        gradesPage.assertFilterNotApplied()

        Log.d(ASSERTION_TAG, "Assert that only the 'Assignments' group header is displayed since we are filtering based on groups, and all of our items are in the default 'Assignments' group.")
        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            gradesPage.assertGroupHeaderIsDisplayed("Assignments")
        }

        Log.d(ASSERTION_TAG, "Assert that none of the 'Due Date' filter group headers are displayed since we are filtering based on 'Group' now.")
        gradesPage.assertGroupHeaderIsNotDisplayed("Upcoming Assignments")
        gradesPage.assertGroupHeaderIsNotDisplayed("Overdue Assignments")
        gradesPage.assertGroupHeaderIsNotDisplayed("Undated Assignments")

        Log.d(ASSERTION_TAG, "Assert that all the four seeded assignments (3 assignments, 1 discussion with checkpoints) are still displayed under the 'Assignments' group.")
        gradesPage.assertAssignmentIsDisplayed(pointsAssignment.name)
        gradesPage.assertAssignmentIsDisplayed(percentageAssignment.name)
        gradesPage.assertAssignmentIsDisplayed(undatedAssignment.name)
        gradesPage.assertAssignmentIsDisplayed(discussionWithCheckpointsTitle)
    }
}
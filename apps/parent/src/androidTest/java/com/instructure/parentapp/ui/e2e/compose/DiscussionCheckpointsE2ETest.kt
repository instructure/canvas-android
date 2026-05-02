/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e.compose

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.SubmissionsApi
import com.instructure.espresso.convertIso8601ToCanvasFormat
import com.instructure.espresso.getCustomDateCalendar
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.pandautils.features.calendar.CalendarPrefs
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.seedData
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@HiltAndroidTest
class DiscussionCheckpointsE2ETest : ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Before
    fun clearPreferences() {
        CalendarPrefs.clearPrefs()
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DISCUSSIONS, TestCategory.E2E, SecondaryFeatureCategory.DISCUSSION_CHECKPOINTS)
    fun testDiscussionCheckpointsCalendarE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, parents = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val parent = data.parentsList[0]
        val course = data.coursesList[0]

        val discussionWithCheckpointsTitle = "Test Discussion with Checkpoints"
        val assignmentName = "Test Assignment with Checkpoints"

        Log.d(PREPARATION_TAG, "Convert dates to match with different formats in different screens (Calendar, Assignment Details)")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val calendarDisplayFormat = SimpleDateFormat(" MMM d 'at' h:mm a", Locale.US)
        val assignmentDetailsDisplayFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        val replyToTopicCalendar = getCustomDateCalendar(2)
        val replyToEntryCalendar = getCustomDateCalendar(4)
        val replyToTopicDueDate = dateFormat.format(replyToTopicCalendar.time)
        val replyToEntryDueDate = dateFormat.format(replyToEntryCalendar.time)
        val assignmentDetailsReplyToTopicDueDate = assignmentDetailsDisplayFormat.format(replyToTopicCalendar.time)
        val assignmentDetailsReplyToEntryDueDate = assignmentDetailsDisplayFormat.format(replyToEntryCalendar.time)
        val convertedReplyToTopicDueDate = calendarDisplayFormat.format(replyToTopicCalendar.time)
        val convertedReplyToEntryDueDate = calendarDisplayFormat.format(replyToEntryCalendar.time)

        Log.d(PREPARATION_TAG, "Seed a discussion topic with checkpoints for '${course.name}' course.")
        DiscussionTopicsApi.createDiscussionTopicWithCheckpoints(course.id, teacher.token, discussionWithCheckpointsTitle, assignmentName, replyToTopicDueDate, replyToEntryDueDate)

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Click on the 'Calendar' bottom menu to navigate to the Calendar page.")
        dashboardPage.clickCalendarBottomMenu()
        composeTestRule.waitForIdle()

        Log.d(STEP_TAG , "Swipe 2 days to the future to find the 'Reply to Topic' Discussion Checkpoint calendar item.")
        calendarScreenPage.swipeEventsLeft(2)

        Log.d(ASSERTION_TAG, "Assert that the '$discussionWithCheckpointsTitle Reply to Topic' checkpoint is displayed on its due date on the Calendar Page.")
        calendarScreenPage.assertItemDetails("$discussionWithCheckpointsTitle Reply to Topic", course.name, "Due$convertedReplyToTopicDueDate")

        Log.d(STEP_TAG, "Select the '$discussionWithCheckpointsTitle Reply to Topic' event and navigate back to the Calendar Page.")
        calendarScreenPage.clickOnItem("$discussionWithCheckpointsTitle Reply to Topic")

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed properly with the correct toolbar title and subtitle.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that the checkpoints are displayed properly on the Assignment Details Page.")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Reply to topic due", assignmentDetailsReplyToTopicDueDate)
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Additional replies (2) due", assignmentDetailsReplyToEntryDueDate)

        Log.d(STEP_TAG, "Navigate back to the Calendar Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG , "Swipe 2 additional days to the future to find the 'Additional replies' Discussion Checkpoint calendar item.")
        calendarScreenPage.swipeEventsLeft(2)

        Log.d(ASSERTION_TAG, "Assert that the '$discussionWithCheckpointsTitle Required Replies (2)' checkpoint is displayed on its due date on the Calendar Page.")
        calendarScreenPage.assertItemDetails("$discussionWithCheckpointsTitle Required Replies (2)", course.name, "Due$convertedReplyToEntryDueDate")

        Log.d(STEP_TAG, "Select the '$discussionWithCheckpointsTitle Required Replies (2)' event and navigate back to the Calendar Page.")
        calendarScreenPage.clickOnItem("$discussionWithCheckpointsTitle Required Replies (2)")

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed properly with the correct toolbar title and subtitle.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that the checkpoints are displayed properly on the Assignment Details Page.")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Reply to topic due", assignmentDetailsReplyToTopicDueDate)
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Additional replies (2) due", assignmentDetailsReplyToEntryDueDate)

        Log.d(STEP_TAG, "Navigate back to the Calendar Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Navigate back 2 days to the 'Reply to Topic' checkpoint due date.")
        calendarScreenPage.swipeEventsRight(2)

        Log.d(ASSERTION_TAG, "Assert that the '$discussionWithCheckpointsTitle Reply to Topic' checkpoint is displayed on its due date on the Calendar Page.")
        calendarScreenPage.assertItemDetails("$discussionWithCheckpointsTitle Reply to Topic", course.name, "Due$convertedReplyToTopicDueDate")

        Log.d(STEP_TAG, "Select the '$discussionWithCheckpointsTitle Reply to Topic' event and navigate back to the Calendar Page.")
        calendarScreenPage.clickOnItem("$discussionWithCheckpointsTitle Reply to Topic")

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed properly with the correct toolbar title and subtitle.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that the checkpoints are displayed properly on the Assignment Details Page.")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Reply to topic due", assignmentDetailsReplyToTopicDueDate)
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Additional replies (2) due", assignmentDetailsReplyToEntryDueDate)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GRADES, TestCategory.E2E, SecondaryFeatureCategory.DISCUSSION_CHECKPOINTS)
    fun testDiscussionCheckpointsGradesListE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, parents = 1, teachers = 1, courses = 1)
        val teacher = data.teachersList[0]
        val parent = data.parentsList[0]
        val course = data.coursesList[0]

        val discussionWithCheckpointsTitle = "Test Discussion with Checkpoints"
        val assignmentName = "Test Assignment with Checkpoints"

        Log.d(PREPARATION_TAG, "Convert dates to match with different formats in different screens (Grades list, Assignment Details)")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val assignmentDetailsDisplayFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        val timeDisplayFormat = SimpleDateFormat("h:mm a", Locale.US)
        val replyToTopicCalendar = getCustomDateCalendar(2)
        val replyToEntryCalendar = getCustomDateCalendar(4)
        val replyToTopicDueDate = dateFormat.format(replyToTopicCalendar.time)
        val replyToEntryDueDate = dateFormat.format(replyToEntryCalendar.time)
        val assignmentDetailsReplyToTopicDueDate = assignmentDetailsDisplayFormat.format(replyToTopicCalendar.time)
        val assignmentDetailsReplyToEntryDueDate = assignmentDetailsDisplayFormat.format(replyToEntryCalendar.time)
        val convertedReplyToTopicDueDate = "Due " + convertIso8601ToCanvasFormat(replyToTopicDueDate) + " " + timeDisplayFormat.format(replyToTopicCalendar.time)
        val convertedReplyToEntryDueDate = "Due " + convertIso8601ToCanvasFormat(replyToEntryDueDate) + " " + timeDisplayFormat.format(replyToEntryCalendar.time)

        Log.d(PREPARATION_TAG, "Seed a discussion topic with checkpoints for '${course.name}' course.")
        DiscussionTopicsApi.createDiscussionTopicWithCheckpoints(course.id, teacher.token, discussionWithCheckpointsTitle, assignmentName, replyToTopicDueDate, replyToEntryDueDate)

        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

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

        Log.d(ASSERTION_TAG, "Assert that the '${discussionWithCheckpointsTitle}' discussion is present along with 2 date info (For the 2 checkpoints).")
        courseDetailsPage.assertHasAssignmentWithCheckpoints(discussionWithCheckpointsTitle, dueAtString = convertedReplyToTopicDueDate, dueAtStringSecondCheckpoint = convertedReplyToEntryDueDate, expectedGrade = "-/15")

        Log.d(STEP_TAG, "Click on the expand icon for the '$discussionWithCheckpointsTitle' discussion to see the individual checkpoint details.")
        courseDetailsPage.clickDiscussionCheckpointExpandCollapseIcon(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that both checkpoints are displayed with the correct due dates and grades after expanding.")
        courseDetailsPage.assertDiscussionCheckpointDetails(2, dueAtReplyToTopic = convertedReplyToTopicDueDate, dueAtAdditionalReplies = convertedReplyToEntryDueDate, gradeReplyToTopic = "-/10", gradeAdditionalReplies = "-/5")

        Log.d(STEP_TAG, "Click on the '$discussionWithCheckpointsTitle' discussion item to open the Assignment Details Page.")
        courseDetailsPage.clickAssignment(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed properly with the correct toolbar title and subtitle.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that the checkpoints are displayed properly on the Assignment Details Page.")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Reply to topic due", assignmentDetailsReplyToTopicDueDate)
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Additional replies (2) due", assignmentDetailsReplyToEntryDueDate)

        Log.d(STEP_TAG, "Navigate back to the Course Details Page.")
        Espresso.pressBack()

        Log.d(PREPARATION_TAG, "Grade the 'Reply to topic' checkpoint of '$discussionWithCheckpointsTitle' with 1 point via the Teacher API.")
        val student = data.studentsList[0]
        val parentAssignment = AssignmentsApi.listAssignments(course.id, teacher.token).first()
        SubmissionsApi.gradeSubmission(teacher.token, course.id, parentAssignment.id, student.id, postedGrade = "1", subAssignmentTag = "reply_to_topic")

        Log.d(ASSERTION_TAG, "Assert that the total grade of '$discussionWithCheckpointsTitle' is updated to '-/15' after grading the 'Reply to topic' checkpoint.")
        courseDetailsPage.assertHasAssignmentWithCheckpoints(discussionWithCheckpointsTitle, dueAtString = convertedReplyToTopicDueDate, dueAtStringSecondCheckpoint = convertedReplyToEntryDueDate, expectedGrade = "-/15")

        Log.d(STEP_TAG, "Click on the expand icon for the '$discussionWithCheckpointsTitle' discussion to see the individual checkpoint details.")
        courseDetailsPage.clickDiscussionCheckpointExpandCollapseIcon(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that the 'Reply to topic' grade is '1/10' and the 'Additional replies' grade is '-/5'.")
        retryWithIncreasingDelay(times = 15, maxDelay = 3000, catchBlock = {
            courseDetailsPage.refresh()
            courseDetailsPage.clickDiscussionCheckpointExpandCollapseIcon(discussionWithCheckpointsTitle)
        })
        {
            courseDetailsPage.assertDiscussionCheckpointDetails(2, dueAtReplyToTopic = convertedReplyToTopicDueDate, statusReplyToTopic = "Graded", dueAtAdditionalReplies = convertedReplyToEntryDueDate, gradeReplyToTopic = "1/10", gradeAdditionalReplies = "-/5")
        }
    }

}

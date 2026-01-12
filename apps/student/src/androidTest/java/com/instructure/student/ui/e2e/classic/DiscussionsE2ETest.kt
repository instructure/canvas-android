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
package com.instructure.student.ui.e2e.classic

import android.os.SystemClock.sleep
import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.pressBackButton
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.EnrollmentsApi
import com.instructure.dataseeding.model.EnrollmentTypes.STUDENT_ENROLLMENT
import com.instructure.dataseeding.model.EnrollmentTypes.TEACHER_ENROLLMENT
import com.instructure.espresso.convertIso8601ToCanvasFormat
import com.instructure.espresso.getDateInCanvasFormat
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DiscussionsE2ETest: StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DISCUSSIONS, TestCategory.E2E)
    fun testDiscussionsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a discussion topic for '${course.name}' course.")
        val topic1 = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Seed another discussion topic for '${course.name}' course.")
        val topic2 = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Seed an announcement for '${course.name}' course.")
        val announcement = DiscussionTopicsApi.createAnnouncement(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Seed another announcement for '${course.name}' course.")
        val announcement2 = DiscussionTopicsApi.createAnnouncement(course.id, teacher.token)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select course: '${course.name}'.")
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course)

        Log.d(ASSERTION_TAG, "Assert that the 'Discussions' and 'Announcements' Tabs are both displayed on the CourseBrowser Page.")
        courseBrowserPage.assertTabDisplayed("Announcements")
        courseBrowserPage.assertTabDisplayed("Discussions")

        Log.d(STEP_TAG, "Navigate to Announcements Page.")
        courseBrowserPage.selectAnnouncements()

        Log.d(ASSERTION_TAG, "Assert that both '${announcement.title}' and '${announcement2.title}' announcements are displayed.")
        discussionListPage.assertTopicDisplayed(announcement.title)
        discussionListPage.assertTopicDisplayed(announcement2.title)

        Log.d(STEP_TAG, "Select '${announcement.title}' announcement.")
        discussionListPage.selectTopic(announcement.title)

        Log.d(ASSERTION_TAG, "Assert if the Discussion Details Page is displayed.")
        discussionDetailsPage.assertToolbarDiscussionTitle(announcement.title)

        Log.d(STEP_TAG, "Navigate back to the Discussion List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the 'Search' button and search for '${announcement2.title}'. announcement.")
        discussionListPage.searchable.clickOnSearchButton()
        discussionListPage.searchable.typeToSearchBar(announcement2.title)

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that the searching method is working well, so '${announcement.title}' won't be displayed and '${announcement2.title}' is displayed.")
        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(announcement2.title)
        discussionListPage.assertTopicNotDisplayed(announcement.title)

        Log.d(STEP_TAG, "Clear the search input field.")
        discussionListPage.searchable.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(announcement.title)

        Log.d(ASSERTION_TAG, "Assert that both announcements, '${announcement.title}' and '${announcement2.title}' has been displayed.")
        discussionListPage.assertTopicDisplayed(announcement2.title)

        Log.d(STEP_TAG, "Navigate back to CourseBrowser Page.")
        pressBackButton(3)

        Log.d(STEP_TAG, "Navigate to Discussions Page.")
        courseBrowserPage.selectDiscussions()

        Log.d(ASSERTION_TAG, "Assert that '${topic1.title}' discussion is displayed.")
        discussionListPage.assertTopicDisplayed(topic1.title)

        Log.d(STEP_TAG, "Select '${topic1.title}' discussion.")
        discussionListPage.selectTopic(topic1.title)

        Log.d(ASSERTION_TAG, "Assert if the details page is displayed and there is no reply for the discussion yet.")
        discussionDetailsPage.assertToolbarDiscussionTitle(topic1.title)

        Log.d(STEP_TAG, "Navigate back to Discussion List Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that the '${topic2.title}' discussion is displayed.")
        discussionListPage.assertTopicDisplayed(topic2.title)

        Log.d(STEP_TAG, "Select '${topic2.title}' discussion.")
        discussionListPage.selectTopic(topic2.title)

        Log.d(ASSERTION_TAG, "Assert if the details page is displayed and there is no reply for the discussion yet.")
        discussionDetailsPage.assertToolbarDiscussionTitle(topic2.title)

        Log.d(STEP_TAG, "Navigate back to Discussion List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Create a new discussion then close it.")
        discussionListPage.launchCreateDiscussionThenClose()

        val replyMessage = "My reply"
        Log.d(PREPARATION_TAG, "Seed a discussion topic (message) entry for the '${topic1.title}' discussion with the '$replyMessage' message as a student.")
        DiscussionTopicsApi.createEntryToDiscussionTopic(student.token, course.id, topic1.id, replyMessage)
        sleep(2000) // Allow some time for entry creation to propagate

        Log.d(STEP_TAG, "Select '${topic1.title}' topic.")
        discussionListPage.selectTopic(topic1.title)

        Log.d(ASSERTION_TAG, "Assert the the previously sent entry message, '$replyMessage')' is displayed on the details (web view) page.")
        discussionDetailsPage.waitForEntryDisplayed(replyMessage)
        discussionDetailsPage.assertEntryDisplayed(replyMessage)

        Log.d(STEP_TAG, "Navigate back to Discussion List Page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Refresh the page. Assert that the previously sent reply has been counted, and there are no unread replies.")
        discussionListPage.pullToUpdate()
        discussionListPage.assertReplyCount(topic1.title, 1)
        discussionListPage.assertUnreadReplyCount(topic1.title, 0)

        val currentDate = getDateInCanvasFormat()
        Log.d(ASSERTION_TAG, "Assert that the due date is the current date (in the expected format).")
        discussionListPage.assertDueDate(topic1.title, currentDate)
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DISCUSSIONS, TestCategory.E2E, SecondaryFeatureCategory.DISCUSSION_CHECKPOINTS)
    fun testDiscussionCheckpointsAssignmentListDetailsE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Enroll '${student.name}' student to the dedicated course (${course.name}) with '${course.id}' id.")
        EnrollmentsApi.enrollUser(course.id, student.id, STUDENT_ENROLLMENT)

        Log.d(PREPARATION_TAG, "Enroll '${teacher.name}' teacher to the dedicated course (${course.name}) with '${course.id}' id.")
        EnrollmentsApi.enrollUser(course.id, teacher.id, TEACHER_ENROLLMENT)

        Log.d(PREPARATION_TAG, "Seed a discussion topic with checkpoints for '${course.name}' course.")
        val discussionWithCheckpointsWithoutDueDatesTitle = "Test Discussion with Checkpoints"
        val assignmentName = "Test Assignment with Checkpoints"
        DiscussionTopicsApi.createDiscussionTopicWithCheckpoints(course.id, teacher.token, discussionWithCheckpointsWithoutDueDatesTitle, assignmentName)

        Log.d(PREPARATION_TAG, "Seed a discussion topic with checkpoints for '${course.name}' course.")
        val discussionWithCheckpointsWithDueDatesTitle = "Test Discussion with Checkpoints with Due Dates"
        val replyToTopicDueDate = "2029-11-12T22:59:00Z"
        val replyToEntryDueDate = "2029-11-19T22:59:00Z"
        DiscussionTopicsApi.createDiscussionTopicWithCheckpoints(course.id, teacher.token, discussionWithCheckpointsWithDueDatesTitle, assignmentName, replyToTopicDueDate, replyToEntryDueDate)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select course: '${course.name}'.")
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course.name)

        Log.d(ASSERTION_TAG, "Assert that the 'Discussions' Tab is displayed on the CourseBrowser Page.")
        courseBrowserPage.assertTabDisplayed("Discussions")

        Log.d(STEP_TAG, "Navigate to Assignment List Page.")
        courseBrowserPage.selectAssignments()

        Log.d(ASSERTION_TAG, "Assert that the '${discussionWithCheckpointsWithoutDueDatesTitle}' discussions are present along with 2 date info (For the 2 checkpoints).")
        assignmentListPage.assertHasAssignmentWithCheckpoints(discussionWithCheckpointsWithoutDueDatesTitle, expectedGrade = "-/15")
        assignmentListPage.assertHasAssignmentWithCheckpoints(discussionWithCheckpointsWithDueDatesTitle, dueAtString = replyToTopicDueDate, dueAtStringSecondCheckpoint = replyToEntryDueDate, expectedGrade = "-/15")

        Log.d(STEP_TAG, "Click on the expand icon for the '$discussionWithCheckpointsWithoutDueDatesTitle' discussion (to see the checkpoints' details).")
        assignmentListPage.clickDiscussionCheckpointExpandCollapseIcon(discussionWithCheckpointsWithoutDueDatesTitle)

        Log.d(ASSERTION_TAG, "Assert that the checkpoints' details are displayed correctly (titles, due dates, points possible, grades).")
        assignmentListPage.assertDiscussionCheckpointDetails(2, "No due date", gradeReplyToTopic = "-/10", gradeAdditionalReplies = "-/5")

        Log.d(STEP_TAG, "Click on the expand icon for the '$discussionWithCheckpointsWithoutDueDatesTitle' discussion (to see the checkpoints' details).")
        assignmentListPage.clickDiscussionCheckpointExpandCollapseIcon(discussionWithCheckpointsWithoutDueDatesTitle)
        sleep(2000) // Allow some time for the collapse action to propagate

        Log.d(STEP_TAG, "Click on the expand icon for the '$discussionWithCheckpointsWithDueDatesTitle' discussion (to see the checkpoints' details).")
        assignmentListPage.clickDiscussionCheckpointExpandCollapseIcon(discussionWithCheckpointsWithDueDatesTitle)

        Log.d(ASSERTION_TAG, "Assert that the checkpoints' details are displayed correctly (titles, due dates, points possible, grades).")
        assignmentListPage.assertDiscussionCheckpointDetails(2, dueAtReplyToTopic = "Due " + convertIso8601ToCanvasFormat(replyToTopicDueDate) + " 11:59 PM", dueAtAdditionalReplies = "Due " + convertIso8601ToCanvasFormat(replyToEntryDueDate) + " 11:59 PM", gradeReplyToTopic = "-/10", gradeAdditionalReplies = "-/5")

        Log.d(STEP_TAG, "Select '${discussionWithCheckpointsWithoutDueDatesTitle}' discussion.")
        assignmentListPage.clickAssignment(discussionWithCheckpointsWithoutDueDatesTitle)

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed properly with the correct toolbar title and subtitle.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that the checkpoints are displayed properly on the Assignment Details Page.")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Reply to topic due","No Due Date")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Additional replies (2) due","No Due Date")
    }

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DISCUSSIONS, TestCategory.E2E, SecondaryFeatureCategory.DISCUSSION_CHECKPOINTS)
    fun testDiscussionCheckpointsSyllabusE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, syllabusBody = "this is the syllabus body") // This course and syllabus will be used once the seeding will be fixed
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Enroll '${student.name}' student to the dedicated course (${course.name}) with '$course.id' id.")
        EnrollmentsApi.enrollUser(course.id, student.id, STUDENT_ENROLLMENT)

        Log.d(PREPARATION_TAG, "Seed a discussion topic with checkpoints for '${course.name}' course.")
        val discussionWithCheckpointsTitle = "Test Discussion with Checkpoints"
        val replyToTopicDueDate = "2029-11-12T22:59:00Z"
        val replyToEntryDueDate = "2029-11-19T22:59:00Z"
        val assignmentName = "Test Assignment with Checkpoints"
        DiscussionTopicsApi.createDiscussionTopicWithCheckpoints(course.id, teacher.token, discussionWithCheckpointsTitle, assignmentName, replyToTopicDueDate, replyToEntryDueDate)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Select course: '${course.name}'.")
        dashboardPage.waitForRender()
        dashboardPage.selectCourse(course.name)

        Log.d(ASSERTION_TAG, "Assert that the 'Syllabus' Tab is displayed on the CourseBrowser Page.")
        courseBrowserPage.assertTabDisplayed("Syllabus")

        Log.d(STEP_TAG, "Navigate to Syllabus Page and the 'Summary' tab within it.")
        courseBrowserPage.selectSyllabus()
        syllabusPage.selectSummaryTab()

        Log.d(ASSERTION_TAG, "Assert that all the discussions with and all their checkpoints are displayed as a separate assignment.")
        syllabusPage.assertItemDisplayed("$discussionWithCheckpointsTitle Reply to Topic")
        syllabusPage.assertItemDisplayed("$discussionWithCheckpointsTitle Required Replies (2)")

        Log.d(STEP_TAG, "Select '$discussionWithCheckpointsTitle Reply to Topic' syllabus summary event.")
        syllabusPage.selectSummaryEvent("$discussionWithCheckpointsTitle Reply to Topic")

        Log.d(ASSERTION_TAG, "Assert that the Assignment Details Page is displayed properly with the correct toolbar title and subtitle.")
        assignmentDetailsPage.assertPageObjects()
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertDisplayToolbarSubtitle(course.name)

        Log.d(ASSERTION_TAG, "Assert that there is a separate view (box) for the checkpoints with their corresponding grades.")
        assignmentDetailsPage.assertCheckpointGradesView("Reply to topic", "-/10")
        assignmentDetailsPage.assertCheckpointGradesView("Additional replies (2)", "-/5")

        Log.d(ASSERTION_TAG, "Assert that the checkpoints are displayed properly on the Assignment Details Page.")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Reply to topic due", convertIso8601ToCanvasFormat(replyToTopicDueDate) + " 11:59 PM")
        assignmentDetailsPage.assertDiscussionCheckpointDetailsOnDetailsPage("Additional replies (2) due",convertIso8601ToCanvasFormat(replyToEntryDueDate) + " 11:59 PM")
    }
}
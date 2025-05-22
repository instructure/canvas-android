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

import android.os.SystemClock.sleep
import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.espresso.ViewUtils
import com.instructure.espresso.getDateInCanvasFormat
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DiscussionsE2ETest: StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DISCUSSIONS, TestCategory.E2E)
    @Stub("There is a known issue with the API on beta, so this would always fail. Remove stubbing when VICE-4849 is done.")
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
        ViewUtils.pressBackButton(3)

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

        Log.d(STEP_TAG, "Create a new discusson then close it.")
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

}
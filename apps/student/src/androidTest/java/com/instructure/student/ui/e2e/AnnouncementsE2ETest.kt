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

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.refresh
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

@HiltAndroidTest
class AnnouncementsE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E)
    fun testAnnouncementsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]
        val replyMessage = "Reply text"

        val announcement = data.announcementsList[0]
        Log.d(PREPARATION_TAG, "Seed an announcement for '${course.name}' course and a (message) entry for this announcement with the '$replyMessage' as a student.")
        DiscussionTopicsApi.createEntryToDiscussionTopic(student.token, course.id, announcement.id, replyMessage)

        Log.d(PREPARATION_TAG, "Seed another announcement for '${course.name}' which is locked so replies not allowed for it.")
        val lockedAnnouncement = DiscussionTopicsApi.createAnnouncement(course.id, teacher.token, locked = true)

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' course is displayed on the Dashboard Page.")
        dashboardPage.assertDisplaysCourse(course)

        Log.d(STEP_TAG, "Navigate to '${course.name}' course's announcements page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAnnouncements()

        Log.d(ASSERTION_TAG, "Assert that '${announcement.title}' announcement is displayed.")
        announcementListPage.assertTopicDisplayed(announcement.title)

        Log.d(ASSERTION_TAG, "Assert that '${lockedAnnouncement.title}' announcement is really locked so that the 'locked' icon is displayed.")
        announcementListPage.assertAnnouncementLocked(lockedAnnouncement.title)

        Log.d(STEP_TAG, "Select '${lockedAnnouncement.title}' announcement.")
        announcementListPage.selectTopic(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert that the '${lockedAnnouncement.title}' announcement's title is displayed on the toolbar.")
        discussionDetailsPage.assertToolbarDiscussionTitle(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert that the 'Reply' button is not available on a locked announcement.")
        discussionDetailsPage.assertReplyButtonNotDisplayed()

        Log.d(STEP_TAG, "Navigate back to Announcement List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Select '${announcement.title}' announcement.")
        announcementListPage.selectTopic(announcement.title)

        Log.d(ASSERTION_TAG, "Assert that the '${announcement.title}' announcement's title is displayed on the toolbar and the 'Reply' button is displayed as this announcement is not locked.")
        discussionDetailsPage.assertToolbarDiscussionTitle(announcement.title)
        discussionDetailsPage.waitForReplyButton()
        discussionDetailsPage.assertReplyButtonDisplayed()

        Log.d(ASSERTION_TAG, "Assert the the previously seeded reply, '$replyMessage', is displayed on the (online) details page.")
        discussionDetailsPage.assertEntryDisplayed(replyMessage)

        Log.d(STEP_TAG, "Click on Search button and type '${announcement.title}' to the search input field.")
        Espresso.pressBack()
        announcementListPage.searchable.clickOnSearchButton()
        announcementListPage.searchable.typeToSearchBar(announcement.title)

        Log.d(ASSERTION_TAG, "Assert that only the matching announcement is displayed on the Announcement List Page.")
        announcementListPage.pullToUpdate()
        announcementListPage.assertTopicDisplayed(announcement.title)
        announcementListPage.assertTopicNotDisplayed(lockedAnnouncement.title)

        Log.d(STEP_TAG, "Clear search input field value.")
        announcementListPage.searchable.clickOnClearSearchButton()
        announcementListPage.waitForDiscussionTopicToDisplay(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert if all the announcements are displayed again on the Announcement List Page.")
        announcementListPage.assertTopicDisplayed(announcement.title)

        Log.d(STEP_TAG, "Type a search value to the search input field which does not much with any of the existing announcements.")
        announcementListPage.searchable.typeToSearchBar("Non existing announcement title")

        sleep(3000) //We need this wait here to let make sure the search process has finished.

        Log.d(ASSERTION_TAG, "Assert that the empty view is displayed and none of the announcements are appearing on the page.")
        announcementListPage.assertEmpty()
        announcementListPage.assertTopicNotDisplayed(announcement.title)
        announcementListPage.assertTopicNotDisplayed(lockedAnnouncement.title)

        Log.d(STEP_TAG, "Clear search input field value.")
        announcementListPage.searchable.clickOnClearSearchButton()
        announcementListPage.waitForDiscussionTopicToDisplay(lockedAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert if all the announcements are displayed again on the Announcement List Page.")
        announcementListPage.assertTopicDisplayed(announcement.title)

        Log.d(ASSERTION_TAG, "Refresh the page and assert that after refresh, still all the announcements are displayed.")
        refresh()
        announcementListPage.assertTopicDisplayed(announcement.title)
        announcementListPage.assertTopicDisplayed(lockedAnnouncement.title)
      }
}
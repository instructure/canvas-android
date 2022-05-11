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
import com.instructure.canvas.espresso.refresh
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.lang.Thread.sleep

private const val STEP_TAG = "AnnouncementsE2ETest #STEP# "
private const val PREPARATION_TAG = "AnnouncementsE2ETest #PREPARATION# "

@HiltAndroidTest
class AnnouncementsE2ETest : StudentTest() {
    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() {
        //We don't want to see accessibility errors on E2E tests
    }

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.E2E)
    fun testAnnouncementsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, announcements = 2, locked = true)
        val student = data.studentsList[0]
        val course = data.coursesList[0]
        val announcement = data.announcementsList[0]
        val secondAnnouncement = data.announcementsList[1]

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId} , password: ${student.password}")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Navigate to ${course.name} course's announcements page.")
        dashboardPage.assertDisplaysCourse(course)
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAnnouncements()

        Log.d(STEP_TAG,"Assert that ${announcement.title} announcement is displayed.")
        discussionListPage.assertTopicDisplayed(announcement.title)

        Log.d(STEP_TAG,"Select ${announcement.title} announcement and assert if we are landing on the Discussion Details Page.")
        discussionListPage.selectTopic(announcement.title)
        discussionDetailsPage.assertTitleText(announcement.title)

        val replyMessage = "Reply text"
        Log.d(STEP_TAG,"Send a reply to the selected announcement with message: $replyMessage.")
        discussionDetailsPage.sendReply(replyMessage)
        discussionDetailsPage.assertPageObjects()

        Log.d(STEP_TAG,"Assert that the previously sent reply has been displayed.")
        val announcementReply  = DiscussionEntry(message = replyMessage)
        discussionDetailsPage.assertIfThereIsAReply()
        discussionDetailsPage.assertReplyDisplayed(announcementReply)

        Log.d(STEP_TAG,"Click on Search button and type ${announcement.title} to the search input field.")
        Espresso.pressBack()
        discussionListPage.clickOnSearchButton()
        discussionListPage.typeToSearchBar(announcement.title)

        Log.d(STEP_TAG,"Assert that only the matching announcement is displayed on the Discussion List Page.")
        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(announcement.title)
        discussionListPage.assertTopicNotDisplayed(secondAnnouncement.title)

        Log.d(STEP_TAG,"Clear search input field value and assert if all the announcements are displayed again on the Discussion List Page.")
        discussionListPage.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(secondAnnouncement.title)
        discussionListPage.assertTopicDisplayed(announcement.title)

        Log.d(STEP_TAG,"Type a search value to the search input field which does not much with any of the existing announcements.")
        discussionListPage.typeToSearchBar("Non existing announcement title")
        sleep(3000) //We need this wait here to let make sure the search process has finished.

        Log.d(STEP_TAG,"Assert that the empty view is displayed and none of the announcements are appearing on the page.")
        discussionListPage.assertEmpty()
        discussionListPage.assertTopicNotDisplayed(announcement.title)
        discussionListPage.assertTopicNotDisplayed(secondAnnouncement.title)

        Log.d(STEP_TAG,"Clear search input field value and assert if all the announcements are displayed again on the Discussion List Page.")
        discussionListPage.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(secondAnnouncement.title)
        discussionListPage.assertTopicDisplayed(announcement.title)

        Log.d(STEP_TAG,"Refresh the page and assert that after refresh, still all the announcements are displayed.")
        refresh()
        discussionListPage.assertTopicDisplayed(announcement.title)
        discussionListPage.assertTopicDisplayed(secondAnnouncement.title)
      }
}
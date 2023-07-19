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
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.model.CanvasUserApiModel
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.espresso.ViewUtils
import com.instructure.espresso.getCurrentDateInCanvasFormat
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
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
    fun testDiscussionsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seed a discussion topic.")
        val topic1 = createDiscussion(course, teacher)

        Log.d(PREPARATION_TAG,"Seed another discussion topic.")
        val topic2 = createDiscussion(course, teacher)

        Log.d(STEP_TAG,"Seed an announcement for ${course.name} course.")
        val announcement = createAnnouncement(course, teacher)

        Log.d(STEP_TAG,"Seed another announcement for ${course.name} course.")
        val announcement2 = createAnnouncement(course, teacher)

        Log.d(STEP_TAG,"Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG,"Select course: ${course.name}.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Verify that the Discussions and Announcements Tabs are both displayed on the CourseBrowser Page.")
        courseBrowserPage.assertTabDisplayed("Announcements")
        courseBrowserPage.assertTabDisplayed("Discussions")

        Log.d(STEP_TAG,"Navigate to Announcements Page. Assert that both ${announcement.title} and ${announcement2.title} announcements are displayed.")
        courseBrowserPage.selectAnnouncements()
        discussionListPage.assertTopicDisplayed(announcement.title)
        discussionListPage.assertTopicDisplayed(announcement2.title)

        Log.d(STEP_TAG,"Select ${announcement.title} announcement and assert if the details page is displayed.")
        discussionListPage.selectTopic(announcement.title)
        discussionDetailsPage.assertTitleText(announcement.title)

        Log.d(STEP_TAG,"Navigate back.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Click on the 'Search' button and search for ${announcement2.title}. announcement.")
        discussionListPage.clickOnSearchButton()
        discussionListPage.typeToSearchBar(announcement2.title)

        Log.d(STEP_TAG,"Refresh the page. Assert that the searching method is working well, so ${announcement.title} won't be displayed and ${announcement2.title} is displayed.")
        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(announcement2.title)
        discussionListPage.assertTopicNotDisplayed(announcement.title)

        Log.d(STEP_TAG,"Clear the search input field and assert that both announcements, ${announcement.title} and ${announcement2.title} has been diplayed.")
        discussionListPage.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(announcement.title)
        discussionListPage.assertTopicDisplayed(announcement2.title)

        Log.d(STEP_TAG,"Navigate back to CourseBrowser Page.")
        ViewUtils.pressBackButton(3)

        Log.d(STEP_TAG,"Navigate to Discussions Page.")
        courseBrowserPage.selectDiscussions()

        Log.d(STEP_TAG,"Select ${topic1.title} discussion and assert if the details page is displayed and there is no reply for the discussion yet.")
        discussionListPage.assertTopicDisplayed(topic1.title)
        discussionListPage.selectTopic(topic1.title)
        discussionDetailsPage.assertTitleText(topic1.title)
        discussionDetailsPage.assertNoRepliesDisplayed()

        Log.d(STEP_TAG,"Navigate back to Discussions Page.")
        Espresso.pressBack() // Back to discussion list

        Log.d(STEP_TAG,"Select ${topic1.title} discussion and assert if the details page is displayed and there is no reply for the discussion yet.")
        discussionListPage.assertTopicDisplayed(topic2.title)
        discussionListPage.selectTopic(topic2.title)
        discussionDetailsPage.assertTitleText(topic2.title)
        discussionDetailsPage.assertNoRepliesDisplayed()

        Log.d(STEP_TAG,"Navigate back to Discussions Page.")
        Espresso.pressBack()

        val newTopicName = "Do we really need discussions?"
        val newTopicDescription = "Let's discuss"
        Log.d(STEP_TAG,"Create a new discussion topic with '$newTopicName' topic name and '$newTopicDescription' topic description.")
        discussionListPage.createDiscussionTopic(newTopicName, newTopicDescription)
        sleep(2000) // Allow some time for creation to propagate

        Log.d(STEP_TAG,"Assert that $newTopicName topic has been created successfully with 0 reply count.")
        discussionListPage.assertTopicDisplayed(newTopicName)
        discussionListPage.assertReplyCount(newTopicName, 0)

        Log.d(STEP_TAG,"Select $newTopicName topic and assert that there is no reply on the details page as well.")
        discussionListPage.selectTopic(newTopicName)
        discussionDetailsPage.assertNoRepliesDisplayed()

        val replyMessage = "My reply"
        Log.d(STEP_TAG,"Send a reply with text: '$replyMessage'.")
        discussionDetailsPage.sendReply(replyMessage)
        sleep(2000) // Allow some time for reply to propagate

        Log.d(STEP_TAG,"Assert the the previously sent reply ($replyMessage) is displayed on the details page.")
        discussionDetailsPage.assertRepliesDisplayed()

        Log.d(STEP_TAG,"Navigate back to Discussions Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously sent reply has been counted, and there are no unread replies.")
        discussionListPage.pullToUpdate()
        discussionListPage.assertReplyCount(newTopicName, 1)
        discussionListPage.assertUnreadReplyCount(newTopicName, 0)

        Log.d(STEP_TAG, "Assert that the due date is the current date (in the expected format).")
        val currentDate = getCurrentDateInCanvasFormat()
        discussionListPage.assertDueDate(newTopicName, currentDate)

    }

    private fun createAnnouncement(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ) = DiscussionTopicsApi.createAnnouncement(
        courseId = course.id,
        token = teacher.token
    )

    private fun createDiscussion(
        course: CourseApiModel,
        teacher: CanvasUserApiModel
    ) = DiscussionTopicsApi.createDiscussion(
        courseId = course.id,
        token = teacher.token
    )
}
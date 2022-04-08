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
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.DiscussionTopicsApi
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
    override fun displaysPageObjects() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // Includes test logic for Announcements
    @E2E
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.DISCUSSIONS, TestCategory.E2E, false)
    fun testDiscussionsE2E() {

        // Seed basic data
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        // Seed some discussion topics and an announcement
        val topic1 = DiscussionTopicsApi.createDiscussion(
                courseId = course.id,
                token = teacher.token
        )

        val topic2 = DiscussionTopicsApi.createDiscussion(
            courseId = course.id,
            token = teacher.token
        )

        val announcement = DiscussionTopicsApi.createAnnouncement(
            courseId = course.id,
            token = teacher.token
        )

        val announcement2 = DiscussionTopicsApi.createAnnouncement(
            courseId = course.id,
            token = teacher.token
        )

        // Sign in our student
        tokenLogin(student)
        dashboardPage.waitForRender()

        // Select the class on the dashboard
        dashboardPage.selectCourse(course)

        // Verify that the "Discussions" and "Announcements" tabs are both displayed in course browser
        courseBrowserPage.assertTabDisplayed("Announcements")
        courseBrowserPage.assertTabDisplayed("Discussions")

        // Drill into seeded announcement, verifying what we can
        // Note that DiscussionListPage, DiscussionDetailsPage are reused for announcements
        courseBrowserPage.selectAnnouncements()
        discussionListPage.assertTopicDisplayed(announcement.title)
        discussionListPage.assertTopicDisplayed(announcement2.title)
        discussionListPage.selectTopic(announcement.title)
        discussionDetailsPage.assertTitleText(announcement.title)
        Espresso.pressBack()

        //Search for an announcement and check if the search works.
        //Also, checking that not matching announcement is disappearing after typing a string into the search field.
        discussionListPage.clickOnSearchButton()
        discussionListPage.typeToSearchBar(announcement2.title)

        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(announcement2.title)
        discussionListPage.assertTopicNotDisplayed(announcement.title)

        discussionListPage.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(announcement.title)
        discussionListPage.assertTopicDisplayed(announcement2.title)

        Espresso.pressBack() // Click away from Search input
        Espresso.pressBack() // Back to announcement list
        Espresso.pressBack() // Back to course browser page

        // Drill into the seeded discussions, verifying what we can
        courseBrowserPage.selectDiscussions()
        discussionListPage.assertTopicDisplayed(topic1.title)
        discussionListPage.selectTopic(topic1.title)
        discussionDetailsPage.assertTitleText(topic1.title)
        discussionDetailsPage.assertNoRepliesDisplayed()
        Espresso.pressBack() // Back to discussion list
        discussionListPage.assertTopicDisplayed(topic2.title)
        discussionListPage.selectTopic(topic2.title)
        discussionDetailsPage.assertTitleText(topic2.title)
        discussionDetailsPage.assertNoRepliesDisplayed()
        Espresso.pressBack() // Back to discussion list

        // Now let's try creating a discussion topic & replying to a discussion via the UI
        val newTopicName = "Do we really need discussions?"
        val newTopicDescription = "Let's discuss"
        discussionListPage.createDiscussionTopic(newTopicName, newTopicDescription)
        sleep(2000) // Allow some time for creation to propagate
        discussionListPage.assertTopicDisplayed(newTopicName)
        discussionListPage.assertReplyCount(newTopicName, 0)
        discussionListPage.selectTopic(newTopicName)
        discussionDetailsPage.assertNoRepliesDisplayed()
        discussionDetailsPage.sendReply("My reply")
        sleep(2000) // Allow some time for reply to propagate
        discussionDetailsPage.assertRepliesDisplayed()
        Espresso.pressBack() // Back to discussion list
        discussionListPage.pullToUpdate()
        discussionListPage.assertReplyCount(newTopicName, 1)


    }
}
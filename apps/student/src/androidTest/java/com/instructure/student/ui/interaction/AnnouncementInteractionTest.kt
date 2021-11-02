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
 */
package com.instructure.student.ui.interaction

import androidx.test.espresso.Espresso
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.models.User
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AnnouncementInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course
    private lateinit var user: User

    // Student enrolled in intended section can see and reply to the announcement
    // (This kind of seems like more of a test of the mocked endpoint, but we'll go with it.)
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testAnnouncement_replyToSectionSpecificAnnouncement() {

        val data = getToCourse(createSections = true)
        val announcement = data.addDiscussionTopicToCourse(
                course = course,
                user = user,
                topicTitle = "Announcement Topic 1",
                topicDescription = "It's an announcement for a single section",
                isAnnouncement = true,
                sections = listOf(course.sections.get(0))
        )

        courseBrowserPage.selectAnnouncements()
        // Note that the announcement list page / announcement details page reuse
        // the discussion list page / discussion details page
        discussionListPage.assertTopicDisplayed(announcement.title!!)
        discussionListPage.selectTopic(announcement.title!!)
        discussionDetailsPage.assertTopicInfoShowing(announcement)
        discussionDetailsPage.sendReply("Will do!")
        //Find our DiscussionReply
        val reply = data.discussionTopics[announcement.id]?.views?.find {it.message == "Will do!"} !!
        discussionDetailsPage.assertReplyDisplayed(reply)

        // Just for fun, let's change the user to be enrolled in a section of the course to which
        // the announcement does not apply, and make sure that the user no longer sees the announcement.
        val enrollment = data.enrollments.values.find  {it.courseId == course.id && it.userId == user.id}!!
        val updatedEnrollment = enrollment.copy(courseSectionId = 1000000)
        data.enrollments[updatedEnrollment.id] = updatedEnrollment

        Espresso.pressBack() // Get to announcement list
        discussionListPage.pullToUpdate()
        discussionListPage.assertEmpty()

    }

    // User can preview an announcement attachment
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testAnnouncement_previewAttachment() {

        val data = getToCourse()
        val announcement = data.addDiscussionTopicToCourse(
                course = course,
                user = user,
                topicTitle = "Announcement Topic 2",
                topicDescription = "It's an announcement, with an attachment",
                isAnnouncement = true
        )

        // Lets attach an html attachment to the announcement
        val attachmentHtml =
                """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>

        <body>
        <h1 id="header1">Famous Quote</h1>
        <p id="p1">Et tu, Brute? -- Julius Caesar</p>
        </body>
        </html> """

        val attachment = DiscussionsInteractionTest.createHtmlAttachment(data, attachmentHtml)
        announcement.attachments = mutableListOf(attachment)

        // Now let's test
        courseBrowserPage.selectAnnouncements()
        discussionListPage.selectTopic(announcement.title!!)
        discussionDetailsPage.assertMainAttachmentDisplayed()
        discussionDetailsPage.previewAndCheckMainAttachment(
                WebViewTextCheck(Locator.ID, "p1", "Et tu, Brute?")
        )

    }

    // View/reply to an announcement
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testAnnouncement_reply() {

        val data = getToCourse()
        val announcement = data.addDiscussionTopicToCourse(
                course = course,
                user = user,
                topicTitle = "Announcement Topic 3",
                topicDescription = "It's an announcement, not a discussion",
                isAnnouncement = true
        )

        courseBrowserPage.selectAnnouncements()
        discussionListPage.assertTopicDisplayed(announcement.title!!)
        discussionListPage.selectTopic(announcement.title!!)
        discussionDetailsPage.assertTopicInfoShowing(announcement)
        discussionDetailsPage.sendReply("Roger!")
        //Find our DiscussionReply
        val reply = data.discussionTopics[announcement.id]?.views?.find {it.message == "Roger!"} !!
        discussionDetailsPage.assertReplyDisplayed(reply)
    }

    // Tests that we can create an announcement (as teacher).
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testAnnouncementCreate_base() {
        val data = getToAnnouncementList()

        val course = data.courses.values.first()
        val announcement = data.courseDiscussionTopicHeaders[course.id]!!.first()
        discussionListPage.assertTopicDisplayed(announcement.title!!)
        discussionListPage.createAnnouncement("Announcement Topic", "Awesome announcement topic")
    }

    // Tests code around closing / aborting announcement creation
    @Test
    @TestMetaData(Priority.P1, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testAnnouncementCreate_abort() {
        val data = getToAnnouncementList()
        val course = data.courses.values.first()
        val announcement =
            data.courseDiscussionTopicHeaders[course.id]!!.filter { th -> th.announcement }.first()

        discussionListPage.assertHasAnnouncement(announcement)
        discussionListPage.assertAnnouncementCount(2) // header + the one test announcement
        discussionListPage.launchCreateAnnouncementThenClose()
        discussionListPage.verifyExitWithoutSavingDialog()
        discussionListPage.acceptExitWithoutSaveDialog()
        discussionListPage.assertHasAnnouncement(announcement)
        discussionListPage.assertAnnouncementCount(2) // header + the one test announcement
    }

    // Tests code around creating an announcement with no description
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testAnnouncementCreate_missingDescription() {
        getToAnnouncementList()

        discussionListPage.createAnnouncement("title", "", verify = false)
        // easier than looking for the "A description is required" toast message
        discussionListPage.assertOnNewAnnouncementPage()
    }

    // Tests code around creating an announcement with no title
    @Test
    @TestMetaData(Priority.P2, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testAnnouncementCreate_missingTitle() {
        getToAnnouncementList()
        discussionListPage.createAnnouncement("", "description")
    }

    @Test
    @TestMetaData(Priority.P1, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION, false)
    fun testSearchAnnouncement() {
        val data = getToAnnouncementList()
        val course = data.courses.values.first()
        val announcement = data.courseDiscussionTopicHeaders[course.id]!!.first()
        val testAnnouncementName = "searchTestAnnouncement"
        val existingAnnouncementName = announcement.title

        discussionListPage.createAnnouncement(testAnnouncementName, "description")

        discussionListPage.clickOnSearchButton()
        discussionListPage.typeToSearchBar(testAnnouncementName)

        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(testAnnouncementName)
        discussionListPage.assertTopicNotDisplayed(existingAnnouncementName)

        discussionListPage.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(existingAnnouncementName!!)
        discussionListPage.assertTopicDisplayed(testAnnouncementName)
    }

    // Mock a specified number of students and courses, and navigate to the first course
    private fun getToCourse(
            studentCount: Int = 1,
            courseCount: Int = 1,
            createSections: Boolean = false
    ): MockCanvas {
        val data = MockCanvas.init(
                studentCount = studentCount,
                courseCount = courseCount,
                favoriteCourseCount = courseCount,
                createSections = createSections)

        course = data.courses.values.first()
        user = data.students[0]

        val announcementsTab = Tab(position = 2, label = "Announcements", visibility = "public", tabId = Tab.ANNOUNCEMENTS_ID)
        data.courseTabs[course.id]!! += announcementsTab

        val token = data.tokenFor(user)!!
        tokenLogin(data.domain, token, user)
        dashboardPage.waitForRender()

        dashboardPage.selectCourse(course)

        return data
    }

    // Mock a student/teacher/course/announcement, than navigate to the announcements list
    private fun getToAnnouncementList() : MockCanvas {
        val data = MockCanvas.init(teacherCount = 1, studentCount = 1, courseCount = 1, favoriteCourseCount = 1)

        val teacher = data.teachers[0]
        val course = data.courses.values.first()

        val announcementsTab = Tab(position = 2, label = "Announcements", visibility = "public", tabId = Tab.ANNOUNCEMENTS_ID)
        data.courseTabs[course.id]!! += announcementsTab

        data.addCoursePermissions(
                course.id,
                CanvasContextPermission(canCreateAnnouncement = true)
        )

        data.addDiscussionTopicToCourse(
                course = course,
                user = teacher,
                isAnnouncement = true
        )

        val token = data.tokenFor(teacher)!!
        tokenLogin(data.domain, token, teacher)

        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAnnouncements()

        return data
    }
}
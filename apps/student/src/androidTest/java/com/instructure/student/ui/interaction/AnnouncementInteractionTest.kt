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
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addCoursePermissions
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addGroupToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.CanvasContextPermission
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.models.User
import com.instructure.student.ui.pages.classic.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class AnnouncementInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course
    private lateinit var user: User

    private lateinit var group : Group
    private lateinit var discussion : DiscussionTopicHeader
    private lateinit var announcement : DiscussionTopicHeader

    // Student enrolled in intended section can see and reply to the announcement
    // (This kind of seems like more of a test of the mocked endpoint, but we'll go with it.)
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    @Stub("This can only test the old discussions, will be modified later to test the old discussions in offline mode")
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
        nativeDiscussionDetailsPage.assertTopicInfoShowing(announcement)
        nativeDiscussionDetailsPage.sendReply("Will do!")
        //Find our DiscussionReply
        val reply = data.discussionTopics[announcement.id]?.views?.find {it.message == "Will do!"} !!
        nativeDiscussionDetailsPage.assertReplyDisplayed(reply)

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
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    @Stub("This can only test the old discussions, will be modified later to test the old discussions in offline mode")
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
        nativeDiscussionDetailsPage.assertMainAttachmentDisplayed()
        nativeDiscussionDetailsPage.previewAndCheckMainAttachment(
                WebViewTextCheck(Locator.ID, "p1", "Et tu, Brute?")
        )

    }

    // View/reply to an announcement
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    @Stub("This can only test the old discussions, will be modified later to test the old discussions in offline mode")
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
        nativeDiscussionDetailsPage.assertTopicInfoShowing(announcement)
        nativeDiscussionDetailsPage.sendReply("Roger!")
        //Find our DiscussionReply
        val reply = data.discussionTopics[announcement.id]?.views?.find {it.message == "Roger!"} !!
        nativeDiscussionDetailsPage.assertReplyDisplayed(reply)
    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ANNOUNCEMENTS, TestCategory.INTERACTION)
    fun testSearchAnnouncement() {
        val data = getToAnnouncementList()
        val course = data.courses.values.first()
        val student = data.students.first()
        val announcement = data.courseDiscussionTopicHeaders[course.id]!!.first()
        val testAnnouncementName = "searchTestAnnouncement"
        val testAnnouncementDescription = "description"
        val existingAnnouncementName = announcement.title

        data.addDiscussionTopicToCourse(
            course = course,
            user = student,
            topicTitle = testAnnouncementName,
            topicDescription = testAnnouncementDescription,
            isAnnouncement = true,
        )
        discussionListPage.pullToUpdate()

        discussionListPage.searchable.clickOnSearchButton()
        discussionListPage.searchable.typeToSearchBar(testAnnouncementName)

        discussionListPage.pullToUpdate()
        discussionListPage.assertTopicDisplayed(testAnnouncementName)
        discussionListPage.assertTopicNotDisplayed(existingAnnouncementName)

        discussionListPage.searchable.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(existingAnnouncementName!!)
        discussionListPage.assertTopicDisplayed(testAnnouncementName)
    }

    // Mock a specified number of students and courses, and navigate to the first course
    private fun getToCourse(
            studentCount: Int = 1,
            courseCount: Int = 1,
            createSections: Boolean = false
    ): MockCanvas {

        val data = initData(studentCount,courseCount,createSections)

        val token = data.tokenFor(user)!!
        tokenLogin(data.domain, token, user)
        dashboardPage.waitForRender()

        dashboardPage.selectCourse(course)

        return data
    }

    private fun getToGroup(
        studentCount: Int = 1,
        courseCount: Int = 1,
        createSections: Boolean = false
    ): MockCanvas {

        val data = initData(studentCount,courseCount,createSections)

        val token = data.tokenFor(user)!!
        tokenLogin(data.domain, token, user)
        dashboardPage.waitForRender()

        dashboardPage.selectGroup(group)

        return data
    }

    private fun initData( studentCount: Int = 1,
                          courseCount: Int = 1,
                          createSections: Boolean = false): MockCanvas {
        val data = MockCanvas.init(
            studentCount = studentCount,
            courseCount = courseCount,
            favoriteCourseCount = courseCount,
            createSections = createSections)

        course = data.courses.values.first()
        user = data.students[0]

        // Add a group
        val user = data.users.values.first()
        group = data.addGroupToCourse(
            course = course,
            members = listOf(user),
            isFavorite = true
        )

        // Add a discussion
        discussion = data.addDiscussionTopicToCourse(
            course = course,
            user = user,
            groupId = group.id
        )

        // Add an announcement
        announcement = data.addDiscussionTopicToCourse(
            course = course,
            user = user,
            groupId = group.id,
            isAnnouncement = true
        )

        val announcementsTab = Tab(position = 2, label = "Announcements", visibility = "public", tabId = Tab.ANNOUNCEMENTS_ID)
        data.courseTabs[course.id]!! += announcementsTab

        data.groupTabs[group.id] = mutableListOf(
            Tab(position = 0, label = "Discussions", tabId = Tab.DISCUSSIONS_ID, visibility = "public"),
            Tab(position = 1, label = "Announcements", tabId = Tab.ANNOUNCEMENTS_ID, visibility = "public"),
        )

        MockCanvas.data.addCoursePermissions(
            course.id,
            CanvasContextPermission(canCreateAnnouncement = true)
        )
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
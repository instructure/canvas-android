/*
 * Copyright (C) 2019 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.ui.interaction

import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockCanvas.addFileToCourse
import com.instructure.canvas.espresso.mockCanvas.addGroupToCourse
import com.instructure.canvas.espresso.mockCanvas.addPageToCourse
import com.instructure.canvas.espresso.mockCanvas.addQuizToCourse
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.junit.Test

class GroupLinksInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var group : Group
    private lateinit var course : Course
    private lateinit var discussion : DiscussionTopicHeader
    private lateinit var announcement : DiscussionTopicHeader
    private lateinit var page: Page
    private val pageBody = "<h1 id=\"header1\">Page body</h1>"
    private var fileId: Long = -1
    private val fileBody = "<h1 id=\"header1\">File body</h1>"
    private var fileDisplayName = "GroupFile.html"

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true)
    fun testGroupLink_base() {
        // Link to group opens group browser - eg: "/groups/:id"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.assertTitleCorrect(group)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.DASHBOARD)
    fun testGroupLink_dashboard() {
        // Link to groups opens dashboard - eg: "/groups"
        setUpGroupAndSignIn()
        dashboardPage.assertDisplaysGroup(group, course)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.FILES)
    fun testGroupLink_filePreview() {
        // Link to file preview opens file - eg: "/groups/:id/files/folder/:id?preview=:id"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectFiles()
        fileListPage.selectItem(fileDisplayName)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "header1", "File body")
        )
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.ANNOUNCEMENTS)
    fun testGroupLink_announcement() {
        // Link to group announcement opens announcement - eg: "/groups/:id/discussion_topics/:id"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectAnnouncements()
        discussionListPage.selectTopic(announcement.title!!)
        discussionDetailsPage.assertTopicInfoShowing(announcement)

    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.ANNOUNCEMENTS)
    fun testGroupLink_announcementList() {
        // Link to group announcements list opens announcements - eg: "/groups/:id/announcements"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectAnnouncements()
        discussionListPage.assertTopicDisplayed(announcement.title!!)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.DISCUSSIONS)
    fun testGroupLink_discussion() {
        // Link to group discussion opens discussion - eg: "/groups/:id/discussion_topics/:id"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectDiscussions()
        discussionListPage.selectTopic(discussion.title!!)
        discussionDetailsPage.assertTopicInfoShowing(discussion)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.DISCUSSIONS)
    fun testGroupLink_discussionList() {
        // Link to group discussion list opens list - eg: "/groups/:id/discussion_topics"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectDiscussions()
        discussionListPage.assertTopicDisplayed(discussion.title!!)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.FILES)
    fun testGroupLink_files() {
        // Link to group files list opens group files list - eg: "/groups/:id/files"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectFiles()
        fileListPage.assertItemDisplayed(fileDisplayName)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.FILES)
    fun testGroupLink_fileFolder() {
        // Link to group files folder opens folder - eg: "/groups/:id/files/folder/:id/"
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.PAGES)
    fun testGroupLink_pagesList() {
        // Link to group page list opens pages - eg: "/groups/:id/pages"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectPages()
        pageListPage.assertRegularPageDisplayed(page)
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.PAGES)
    fun testGroupLink_Page() {
        // Link to group page opens page - eg: "/groups/:id/pages/:id"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectPages()
        pageListPage.selectRegularPage(page)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "header1", "Page body")
        )
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.GROUPS, TestCategory.INTERACTION, true, FeatureCategory.PEOPLE)
    fun testGroupLink_people() {
        // Link to group people list opens list - eg: "/groups/:id/users"
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectPeople() // Why does this call https://mock-data.instructure.com/api/v1/courses/1/users?include[]=enrollments&include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio&enrollment_type=teacher&per_page=100

        for(user in group.users) {
            peopleListPage.assertPersonListed(user)
        }
    }

    // Mock a specified number of students and courses, sign in, then navigate to course browser page for
    // first course.
    private fun setUpGroupAndSignIn(
            studentCount: Int = 1,
            courseCount: Int = 1
    ): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
                studentCount = studentCount,
                courseCount = courseCount,
                favoriteCourseCount = courseCount)
        course = data.courses.values.first()
        val user = data.users.values.first()
        group = data.addGroupToCourse(
                course = course,
                members = listOf(user)
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

        // Add a page
        page = data.addPageToCourse(
                pageId = data.newItemId(),
                courseId = course.id,
                published = true,
                groupId = group.id,
                body = pageBody
        )

        // Add a file
        fileId = data.addFileToCourse(
                courseId = course.id,
                displayName = fileDisplayName,
                fileContent = fileBody,
                contentType = "text/html",
                groupId = group.id
        )

        // Make sure that we have group tabs
        data.groupTabs[group.id] = mutableListOf(
                Tab(position = 0, label = "Discussions", tabId = Tab.DISCUSSIONS_ID, visibility = "public"),
                Tab(position = 1, label = "Announcements", tabId = Tab.ANNOUNCEMENTS_ID, visibility = "public"),
                Tab(position = 2, label = "People", tabId = Tab.PEOPLE_ID, visibility = "public"),
                Tab(position = 3, label = "Pages", tabId = Tab.PAGES_ID, visibility = "public"),
                Tab(position = 4, label = "Files", tabId = Tab.FILES_ID, visibility = "public")
        )


        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        return data
    }
}

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

import android.os.Build
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockcanvas.addFileToFolder
import com.instructure.canvas.espresso.mockcanvas.addFolderToCourse
import com.instructure.canvas.espresso.mockcanvas.addGroupToCourse
import com.instructure.canvas.espresso.mockcanvas.addPageToCourse
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvas.espresso.refresh
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Group
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.Tab
import com.instructure.student.ui.pages.classic.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class GroupLinksInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var group : Group
    private lateinit var course : Course
    private lateinit var discussion : DiscussionTopicHeader
    private lateinit var announcement : DiscussionTopicHeader
    private lateinit var page: Page
    private val pageBody = "<h1 id=\"header1\">Page body</h1>"
    private val fileBody = "<h1 id=\"header1\">File body</h1>"
    private var fileDisplayName = "GroupFile.html"
    private var groupFolderDisplayName = "Group Folder"

    // Link to group opens group browser - eg: "/groups/:id"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION)
    fun testGroupLink_base() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        groupBrowserPage.assertTitleCorrect(group)
    }

    // Link to groups opens dashboard - eg: "/groups"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_DASHBOARD)
    fun testGroupLink_dashboard() {
        setUpGroupAndSignIn()
        dashboardPage.assertDisplaysGroup(group, course)
    }

    // Test not favorite group on dashboard
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_DASHBOARD)
    fun testGroupLink_dashboard_favoriteLogics() {
        val data = setUpGroupAndSignIn()
        val user = data.users.values.first()
        val nonFavoriteGroup = data.addGroupToCourse(
            course = course,
            members = listOf(user),
            isFavorite = false
        )
        refresh() //Need to refresh because when we navigated to Dashboard page the nonFavoriteGroup was not existed yet. (However it won't be displayed because it's not favorite)
        dashboardPage.assertGroupNotDisplayed(nonFavoriteGroup)
        dashboardPage.assertDisplaysGroup(group, course)
    }

    // Test that if no groups has selected as favorite then we display all groups
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_DASHBOARD)
    fun testGroupLink_dashboard_not_selected_displays_all() {
        val data = setUpGroupAndSignIn(isFavorite = false)
        val user = data.users.values.first()
        val group2 = data.addGroupToCourse(
            course = course,
            members = listOf(user),
            isFavorite = false
        )
        refresh() //Need to refresh because when we navigated to Dashboard page the group2 was not existed yet.
        dashboardPage.assertDisplaysGroup(group, course)
        dashboardPage.assertDisplaysGroup(group2, course)
    }

    // Link to file preview opens file - eg: "/groups/:id/files/folder/:id?preview=:id"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_FILES)
    fun testGroupLink_filePreview() {

        // MBL-13499: This will cause an http request to our mock web server, and http requests from webviews are illegal
        // in SDK 28 and above.  So skip for SDK 28 and above.
        if(Build.VERSION.SDK_INT > 27) {
            return
        }

        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectFiles()
        fileListPage.selectItem(groupFolderDisplayName)
        fileListPage.selectItem(fileDisplayName)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "header1", "File body")
        )
    }

    // Link to group announcement opens announcement - eg: "/groups/:id/discussion_topics/:id"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_ANNOUNCEMENTS)
    fun testGroupLink_announcement() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectAnnouncements()
        discussionListPage.selectTopic(announcement.title!!)
        discussionDetailsPage.assertToolbarDiscussionTitle(announcement.title!!)

    }

    // Link to group announcements list opens announcements - eg: "/groups/:id/announcements"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_ANNOUNCEMENTS)
    fun testGroupLink_announcementList() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectAnnouncements()
        discussionListPage.assertTopicDisplayed(announcement.title!!)
    }

    // Link to group discussion opens discussion - eg: "/groups/:id/discussion_topics/:id"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_DISCUSSIONS)
    fun testGroupLink_discussion() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectDiscussions()
        discussionListPage.selectTopic(discussion.title!!)
        discussionDetailsPage.assertToolbarDiscussionTitle(discussion.title!!)
    }

    // Link to group discussion list opens list - eg: "/groups/:id/discussion_topics"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_DISCUSSIONS)
    fun testGroupLink_discussionList() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectDiscussions()
        discussionListPage.assertTopicDisplayed(discussion.title!!)
    }

    // Link to group files list opens group files list - eg: "/groups/:id/files"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_FILES)
    fun testGroupLink_files() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectFiles()
        fileListPage.assertItemDisplayed(groupFolderDisplayName)
    }

    // Link to group files folder opens folder - eg: "/groups/:id/files/folder/:id/"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_FILES)
    fun testGroupLink_fileFolder() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectFiles()
        fileListPage.assertItemDisplayed(groupFolderDisplayName)
        fileListPage.selectItem(groupFolderDisplayName)
        fileListPage.assertItemDisplayed(fileDisplayName)
    }

    // Link to group page list opens pages - eg: "/groups/:id/pages"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_PAGES)
    fun testGroupLink_pagesList() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectPages()
        pageListPage.assertRegularPageDisplayed(page)
    }

    // Link to group page opens page - eg: "/groups/:id/pages/:id"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_PAGES)
    fun testGroupLink_Page() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectPages()
        pageListPage.selectRegularPage(page)
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "header1", "Page body")
        )
    }

    // Link to group people list opens list - eg: "/groups/:id/users"
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.GROUPS, TestCategory.INTERACTION, SecondaryFeatureCategory.GROUPS_PEOPLE)
    fun testGroupLink_people() {
        setUpGroupAndSignIn()
        dashboardPage.selectGroup(group)
        courseBrowserPage.selectPeople() // Why does this call https://mock-data.instructure.com/api/v1/courses/1/users?include[]=enrollments&include[]=avatar_url&include[]=user_id&include[]=email&include[]=bio&enrollment_type=teacher&per_page=100

        for(user in group.users) {
            peopleListPage.assertPersonListed(user)
        }
    }

    // Mock a single student and course, mock a group and a number of items associated with the group,
    // sign in, then navigate to the dashboard.
    private fun setUpGroupAndSignIn(isFavorite: Boolean = true): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1,
                favoriteCourseCount = 1)

        course = data.courses.values.first()
        val user = data.users.values.first()

        // Add a group
        group = data.addGroupToCourse(
                course = course,
                members = listOf(user),
                isFavorite = isFavorite
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

        // Add a folder
        val folderId = data.addFolderToCourse(
                courseId = course.id,
                displayName = groupFolderDisplayName,
                groupId = group.id
        )

        // Add a file to the folder
        val fileId = data.addFileToFolder(
                folderId = folderId,
                displayName = fileDisplayName,
                fileContent = fileBody,
                contentType = "text.html"
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

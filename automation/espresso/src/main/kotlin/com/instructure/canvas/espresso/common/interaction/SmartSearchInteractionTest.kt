/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.canvas.espresso.common.interaction

import com.instructure.canvas.espresso.CanvasComposeTest
import com.instructure.canvas.espresso.common.pages.AssignmentDetailsPage
import com.instructure.canvas.espresso.common.pages.compose.SmartSearchPage
import com.instructure.canvas.espresso.common.pages.compose.SmartSearchPreferencesPage
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.addDiscussionTopicToCourse
import com.instructure.canvas.espresso.mockcanvas.addPageToCourse
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.models.Page
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.espresso.ModuleItemInteractions
import org.junit.Test
import kotlin.random.Random

abstract class SmartSearchInteractionTest : CanvasComposeTest() {

    private val smartSearchPage = SmartSearchPage(composeTestRule)
    private val smartSearchPreferencesPage = SmartSearchPreferencesPage(composeTestRule)
    private val assignmentDetailsPage = AssignmentDetailsPage(ModuleItemInteractions())

    @Test
    fun assertQuery() {
        val data = initData()

        goToSmartSearch(data, "queryTest")

        composeTestRule.waitForIdle()

        smartSearchPage.assertQuery("queryTest")
    }

    @Test
    fun assertCourseName() {
        val data = initData()
        val courseName = data.courses.values.first().name

        goToSmartSearch(data, "queryTest")

        composeTestRule.waitForIdle()

        smartSearchPage.assertCourse(courseName)
    }

    @Test
    fun assertAssignment() {
        val data = initData()
        val course = data.courses.values.first()
        val assignment = data.addAssignment(course.id, name = "Test Assignment for query")

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.assertItemDisplayed("Test Assignment for query", "Assignment")
        smartSearchPage.clickOnItem("Test Assignment for query")

        assignmentDetailsPage.assertAssignmentDetails(assignment)
    }

    @Test
    fun assertAnnouncement() {
        val data = initData()
        val course = data.courses.values.first()
        val announcement = data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Announcement for query",
            isAnnouncement = true
        )

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.assertItemDisplayed("Test Announcement for query", "Announcement")
        smartSearchPage.clickOnItem("Test Announcement for query")

        assertDiscussionPage(announcement)
    }

    @Test
    fun assertDiscussion() {
        val data = initData()
        val course = data.courses.values.first()
        val discussion = data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Discussion for query",
            isAnnouncement = false
        )

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")
        smartSearchPage.clickOnItem("Test Discussion for query")

        assertDiscussionPage(discussion)
    }

    @Test
    fun assertWikiPage() {
        val data = initData()
        val course = data.courses.values.first()
        val pageId = Random.nextLong(0, 100)
        val page = data.addPageToCourse(
            course.id,
            pageId,
            title = "Test Page for query",
            body = "Test Body for query",
            url = "https://mock-data.instructure.com/courses/${course.id}/pages/$pageId"
        )

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.assertItemDisplayed("Test Page for query", "Page")
        smartSearchPage.clickOnItem("Test Page for query")

        assertPage(page)
    }

    @Test
    fun assertFilters() {
        val data = initData()

        val course = data.courses.values.first()
        val pageId = Random.nextLong(0, 100)
        data.addPageToCourse(
            course.id,
            pageId,
            title = "Test Page for query",
            body = "Test Body for query",
            url = "https://mock-data.instructure.com/courses/${course.id}/pages/$pageId"
        )

        data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Discussion for query",
            isAnnouncement = false
        )

        data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Announcement for query",
            isAnnouncement = true
        )

        data.addAssignment(course.id, name = "Test Assignment for query")

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.assertItemDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")
        smartSearchPage.assertItemDisplayed("Test Announcement for query", "Announcement")
        smartSearchPage.assertItemDisplayed("Test Assignment for query", "Assignment")

        smartSearchPage.clickOnFilters()

        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.PAGES)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.DISCUSSION_TOPICS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.ANNOUNCEMENTS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.ASSIGNMENTS)

        smartSearchPreferencesPage.clickOnFilter(SmartSearchFilter.PAGES)
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertItemNotDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")
        smartSearchPage.assertItemDisplayed("Test Announcement for query", "Announcement")
        smartSearchPage.assertItemDisplayed("Test Assignment for query", "Assignment")

        smartSearchPage.clickOnFilters()

        smartSearchPreferencesPage.assertFilterNotChecked(SmartSearchFilter.PAGES)

        smartSearchPreferencesPage.clickOnFilter(SmartSearchFilter.ASSIGNMENTS)
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertItemNotDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")
        smartSearchPage.assertItemDisplayed("Test Announcement for query", "Announcement")
        smartSearchPage.assertItemNotDisplayed("Test Assignment for query", "Assignment")

        smartSearchPage.clickOnFilters()
        smartSearchPreferencesPage.toggleAll()
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertItemDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")
        smartSearchPage.assertItemDisplayed("Test Announcement for query", "Announcement")
        smartSearchPage.assertItemDisplayed("Test Assignment for query", "Assignment")

        smartSearchPage.clickOnFilters()
        smartSearchPreferencesPage.toggleAll()
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertItemDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")
        smartSearchPage.assertItemDisplayed("Test Announcement for query", "Announcement")
        smartSearchPage.assertItemDisplayed("Test Assignment for query", "Assignment")
    }

    @Test
    fun assertGroups() {
        val data = initData()

        val course = data.courses.values.first()
        val pageId = Random.nextLong(0, 100)
        data.addPageToCourse(
            course.id,
            pageId,
            title = "Test Page for query",
            body = "Test Body for query",
            url = "https://mock-data.instructure.com/courses/${course.id}/pages/$pageId"
        )

        data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Discussion for query",
            isAnnouncement = false
        )

        data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Announcement for query",
            isAnnouncement = true
        )

        data.addAssignment(course.id, name = "Test Assignment for query")

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.clickOnFilters()
        smartSearchPreferencesPage.selectTypeSortType()
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.DISCUSSION_TOPIC)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.ANNOUNCEMENT)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.ASSIGNMENT)

        smartSearchPage.assertItemDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")
        smartSearchPage.assertItemDisplayed("Test Announcement for query", "Announcement")
        smartSearchPage.assertItemDisplayed("Test Assignment for query", "Assignment")
    }

    @Test
    fun toggleGroups() {
        val data = initData()

        val course = data.courses.values.first()
        val pageId = Random.nextLong(0, 100)
        data.addPageToCourse(
            course.id,
            pageId,
            title = "Test Page for query",
            body = "Test Body for query",
            url = "https://mock-data.instructure.com/courses/${course.id}/pages/$pageId"
        )

        data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Discussion for query",
            isAnnouncement = false
        )

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.clickOnFilters()
        smartSearchPreferencesPage.selectTypeSortType()
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.DISCUSSION_TOPIC)

        smartSearchPage.assertItemDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")

        smartSearchPage.toggleGroup(SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertItemNotDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemDisplayed("Test Discussion for query", "Discussion")

        smartSearchPage.toggleGroup(SmartSearchContentType.DISCUSSION_TOPIC)
        smartSearchPage.assertItemNotDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemNotDisplayed("Test Discussion for query", "Discussion")

        smartSearchPage.toggleGroup(SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertItemDisplayed("Test Page for query", "Page")
        smartSearchPage.assertItemNotDisplayed("Test Discussion for query", "Discussion")
    }

    @Test
    fun assertGroupToRelevance() {
        val data = initData()

        val course = data.courses.values.first()
        val pageId = Random.nextLong(0, 100)
        data.addPageToCourse(
            course.id,
            pageId,
            title = "Test Page for query",
            body = "Test Body for query",
            url = "https://mock-data.instructure.com/courses/${course.id}/pages/$pageId"
        )

        data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Discussion for query",
            isAnnouncement = false
        )

        data.addDiscussionTopicToCourse(
            course,
            data.teachers.first(),
            topicTitle = "Test Announcement for query",
            isAnnouncement = true
        )

        data.addAssignment(course.id, name = "Test Assignment for query")

        goToSmartSearch(data, "query")

        composeTestRule.waitForIdle()

        smartSearchPage.clickOnFilters()
        smartSearchPreferencesPage.selectTypeSortType()
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.DISCUSSION_TOPIC)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.ANNOUNCEMENT)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.ASSIGNMENT)

        smartSearchPage.clickOnFilters()
        smartSearchPreferencesPage.selectRelevanceSortType()
        smartSearchPreferencesPage.applyFilters()

        smartSearchPage.assertGroupHeaderNotDisplayed(SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertGroupHeaderNotDisplayed(SmartSearchContentType.DISCUSSION_TOPIC)
        smartSearchPage.assertGroupHeaderNotDisplayed(SmartSearchContentType.ANNOUNCEMENT)
        smartSearchPage.assertGroupHeaderNotDisplayed(SmartSearchContentType.ASSIGNMENT)
    }


    abstract fun initData(): MockCanvas

    abstract fun goToSmartSearch(data: MockCanvas, query: String)

    abstract fun assertDiscussionPage(discussionTopic: DiscussionTopicHeader)

    abstract fun assertPage(page: Page)

    override fun displaysPageObjects() = Unit
}
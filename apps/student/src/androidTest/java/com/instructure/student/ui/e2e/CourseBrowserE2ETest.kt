/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvasapi2.models.SmartSearchContentType
import com.instructure.canvasapi2.models.SmartSearchFilter
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.PagesApi
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class CourseBrowserE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.COURSE_BROWSER, TestCategory.E2E, SecondaryFeatureCategory.SMART_SEARCH)
    fun testSmartSearchE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for '${course.name}' course with 2 days ahead due date.")
        val testAssignment = AssignmentsApi.createAssignment(course.id, teacher.token, dueAt = 2.days.fromNow.iso8601, pointsPossible = 15.0, submissionTypes = listOf(
            SubmissionType.ONLINE_TEXT_ENTRY), assignmentName = "Test SmartSearch Assignment")

        Log.d(STEP_TAG,"Seed an announcement for '${course.name}' course.")
        val testAnnouncement = DiscussionTopicsApi.createAnnouncement(course.id, teacher.token, announcementTitle = "Test Announcement")

        Log.d(PREPARATION_TAG,"Create a discussion topic for '${course.name}' course.")
        val testDiscussion = DiscussionTopicsApi.createDiscussion(courseId = course.id, token = teacher.token, discussionTitle = "Test Discussion")

        Log.d(PREPARATION_TAG,"Create a published page for course: '${course.name}'.")
        val testPage = PagesApi.createCoursePage(course.id, teacher.token, published = true, frontPage = false, pageTitle = "Test SmartSearch Page", body = "<h1 id=\"header1\">Test SmartSearch Page</h1>")

        Log.d(STEP_TAG,"Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG,"Wait for the Dashboard Page to be rendered. Select course: '${course.name}'.")
        dashboardPage.waitForRender()
        Thread.sleep(10000) // Wait for the API creations to complete, sometimes the creation processes are too slow on the API side
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Assert that the Course Browser Page is displayed.")
        courseBrowserPage.assertPageObjects()

        Log.d(STEP_TAG, "Click on the 'Smart Search' (Magnifying glass) icon on the top right corner of the Course Browser page.")
        courseBrowserPage.clickOnSmartSearch()

        val smartSearchText = testAssignment.name.take(4) // "Test" will be the search text.
        Log.d(STEP_TAG, "Type the '$smartSearchText' into the search input field.")
        composeTestRule.onNodeWithTag("searchField")
            .requestFocus()
            .performClick()
            .performTextInput(smartSearchText)
        composeTestRule.onNodeWithTag("searchField").performImeAction()
        composeTestRule.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the '$smartSearchText' text is displayed in the search input field and the filter button is displayed on the search bar.")
        smartSearchPage.assertQuery(smartSearchText)
        composeTestRule.onNodeWithTag("filterButton").assertIsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that the '${course.name}' is displayed under the 'Results in course' section.")
        smartSearchPage.assertCourse(course.name)

        Log.d(ASSERTION_TAG, "Assert that the '${testAssignment.name}' assignment is displayed and it's type is 'Assignment'.")
        smartSearchPage.assertItemDisplayed(testAssignment.name, "Assignment")

        Log.d(ASSERTION_TAG, "Assert that the '${testAnnouncement.title}' announcement is displayed and it's type is 'Announcement'.")
        smartSearchPage.assertItemDisplayed(testAnnouncement.title, "Announcement")

        Log.d(ASSERTION_TAG, "Assert that the '${testDiscussion.title}' discussion is displayed and it's type is 'Discussion'.")
        smartSearchPage.assertItemDisplayed(testDiscussion.title, "Discussion")

        Log.d(ASSERTION_TAG, "Assert that the '${testPage.title}' page is displayed and it's type is 'Page'.")
        smartSearchPage.assertItemDisplayed(testPage.title, "Page")

        Log.d(STEP_TAG, "Click on the '${testAssignment.name}' assignment.")
        smartSearchPage.clickOnItem(testAssignment.name)

        Log.d(ASSERTION_TAG, "Assert that the previous click action will navigate to the '${testAssignment.name}' assignment's details page.")
        assignmentDetailsPage.assertAssignmentDetails(testAssignment)

        Log.d(STEP_TAG, "Navigate back to the Smart Search Result List page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the '${testAnnouncement.title}' announcement.")
        smartSearchPage.clickOnItem(testAnnouncement.title)

        Log.d(ASSERTION_TAG, "Assert that the previous click action will navigate to the '${testAnnouncement.title}' announcement's details page.")
        discussionDetailsPage.assertToolbarDiscussionTitle(testAnnouncement.title)

        Log.d(STEP_TAG, "Navigate back to the Smart Search Result List page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the '${testDiscussion.title}' discussion.")
        smartSearchPage.clickOnItem(testDiscussion.title)

        Log.d(ASSERTION_TAG, "Assert that the previous click action will navigate to the '${testDiscussion.title}' discussion's details page.")
        discussionDetailsPage.assertToolbarDiscussionTitle(testDiscussion.title)

        Log.d(STEP_TAG, "Navigate back to the Smart Search Result List page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the '${testPage.title}' page.")
        smartSearchPage.clickOnItem(testPage.title)

        Log.d(ASSERTION_TAG, "Assert that the previous click action will navigate to the '${testPage.title}' page's details page. (Assert for the URL as we are displaying the URL of the page when navigating to it's details page from a link.")
        pageDetailsPage.assertToolbarTitle(testPage.url)

        Log.d(STEP_TAG, "Navigate back to the Smart Search Result List page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Click on the 'Filters' icon on the top-right corner.")
        smartSearchPage.clickOnFilters()

        Log.d(ASSERTION_TAG, "Assert that all of the types (Pages, Discussion Topics, Announcements, Assignments) are checked by default.")
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.PAGES)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.DISCUSSION_TOPICS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.ANNOUNCEMENTS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.ASSIGNMENTS)

        Log.d(STEP_TAG, "Click on the 'Pages' and 'Announcements' filters to turn them off. Apply the filters.")
        smartSearchPreferencesPage.clickOnFilter(SmartSearchFilter.PAGES)
        smartSearchPreferencesPage.clickOnFilter(SmartSearchFilter.ANNOUNCEMENTS)
        smartSearchPreferencesPage.applyFilters()

        Log.d(ASSERTION_TAG, "Assert that the 'Page' and 'Announcement' result items are not displayed any more on the Smart Search Results page but the 'Discussion Topic' and 'Assignment' are still displayed.")
        smartSearchPage.assertItemNotDisplayed(testPage.title, "Page")
        smartSearchPage.assertItemNotDisplayed(testAnnouncement.title, "Announcement")
        smartSearchPage.assertItemDisplayed(testDiscussion.title, "Discussion")
        smartSearchPage.assertItemDisplayed(testAssignment.name, "Assignment")

        Log.d(STEP_TAG, "Click on the 'Filters' icon on the top-right corner.")
        smartSearchPage.clickOnFilters()

        Log.d(ASSERTION_TAG, "Assert that the 'Pages' and 'Announcements' filters are unchecked (as we modified it recently) and the 'Discussion Topics' and 'Assignments' are remain checked.")
        smartSearchPreferencesPage.assertFilterNotChecked(SmartSearchFilter.PAGES)
        smartSearchPreferencesPage.assertFilterNotChecked(SmartSearchFilter.ANNOUNCEMENTS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.DISCUSSION_TOPICS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.ASSIGNMENTS)

        Log.d(STEP_TAG, "Click on the 'Discussion Topic' and 'Assignment' filters to turn them off as well. Apply the new filters.")
        smartSearchPreferencesPage.clickOnFilter(SmartSearchFilter.DISCUSSION_TOPICS)
        smartSearchPreferencesPage.clickOnFilter(SmartSearchFilter.ASSIGNMENTS)
        smartSearchPreferencesPage.applyFilters()

        Log.d(ASSERTION_TAG, "Assert that all the types of result items are displayed as we have a logic if nothing is selected then we show every item on the Smart Search Result page.")
        smartSearchPage.assertItemDisplayed(testPage.title, "Page")
        smartSearchPage.assertItemDisplayed(testAnnouncement.title, "Announcement")
        smartSearchPage.assertItemDisplayed(testDiscussion.title, "Discussion")
        smartSearchPage.assertItemDisplayed(testAssignment.name, "Assignment")

        Log.d(STEP_TAG, "Click on the 'Filters' icon on the top-right corner.")
        smartSearchPage.clickOnFilters()

        Log.d(ASSERTION_TAG, "Assert that none of the type filters are checked.")
        smartSearchPreferencesPage.assertFilterNotChecked(SmartSearchFilter.PAGES)
        smartSearchPreferencesPage.assertFilterNotChecked(SmartSearchFilter.ANNOUNCEMENTS)
        smartSearchPreferencesPage.assertFilterNotChecked(SmartSearchFilter.DISCUSSION_TOPICS)
        smartSearchPreferencesPage.assertFilterNotChecked(SmartSearchFilter.ASSIGNMENTS)

        Log.d(STEP_TAG, "Click on the 'Select All' button.")
        smartSearchPreferencesPage.toggleAll()

        Log.d(ASSERTION_TAG, "Assert that all of the type filters are checked.")
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.PAGES)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.ANNOUNCEMENTS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.DISCUSSION_TOPICS)
        smartSearchPreferencesPage.assertFilterChecked(SmartSearchFilter.ASSIGNMENTS)

        Log.d(ASSERTION_TAG, "Assert that the 'Sort By' section displayed properly. By default, the 'Relevance' radiobutton should be selected.")
        smartSearchPreferencesPage.assertSortByDetails()
        smartSearchPreferencesPage.assertRadioButtonSelected("Relevance")

        Log.d(STEP_TAG, "Select the 'Type' sorting type and apply the filters.")
        smartSearchPreferencesPage.selectTypeSortType()
        smartSearchPreferencesPage.applyFilters()

        Log.d(ASSERTION_TAG, "Assert that the four different group header titles (Pages, Discussion Topics, Announcements, Assignments) are displayed.")
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.DISCUSSION_TOPIC)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.ANNOUNCEMENT)
        smartSearchPage.assertGroupHeaderDisplayed(SmartSearchContentType.ASSIGNMENT)

        Log.d(ASSERTION_TAG, "Assert that all the 4 groups has 1 item and it displays by the counter.")
        smartSearchPage.assertGroupItemCount("1", SmartSearchContentType.WIKI_PAGE)
        smartSearchPage.assertGroupItemCount("1", SmartSearchContentType.DISCUSSION_TOPIC)
        smartSearchPage.assertGroupItemCount("1", SmartSearchContentType.ANNOUNCEMENT)
        smartSearchPage.assertGroupItemCount("1", SmartSearchContentType.ASSIGNMENT)

        Log.d(ASSERTION_TAG, "Assert that all the types of result items are displayed on the Smart Search Result page.")
        smartSearchPage.assertItemDisplayed(testPage.title, "Page")
        smartSearchPage.assertItemDisplayed(testAnnouncement.title, "Announcement")
        smartSearchPage.assertItemDisplayed(testDiscussion.title, "Discussion")
        smartSearchPage.assertItemDisplayed(testAssignment.name, "Assignment")
    }

}
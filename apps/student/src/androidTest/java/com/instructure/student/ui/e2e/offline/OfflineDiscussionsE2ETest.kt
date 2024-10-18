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
 *
 */
package com.instructure.student.ui.e2e.offline

import android.util.Log
import androidx.test.espresso.Espresso
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.OfflineE2E
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.checkToastText
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.espresso.getDateInCanvasFormat
import com.instructure.student.R
import com.instructure.student.ui.e2e.offline.utils.OfflineTestUtils
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.openOverflowMenu
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineDiscussionsE2ETest : StudentTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @Stub // TODO: Investigate flaky test
    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DISCUSSIONS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineDiscussionsE2E() {

        Log.d(PREPARATION_TAG,"Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG,"Seed a discussion topic for '${course.name}' course.")
        val discussion1 = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        Log.d(PREPARATION_TAG,"Seed another discussion topic for '${course.name}' course.")
        val discussion2 = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        Log.d(PREPARATION_TAG,"Seed an entry ('main reply') for the '${course.name}' course's '${discussion1.title}' discussion topic.")
        DiscussionTopicsApi.createEntryToDiscussionTopic(student.token, course.id, discussion1.id, "My reply")

        Log.d(STEP_TAG,"Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG,"Wait for the Dashboard Page to be rendered.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course and navigate to Discussion List page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectDiscussions()

        Log.d(STEP_TAG,"Select '$discussion1' discussion topic and assert that there is no reply on the details page as well.")
        discussionListPage.selectTopic(discussion1.title)

        Log.d(STEP_TAG, "Assert that the 'Reply' button is displayed on the Discussion Details (Web view) Page.")
        discussionDetailsPage.waitForReplyButton()
        discussionDetailsPage.assertReplyButtonDisplayed()

        Log.d(STEP_TAG,"Assert the the previously sent reply 'My reply', is displayed on the (online) details page.")
        discussionDetailsPage.assertEntryDisplayed("My reply")

        Log.d(STEP_TAG, "Navigate back to the Dashboard page.")
        ViewUtils.pressBackButton(3)

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Expand '${course.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course.name)

        Log.d(STEP_TAG, "Select the 'Discussions' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState("Discussions")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(STEP_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Select '${course.name}' course and open 'Announcements' menu.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG,"Navigate to Discussion List Page.")
        courseBrowserPage.selectDiscussions()

        Log.d(STEP_TAG, "Assert that both the '${discussion1.title}' and '${discussion2.title}' discussion are displayed on the Discussion List page.")
        discussionListPage.assertTopicDisplayed(discussion1.title)
        discussionListPage.assertTopicDisplayed(discussion2.title)

        Log.d(STEP_TAG,"Refresh the page. Assert that the previously sent reply has been counted, and there are no unread replies.")
        discussionListPage.assertReplyCount(discussion1.title, 1)
        discussionListPage.assertUnreadReplyCount(discussion1.title, 0)

        Log.d(STEP_TAG, "Assert that the due date is the current date (in the expected format).")
        val currentDate = getDateInCanvasFormat()
        discussionListPage.assertDueDate(discussion1.title, currentDate)

        Log.d(STEP_TAG, "Click on the Search (magnifying glass) icon and the '${discussion1.title}' discussion's title into the search input field.")
        discussionListPage.searchable.clickOnSearchButton()
        discussionListPage.searchable.typeToSearchBar(discussion1.title)

        Log.d(STEP_TAG, "Assert that only the '${discussion1.title}' discussion displayed as a search result and the other, '${discussion2.title}' discussion has not displayed.")
        discussionListPage.assertTopicDisplayed(discussion1.title)
        discussionListPage.assertTopicNotDisplayed(discussion2.title)

        Log.d(STEP_TAG, "Click on the 'Clear Search' (X) icon and assert that both of the discussion should be displayed again.")
        discussionListPage.searchable.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(discussion2.title)
        discussionListPage.assertTopicDisplayed(discussion1.title)

        Log.d(STEP_TAG,"Select '${discussion1.title}' discussion and assert if the corresponding discussion title is displayed.")
        discussionListPage.selectTopic(discussion1.title)
        nativeDiscussionDetailsPage.assertTitleText(discussion1.title)

        Log.d(STEP_TAG, "Try to click on the (main) 'Reply' button and assert that the 'No Internet Connection' dialog has displayed. Dismiss the dialog.")
        nativeDiscussionDetailsPage.clickReply()
        OfflineTestUtils.assertNoInternetConnectionDialog()
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Try to click on the (inner) 'Reply' button (so try to 'reply to a reply') and assert that the 'No Internet Connection' dialog has displayed. Dismiss the dialog.")
        nativeDiscussionDetailsPage.clickOnInnerReply()
        OfflineTestUtils.assertNoInternetConnectionDialog()
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG,"Navigate back to Discussion List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG,"Select '${discussion2.title}' discussion and assert if the Discussion Details page is displayed and there is no reply for the discussion yet.")
        discussionListPage.selectTopic(discussion2.title)
        nativeDiscussionDetailsPage.assertTitleText(discussion2.title)
        nativeDiscussionDetailsPage.assertNoRepliesDisplayed()

        Log.d(STEP_TAG, "Try to click on 'Add Bookmark' overflow menu and assert that the 'Functionality unavailable while offline' toast message is displayed.")
        openOverflowMenu()
        nativeDiscussionDetailsPage.clickOnAddBookmarkMenu()
        checkToastText(R.string.notAvailableOffline, activityRule.activity)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }
}
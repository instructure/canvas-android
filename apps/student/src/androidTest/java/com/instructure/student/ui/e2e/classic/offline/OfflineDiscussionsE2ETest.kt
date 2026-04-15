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
package com.instructure.student.ui.e2e.classic.offline

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.OfflineE2E
import com.instructure.canvas.espresso.utils.checkToastText
import com.instructure.canvas.espresso.utils.pressBackButton
import com.instructure.canvas.espresso.utils.refresh
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.FileFolderApi
import com.instructure.dataseeding.api.FileUploadsApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.espresso.convertIso8601ToCanvasFormat
import com.instructure.espresso.getDateInCanvasFormat
import com.instructure.student.R
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.openOverflowMenu
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import com.instructure.student.ui.utils.offline.OfflineTestUtils
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Test

@HiltAndroidTest
class OfflineDiscussionsE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @OfflineE2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.DISCUSSIONS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineDiscussionsE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Seed a discussion topic for '${course.name}' course.")
        val discussion1 = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Seed another discussion topic for '${course.name}' course.")
        val discussion2 = DiscussionTopicsApi.createDiscussion(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Seed an entry ('main reply') for the '${course.name}' course's '${discussion1.title}' discussion topic.")
        DiscussionTopicsApi.createEntryToDiscussionTopic(student.token, course.id, discussion1.id, "My reply")

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered.")
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Select '${course.name}' course and navigate to Discussion List page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectDiscussions()

        Log.d(STEP_TAG, "Select '$discussion1' discussion topic and assert that there is no reply on the details page as well.")
        discussionListPage.selectTopic(discussion1.title)

        Log.d(ASSERTION_TAG, "Assert that the 'Reply' button is displayed on the Discussion Details (Web view) Page.")
        discussionDetailsPage.waitForReplyButton()
        discussionDetailsPage.assertReplyButtonDisplayed()

        Log.d(ASSERTION_TAG, "Assert the the previously sent reply 'My reply', is displayed on the (online) details page.")
        discussionDetailsPage.assertEntryDisplayed("My reply")

        Log.d(STEP_TAG, "Navigate back to the Dashboard page.")
        pressBackButton(3)

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Expand '${course.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course.name)

        Log.d(STEP_TAG, "Select the 'Discussions' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState("Discussions")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Select '${course.name}' course and open 'Announcements' menu.")
        dashboardPage.selectCourse(course)

        Log.d(STEP_TAG, "Navigate to Discussion List Page.")
        courseBrowserPage.selectDiscussions()

        Log.d(ASSERTION_TAG, "Assert that both the '${discussion1.title}' and '${discussion2.title}' discussion are displayed on the Discussion List page.")
        discussionListPage.assertTopicDisplayed(discussion1.title)
        discussionListPage.assertTopicDisplayed(discussion2.title)

        Log.d(ASSERTION_TAG, "Assert that the previously sent reply has been counted, and there are no unread replies.")
        discussionListPage.assertReplyCount(discussion1.title, 1)
        discussionListPage.assertUnreadReplyCount(discussion1.title, 0)

        val currentDate = getDateInCanvasFormat()
        Log.d(ASSERTION_TAG, "Assert that the due date is the current date (in the expected format).")
        discussionListPage.assertDueDate(discussion1.title, currentDate)

        Log.d(STEP_TAG, "Click on the Search (magnifying glass) icon and the '${discussion1.title}' discussion's title into the search input field.")
        discussionListPage.searchable.clickOnSearchButton()
        discussionListPage.searchable.typeToSearchBar(discussion1.title)

        Log.d(ASSERTION_TAG, "Assert that only the '${discussion1.title}' discussion displayed as a search result and the other, '${discussion2.title}' discussion has not displayed.")
        discussionListPage.assertTopicDisplayed(discussion1.title)
        discussionListPage.assertTopicNotDisplayed(discussion2.title)

        Log.d(STEP_TAG, "Click on the 'Clear Search' (X) icon.")
        discussionListPage.searchable.clickOnClearSearchButton()
        discussionListPage.waitForDiscussionTopicToDisplay(discussion2.title)

        Log.d(ASSERTION_TAG, "Assert that both of the discussion should be displayed again.")
        discussionListPage.assertTopicDisplayed(discussion1.title)

        Log.d(STEP_TAG, "Select '${discussion1.title}' discussion.")
        discussionListPage.selectTopic(discussion1.title)

        Log.d(ASSERTION_TAG, "Assert if the corresponding discussion title is displayed.")
        nativeDiscussionDetailsPage.assertTitleText(discussion1.title)

        Log.d(STEP_TAG, "Try to click on the (main) 'Reply' button.")
        nativeDiscussionDetailsPage.clickReply()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog has displayed.")
        OfflineTestUtils.assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Try to click on the (inner) 'Reply' button (so try to 'reply to a reply').")
        nativeDiscussionDetailsPage.clickOnInnerReply()

        Log.d(ASSERTION_TAG, "Assert that the 'No Internet Connection' dialog has displayed.")
        OfflineTestUtils.assertNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Dismiss the 'No Internet Connection' dialog.")
        OfflineTestUtils.dismissNoInternetConnectionDialog()

        Log.d(STEP_TAG, "Navigate back to Discussion List Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Select '${discussion2.title}' discussion.")
        discussionListPage.selectTopic(discussion2.title)

        Log.d(ASSERTION_TAG, "Assert if the Discussion Details page is displayed and there is no reply for the discussion yet.")
        nativeDiscussionDetailsPage.assertTitleText(discussion2.title)
        nativeDiscussionDetailsPage.assertNoRepliesDisplayed()

        Log.d(STEP_TAG, "Try to click on 'Add Bookmark' overflow menu.")
        openOverflowMenu()
        nativeDiscussionDetailsPage.clickOnAddBookmarkMenu()

        Log.d(ASSERTION_TAG, "Assert that the 'Functionality unavailable while offline' toast message is displayed.")
        checkToastText(R.string.notAvailableOffline, activityRule.activity)
    }

    @OfflineE2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DISCUSSIONS, TestCategory.E2E, SecondaryFeatureCategory.OFFLINE_MODE)
    fun testOfflineDiscussionCheckpointWithPdfAttachmentE2E() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Get course root folder to upload the PDF file.")
        val courseRootFolder = FileFolderApi.getCourseRootFolder(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Read PDF file from assets.")
        val pdfFileName = "samplepdf.pdf"
        val context = InstrumentationRegistry.getInstrumentation().context
        val inputStream = context.assets.open(pdfFileName)
        val pdfBytes = inputStream.readBytes()
        inputStream.close()

        Log.d(PREPARATION_TAG, "Upload PDF file to course root folder using teacher token.")
        val uploadedFile = FileUploadsApi.uploadFile(courseId = courseRootFolder.id, assignmentId = null, file = pdfBytes, fileName = pdfFileName, token = teacher.token, fileUploadType = FileUploadType.COURSE_FILE)

        Log.d(PREPARATION_TAG, "Seed a discussion topic with checkpoints and PDF attachment for '${course.name}' course.")
        val discussionWithCheckpointsTitle = "Discussion with PDF Attachment"
        val assignmentName = "Assignment with Checkpoints and PDF"
        val replyToTopicDueDate = "2029-11-12T22:59:00Z"
        val replyToEntryDueDate = "2029-11-19T22:59:00Z"
        DiscussionTopicsApi.createDiscussionTopicWithCheckpoints(courseId = course.id, token = teacher.token, discussionTitle = discussionWithCheckpointsTitle, assignmentName = assignmentName, replyToTopicDueDate = replyToTopicDueDate, replyToEntryDueDate = replyToEntryDueDate, fileId = uploadedFile.id.toString())

        val convertedReplyToTopicDueDate = "Due " + convertIso8601ToCanvasFormat("2029-11-12T22:59:00Z") + " 2:59 PM"
        val convertedReplyToEntryDueDate = "Due " + convertIso8601ToCanvasFormat("2029-11-19T22:59:00Z") + " 2:59 PM"

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Open the '${course.name}' course's 'Manage Offline Content' page via the more menu of the Dashboard Page.")
        dashboardPage.clickCourseOverflowMenu(course.name, "Manage Offline Content")

        Log.d(STEP_TAG, "Expand '${course.name}' course.")
        manageOfflineContentPage.expandCollapseItem(course.name)

        Log.d(STEP_TAG, "Select the 'Assignments' and 'Discussions' of '${course.name}' course for sync. Click on the 'Sync' button.")
        manageOfflineContentPage.changeItemSelectionState("Assignments")
        manageOfflineContentPage.changeItemSelectionState("Discussions")
        manageOfflineContentPage.clickOnSyncButtonAndConfirm()

        Log.d(ASSERTION_TAG, "Assert that the offline sync icon only displayed on the synced course's course card.")
        dashboardPage.assertCourseOfflineSyncIconVisible(course.name)
        device.waitForIdle()

        Log.d(PREPARATION_TAG, "Turn off the Wi-Fi and Mobile Data on the device, so it will go offline.")
        turnOffConnectionViaADB()
        OfflineTestUtils.waitForNetworkToGoOffline(device)

        Log.d(STEP_TAG, "Wait for the Dashboard Page to be rendered. Refresh the page.")
        dashboardPage.waitForRender()
        refresh()

        Log.d(ASSERTION_TAG, "Assert that the Offline Indicator (bottom banner) is displayed on the Dashboard Page.")
        OfflineTestUtils.assertOfflineIndicator()

        Log.d(STEP_TAG, "Select course: '${course.name}' and navigate to Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(ASSERTION_TAG, "Assert that the '${discussionWithCheckpointsTitle}' discussion is present along with 2 date info (for the 2 checkpoints).")
        assignmentListPage.assertHasAssignmentWithCheckpoints(discussionWithCheckpointsTitle, dueAtString = convertedReplyToTopicDueDate, dueAtStringSecondCheckpoint = convertedReplyToEntryDueDate, expectedGrade = "-/15")

        Log.d(STEP_TAG, "Click on '$discussionWithCheckpointsTitle' assignment.")
        assignmentListPage.clickAssignment(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that Assignment Details Page is displayed with correct title.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertAssignmentTitle(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that attachment icon is displayed.")
        assignmentDetailsPage.assertAttachmentIconDisplayed()

        Log.d(STEP_TAG, "Click on attachment icon to attempt to view the PDF attachment while offline.")
        assignmentDetailsPage.clickAttachmentIcon()

        Log.d(ASSERTION_TAG, "Verify PDF viewer toolbar is displayed.")
        assignmentDetailsPage.assertPdfViewerToolbarDisplayed()

        Log.d(STEP_TAG, "Navigate back from PDF viewer to Assignment Details page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that we're back on the Assignment Details page.")
        assignmentDetailsPage.assertAssignmentTitle(discussionWithCheckpointsTitle)

        Log.d(STEP_TAG, "Click on 'View Discussion' button to navigate to the Discussion Details page.")
        assignmentDetailsPage.clickSubmit()

        Log.d(ASSERTION_TAG, "Assert that the Discussion Details page is displayed with the correct title.")
        nativeDiscussionDetailsPage.assertTitleText(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that the attachment icon is displayed on the Discussion Details page.")
        nativeDiscussionDetailsPage.assertMainAttachmentDisplayed()

        Log.d(STEP_TAG, "Click on the attachment icon to view the PDF attachment from the Discussion Details page.")
        nativeDiscussionDetailsPage.clickAttachmentIcon()

        Log.d(ASSERTION_TAG, "Verify PDF viewer toolbar is displayed.")
        assignmentDetailsPage.assertPdfViewerToolbarDisplayed()

        Log.d(STEP_TAG, "Navigate back from PDF viewer to Discussion Details page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that we're back on the Discussion Details page.")
        nativeDiscussionDetailsPage.assertTitleText(discussionWithCheckpointsTitle)
    }

    @After
    fun tearDown() {
        Log.d(PREPARATION_TAG, "Turn back on the Wi-Fi and Mobile Data on the device, so it will come back online.")
        turnOnConnectionViaADB()
    }
}
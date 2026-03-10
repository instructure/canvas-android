/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e.compose

import android.os.SystemClock.sleep
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.SecondaryFeatureCategory
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.dataseeding.api.DiscussionTopicsApi
import com.instructure.dataseeding.api.FileFolderApi
import com.instructure.dataseeding.api.FileUploadsApi
import com.instructure.dataseeding.model.FileUploadType
import com.instructure.espresso.convertIso8601ToCanvasFormat
import com.instructure.espresso.retryWithIncreasingDelay
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.extensions.seedData
import com.instructure.parentapp.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DiscussionsE2ETest: ParentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.DISCUSSIONS, TestCategory.E2E, SecondaryFeatureCategory.DISCUSSION_CHECKPOINTS)
    fun testDiscussionCheckpointWithPdfAttachmentE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1, parents = 1)
        val student = data.studentsList[0]
        val teacher = data.teachersList[0]
        val parent = data.parentsList[0]
        val course = data.coursesList[0]

        Log.d(PREPARATION_TAG, "Get course root folder to upload the PDF file.")
        val courseRootFolder = FileFolderApi.getCourseRootFolder(course.id, teacher.token)

        Log.d(PREPARATION_TAG, "Read PDF file from assets.")
        val pdfFileName = "samplepdf.pdf"
        val context = InstrumentationRegistry.getInstrumentation().context
        val pdfBytes = context.assets.open(pdfFileName).use { it.readBytes() }

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
        Log.d(STEP_TAG, "Login with user: '${parent.name}', login id: '${parent.loginId}'.")
        tokenLogin(parent)

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is the landing page and it is loaded successfully.")
        dashboardPage.waitForRender()
        dashboardPage.assertPageObjects()

        Log.d(STEP_TAG, "Open the student selector and select '${student.shortName}'.")
        dashboardPage.openStudentSelector()
        dashboardPage.selectStudent(student.shortName)

        Log.d(STEP_TAG, "Click on the '${course.name}' course.")
        coursesPage.clickCourseItem(course.name)

        Log.d(ASSERTION_TAG, "Assert that the details of the course has opened.")
        courseDetailsPage.assertCourseNameDisplayed(course)

        Log.d(ASSERTION_TAG, "Assert that the '${discussionWithCheckpointsTitle}' discussion is present along with 2 date info (For the 2 checkpoints).")
        courseDetailsPage.assertHasAssignmentWithCheckpoints(discussionWithCheckpointsTitle, dueAtString = convertedReplyToTopicDueDate, dueAtStringSecondCheckpoint = convertedReplyToEntryDueDate, expectedGrade = "-/15")

        Log.d(STEP_TAG, "Click on '$discussionWithCheckpointsTitle' assignment to open its details.")
        courseDetailsPage.clickAssignment(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that Assignment Details Page is displayed with correct title.")
        assignmentDetailsPage.assertDisplayToolbarTitle()
        assignmentDetailsPage.assertAssignmentTitle(discussionWithCheckpointsTitle)

        Log.d(ASSERTION_TAG, "Assert that attachment icon is displayed.")
        assignmentDetailsPage.assertAttachmentIconDisplayed()

        Log.d(STEP_TAG, "Click on attachment icon to download it.")
        assignmentDetailsPage.clickAttachmentIcon()

        Log.d(STEP_TAG, "Wait for download to complete.")
        sleep(5000)

        Log.d(STEP_TAG, "Open the Notification bar.")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.openNotification()

        retryWithIncreasingDelay(times = 10, maxDelay = 3000) {
            Log.d(STEP_TAG, "Find download notification.")
            val downloadNotification = device.findObject(UiSelector().textContains(pdfFileName).className("android.widget.TextView"))

            Log.d(ASSERTION_TAG, "Assert that 'Download complete' text is displayed in notification.")
            val downloadCompleteText = device.findObject(UiSelector().textContains("Download complete"))
            assert(downloadCompleteText.exists()) { "Download complete text not found in notification" }

            Log.d(ASSERTION_TAG, "Assert that file name '$pdfFileName' is displayed in notification.")
            assert(downloadNotification.exists()) { "File name '$pdfFileName' not found in notification" }
        }

        Log.d(STEP_TAG, "Close notification shade.")
        device.pressBack()

        Log.d(STEP_TAG, "Navigate back from the Assignment details page.")
        Espresso.pressBack()

        Log.d(ASSERTION_TAG, "Assert that we are back to the course details page.")
        courseDetailsPage.assertCourseNameDisplayed(course)
    }

}
/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.student.ui.e2e.classic

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.canvas.espresso.common.pages.compose.AssignmentListPage
import com.instructure.canvas.espresso.pressBackButton
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.extensions.seedData
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class ShareExtensionE2ETest: StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    fun shareExtensionE2ETest() {

        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 1, teachers = 1, courses = 1)
        val jpgTestFileName = "sample.jpg"
        val pdfTestFileName = "samplepdf.pdf"
        val uri = setupFileOnDevice(jpgTestFileName)
        val uri2 = setupFileOnDevice(pdfTestFileName)
        val student = data.studentsList[0]
        val course = data.coursesList[0]
        val teacher = data.teachersList[0]

        Log.d(PREPARATION_TAG, "Seeding 'File upload' assignment for '${course.name}' course.")
        val testAssignmentOne = AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 15.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD))

        Log.d(PREPARATION_TAG, "Seeding another 'File upload' assignment for '${course.name}' course.")
        AssignmentsApi.createAssignment(course.id, teacher.token, gradingType = GradingType.POINTS, pointsPossible = 30.0, dueAt = 1.days.fromNow.iso8601, submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD))

        Log.d(PREPARATION_TAG, "Get the device to be able to perform app-independent actions on it.")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        Log.d(STEP_TAG, "Login with user: '${student.name}', login id: '${student.loginId}'.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Press 'Home' button on the device so it will take the Student application into the background.")
        device.pressHome()

        Log.d(STEP_TAG, "Share the '$jpgTestFileName' and '$pdfTestFileName' files from the following uris: '$uri' and '$uri2'.")
        shareMultipleFiles(arrayListOf(uri, uri2))

        Log.d(STEP_TAG, "Click on the Canvas Student app.")
        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the Share Extension Page is displayed and the " +
                "'My Files' button is selected by default, and so the '${student.name}' username is displayed as well.")
        shareExtensionTargetPage.assertPageObjects()
        shareExtensionTargetPage.assertFilesCheckboxIsSelected()
        shareExtensionTargetPage.assertUserName(student.name)

        Log.d(STEP_TAG, "Select 'Upload as Submission'.")
        shareExtensionTargetPage.selectSubmission()

        Log.d(ASSERTION_TAG, "Assert that the corresponding course and assignment are displayed within the spinners.")
        shareExtensionTargetPage.assertCourseSelectorDisplayedWithCourse(course.name)
        shareExtensionTargetPage.assertAssignmentSelectorDisplayedWithAssignment(testAssignmentOne.name)
        
        Log.d(STEP_TAG, "Click on 'Next' button.")
        shareExtensionTargetPage.pressNext()

        Log.d(ASSERTION_TAG, "Assert that the File Upload page is displayed with the corresponding title.")
        fileChooserPage.assertPageObjects()
        fileChooserPage.assertDialogTitle("Submission")

        Log.d(STEP_TAG, "Click on 'Turn In' button to upload both of the files.")
        fileChooserPage.clickTurnIn()

        Log.d(ASSERTION_TAG, "Assert that the submission upload was successful.")
        shareExtensionStatusPage.assertPageObjects(30)
        shareExtensionStatusPage.assertAssignmentSubmissionSuccess()

        Log.d(STEP_TAG, "Click on 'Done' button.")
        shareExtensionStatusPage.clickOnDone()

        Log.d(STEP_TAG, "Click 'Recent Apps' device button and bring Canvas Student into the foreground again.")
        device.pressRecentApps()
        device.findObject(UiSelector().descriptionContains("Canvas")).click()

        Log.d(ASSERTION_TAG, "Assert that the Dashboard Page is displayed.")
        dashboardPage.assertPageObjects()

        Log.d(ASSERTION_TAG, "Assert that the 'Submission Successful' titled dashboard notification is displayed," +
                "and the '${testAssignmentOne.name}' assignment's name is displayed as the subtitle of the notification. ")
        dashboardPage.assertDashboardNotificationDisplayed("Submission Successful", testAssignmentOne.name)

        Log.d(STEP_TAG, "Click on the dashboard notification.")
        dashboardPage.clickOnDashboardNotification(testAssignmentOne.name)

        Log.d(ASSERTION_TAG, "Assert that the Submission Details Page is displayed correctly.")
        submissionDetailsPage.assertPageObjects()

        Log.d(STEP_TAG, "Press back button to navigate back to the Dashboard Page.")
        Espresso.pressBack()

        Log.d(STEP_TAG, "Select '${course.name}' and navigate to Assignments Page.")
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG, "Click on '$testAssignmentOne' assignment and refresh the Assignment Details Page.")
        assignmentListPage.clickAssignment(testAssignmentOne)
        assignmentDetailsPage.refresh()

        Log.d(ASSERTION_TAG, "Assert that the '$testAssignmentOne' assignment's status is 'Submitted'.")
        assignmentDetailsPage.assertAssignmentSubmitted()

        Log.d(STEP_TAG, "Press 'Home' button on the device so it will take the Student application into the background.")
        device.pressHome()

        Log.d(STEP_TAG, "Share the '$jpgTestFileName' and '$pdfTestFileName' files from the following uris: '$uri' and '$uri2'.")
        shareMultipleFiles(arrayListOf(uri, uri2))

        Log.d(STEP_TAG, "Click on the Canvas Student app.")
        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        Log.d(ASSERTION_TAG, "Assert that the Share Extension Page is displayed and the " +
                "'My Files' button is selected by default, and so the '${student.name}' username is displayed as well.")
        shareExtensionTargetPage.assertPageObjects()
        shareExtensionTargetPage.assertFilesCheckboxIsSelected()
        shareExtensionTargetPage.assertUserName(student.name)

        Log.d(STEP_TAG, "Press 'Next' button.")
        shareExtensionTargetPage.pressNext()

        Log.d(ASSERTION_TAG, "Assert that the title of the File Upload Page is correct and both of the shared files are displayed.")
        fileChooserPage.assertPageObjects()
        fileChooserPage.assertDialogTitle("Upload To My Files")
        fileChooserPage.assertFileDisplayed(jpgTestFileName)
        fileChooserPage.assertFileDisplayed(pdfTestFileName)

        Log.d(STEP_TAG, "Remove '$pdfTestFileName' file.")
        fileChooserPage.removeFile("samplepdf")

        Log.d(ASSERTION_TAG, "Assert that the '$pdfTestFileName' file not displayed any more on the list but the other file is displayed.")
        fileChooserPage.assertFileNotDisplayed(pdfTestFileName)
        fileChooserPage.assertFileDisplayed(jpgTestFileName)

        Log.d(STEP_TAG, "Click on 'Upload' button to upload the file.")
        fileChooserPage.clickUpload()

        Log.d(ASSERTION_TAG, "Assert that the file upload (into my 'Files') was successful.")
        shareExtensionStatusPage.assertPageObjects()
        shareExtensionStatusPage.assertFileUploadSuccess()

        Log.d(STEP_TAG, "Click on 'Done' button.")
        shareExtensionStatusPage.clickOnDone()

        Log.d(STEP_TAG, "Click 'Recent Apps' device button and bring Canvas Student into the foreground again.")
        device.pressRecentApps()
        device.findObject(UiSelector().descriptionContains("Canvas")).click()

        Log.d(STEP_TAG, "Press back button to navigate back to the Dashboard Page.")
        assignmentDetailsPage.assertPageObjects()
        pressBackButton(3)

        Log.d(STEP_TAG, "Assert that the Dashboard Page is displayed correctly.")
        dashboardPage.assertPageObjects()

        Thread.sleep(4000) //Make sure that the toast message has disappeared.

        Log.d(ASSERTION_TAG, "Assert that the 'File Upload Successful' titled dashboard notification is displayed and the subtitle is the uploaded file(s) name ('${jpgTestFileName}').")
        dashboardPage.assertDashboardNotificationDisplayed("File Upload Successful", jpgTestFileName)

        Log.d(STEP_TAG, "Click on the 'File Upload Successful' dashboard notification.")
        dashboardPage.clickOnDashboardNotification(jpgTestFileName)

        Log.d(ASSERTION_TAG, "Assert that the 'unfiled' directory is displayed.")
        fileListPage.assertItemDisplayed("unfiled")

        Log.d(STEP_TAG, "Click on the 'unfiled' directory.")
        fileListPage.selectItem("unfiled")

        Log.d(ASSERTION_TAG, "Assert that the previously uploaded file ('$jpgTestFileName') is displayed within the folder.")
        fileListPage.assertItemDisplayed(jpgTestFileName)
    }

    private fun shareMultipleFiles(uris: ArrayList<Uri>) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            type = "*/*"
        }

        val chooser = Intent.createChooser(intent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        InstrumentationRegistry.getInstrumentation().context.startActivity(chooser)
    }

}
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
package com.instructure.student.ui.e2e

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.E2E
import com.instructure.dataseeding.api.AssignmentsApi
import com.instructure.dataseeding.model.GradingType
import com.instructure.dataseeding.model.SubmissionType
import com.instructure.dataseeding.util.days
import com.instructure.dataseeding.util.fromNow
import com.instructure.dataseeding.util.iso8601
import com.instructure.pandautils.utils.Const
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.ViewUtils
import com.instructure.student.ui.utils.seedData
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File

@HiltAndroidTest
class ShareExtensionE2ETest: StudentTest() {

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

        Log.d(PREPARATION_TAG,"Seeding 'Text Entry' assignment for ${course.name} course.")
        val testAssignmentOne = AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
            courseId = course.id,
            submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD),
            gradingType = GradingType.POINTS,
            teacherToken = teacher.token,
            pointsPossible = 15.0,
            dueAt = 1.days.fromNow.iso8601
        ))

        AssignmentsApi.createAssignment(
            AssignmentsApi.CreateAssignmentRequest(
                courseId = course.id,
                submissionTypes = listOf(SubmissionType.ONLINE_UPLOAD),
                gradingType = GradingType.POINTS,
                teacherToken = teacher.token,
                pointsPossible = 30.0,
                dueAt = 1.days.fromNow.iso8601
            ))

        Log.d(PREPARATION_TAG, "Get the device to be able to perform app-independent actions on it.")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        Log.d(STEP_TAG, "Login with user: ${student.name}, login id: ${student.loginId}.")
        tokenLogin(student)

        Log.d(STEP_TAG, "Press 'Home' button on the device so it will take the Student application into the background.")
        device.pressHome()

        Log.d(STEP_TAG," Share the '$jpgTestFileName' and '$pdfTestFileName' files from the following uris: '$uri' and '$uri2'.")
        shareMultipleFiles(arrayListOf(uri, uri2))

        Log.d(STEP_TAG,"Click on the Canvas Student app.")
        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        Log.d(STEP_TAG,"Assert that the Share Extension Page is displayed and the " +
                "'My Files' button is selected by default, and so the '${student.name}' username is displayed as well.")
        shareExtensionTargetPage.assertPageObjects()
        shareExtensionTargetPage.assertFilesCheckboxIsSelected()
        shareExtensionTargetPage.assertUserName(student.name)

        Log.d(STEP_TAG, "Select 'Upload as Submission' and assert that the corresponding course and assignment are displayed within the spinners.")
        shareExtensionTargetPage.selectSubmission()
        shareExtensionTargetPage.assertCourseSelectorDisplayedWithCourse(course.name)
        shareExtensionTargetPage.assertAssignmentSelectorDisplayedWithAssignment(testAssignmentOne.name)
        
        Log.d(STEP_TAG, "Click on 'Next' button.")
        shareExtensionTargetPage.pressNext()

        Log.d(STEP_TAG, "Assert that the File Upload page is displayed with the corresponding title.")
        fileUploadPage.assertPageObjects()
        fileUploadPage.assertDialogTitle("Submission")

        Log.d(STEP_TAG, "Click on 'Turn In' button to upload both of the files.")
        fileUploadPage.clickTurnIn()

        Log.d(STEP_TAG, "Assert that the submission upload was successful.")
        shareExtensionStatusPage.assertPageObjects(30)
        shareExtensionStatusPage.assertAssignmentSubmissionSuccess()

        Log.d(STEP_TAG, "Click on 'Done' button.")
        shareExtensionStatusPage.clickOnDone()

        Log.d(STEP_TAG, "Click 'Recent Apps' device button and bring Canvas Student into the foreground again." +
                "Assert that the Dashboard Page is displayed.")
        device.pressRecentApps()
        device.findObject(UiSelector().descriptionContains("Canvas")).click()

        Log.d(STEP_TAG, "Assert that the Dashboard Page is displayed. Select ${course.name} and navigate to Assignments Page.")
        dashboardPage.assertPageObjects()
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()

        Log.d(STEP_TAG, "Click on $testAssignmentOne assignment and refresh the Assignment Details Page." +
                "Assert that the $testAssignmentOne assignment's status is 'Submitted'.")
        assignmentListPage.clickAssignment(testAssignmentOne)
        assignmentDetailsPage.refresh()
        assignmentDetailsPage.assertAssignmentSubmitted()

        Log.d(STEP_TAG, "Press 'Home' button on the device so it will take the Student application into the background.")
        device.pressHome()

        Log.d(STEP_TAG," Share the '$jpgTestFileName' and '$pdfTestFileName' files from the following uris: '$uri' and '$uri2'.")
        shareMultipleFiles(arrayListOf(uri, uri2))

        Log.d(STEP_TAG,"Click on the Canvas Student app.")
        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        Log.d(STEP_TAG,"Assert that the Share Extension Page is displayed and the " +
                "'My Files' button is selected by default, and so the '${student.name}' username is displayed as well.")
        shareExtensionTargetPage.assertPageObjects()
        shareExtensionTargetPage.assertFilesCheckboxIsSelected()
        shareExtensionTargetPage.assertUserName(student.name)

        Log.d(STEP_TAG, "Press 'Next' button.")
        shareExtensionTargetPage.pressNext()

        Log.d(STEP_TAG,"Assert that the title of the File Upload Page is correct and both of the shared files are displayed.")
        fileUploadPage.assertPageObjects()
        fileUploadPage.assertDialogTitle("Upload To My Files")
        fileUploadPage.assertFileDisplayed(jpgTestFileName)
        fileUploadPage.assertFileDisplayed(pdfTestFileName)

        Log.d(STEP_TAG,"Remove '$pdfTestFileName' file and assert that it's not displayed any more on the list but the other file is displayed.")
        fileUploadPage.removeFile(pdfTestFileName)
        fileUploadPage.assertFileNotDisplayed(pdfTestFileName)
        fileUploadPage.assertFileDisplayed("$pdfTestFileName.jpg")

        Log.d(STEP_TAG, "Click on 'Upload' button to upload the file.")
        fileUploadPage.clickUpload()

        Log.d(STEP_TAG, "Assert that the file upload (into my 'Files') was successful.")
        shareExtensionStatusPage.assertPageObjects()
        shareExtensionStatusPage.assertFileUploadSuccess()

        Log.d(STEP_TAG, "Click on 'Done' button.")
        shareExtensionStatusPage.clickOnDone()

        Log.d(STEP_TAG, "Click 'Recent Apps' device button and bring Canvas Student into the foreground again." +
                "Assert that the Dashboard Page is displayed.")
        device.pressRecentApps()
        device.findObject(UiSelector().descriptionContains("Canvas")).click()

        Log.d(STEP_TAG, "Press back button to navigate back to the Dashboard Page.")
        assignmentDetailsPage.assertPageObjects()
        ViewUtils.pressBackButton(3)

        Log.d(STEP_TAG, "Navigate to (Global) Files Page.")
        dashboardPage.assertPageObjects()
        Thread.sleep(4000) //Make sure that the toast message has disappeared.
        leftSideNavigationDrawerPage.clickFilesMenu()

        Log.d(STEP_TAG, "Assert that the 'unfiled' directory is displayed." +
                "Click on it, and assert that the previously uploaded file ($jpgTestFileName) is displayed within the folder.")
        fileListPage.assertItemDisplayed("unfiled")
        fileListPage.selectItem("unfiled")
        fileListPage.assertItemDisplayed(jpgTestFileName)

    }

    private fun setupFileOnDevice(fileName: String): Uri {
        copyAssetFileToExternalCache(activityRule.activity, fileName)

        val dir = activityRule.activity.externalCacheDir
        val file = File(dir?.path, fileName)

        val instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        return FileProvider.getUriForFile(
            instrumentationContext,
            "com.instructure.candroid" + Const.FILE_PROVIDER_AUTHORITY,
            file
        )
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
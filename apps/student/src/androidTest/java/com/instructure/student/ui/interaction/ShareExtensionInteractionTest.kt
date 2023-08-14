/*
 * Copyright (C) 2022 - present Instructure, Inc.
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

import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.User
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import java.io.File

@HiltAndroidTest
class ShareExtensionInteractionTest : StudentTest() {

    override fun displaysPageObjects() = Unit

    @Test
    fun shareExtensionShowsUpCorrectlyWhenSharingFileFromExternalSource() {
        val data = createMockData()
        val student = data.students[0]
        val uri = setupFileOnDevice("sample.jpg")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        login(student)
        device.pressHome()

        shareExternalFile(uri)

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.assertPageObjects()
        shareExtensionTargetPage.assertFilesCheckboxIsSelected()
        shareExtensionTargetPage.assertUserName(student.name)
    }

    @Test
    fun fileUploadDialogShowsCorrectlyForMyFilesUpload() {
        val data = createMockData()
        val student = data.students[0]

        File(getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()
        val uri = setupFileOnDevice("sample.jpg")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        login(student)
        device.pressHome()

        shareExternalFile(uri)

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.pressNext()

        fileUploadPage.assertPageObjects()
        fileUploadPage.assertDialogTitle("Upload To My Files")
        fileUploadPage.assertFileDisplayed("sample.jpg")
    }

    @Test
    fun addAndRemoveFileFromFileUploadDialog() {
        val data = createMockData()
        val student = data.students[0]
        val uri = setupFileOnDevice("sample.jpg")
        setupFileOnDevice("samplepdf.pdf")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        login(student)
        device.pressHome()

        shareExternalFile(uri)

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.pressNext()

        fileUploadPage.assertPageObjects()
        fileUploadPage.assertFileDisplayed("sample.jpg")

        fileUploadPage.removeFile("sample.jpg")

        // Add new file
        Intents.init()
        try {
            stubFilePickerIntent("samplepdf.pdf")
            fileUploadPage.chooseDevice()
        }
        finally {
            Intents.release()
        }

        fileUploadPage.assertFileNotDisplayed("sample.jpg")
        fileUploadPage.assertFileDisplayed("samplepdf.pdf")
    }

    @Test
    fun fileUploadDialogShowsCorrectlyForAssignmentSubmission() {
        val data = createMockData()
        val student = data.students[0]
        val uri = setupFileOnDevice("sample.jpg")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val assignment = data.addAssignment(data.courses.values.first().id, submissionType = Assignment.SubmissionType.ONLINE_UPLOAD)

        login(student)
        device.pressHome()

        shareExternalFile(uri)

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.selectSubmission()
        shareExtensionTargetPage.assertCourseSelectorDisplayedWithCourse(data.courses.values.first().name)
        shareExtensionTargetPage.assertAssignmentSelectorDisplayedWithAssignment(assignment.name!!)
        shareExtensionTargetPage.pressNext()

        fileUploadPage.assertPageObjects()
        fileUploadPage.assertDialogTitle("Submission")
        fileUploadPage.assertFileDisplayed("sample.jpg")
    }

    @Test
    fun shareExtensionNoAssignmentTest() {
        val data = createMockData()
        val student = data.students[0]
        val uri = setupFileOnDevice("sample.jpg")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        login(student)
        device.pressHome()

        shareExternalFile(uri)

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.selectSubmission()
        shareExtensionTargetPage.assertNoAssignmentSelectedStringDisplayed()
        shareExtensionTargetPage.pressNext()
        shareExtensionTargetPage.assertPageObjects() //Make sure that we are still on the Target Page.
    }


    // Clicking spinner item not working.
    @Test
    @Stub
    fun changeTargetAssignment() {
        val data = createMockData()
        val student = data.students[0]
        val uri = setupFileOnDevice("sample.jpg")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        data.addAssignment(data.courses.values.first().id, submissionType = Assignment.SubmissionType.ONLINE_UPLOAD)
        val assignment2 = data.addAssignment(data.courses.values.first().id, submissionType = Assignment.SubmissionType.ONLINE_UPLOAD)

        login(student)
        device.pressHome()

        shareExternalFile(uri)

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.selectSubmission()
        shareExtensionTargetPage.selectAssignment(assignment2.name!!)

        shareExtensionTargetPage.pressNext()

        fileUploadPage.assertPageObjects()
        fileUploadPage.assertDialogTitle("Submission")
        fileUploadPage.assertFileDisplayed("sample.jpg")
    }

    @Test
    fun shareExtensionShowsUpCorrectlyWhenSharingMultipleFiles() {
        val data = createMockData()
        val student = data.students[0]

        File(getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()
        val uri = setupFileOnDevice("sample.jpg")
        val uri2 = setupFileOnDevice("samplepdf.pdf")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        login(student)
        device.pressHome()

        shareMultipleFiles(arrayListOf(uri, uri2))

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.assertPageObjects()
        shareExtensionTargetPage.assertFilesCheckboxIsSelected()
        shareExtensionTargetPage.assertUserName(student.name)

        shareExtensionTargetPage.pressNext()

        fileUploadPage.assertPageObjects()
        fileUploadPage.assertFileDisplayed("sample.jpg")
        fileUploadPage.assertFileDisplayed("samplepdf.pdf")
    }

    @Test
    fun testFileAssignmentSubmission() {
        val data = createMockData()
        val student = data.students[0]
        val uri = setupFileOnDevice("sample.jpg")
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        data.addAssignment(data.courses.values.first().id, submissionType = Assignment.SubmissionType.ONLINE_UPLOAD)

        login(student)
        device.pressHome()

        shareExternalFile(uri)

        device.findObject(UiSelector().text("Canvas")).click()
        device.waitForIdle()

        shareExtensionTargetPage.selectSubmission()
        shareExtensionTargetPage.pressNext()
        fileUploadPage.clickTurnIn()

        shareExtensionStatusPage.assertPageObjects()
        shareExtensionStatusPage.assertAssignmentSubmissionSuccess()
    }

    private fun createMockData(): MockCanvas {

        val data = MockCanvas.init(
            studentCount = 1,
            teacherCount = 1,
            courseCount = 1,
            favoriteCourseCount = 1
        )

        return data
    }

    private fun login(student: User) {
        val token = MockCanvas.data.tokenFor(student)
        tokenLogin(MockCanvas.data.domain, token!!, student)
    }

    private fun shareExternalFile(uri: Uri) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpg"
        }

        val chooser = Intent.createChooser(intent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        InstrumentationRegistry.getInstrumentation().context.startActivity(chooser)
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
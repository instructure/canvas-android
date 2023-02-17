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
package com.emeritus.student.ui.interaction

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addAssignment
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Assignment
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.emeritus.student.ui.utils.StudentTest
import com.emeritus.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.core.AllOf
import org.junit.Before
import org.junit.Test
import java.io.File

@HiltAndroidTest
class PickerSubmissionUploadInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit

    private val mockedFileName = "sample.jpg" // A file in our assets area
    private lateinit var activity : Activity
    private lateinit var activityResult: Instrumentation.ActivityResult

    @Before
    fun setUp() {
        // Read this at set-up, because it may become nulled out soon thereafter
        activity = activityRule.activity

        // Copy our sample file from the assets area to the external cache dir
        copyAssetFileToExternalCache(activity, mockedFileName)

        // Now create an ActivityResult that points to the sample file in the external cache dir
        val resultData = Intent()
        val dir = activity.externalCacheDir
        val file = File(dir?.path, mockedFileName)
        val uri = Uri.fromFile(file)
        resultData.data = uri
        activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testFab_camera() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testFab_galleryPicker() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testFab_filePicker() {

    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, true)
    fun testDeleteFile() {

    }

    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION, false)
    fun testSubmit() {
        val data = goToSubmissionPicker()

        // Let's mock grabbing a file from our device
        Intents.init()
        try {
            // Set up the "from device" mock result, then press the "device" icon
            Intents.intending(
                    AllOf.allOf(
                            IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT),
                            IntentMatchers.hasType("*/*"),
                            IntentMatchers.hasFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    )
            ).respondWith(activityResult)
            pickerSubmissionUploadPage.chooseDevice()
        }
        finally {
            Intents.release()
        }

        // It's possible for the Submit button to wait a beat before appearing
        pickerSubmissionUploadPage.waitForSubmitButtonToAppear()

        // Now submit the file
        pickerSubmissionUploadPage.submit()

        // The screen may go through several refactorings while the submission is being submitted,
        // which could potentially throw off our tests.  So wait until we get the "all clear".
        assignmentDetailsPage.waitForSubmissionComplete()

        // Should be back to assignment details page
        assignmentDetailsPage.goToSubmissionDetails()
        submissionDetailsPage.openFiles()
        submissionDetailsPage.assertFileDisplayed(mockedFileName)
        // The submission details screen will show "This media format is not supported" because
        // we're not yet processing the binary jpg data correctly in our mocked server.
        // But the file name should still be displayed, so we're going to be
        // happy with that.
    }

    // Seed course, user, assignment and navigate to submission picker for assignment
    private fun goToSubmissionPicker() : MockCanvas {

        // Basic mock setup
        val data = MockCanvas.init(
                courseCount = 1,
                favoriteCourseCount = 1,
                studentCount = 1,
                teacherCount = 1
        )

        val student = data.students.first()
        val course = data.courses.values.first()

        // Let's set up an assignment that requires an online upload
        val assignment = data.addAssignment(
                courseId = course.id,
                submissionType = Assignment.SubmissionType.ONLINE_UPLOAD
        )

        // Sign in
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        // Navigate to submission picker page
        dashboardPage.selectCourse(course)
        courseBrowserPage.selectAssignments()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.clickSubmit()

        return data
    }
}
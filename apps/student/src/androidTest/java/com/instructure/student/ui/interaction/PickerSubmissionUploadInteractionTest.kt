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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.Intents.release
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.GrantPermissionRule
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils
import com.google.android.apps.common.testing.accessibility.framework.checks.SpeakableTextPresentCheck
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvas.espresso.common.pages.compose.AssignmentListPage
import com.instructure.canvas.espresso.mockcanvas.MockCanvas
import com.instructure.canvas.espresso.mockcanvas.addAssignment
import com.instructure.canvas.espresso.mockcanvas.fakes.FakeCustomGradeStatusesManager
import com.instructure.canvas.espresso.mockcanvas.init
import com.instructure.canvasapi2.di.graphql.CustomGradeStatusModule
import com.instructure.canvasapi2.managers.graphql.CustomGradeStatusesManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.pandautils.utils.FilePrefs
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.AllOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@HiltAndroidTest
@UninstallModules(CustomGradeStatusModule::class)
class PickerSubmissionUploadInteractionTest : StudentTest() {

    @BindValue
    @JvmField
    val customGradeStatusesManager: CustomGradeStatusesManager = FakeCustomGradeStatusesManager()

    override fun displaysPageObjects() = Unit

    private val mockedFileName = "sample.jpg" // A file in our assets area
    private lateinit var activity : Activity
    private lateinit var activityResult: Instrumentation.ActivityResult

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    val assignmentListPage by lazy { AssignmentListPage(composeTestRule) }

    @Before
    fun setUp() {
        // Read this at set-up, because it may become null soon thereafter
        activity = activityRule.activity

        //Clear file upload cache dir.
        File(getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()

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

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.CAMERA)


    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testFab_camera() {
        goToSubmissionPicker()

        Intents.init()
        try {
            intending(allOf(
                hasAction(MediaStore.ACTION_IMAGE_CAPTURE),
                hasExtraWithKey(MediaStore.EXTRA_OUTPUT)
            )).respondWithFunction { intent ->
                val outputUri = intent.extras?.get(MediaStore.EXTRA_OUTPUT) as? Uri
                if (outputUri != null) {
                    val context = getInstrumentation().targetContext
                    val dir = context.externalCacheDir
                    val sampleFile = File(dir, mockedFileName)
                    if (outputUri.scheme == "file") {
                        val destFile = File(outputUri.path!!)
                        destFile.parentFile?.mkdirs()
                        sampleFile.copyTo(destFile, overwrite = true)
                    } else if (outputUri.scheme == "content") {
                        context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                            sampleFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                }
                Instrumentation.ActivityResult(Activity.RESULT_OK, Intent())
            }
            pickerSubmissionUploadPage.chooseCamera()
        } finally {
            release()
        }

        pickerSubmissionUploadPage.waitForSubmitButtonToAppear()

        val fileName = File(Uri.parse(FilePrefs.tempCaptureUri).path!!).name
        pickerSubmissionUploadPage.assertFileDisplayed(fileName)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testFab_galleryPicker() {
        goToSubmissionPicker()

        Intents.init()
        try {
            intending(
                AllOf.allOf(
                        hasAction(Intent.ACTION_PICK),
                        IntentMatchers.hasType("image/*"),
                        IntentMatchers.hasFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            ).respondWith(activityResult)
            pickerSubmissionUploadPage.chooseGallery()
        }
        finally {
            release()
        }

        pickerSubmissionUploadPage.waitForSubmitButtonToAppear()

        pickerSubmissionUploadPage.assertFileDisplayed(mockedFileName)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testFab_filePicker() {
        goToSubmissionPicker()

        Intents.init()
        try {
            intending(
                AllOf.allOf(
                    hasAction(Intent.ACTION_GET_CONTENT),
                    IntentMatchers.hasType("*/*"),
                    IntentMatchers.hasFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            ).respondWith(activityResult)
            pickerSubmissionUploadPage.chooseDevice()
        }
        finally {
            release()
        }

        pickerSubmissionUploadPage.waitForSubmitButtonToAppear()

        pickerSubmissionUploadPage.assertFileDisplayed(mockedFileName)
    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testFab_scanner(){
        val scannerComponent = "com.instructure.student.features.documentscanning.DocumentScanningActivity"

        goToSubmissionPicker()

        Intents.init()
        try {
            val context = getInstrumentation().targetContext
            val dir = context.externalCacheDir
            val sampleFile = File(dir, mockedFileName)
            val uri = Uri.fromFile(sampleFile)
            val resultData = Intent().apply { data = uri }
            val scannerResult = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

            intending(
                IntentMatchers.hasComponent(scannerComponent)
            ).respondWith(scannerResult)

            pickerSubmissionUploadPage.chooseScanner()
        } finally {
            release()
        }

        pickerSubmissionUploadPage.waitForSubmitButtonToAppear()

        pickerSubmissionUploadPage.assertFileDisplayed(mockedFileName)

    }

    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testDeleteFile() {
        goToSubmissionPicker()

        Intents.init()
        try {
            intending(
                AllOf.allOf(
                    hasAction(Intent.ACTION_GET_CONTENT),
                    IntentMatchers.hasType("*/*"),
                    IntentMatchers.hasFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            ).respondWith(activityResult)
            pickerSubmissionUploadPage.chooseDevice()
        }
        finally {
            release()
        }

        pickerSubmissionUploadPage.waitForSubmitButtonToAppear()
        pickerSubmissionUploadPage.clickDeleteButton()
        pickerSubmissionUploadPage.assertEmptyViewDisplayed()
    }

    @Stub
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.SUBMISSIONS, TestCategory.INTERACTION)
    fun testSubmit() {
        val data = goToSubmissionPicker()
        val assignment = data.assignments.values.first()

        Intents.init()
        try {
            intending(
                    AllOf.allOf(
                            hasAction(Intent.ACTION_GET_CONTENT),
                            IntentMatchers.hasType("*/*"),
                            IntentMatchers.hasFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    )
            ).respondWith(activityResult)

            pickerSubmissionUploadPage.chooseDevice()

        } finally {
            release()
        }

        pickerSubmissionUploadPage.waitForSubmitButtonToAppear()
        pickerSubmissionUploadPage.assertFileDisplayed(mockedFileName)

        pickerSubmissionUploadPage.submit()

        composeTestRule.waitForIdle()
        Espresso.pressBack()
        assignmentListPage.clickAssignment(assignment)
        assignmentDetailsPage.waitForSubmissionComplete()

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
                submissionTypeList = listOf(Assignment.SubmissionType.ONLINE_UPLOAD)
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

    override fun enableAndConfigureAccessibilityChecks() {
        extraAccessibilitySupressions = allOf(
            AccessibilityCheckResultUtils.matchesCheck(
                SpeakableTextPresentCheck::class.java
            ),
            AccessibilityCheckResultUtils.matchesViews(
                ViewMatchers.withParent(
                    ViewMatchers.withClassName(
                        Matchers.equalTo(ComposeView::class.java.name)
                    )
                )
            )
        )

        super.enableAndConfigureAccessibilityChecks()
    }
}
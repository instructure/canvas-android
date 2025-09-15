/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.ui.interaction

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.test.espresso.intent.ActivityResultFunction
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.annotations.Stub
import com.instructure.canvas.espresso.annotations.StubCoverage
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.pandautils.utils.Const
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.extensions.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@HiltAndroidTest
class UserFilesInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var activity : Activity
    private lateinit var activityResult: Instrumentation.ActivityResult

    // This will give our test(s) permission to take a picture with the device camera
    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
    )

    @Before
    fun setUp() {
        // Read this at set-up, because it may become nulled out soon thereafter
        activity = activityRule.activity

        // Copy our sample.jpg file from the assets area to the external cache dir
        copyAssetFileToExternalCache(activity, "sample.jpg")

        // Now create an ActivityResult that points to the sample.jpg in the external cache dir
        val resultData = Intent()
        val dir = activity.externalCacheDir
        val file = File(dir?.path, "sample.jpg")
        val uri = FileProvider.getUriForFile(
            activityRule.activity,
            "com.instructure.candroid" + Const.FILE_PROVIDER_AUTHORITY,
            file
        )
        resultData.data = uri
        activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    // For the fileFromCamera test, the app code creates a path/name for the file containing
    // the new photo.  We'll copy our sample.jpg picture to that location.
    private fun copySampleTo(newFilePath: String) {
        val fileFrom = File(activity.externalCacheDir, "sample.jpg")
        val fileTo = File(activity.externalCacheDir, newFilePath)
        fileFrom.copyTo(target = fileTo, overwrite = true)
    }

    // Should be able to upload a file from the user's device
    // Mocks the result from the expected intent, then uploads it.
    @Test
    @StubCoverage("Cannot init FileUploadWorker and OfflineSyncWorker")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.FILES, TestCategory.INTERACTION)
    fun testUpload_deviceFile() {
        goToFilePicker()

        Intents.init()
        try {
            // Set up the "from device" mock result, then press "from device"
            intending(
                    allOf(
                            hasAction(Intent.ACTION_GET_CONTENT),
                            hasType("*/*")
                    )
            ).respondWith(activityResult)
            fileChooserPage.chooseDevice()
        }
        finally {
            Intents.release()
        }

        // Now press the "Upload" button and verify that the file shows up in our list
        fileChooserPage.clickUpload()
        // Should be on file list page now
        fileListPage.refresh()
        fileListPage.assertItemDisplayed("sample.jpg")
    }
    
    // Should be able to upload a file from the camera
    // Mocks the result from the expected intent, then uploads it.
    @Test
    @StubCoverage("Cannot init FileUploadWorker and OfflineSyncWorker")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.FILES, TestCategory.INTERACTION)
    fun testUpload_fileFromCamera() {

        goToFilePicker()

        var fileName : String? = null // Will be set from the intent

        Intents.init()
        try {
            // Set up our mock result for the image capture, then choose "from camera"
            // This is a little different than the others -- we have to make sure that the
            // file/path specified in the intent is created and populated.  That means
            // that we have to use "respondWithFunction", so that we can read the incoming intent.
            intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWithFunction(object : ActivityResultFunction {
                override fun apply(intent: Intent?): Instrumentation.ActivityResult {
                    val uri = intent?.extras?.get(MediaStore.EXTRA_OUTPUT)
                    fileName = (uri as Uri).pathSegments.takeLast(1).first()
                    val newFilePath = uri.pathSegments.takeLast(2).joinToString(separator="/")
                    copySampleTo(newFilePath)

                    var resultData = Intent()
                    resultData.data = uri
                    return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
                }
            })
            fileChooserPage.chooseCamera()
        }
        finally {
            Intents.release()
        }

        // Now upload our new image and verify that it now shows up in the file list.
        fileChooserPage.clickUpload()
        // Should be on fileListPage by now
        fileListPage.refresh()
        fileListPage.assertItemDisplayed(fileName!!)
    }

    // Should be able to upload a file from the user's photo gallery
    // Mocks the result from the expected intent, then uploads it.
    @Test
    @StubCoverage("Cannot init FileUploadWorker and OfflineSyncWorker")
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.FILES, TestCategory.INTERACTION)
    fun testUpload_gallery() {
        goToFilePicker()

        Intents.init()

        try {
            // Set up the "from gallery" mock result, then press "from gallery"
            intending(
                    allOf(
                        hasAction(Intent.ACTION_GET_CONTENT),
                        hasType("image/*")
                    )
            ).respondWith(activityResult)
            fileChooserPage.chooseGallery()
        }
        finally {
            Intents.release()
        }

        // Now upload our file and verify that it shows up in the file list
        fileChooserPage.clickUpload()
        // Should be on file list page now
        fileListPage.refresh()
        fileListPage.assertItemDisplayed("sample.jpg")

    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.FILES, TestCategory.INTERACTION)
    fun testView_previewAudio() {
        // Should be able to preview an audio file
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.FILES, TestCategory.INTERACTION)
    fun testView_previewVideo() {
        // Should be able to preview a video file
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.FILES, TestCategory.INTERACTION)
    fun testView_createDirectory() {
        // Should be able to create a directory and upload a file to that directory
    }

    @Stub
    @Test
    @TestMetaData(Priority.COMMON, FeatureCategory.FILES, TestCategory.INTERACTION)
    fun testView_previewImage() {
        // Should be able to preview an image file
    }

    // TODO - Add all interaction tests for supported file view/previews

    // Set up some rudimentary mock data, navigate to the file list page, then
    // initiate a file upload
    private fun goToFilePicker() : MockCanvas {

        File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()

        val data = MockCanvas.init(
                courseCount = 1,
                favoriteCourseCount = 1,
                studentCount = 1,
                teacherCount = 1
        )

        val student = data.students.first()

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        leftSideNavigationDrawerPage.clickFilesMenu()
        fileListPage.clickAddButton()
        fileListPage.clickUploadFileButton()

        return data
    }
}
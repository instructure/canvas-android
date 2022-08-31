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
 */
package com.instructure.student.ui.interaction

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.intent.ActivityResultFunction
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.rule.GrantPermissionRule
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.espresso.InstructureActivityTestRule
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.activity.LoginActivity
import com.instructure.student.features.shareextension.StudentShareExtensionActivity
import com.instructure.student.ui.utils.StudentActivityTestRule
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File


@HiltAndroidTest
class ShareExtensionInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit

    /*override val activityRule: InstructureActivityTestRule<out Activity> =
        StudentActivityTestRule(StudentShareExtensionActivity::class.java)*/

    private lateinit var activity : Activity
    private lateinit var activityResult: Instrumentation.ActivityResult

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.CAMERA
    )

    @Test
    fun shareExtensionMyFilesSuccessfulTest() {
        //myObj = MyObject.mockObject();
        val data = MockCanvas.init(
            courseCount = 1,
            favoriteCourseCount = 1,
            studentCount = 1,
            teacherCount = 1
        )

        val student = data.students.first()

        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
    //    dashboardPage.waitForRender()

        val loginIntent = LoginActivity.createIntent(getApplicationContext())

        val intent = StudentShareExtensionActivity.createIntent(getApplicationContext())
   //     startActivity(getApplicationContext(),loginIntent, null)
        val activityRule: InstructureActivityTestRule<out Activity> =
            StudentActivityTestRule(StudentShareExtensionActivity::class.java)
        activityRule.runOnUiThread {
            (originalActivity as LoginActivity).generateLoginData(
                token,
                data.domain,
                student
            )
        }
        activityRule.launchActivity(intent)

        activityRule.runOnUiThread {
       //     activityRule.launchActivity(loginIntent)
            val int = activityRule.activity.intent
        }
        activityRule.launchActivity(intent)
        val a = activityRule.activity
    //    activityRule.
        val i = Intent();
       //i.putExtra("myobj", myObj);
        val extras: Bundle? = i.extras



        ActivityScenario.launch(StudentShareExtensionActivity::class.java)
     //   activityRule.launchActivity(i);
        print("asd")
        //...
    }

    @Before
    fun setUp() {
        activity = activityRule.activity
        copyAssetFileToExternalCache(activity, "sample.jpg")

        val resultData = Intent()
        val dir = activity.externalCacheDir
        val file = File(dir?.path, "sample.jpg")
        val uri = Uri.fromFile(file)
        resultData.data = uri
        activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }
    
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.FILES, TestCategory.INTERACTION, false)
    fun fileUploadFromCameraTest() {

        navigateToFilePicker()
        var fileName : String? = null

        Intents.init()
        try {
            intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWithFunction(object : ActivityResultFunction {
                override fun apply(intent: Intent?): Instrumentation.ActivityResult {
                    val uri = intent?.extras?.get(MediaStore.EXTRA_OUTPUT)
                    fileName = (uri as Uri).pathSegments.takeLast(1).first()

                    val newFilePath = uri.pathSegments.takeLast(2).joinToString(separator="/")
                    val fileFrom = File(activity.externalCacheDir, "sample.jpg")
                    val fileTo = File(activity.externalCacheDir, newFilePath)
                    fileFrom.copyTo(target = fileTo, overwrite = true)

                    var resultData = Intent()
                    resultData.data = uri
                    return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
                }
            })
            fileUploadPage.chooseCamera()
        }
        finally {
            Intents.release()
        }

        fileUploadPage.clickUpload()
        fileListPage.refresh()
        fileListPage.assertItemDisplayed(fileName!!)
    }

    private fun navigateToFilePicker() : MockCanvas {
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

        dashboardPage.gotoGlobalFiles()
        fileListPage.clickAddButton()
        fileListPage.clickUploadFileButton()

        return data
    }
}
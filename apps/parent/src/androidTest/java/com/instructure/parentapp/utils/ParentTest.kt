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

package com.instructure.parentapp.utils

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvas.espresso.CanvasTest
import com.instructure.canvas.espresso.common.pages.AboutPage
import com.instructure.canvas.espresso.common.pages.CanvasNetworkSignInPage
import com.instructure.canvas.espresso.common.pages.FileChooserPage
import com.instructure.canvas.espresso.common.pages.InboxPage
import com.instructure.canvas.espresso.common.pages.LegalPage
import com.instructure.canvas.espresso.common.pages.LoginFindSchoolPage
import com.instructure.canvas.espresso.common.pages.LoginLandingPage
import com.instructure.canvas.espresso.common.pages.LoginSignInPage
import com.instructure.canvas.espresso.common.pages.WrongDomainPage
import com.instructure.pandautils.utils.Const
import com.instructure.parentapp.BuildConfig
import com.instructure.parentapp.features.login.LoginActivity
import com.instructure.parentapp.ui.pages.classic.DashboardPage
import com.instructure.parentapp.ui.pages.classic.FrontPagePage
import com.instructure.parentapp.ui.pages.classic.HelpPage
import com.instructure.parentapp.ui.pages.classic.LeftSideNavigationDrawerPage
import com.instructure.parentapp.ui.pages.compose.SyllabusPage
import org.hamcrest.core.AllOf
import java.io.File


abstract class ParentTest : CanvasTest() {

    override val isTesting = BuildConfig.IS_TESTING

    override val activityRule = ParentActivityTestRule(LoginActivity::class.java)

    //Regular pages (non-compose)
    val dashboardPage = DashboardPage()
    val leftSideNavigationDrawerPage = LeftSideNavigationDrawerPage()
    val helpPage = HelpPage()
    val syllabusPage = SyllabusPage()
    val frontPagePage = FrontPagePage()
    val fileChooserPage = FileChooserPage()

    // Common pages (it's common for all apps)
    val loginLandingPage = LoginLandingPage()
    val canvasNetworkSignInPage = CanvasNetworkSignInPage()
    val loginFindSchoolPage = LoginFindSchoolPage()
    val loginSignInPage = LoginSignInPage()
    val wrongDomainPage = WrongDomainPage()
    val inboxPage = InboxPage()
    val legalPage = LegalPage()
    val aboutPage = AboutPage()

    fun setupFileOnDevice(fileName: String): Uri {
        File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "file_upload").deleteRecursively()
        copyAssetFileToExternalCache(activityRule.activity, fileName)

        val dir = activityRule.activity.externalCacheDir
        val file = File(dir?.path, fileName)

        val instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        return FileProvider.getUriForFile(
            instrumentationContext,
            "com.instructure.parentapp" + Const.FILE_PROVIDER_AUTHORITY,
            file
        )
    }

    fun stubFilePickerIntent(fileName: String) {
        val resultData = Intent()
        val dir = activityRule.activity.externalCacheDir
        val file = File(dir?.path, fileName)
        val newFileUri = FileProvider.getUriForFile(
            activityRule.activity,
            "com.instructure.parentapp" + Const.FILE_PROVIDER_AUTHORITY,
            file
        )
        resultData.data = newFileUri
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Intents.intending(
            AllOf.allOf(
                IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT),
                IntentMatchers.hasType("*/*"),
            )
        ).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, resultData))
    }

}
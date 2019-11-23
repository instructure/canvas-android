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

import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.Stub
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.hamcrest.CoreMatchers
import org.junit.Test

class SettingsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_askQuestion() {
        // Should open a dialog and send a question for the selected course
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        helpPage.askAQuestion(course, "Here's a question")
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_searchCanvasGuides() {
        // Should open the Canvas guides in a WebView
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        helpPage.launchGuides()
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.ID, "links", "Community Guidelines")
        )
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_reportAProblem() {
        // Should send an error report
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        helpPage.reportAProblem("Problem", "It's a problem!")
    }

//    @Stub
//    @Test
//    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
//    fun testHelp_submitFeatureIdea() {
//        // Should send a pre-filled email intent. Should be addressed to mobilesupport@instructure.com.
//        setUpAndSignIn()
//
//        dashboardPage.launchSettingsPage()
//        settingsPage.launchHelpPage()
//
//        Intents.init()
//        try {
//            val pickerIntent = IntentMatchers.hasAction(Intent.ACTION_CHOOSER)
//            val expectedIntent = CoreMatchers.allOf(
//                    IntentMatchers.hasAction(Intent.ACTION_SEND),
//                    IntentMatchers.hasType("message/rfc822")
////                    CoreMatchers.anyOf(
////                            IntentMatchers.hasExtra(Intent.EXTRA_EMAIL, arrayOf("support@instructure.com")),
////                            IntentMatchers.hasExtra(Intent.EXTRA_EMAIL, arrayOf("mobilesupport@instructure.com"))
////                    )
//            )
//            Intents.intending(pickerIntent).respondWith(Instrumentation.ActivityResult(0, null))
//            Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))
//            helpPage.submitFeature()
//            Intents.intended(expectedIntent)
//        }
//        finally {
//            Intents.release()
//        }
//    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testHelp_shareYourLove() {
        // Should send an intent to open the listing for Student App in the Play Store
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        Intents.init()
        try {
            val expectedIntent = CoreMatchers.allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    CoreMatchers.anyOf(
                            IntentMatchers.hasData("market://details?id=com.instructure.candroid"),
                            IntentMatchers.hasData("https://play.google.com/store/apps/details?id=com.instructure.candroid")
                    )
            )
            Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))
            helpPage.shareYourLove()
            Intents.intended(expectedIntent)
        }
        finally {
            Intents.release()
        }
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testLegal_showCanvasOnGithub() {
        // Should display a list of open source dependencies used in the app, along with their licenses
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchLegalPage()

        Intents.init()
        try {
            val expectedIntent = CoreMatchers.allOf(IntentMatchers.hasAction(Intent.ACTION_VIEW), IntentMatchers.hasData("https://github.com/instructure/canvas-android"))
            Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))
            legalPage.openCanvasOnGithub()
            Intents.intended(expectedIntent)
        }
        finally {
            Intents.release()
        }
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testLegal_showTermsOfUse() {
        // Should display terms of use in a WebView
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchLegalPage()
        legalPage.openTermsOfUse()
        legalPage.assertTermsOfUseDisplayed()
    }

    @Stub
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, true)
    fun testLegal_showPrivacyPolicy() {
        // Should display the privacy policy in a WebView
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchLegalPage()
        legalPage.openPrivacyPolicy()
        canvasWebViewPage.runTextChecks(
                WebViewTextCheck(Locator.CLASS_NAME, "subnav-wrapper", "POLICIES HOME")
        )
    }

    // Mock a single student and course, sign in, then navigate to the dashboard.
    private fun setUpAndSignIn(): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
                studentCount = 1,
                courseCount = 1,
                favoriteCourseCount = 1)

        course = data.courses.values.first()
        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        return data
    }

}

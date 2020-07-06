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
import android.util.Log
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.web.webdriver.Locator
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs

import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.pages.WebViewTextCheck
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

class SettingsInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        // If we try to read this later, it may be null, possibly because we will have navigated
        // away from our initial activity.
        activity = activityRule.activity


    }

    // Should open a dialog and send a question for the selected course
    // (Checks to see that we can fill out the question and the SEND button exists.)
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_askQuestion() {

        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        helpPage.verifyAskAQuestion(course, "Here's a question")
    }

    // Should open the Canvas guides in a WebView
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_searchCanvasGuides() {
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        helpPage.launchGuides()
        canvasWebViewPage.runTextChecks(
                // Potentially brittle -- the web content could be changed by another team
                WebViewTextCheck(Locator.ID, "links", "Community Guidelines", 25)
        )
    }

    // Should send an error report
    // (Checks to see that we can fill out an error report and that the SEND button is displayed.)
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_reportAProblem() {

        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        helpPage.verifyReportAProblem("Problem", "It's a problem!")
    }

    // Should send a pre-filled email intent. Should be addressed to mobilesupport@instructure.com.
    //
    // There is a LOT of aspirational code here, in that we would like to be able to handle
    // an intent for a specific email app if one is present.  However, our app always launches
    // an email app chooser, even if there is only one option.
    //
    // So this is a watered-down test that just checks whether an email app chooser gets displayed.
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_submitFeatureIdea() {
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()

        // Figure out which email apps we have installed on the device
        var pkgMgr = activity.packageManager
        var intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822"
        val activities = pkgMgr.queryIntentActivities(intent, 0)
        val matchedChooserActivities = activities.count()
        for (activity in activities) {
            Log.d("submitFeatureIdea","Resolved activity = $activity")
        }

        Intents.init()
        try {
            // Try to formulate what an email app chooser intent would look like, and how we might resolve it
            val chooserIntentMatcher = IntentMatchers.hasAction(Intent.ACTION_CHOOSER)
            val expectedChooserIntent = Intent(Intent.ACTION_SEND)
            expectedChooserIntent.type = "message/rfc822"
            expectedChooserIntent.`package` = "com.google.android.gm"

            // Formulate what an actual email intent (NOT a chooser intent) would look like
            val emailIntentMatcher = CoreMatchers.allOf(
                    IntentMatchers.hasAction(Intent.ACTION_SEND),
                    IntentMatchers.hasType("message/rfc822"),
                    CoreMatchers.anyOf(
                            IntentMatchers.hasExtra(Intent.EXTRA_EMAIL, arrayOf("support@instructure.com")),
                            IntentMatchers.hasExtra(Intent.EXTRA_EMAIL, arrayOf("mobilesupport@instructure.com"))
                    )
            )

            // Set up our intent catchers
            Intents.intending(chooserIntentMatcher).respondWith(Instrumentation.ActivityResult(0, expectedChooserIntent))
            Intents.intending(emailIntentMatcher).respondWith(Instrumentation.ActivityResult(0, null))

            // Press the "Submit Feature" button
            helpPage.submitFeature()

//            // Depending on how many activities matched our email intent...
//            if(matchedChooserActivities > 1) {
//                // If multiple, just check that the chooser appeared.
//                Intents.intended(chooserIntentMatcher)
//            }
//            else if(matchedChooserActivities == 1){
//                // If single, check that our email intent was dispatched
//                Intents.intended(emailIntentMatcher)
//            }
//            else {
//                // If none, there is nothing much to do here
//                Log.d("submitFeatureIdea","Not matched activities for SEND on device!")
//            }

            // :-( Our production code creates a chooser every time, even if there is only one email app option...
            Intents.intended(chooserIntentMatcher)
        }
        finally {
            Intents.release()
        }
    }

    // Should send an intent to open the listing for Student App in the Play Store
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_shareYourLove() {
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchHelpPage()
        Intents.init()
        try {
            val expectedIntent = CoreMatchers.allOf(
                    IntentMatchers.hasAction(Intent.ACTION_VIEW),
                    CoreMatchers.anyOf(
                            // Could be either of these, depending on whether the play store app is installed
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

    // Should launch an intent to go to our canvas-android github page
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testLegal_showCanvasOnGithub() {
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

    // Should display terms of use in a WebView
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testLegal_showTermsOfUse() {
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchLegalPage()
        legalPage.openTermsOfUse()
        legalPage.assertTermsOfUseDisplayed()
    }

    // Should display the privacy policy in a WebView
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testLegal_showPrivacyPolicy() {
        setUpAndSignIn()

        dashboardPage.launchSettingsPage()
        settingsPage.launchLegalPage()
        legalPage.openPrivacyPolicy()
        canvasWebViewPage.acceptCookiePolicyIfNecessary()
        canvasWebViewPage.runTextChecks(
                // Potentially brittle, as this content could be changed by another team.
                WebViewTextCheck(Locator.CLASS_NAME, "subnav-wrapper", "Privacy", 20)
        )
    }

    // Should open a page and have a pairing code that can be refreshed
    // (Checks to see that we can refresh and get a new code)
    @Test
    @TestMetaData(Priority.P0, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testPairObserver_refreshCode() {

        // Let's simulate the QR_PAIR_OBSERVER_ENABLED remote-config flag being on,
        // remembering our original value.
        val originalVal = RemoteConfigPrefs.getString(RemoteConfigParam.QR_PAIR_OBSERVER_ENABLED.rc_name);
        RemoteConfigPrefs.putString(RemoteConfigParam.QR_PAIR_OBSERVER_ENABLED.rc_name, "true")

        try {
            setUpAndSignIn()

            ApiPrefs.canGeneratePairingCode = true
            dashboardPage.launchSettingsPage()
            settingsPage.launchPairObserverPage()

            pairObserverPage.hasCode("1")
            pairObserverPage.refresh()
            pairObserverPage.hasCode("2")
        }
        finally {
            // Restore the original remote-config setting
            if(originalVal == null) {
                RemoteConfigPrefs.remove(RemoteConfigParam.QR_PAIR_OBSERVER_ENABLED.rc_name);
            }
            else {
                RemoteConfigPrefs.putString(RemoteConfigParam.QR_PAIR_OBSERVER_ENABLED.rc_name, originalVal);
            }
        }
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

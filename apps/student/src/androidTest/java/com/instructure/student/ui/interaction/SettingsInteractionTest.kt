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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.StubMultiAPILevel
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.student.ui.utils.StudentComposeTest
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class SettingsInteractionTest : StudentComposeTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var course: Course
    private lateinit var activity: Activity

    @Before
    fun setUp() {
        // If we try to read this later, it may be null, possibly because we will have navigated
        // away from our initial activity.
        activity = activityRule.activity


    }

    // Should launch an intent to go to our canvas-android github page
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testLegal_showCanvasOnGithub() {
        setUpAndSignIn()

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Legal")

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
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testLegal_showTermsOfUse() {
        setUpAndSignIn()

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Legal")
        legalPage.openTermsOfUse()
        legalPage.assertTermsOfUseDisplayed()
    }

    // Should display the privacy policy in a WebView
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    @StubMultiAPILevel("Failed API levels = { 28 }", "Somehow the Privacy Policy URL does not load on API lvl 28, but does on other API lvl devices.")
    fun testLegal_showPrivacyPolicy() {
        setUpAndSignIn()

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Legal")
        legalPage.openPrivacyPolicy()
        canvasWebViewPage.acceptCookiePolicyIfNecessary()
        canvasWebViewPage.checkWebViewURL("https://www.instructure.com/policies/product-privacy-policy")

    }

    // Should open a page and have a pairing code that can be refreshed
    // (Checks to see that we can refresh and get a new code)
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testPairObserver_refreshCode() {
        setUpAndSignIn()

        ApiPrefs.canGeneratePairingCode = true
        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.clickOnSettingsItem("Pair with Observer")

        pairObserverPage.hasCode("1")
        pairObserverPage.refresh()
        pairObserverPage.hasCode("2")
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testOfflineContent_notDisplayedIfFeatureIsDisabled() {
        setUpAndSignIn(offlineEnabled = false)

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertOfflineContentNotDisplayed()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testOfflineContent_displayedIfFeatureIsEnabled() {
        setUpAndSignIn(offlineEnabled = true)

        leftSideNavigationDrawerPage.clickSettingsMenu()
        settingsPage.assertSettingsItemDisplayed("Synchronization")
    }

    // Mock a single student and course, sign in, then navigate to the dashboard.
    private fun setUpAndSignIn(offlineEnabled: Boolean = false): MockCanvas {

        // Basic info
        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = 1,
            favoriteCourseCount = 1
        )

        data.offlineModeEnabled = offlineEnabled

        course = data.courses.values.first()
        // Sign in
        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLogin(data.domain, token, student)
        dashboardPage.waitForRender()

        return data
    }
}

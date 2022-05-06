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
import android.os.Build
import android.util.Log
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.panda_annotations.FeatureCategory
import com.instructure.panda_annotations.Priority
import com.instructure.panda_annotations.TestCategory
import com.instructure.panda_annotations.TestMetaData
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.student.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class NavigationDrawerInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var student1: User
    private lateinit var student2: User
    private lateinit var course: Course

    private lateinit var activity: Activity

    @Before
    fun setUp() {
        // If we try to read this later, it may be null, possibly because we will have navigated
        // away from our initial activity.
        activity = activityRule.activity


    }

    // Should be able to change the user from the navigation drawer
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.INTERACTION, false)
    fun testNavDrawer_changeUser() {

        // This test fails on API-28 in FTL due to a "TOO_MANY_REGISTRATIONS" issue on logout.
        // IMO, this is not something that we can fix.  So let's not run the test.
        if(Build.VERSION.SDK_INT == 28) {
            return
        }

        // Sign in student 1, then sign him out
        val data = signInStudent()

        // Need to remember student1 via PreviousUserUtils in order to be able to "change user"
        // back to student1.
        PreviousUsersUtils.add(ContextKeeper.appContext, SignedInUser(
                user = student1,
                domain = data.domain,
                protocol = ApiPrefs.protocol,
                token = data.tokenFor(student1)!!,
                accessToken = "",
                refreshToken = "",
                clientId = "",
                clientSecret = "",
                calendarFilterPrefs = null
        ))
        dashboardPage.pressChangeUser()

        // Sign in student 2
        val token = data.tokenFor(student2)!!
        tokenLogin(data.domain, token, student2)
        dashboardPage.waitForRender()

        // Change back to student 1
        dashboardPage.pressChangeUser()
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.loginWithPreviousUser(student1)

        // Make sure that student 1 is now logged in
        dashboardPage.waitForRender()
        dashboardPage.assertUserLoggedIn(student1)
    }

    // Should be able to log out from the navigation drawer
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.INTERACTION, false)
    fun testNavDrawer_logOut() {

        // This test fails on API-28 in FTL due to a "TOO_MANY_REGISTRATIONS" issue on logout.
        // IMO, this is not something that we can fix.  So let's not run the test.
        if(Build.VERSION.SDK_INT == 28) {
            return
        }

        signInStudent()

        dashboardPage.signOut()
        loginLandingPage.assertPageObjects()
    }

    /**
     * Create two mocked students, sign in the first one, end up on the dashboard page
     */
    private fun signInStudent() : MockCanvas {
        val data = MockCanvas.init(
                studentCount = 2,
                courseCount = 1,
                favoriteCourseCount = 1
        )

        student1 = data.students.first()
        student2 = data.students.last()

        course = data.courses.values.first()

        val token = data.tokenFor(student1)!!
        tokenLogin(data.domain, token, student1)
        dashboardPage.waitForRender()

        return data
    }

    // Should open a dialog and send a question for the selected course
    // (Checks to see that we can fill out the question and the SEND button exists.)
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_askQuestion() {

        signInStudent()

        dashboardPage.goToHelp()
        helpPage.verifyAskAQuestion(course, "Here's a question")
    }

    // Should open the Canvas guides in a WebView
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_searchCanvasGuides() {
        signInStudent()

        dashboardPage.goToHelp()
        helpPage.launchGuides()
        canvasWebViewPage.verifyTitle(R.string.searchGuides)
    }

    // Should send an error report
    // (Checks to see that we can fill out an error report and that the SEND button is displayed.)
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_reportAProblem() {

        signInStudent()

        dashboardPage.goToHelp()
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
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_submitFeatureIdea() {
        signInStudent()

        dashboardPage.goToHelp()

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

            // :-( Our production code creates a chooser every time, even if there is only one email app option...
            Intents.intended(chooserIntentMatcher)
        }
        finally {
            Intents.release()
        }
    }

    // Should send an intent to open the listing for Student App in the Play Store
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION, false)
    fun testHelp_shareYourLove() {
        signInStudent()

        dashboardPage.goToHelp()
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
}

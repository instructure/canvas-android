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
import androidx.lifecycle.MutableLiveData
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.loginapi.login.model.SignedInUser
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.pandautils.di.NetworkStateProviderModule
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.student.R
import com.instructure.student.espresso.fakes.FakeNetworkStateProvider
import com.instructure.student.ui.utils.StudentTest
import com.instructure.student.ui.utils.tokenLogin
import com.instructure.student.ui.utils.tokenLoginElementary
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

@UninstallModules(NetworkStateProviderModule::class)
@HiltAndroidTest
class NavigationDrawerInteractionTest : StudentTest() {
    override fun displaysPageObjects() = Unit // Not used for interaction tests

    private lateinit var student1: User
    private lateinit var student2: User
    private lateinit var course: Course

    private lateinit var activity: Activity

    private val isOnlineLiveData = MutableLiveData<Boolean>()

    @BindValue
    @JvmField
    val networkStateProvider: NetworkStateProvider = FakeNetworkStateProvider(isOnlineLiveData)

    @Before
    fun setUp() {
        // If we try to read this later, it may be null, possibly because we will have navigated
        // away from our initial activity.
        activity = activityRule.activity

        isOnlineLiveData.postValue(true)
    }

    // Should be able to change the user from the navigation drawer
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.INTERACTION)
    fun testNavDrawer_changeUser() {

        // This test fails on API-28 in FTL due to a "TOO_MANY_REGISTRATIONS" issue on logout.
        // IMO, this is not something that we can fix.  So let's not run the test.
        if (Build.VERSION.SDK_INT == 28) {
            return
        }

        // Sign in student 1, then sign him out
        val data = signInStudent()

        // Need to remember student1 via PreviousUserUtils in order to be able to "change user"
        // back to student1.
        PreviousUsersUtils.add(
            ContextKeeper.appContext, SignedInUser(
                user = student1,
                domain = data.domain,
                protocol = ApiPrefs.protocol,
                token = data.tokenFor(student1)!!,
                accessToken = "",
                refreshToken = "",
                clientId = "",
                clientSecret = "",
                calendarFilterPrefs = null
            )
        )
        leftSideNavigationDrawerPage.clickChangeUserMenu()

        // Sign in student 2
        val token = data.tokenFor(student2)!!
        tokenLogin(data.domain, token, student2)
        dashboardPage.waitForRender()

        // Change back to student 1
        leftSideNavigationDrawerPage.clickChangeUserMenu()
        loginLandingPage.assertDisplaysPreviousLogins()
        loginLandingPage.loginWithPreviousUser(student1)

        // Make sure that student 1 is now logged in
        dashboardPage.waitForRender()
        leftSideNavigationDrawerPage.assertUserLoggedIn(student1)
    }

    // Should be able to log out from the navigation drawer
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.LOGIN, TestCategory.INTERACTION)
    fun testNavDrawer_logOut() {

        // This test fails on API-28 in FTL due to a "TOO_MANY_REGISTRATIONS" issue on logout.
        // IMO, this is not something that we can fix.  So let's not run the test.
        if (Build.VERSION.SDK_INT == 28) {
            return
        }

        signInStudent()

        leftSideNavigationDrawerPage.logout()
        loginLandingPage.assertPageObjects()
    }

    // Should open a dialog and send a question for the selected course
    // (Checks to see that we can fill out the question and the SEND button exists.)
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testHelp_askQuestion() {

        signInStudent()

        leftSideNavigationDrawerPage.clickHelpMenu()
        helpPage.verifyAskAQuestion(course, "Here's a question")
    }

    // Should open the Canvas guides in a WebView
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testHelp_searchCanvasGuides() {
        signInStudent()

        leftSideNavigationDrawerPage.clickHelpMenu()
        helpPage.launchGuides()
        canvasWebViewPage.assertTitle(R.string.searchGuides)
    }

    // Should send an error report
    // (Checks to see that we can fill out an error report and that the SEND button is displayed.)
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testHelp_reportAProblem() {

        signInStudent()

        leftSideNavigationDrawerPage.clickHelpMenu()
        helpPage.verifyReportAProblem("Problem", "It's a problem!")
    }

    // Should send an intent to open the listing for Student App in the Play Store
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testHelp_shareYourLove() {
        signInStudent()

        leftSideNavigationDrawerPage.clickHelpMenu()
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
        } finally {
            Intents.release()
        }
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testMenuItemForDefaultStudent() {
        signInStudent()

        leftSideNavigationDrawerPage.assertMenuItems(false)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testMenuItemForElementaryStudent() {
        signInElementaryStudent()

        leftSideNavigationDrawerPage.assertMenuItems(true)
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testOfflineIndicatorDisplayedIfOffline() {
        signInStudent()

        isOnlineLiveData.postValue(false)

        leftSideNavigationDrawerPage.assertOfflineIndicatorDisplayed()
    }

    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.INTERACTION)
    fun testOfflineIndicatorNotDisplayedIfOnline() {
        signInStudent()

        isOnlineLiveData.postValue(true)

        leftSideNavigationDrawerPage.assertOfflineIndicatorNotDisplayed()
    }

    /**
     * Create two mocked students, sign in the first one, end up on the dashboard page
     */
    private fun signInStudent(courseCount: Int = 1, studentCount: Int = 2, favoriteCourseCount: Int = 1): MockCanvas {
        val data = MockCanvas.init(
            studentCount = studentCount,
            courseCount = courseCount,
            favoriteCourseCount = favoriteCourseCount
        )

        student1 = data.students.first()
        student2 = data.students.last()

        course = data.courses.values.first()

        val token = data.tokenFor(student1)!!
        tokenLogin(data.domain, token, student1)
        dashboardPage.waitForRender()

        return data
    }

    private fun signInElementaryStudent(
        courseCount: Int = 1,
        pastCourseCount: Int = 0,
        favoriteCourseCount: Int = 0,
        announcementCount: Int = 0
    ): MockCanvas {

        val data = MockCanvas.init(
            studentCount = 1,
            courseCount = courseCount,
            pastCourseCount = pastCourseCount,
            favoriteCourseCount = favoriteCourseCount,
            accountNotificationCount = announcementCount
        )

        val student = data.students[0]
        val token = data.tokenFor(student)!!
        tokenLoginElementary(data.domain, token, student)
        elementaryDashboardPage.waitForRender()
        return data
    }
}

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
package com.instructure.student.ui.e2e.classic

import android.util.Log
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.canvas.espresso.annotations.E2E
import com.instructure.student.BuildConfig
import com.instructure.student.ui.utils.StudentComposeTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class PushNotificationsE2ETest : StudentComposeTest() {

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.MANDATORY, FeatureCategory.SETTINGS, TestCategory.E2E)
    fun testPushNotificationsUIE2E() {

        Log.d(STEP_TAG, "Click 'Find My School' button.")
        loginLandingPage.clickFindMySchoolButton()

        Log.d(STEP_TAG, "Enter domain: 'mobileqa.instructure.com'.") //Push Notifications page is giving 'Unexpected Error' on beta yet, so we test it on original instance until it's fixed.
        loginFindSchoolPage.enterDomain("mobileqa.instructure.com")

        Log.d(STEP_TAG, "Click on 'Next' button on the Toolbar.")
        loginFindSchoolPage.clickToolbarNextMenuItem()

        Log.d(STEP_TAG, "Log in with any existing teacher user to test the Push Notification Page.")
        loginSignInPage.loginAs(BuildConfig.PUSH_NOTIFICATIONS_STUDENT_TEST_USER, BuildConfig.PUSH_NOTIFICATIONS_STUDENT_TEST_PASSWORD)
        dashboardPage.waitForRender()

        Log.d(STEP_TAG, "Navigate to User Settings Page.")
        leftSideNavigationDrawerPage.clickSettingsMenu()

        Log.d(STEP_TAG, "Open Push Notifications Page.")
        settingsPage.clickOnSettingsItem("Push Notifications")

        Log.d(ASSERTION_TAG, "Assert that the toolbar title is 'Push Notifications' on the Push Notifications Page.")
        pushNotificationsPage.assertToolbarTitle()

        Log.d(ASSERTION_TAG, "Assert that all the 'Course Activities' push notifications (with their descriptions) are displayed.")
        pushNotificationsPage.assertCourseActivitiesPushNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Discussions' push notifications (with their descriptions) are displayed.")
        pushNotificationsPage.assertDiscussionsPushNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Conversations' push notifications (with their descriptions) are displayed.")
        pushNotificationsPage.assertConversationsPushNotificationsDisplayed()

        Log.d(ASSERTION_TAG, "Assert that all the 'Scheduling' push notifications (with their descriptions) are displayed.")
        pushNotificationsPage.assertSchedulingPushNotificationsDisplayed()
    }

}
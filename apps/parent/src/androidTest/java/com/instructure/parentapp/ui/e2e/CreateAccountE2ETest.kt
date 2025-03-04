/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.parentapp.ui.e2e

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.util.Log
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.instructure.canvas.espresso.E2E
import com.instructure.canvas.espresso.FeatureCategory
import com.instructure.canvas.espresso.Priority
import com.instructure.canvas.espresso.TestCategory
import com.instructure.canvas.espresso.TestMetaData
import com.instructure.dataseeding.api.UserApi
import com.instructure.espresso.randomString
import com.instructure.parentapp.utils.ParentComposeTest
import com.instructure.parentapp.utils.seedData
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.core.AllOf
import org.junit.Test

@HiltAndroidTest
class CreateAccountE2ETest : ParentComposeTest() {

    private lateinit var activityResult: Instrumentation.ActivityResult

    override fun displaysPageObjects() = Unit

    override fun enableAndConfigureAccessibilityChecks() = Unit

    @E2E
    @Test
    @TestMetaData(Priority.IMPORTANT, FeatureCategory.ACCOUNT_CREATION, TestCategory.E2E)
    fun testAccountCreationE2E() {
        Log.d(PREPARATION_TAG, "Seeding data.")
        val data = seedData(students = 2, teachers = 1, parents = 1, courses = 1)
        val student = data.studentsList[0]

        Log.d(PREPARATION_TAG, "Generate pairing code for student with ${student.id} id")
        val pairingCode = UserApi.postGeneratePairingCode(student.id)

        Log.d(PREPARATION_TAG, "Fetch Terms of Service")
        val terms = UserApi.getTermsOfService()

        Log.d(
            STEP_TAG,
            "Navigate to create account screen using generated QR code data: domain: ${student.domain}, pairing code: $pairingCode, accountId: ${terms.accountId}"
        )
        goToCreateAccount(student.domain, pairingCode.pairingCode, terms.accountId)
        composeTestRule.waitUntil { !createAccountPage.isLoading() }
        val email = "${randomString()}@test.com"

        Log.d(STEP_TAG, "Fill account creation form using $email email")
        createAccountPage.fillValidData(email)

        Log.d(STEP_TAG, "Create account")
        createAccountPage.clickCreateAccountButton()
        Thread.sleep(5000) // Wait for the account to be created

        Log.d(STEP_TAG, "On login page login parent with $email email")
        loginSignInPage.loginAs(email, "password")

        Log.d(STEP_TAG, "On dashboard screen assert that student name is displayed")
        dashboardPage.waitForRender()
        dashboardPage.assertSelectedStudent(student.shortName)

        Log.d(STEP_TAG, "On dashboard screen assert that parent is logged in")
        leftSideNavigationDrawerPage.assertUserLoggedIn(email)
    }

    private fun goToCreateAccount(domain: String, pairingCode: String?, accountId: Long) {
        activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(
                com.google.zxing.client.android.Intents.Scan.RESULT,
                "canvas-parent://$domain/pair?code=$pairingCode&account_id=$accountId"
            )
        })
        loginLandingPage.clickQRCodeButton()
        composeTestRule.onNodeWithText("I don\'t have a Canvas account").performClick()

        Intents.init()
        try {
            intending(
                AllOf.allOf(
                    IntentMatchers.anyIntent()
                )
            ).respondWith(activityResult)
            qrPairingPage.tapNext()
        } finally {
            Intents.release()
        }

        composeTestRule.waitForIdle()
    }
}

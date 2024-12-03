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
 */
package com.instructure.parentapp.ui.interaction

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import com.instructure.canvas.espresso.mockCanvas.MockCanvas
import com.instructure.canvas.espresso.mockCanvas.addPairingCode
import com.instructure.canvas.espresso.mockCanvas.init
import com.instructure.parentapp.utils.ParentComposeTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.core.AllOf
import org.junit.Test

@HiltAndroidTest
class CreateAccountInteractionTest : ParentComposeTest() {

    private lateinit var activityResult: Instrumentation.ActivityResult

    @Test
    fun testCreateAccountDisplayed() {
        val data = initData()
        goToCreateAccount(data)
        createAccountPage.assertCreateAccountDisplayed()
    }

    @Test
    fun testEmptyFieldErrorsDisplayed() {
        val data = initData()
        goToCreateAccount(data)
        composeTestRule.waitUntil { !createAccountPage.isRecipientsLoading() }
        createAccountPage.clickCreateAccountButton()

        composeTestRule.onNodeWithText("Please enter full name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please enter an email address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun testInvalidFieldErrorsDisplayed() {
        val data = initData()
        goToCreateAccount(data)
        composeTestRule.waitUntil { !createAccountPage.isRecipientsLoading() }
        createAccountPage.fillInvalidData()
        createAccountPage.clickCreateAccountButton()

        composeTestRule.onNodeWithText("Please enter full name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please enter a valid email address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password must contain at least 8 characters")
            .assertIsDisplayed()
    }

    @Test
    fun testValidData() {
        val data = initData()
        goToCreateAccount(data)
        composeTestRule.waitUntil { !createAccountPage.isRecipientsLoading() }
        createAccountPage.fillValidData()
        createAccountPage.clickCreateAccountButton()

        composeTestRule.onNodeWithTag("nameError").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("emailError").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("passwordError").assertIsNotDisplayed()
    }

    private fun initData(): MockCanvas {
        val data = MockCanvas.init(
            courseCount = 1,
            studentCount = 1
        )

        return data
    }

    private fun goToCreateAccount(data: MockCanvas) {
        val student = data.students[0]
        val code = data.addPairingCode(student)
        val accountId = "123L"

        activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().apply {
            putExtra(
                com.google.zxing.client.android.Intents.Scan.RESULT,
                "canvas-parent://${data.domain}/pair?code=$code&account_id=$accountId"
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

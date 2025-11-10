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

package com.instructure.parentapp.ui.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput

class CreateAccountPage(private val composeTestRule: ComposeTestRule) {

    fun assertCreateAccountDisplayed() {
        scrollToText("Full Name")
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        scrollToText("Email Address")
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        scrollToText("Password")
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        scrollToText("Create Account")
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    fun fillValidData(email: String = "test@email.com") {
        composeTestRule.onNodeWithTag("nameInput").performTextInput("Test Name")
        composeTestRule.onNodeWithTag("emailInput").performTextInput(email)
        composeTestRule.onNodeWithTag("passwordInput").performTextInput("password")
    }

    fun fillInvalidData() {
        composeTestRule.onNodeWithTag("emailInput").performTextInput("email")
        composeTestRule.onNodeWithTag("passwordInput").performTextInput("pwd")
    }

    fun clickCreateAccountButton() {
        scrollToText("Create Account")
        composeTestRule.onNodeWithText("Create Account").performScrollTo().performClick()
    }

    fun isLoading(): Boolean {
        composeTestRule.waitForIdle()
        return composeTestRule.onNodeWithTag("loading").isDisplayed()
    }

    fun scrollToText(text: String) {
        composeTestRule.onNodeWithText(text, useUnmergedTree = true).performScrollTo()
    }
}

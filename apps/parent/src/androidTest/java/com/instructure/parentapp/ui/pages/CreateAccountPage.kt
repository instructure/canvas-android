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

package com.instructure.parentapp.ui.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput

class CreateAccountPage(private val composeTestRule: ComposeTestRule) {

    fun assertCreateAccountDisplayed() {
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    fun createTestAccount() {
        composeTestRule.onNodeWithTag("NameInput").performTextInput("Test Name")
        composeTestRule.onNodeWithTag("emailInput").performTextInput("test@email.com")
        composeTestRule.onNodeWithTag("passwordInput").performTextInput("password")
        composeTestRule.onNodeWithText("Create Account").performClick()
    }
}

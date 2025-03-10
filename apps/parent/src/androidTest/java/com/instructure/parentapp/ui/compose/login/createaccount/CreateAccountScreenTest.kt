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

package com.instructure.parentapp.ui.compose.login.createaccount

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.parentapp.features.login.createaccount.CreateAccountScreen
import com.instructure.parentapp.features.login.createaccount.CreateAccountUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateAccountScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assertContent() {
        composeTestRule.setContent {
            CreateAccountScreen(
                uiState = CreateAccountUiState(
                    termsOfService = TermsOfService(content = "Content")
                ),
                actionHandler = {}
            )
        }

        scrollToText("Full Name")
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        scrollToText("Email Address")
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        scrollToText("Password")
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        scrollToText("Create Account")
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("By tapping \'Create Account\', you agree to the Terms of Service and acknowledge the Privacy Policy")
            .assertIsDisplayed()
    }

    @Test
    fun assertContentWithPassiveTerms() {
        val uiState = CreateAccountUiState(
            termsOfService = TermsOfService(
                content = "Content",
                passive = true
            )
        )
        composeTestRule.setContent {
            CreateAccountScreen(
                uiState = uiState,
                actionHandler = {}
            )
        }

        scrollToText("Full Name")
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        scrollToText("Email Address")
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        scrollToText("Password")
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        scrollToText("Create Account")
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("View the Privacy Policy").assertIsDisplayed()
    }

    @Test
    fun assertSnackbar() {
        composeTestRule.setContent {
            CreateAccountScreen(
                uiState = CreateAccountUiState(
                    showErrorSnack = true,
                    errorSnackMessage = "An error occurred creating your account. Please check your internet connection and try again."
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithText("An error occurred creating your account. Please check your internet connection and try again.")
            .assertIsDisplayed()
    }

    @Test
    fun assertLoading() {
        composeTestRule.setContent {
            CreateAccountScreen(
                uiState = CreateAccountUiState(
                    isLoading = true
                ),
                actionHandler = {}
            )
        }

        composeTestRule.onNodeWithTag("loading").assertIsDisplayed()
    }

    @Test
    fun assertErrorMessages() {
        composeTestRule.setContent {
            CreateAccountScreen(
                uiState = CreateAccountUiState(
                    nameError = "Please enter full name",
                    emailError = "Please enter an email address",
                    passwordError = "Password is required"
                ),
                actionHandler = {}
            )
        }

        scrollToText("Create Account")
        composeTestRule.onNodeWithText("Create Account").performClick()
        scrollToText("Full Name")
        composeTestRule.onNodeWithText("Please enter full name").assertIsDisplayed()
        scrollToText("Email Address")
        composeTestRule.onNodeWithText("Please enter an email address").assertIsDisplayed()
        scrollToText("Password")
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    private fun scrollToText(text: String) {
        composeTestRule.onNodeWithTag("CreateAccountScreen")
            .performScrollToNode(hasText(text))
    }
}

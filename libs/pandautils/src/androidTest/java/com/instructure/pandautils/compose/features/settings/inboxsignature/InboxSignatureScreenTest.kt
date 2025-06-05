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
 */
package com.instructure.pandautils.compose.features.settings.inboxsignature

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.features.settings.inboxsignature.InboxSignatureScreen
import com.instructure.pandautils.features.settings.inboxsignature.InboxSignatureUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InboxSignatureScreenTest {

    @get:Rule
    var composeTestRule = createComposeRule()

    @Test
    fun testLoadingState() {
        val uiState = InboxSignatureUiState(loading = true)
        composeTestRule.setContent {
            InboxSignatureScreen(uiState, {}, {})
        }
        composeTestRule.onNodeWithTag("loading").assertIsDisplayed()
    }

    @Test
    fun testErrorState() {
        val uiState = InboxSignatureUiState(error = true)
        composeTestRule.setContent {
            InboxSignatureScreen(uiState, {}, {})
        }
        composeTestRule.onNodeWithText("Failed to load inbox signature").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun testSaveEnabled() {
        val uiState = InboxSignatureUiState(saveEnabled = true)
        composeTestRule.setContent {
            InboxSignatureScreen(uiState, {}, {})
        }
        composeTestRule.onNodeWithText("Save").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun assertSignatureEnabledWithText() {
        val uiState = InboxSignatureUiState(signatureEnabled = true, signatureText = TextFieldValue("UI tests are awesome!"))
        composeTestRule.setContent {
            InboxSignatureScreen(uiState, {}, {})
        }
        composeTestRule.onNodeWithText("UI tests are awesome!").assertIsDisplayed()
        composeTestRule.onNodeWithTag("switch").assertIsToggleable().assertIsOn()
        composeTestRule.onNodeWithText("Signature").assertIsDisplayed()
        composeTestRule.onNodeWithText("Signature text").assertIsDisplayed()
    }

    @Test
    fun assertSignatureEnabledWithHint() {
        val uiState = InboxSignatureUiState(signatureEnabled = true, signatureText = TextFieldValue(""))
        composeTestRule.setContent {
            InboxSignatureScreen(uiState, {}, {})
        }
        composeTestRule.onNodeWithText("UI tests are awesome!").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Write your signature here").assertIsDisplayed()
    }
}
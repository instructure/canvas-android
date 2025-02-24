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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.instructure.espresso.page.BasePage

class InboxSignatureSettingsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertSignatureText(signatureText: String) {
        composeTestRule.onNodeWithTag("textFieldWithHeaderTextField").assertTextEquals(signatureText)
    }

    fun assertSignatureEnabledState(enabled: Boolean) {
        val node = composeTestRule.onNodeWithTag("switch")
        node.assertIsToggleable()

        if (enabled) node.assertIsOn() else node.assertIsOff()
    }

    fun changeSignatureText(newSignatureText: String) {
        composeTestRule.onNodeWithTag("textFieldWithHeaderTextField").performTextInput(newSignatureText)
    }

    fun toggleSignatureEnabledState() {
        composeTestRule.onNodeWithTag("switch").performClick()
    }

    fun saveChanges() {
        composeTestRule.onNodeWithText("Save").performClick()
    }
}
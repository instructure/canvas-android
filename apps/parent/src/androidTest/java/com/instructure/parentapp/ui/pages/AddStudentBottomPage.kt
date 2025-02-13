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
package com.instructure.parentapp.ui.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick

class AddStudentBottomPage(private val composeTestRule: ComposeTestRule) {

    // Actions

    fun clickOnPairingCode() {
        composeTestRule.onNode(hasText("Pairing Code") and hasAnyAncestor(hasTestTag("AddStudentOptions"))).performClick()
    }

    fun clickOnQRCode() {
        composeTestRule.onNode(hasText("QR Code") and hasAnyAncestor(hasTestTag("AddStudentOptions"))).performClick()
    }

    // Assertions

    fun assertAddStudentWithLabel() {
        composeTestRule.onNode(hasText("Add student with…") and hasAnyAncestor(hasTestTag("AddStudentOptions"))).assertIsDisplayed()
    }
    fun assertPairingCodeOptionDisplayed() {
        composeTestRule.onNode(hasText("Pairing Code") and hasAnyAncestor(hasTestTag("AddStudentOptions"))).assertIsDisplayed()
    }

    fun assertQRCodeOptionDisplayed() {
        composeTestRule.onNode(hasText("QR Code") and hasAnyAncestor(hasTestTag("AddStudentOptions"))).assertIsDisplayed()
    }

}
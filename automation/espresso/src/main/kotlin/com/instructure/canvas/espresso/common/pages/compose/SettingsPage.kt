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
package com.instructure.canvas.espresso.common.pages.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText

class SettingsPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertSettingsItemDisplayed(title: String) {
        composeTestRule.onNode(
            hasTestTag("settingsItem").and(hasAnyDescendant(hasText(title))),
            useUnmergedTree = true
        )
            .assertIsDisplayed()
    }

    fun clickOnSettingsItem(title: String) {
        composeTestRule.onNode(
            hasTestTag("settingsItem").and(hasAnyDescendant(hasText(title))),
            useUnmergedTree = true
        )
            .performClick()
    }

    fun assertThemeSelectorOpened() {
        onViewWithText("Select app theme").assertDisplayed()
    }

    fun assertAboutDialogOpened() {
        onViewWithText("About").assertDisplayed()
    }

    fun assertLegalDialogOpened() {
        onViewWithText("Legal").assertDisplayed()
    }
}
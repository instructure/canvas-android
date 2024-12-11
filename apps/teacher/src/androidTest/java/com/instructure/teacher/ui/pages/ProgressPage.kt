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
 *
 */

package com.instructure.teacher.ui.pages

import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.getStringFromResource

@OptIn(ExperimentalTestApi::class)
class ProgressPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun clickDone() {
        composeTestRule.waitForIdle()
        composeTestRule.waitUntilExactlyOneExists(hasText("Done"), 20000)
        composeTestRule.onNodeWithText("Done").performClick()
    }

   fun assertProgressPageTitle(@StringRes title: Int) {
        composeTestRule.waitUntilExactlyOneExists(hasText(getStringFromResource(title)), 10000)
        composeTestRule.onNodeWithText(getStringFromResource(title)).assertIsDisplayed()
    }

    fun assertProgressPageNote(@StringRes note: Int) {
        composeTestRule.waitUntilExactlyOneExists(hasText(getStringFromResource(note)), 10000)
        composeTestRule.onNodeWithText(getStringFromResource(note)).assertIsDisplayed()
    }
}
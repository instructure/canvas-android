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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.pandautils.R

class ToDoFilterPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertFilterScreenTitle() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterPreferences))
            .assertIsDisplayed()
    }

    fun clickDone() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.done))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun toggleShowPersonalToDos() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterShowPersonalToDos))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun toggleShowCalendarEvents() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterShowCalendarEvents))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun toggleShowCompleted() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoFilterShowCompleted))
            .performClick()
        composeTestRule.waitForIdle()
    }
}

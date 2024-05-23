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
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.instructure.espresso.page.BasePage

class CalendarToDoCreateUpdatePage(private val composeTestRule: ComposeTestRule) {

    fun assertPageTitle(pageTitle: String) {
        composeTestRule.onNodeWithText(pageTitle).assertIsDisplayed()
    }

    fun typeTodoTitle(todoTitle: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertExists().performTextReplacement(todoTitle)
        composeTestRule.waitForIdle()
    }

    fun assertTodoTitle(todoTitle: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertTextEquals(todoTitle)
    }

    fun typeDetails(details: String) {
        composeTestRule.onNodeWithTag("TodoDetailsTextField").performTextReplacement(details)
        composeTestRule.waitForIdle()
    }

    fun assertDetails(details: String) {
        composeTestRule.onNodeWithTag("TodoDetailsTextField").assertTextEquals(details)
    }

    fun clickSave() {
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
    }
}
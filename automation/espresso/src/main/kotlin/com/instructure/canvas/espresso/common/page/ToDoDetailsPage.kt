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
package com.instructure.canvas.espresso.common.page

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.instructure.espresso.assertTextColor

class ToDoDetailsPage(private val composeTestRule: ComposeTestRule) {

    fun waitForToolbar() {
        composeTestRule.waitUntil {
            composeTestRule.onNode(hasParent(hasTestTag("Toolbar")).and(hasText("To Do")))
                .isDisplayed()
        }
    }

    fun assertTitle(title: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("title")
            .assertTextEquals(title).isDisplayed()
    }

    fun assertCanvasContext(title: String, color: Int) {
        composeTestRule.onNodeWithText(title)
            .assertIsDisplayed()
            .assertTextColor(Color(color))
    }
}
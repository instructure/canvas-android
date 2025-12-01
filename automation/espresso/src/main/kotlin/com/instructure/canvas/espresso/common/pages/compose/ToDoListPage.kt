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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.getStringFromResource
import com.instructure.pandautils.R

class ToDoListPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun clickFilterButton() {
        composeTestRule.onNodeWithContentDescription(getStringFromResource(R.string.a11y_contentDescriptionToDoFilter))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickOnItem(itemTitle: String) {
        composeTestRule.onNodeWithText(itemTitle).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertItemDisplayed(itemTitle: String) {
        composeTestRule.onNodeWithText(itemTitle).assertIsDisplayed()
    }

    fun assertItemNotDisplayed(itemTitle: String) {
        composeTestRule.onNodeWithText(itemTitle).assertDoesNotExist()
    }

    fun clickCheckbox(itemId: Long) {
        composeTestRule.onNodeWithTag("todoCheckbox_$itemId")
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun swipeItemLeft(itemId: Long) {
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNodeWithTag("todoItem_$itemId").performTouchInput {
            swipeLeft()
        }

        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    fun swipeItemRight(itemId: Long) {
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNodeWithTag("todoItem_$itemId").performTouchInput {
            swipeRight()
        }

        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    fun assertSnackbarDisplayed(itemTitle: String) {
        val message = getStringFromResource(R.string.todoMarkedAsDone, itemTitle)
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }

    fun clickSnackbarUndo() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.todoMarkedAsDoneSnackbarUndo))
            .performClick()
        composeTestRule.waitForIdle()
    }

    fun clickDateBadge(dayOfMonth: Int) {
        composeTestRule.onNodeWithText(dayOfMonth.toString()).performClick()
        composeTestRule.waitForIdle()
    }

    fun assertFilterIconOutline() {
        composeTestRule.onNodeWithContentDescription(getStringFromResource(R.string.a11y_contentDescriptionToDoFilter))
            .assertExists()
    }

    fun assertFilterIconFilled() {
        composeTestRule.onNodeWithContentDescription(getStringFromResource(R.string.a11y_contentDescriptionToDoFilter))
            .assertExists()
    }

    fun assertEmptyState() {
        composeTestRule.onNodeWithText(getStringFromResource(R.string.noToDosForNow))
            .assertIsDisplayed()
    }

    fun waitForSnackbar(itemTitle: String, timeoutMillis: Long = 5000) {
        val message = getStringFromResource(R.string.todoMarkedAsDone, itemTitle)
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun waitForItemToDisappear(itemTitle: String, timeoutMillis: Long = 5000) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithText(itemTitle).fetchSemanticsNodes().isEmpty()
        }
    }

    fun waitForItemToAppear(itemTitle: String, timeoutMillis: Long = 5000) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithText(itemTitle).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

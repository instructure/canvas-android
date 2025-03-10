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

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.instructure.canvasapi2.utils.DateHelper
import java.util.Date

class CalendarToDoDetailsPage(private val composeTestRule: ComposeTestRule) {

    fun assertPageTitle(pageTitle: String) {
        composeTestRule.onNode(hasTestTag("todoDetailsPageTitle") and hasText(pageTitle), useUnmergedTree = true).assertIsDisplayed()
    }

    fun assertTitle(title: String) {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("title")
            .assertTextEquals(title).isDisplayed()
    }

    fun assertCanvasContext(title: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun assertDate(context: Context, date: Date) {
        val dateTitle = date.let {
            val dateText = DateHelper.dayMonthDateFormat.format(it)
            val timeText = DateHelper.getFormattedTime(context, it)
            "$dateText at $timeText"
        }

        composeTestRule.onNodeWithTag("date")
            .assertTextEquals(dateTitle).isDisplayed()
    }

    fun assertDate(dateString: String) {
        composeTestRule.onNodeWithTag("date")
            .assertTextEquals(dateString).isDisplayed()
    }

    fun assertDescription(description: String) {
        composeTestRule.onNodeWithTag("description")
            .assertTextEquals(description).isDisplayed()
    }

    fun clickToolbarMenu() {
        composeTestRule.onNode(
            hasParent(hasTestTag("toolbar"))
                .and(hasContentDescription("More options"))
        )
            .performClick()
    }

    fun clickEditMenu() {
        composeTestRule.onNodeWithText("Edit").performClick()
    }

    fun clickDeleteMenu() {
        composeTestRule.onNodeWithText("Delete").performClick()
    }

    fun assertDeleteDialog() {
        composeTestRule.onNodeWithText("Delete To Do?").assertIsDisplayed()
    }

    fun confirmDeletion() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Delete To Do?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()
    }
}
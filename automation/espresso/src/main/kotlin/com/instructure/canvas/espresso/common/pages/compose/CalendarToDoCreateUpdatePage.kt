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
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.contrib.PickerActions
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.waitForViewWithClassName
import org.hamcrest.Matchers
import java.util.Calendar
import java.util.Date

class CalendarToDoCreateUpdatePage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertPageTitle(pageTitle: String) {
        composeTestRule.onNodeWithText(pageTitle).assertIsDisplayed()
    }

    fun typeTodoTitle(todoTitle: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertExists().performTextReplacement(todoTitle)
        composeTestRule.waitForIdle()
    }

    fun selectDate(calendar: Calendar) {
        composeTestRule.onNodeWithTag("dateRow").performScrollTo().performClick()
        waitForViewWithClassName(Matchers.equalTo(DatePicker::class.java.name)).perform(
            PickerActions.setDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        )
        onViewWithId(android.R.id.button1).click()
        composeTestRule.waitForIdle()
    }

    fun assertDate(date: Date) {
        val dateText = DateHelper.dayMonthDateFormat.format(date)
        composeTestRule.onNode(hasParent(hasTestTag("dateRow")).and(hasText(dateText)), true).assertIsDisplayed()
    }

    fun assertTime(context: Context, date: Date) {
        val timeText = DateHelper.getFormattedTime(context, date).orEmpty()
        composeTestRule.onNode(hasParent(hasTestTag("timeRow")).and(hasText(timeText)), true).assertIsDisplayed()
    }

    fun selectTime(calendar: Calendar) {
        composeTestRule.onNodeWithText("Time").performScrollTo().performClick()
        waitForViewWithClassName(Matchers.equalTo(TimePicker::class.java.name)).perform(
            PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
        )
        onViewWithId(android.R.id.button1).click()
        composeTestRule.waitForIdle()
    }

    fun selectCanvasContext(canvasContext: String) {
        composeTestRule.onNodeWithTag("canvasContextRow").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("calendar_$canvasContext").performClick()
        composeTestRule.waitForIdle()
    }

    fun assertCanvasContext(canvasContext: String) {
        composeTestRule.onNode(hasParent(hasTestTag("canvasContextRow")).and(hasText(canvasContext)), true).assertIsDisplayed()
    }

    fun assertTodoTitle(todoTitle: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertTextEquals(todoTitle)
    }

    fun typeDetails(details: String) {
        composeTestRule.onNodeWithTag("todoDetailsTextField").performTextReplacement(details)
        composeTestRule.waitForIdle()
    }

    fun assertDetails(details: String) {
        composeTestRule.onNodeWithTag("todoDetailsTextField").assertTextEquals(details)
    }

    fun clickSave() {
        composeTestRule.onNodeWithText("Save").performClick()
        composeTestRule.waitForIdle()
    }

    fun assertUnsavedChangesDialog() {
        composeTestRule.onNodeWithText("Exit without saving?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you would like to exit without saving?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exit").assertIsDisplayed()
    }

    fun clickClose() {
        composeTestRule.onNodeWithContentDescription("Close").performClick()
    }

    fun assertSaveDisabled() {
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    fun assertSaveEnabled() {
        composeTestRule.onNodeWithText("Save").assertIsEnabled()
    }
}
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

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.contrib.PickerActions
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.waitForViewWithClassName
import org.hamcrest.Matchers
import java.util.Calendar

class CalendarEventCreateEditPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertScreenTitle(title: String) {
        composeTestRule.onNode(hasParent(hasTestTag("toolbar")).and(hasText(title))).assertIsDisplayed()
    }

    fun assertTitle(title: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun typeTitle(title: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertExists().performTextReplacement(title)
        composeTestRule.waitForIdle()
    }

    fun selectDate(calendar: Calendar) {
        composeTestRule.onNodeWithText("Date").performScrollTo().performClick()
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

    fun selectTime(label: String, calendar: Calendar) {
        composeTestRule.onNodeWithText(label).performScrollTo().performClick()
        waitForViewWithClassName(Matchers.equalTo(TimePicker::class.java.name)).perform(
            PickerActions.setTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
        )
        onViewWithId(android.R.id.button1).click()
        composeTestRule.waitForIdle()
    }

    fun selectFrequency(frequency: String) {
        composeTestRule.onNodeWithText("Frequency").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(frequency).performClick()
        composeTestRule.waitForIdle()
    }

    fun typeLocation(location: String) {
        composeTestRule.onNodeWithTag("locationTextField").onChildAt(0).performTextReplacement(location)
        composeTestRule.waitForIdle()
    }

    fun typeAddress(address: String) {
        composeTestRule.onNodeWithTag("addressTextField").onChildAt(0).performTextReplacement(address)
        composeTestRule.waitForIdle()
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
        composeTestRule.onNodeWithText("Save").assertIsDisplayed().assertIsNotEnabled()
    }

    fun assertSaveEnabled() {
        composeTestRule.onNodeWithText("Save").assertIsDisplayed().assertIsEnabled()
    }
}
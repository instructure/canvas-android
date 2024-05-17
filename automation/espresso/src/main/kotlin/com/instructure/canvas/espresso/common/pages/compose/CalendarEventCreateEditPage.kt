/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.canvas.espresso.common.pages.compose

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.contrib.PickerActions
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithId
import com.instructure.espresso.page.waitForViewWithClassName
import org.hamcrest.Matchers
import java.util.Calendar

class CalendarEventCreateEditPage(private val composeTestRule: ComposeTestRule) : BasePage() {

    fun assertTitle(title: String) {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    fun typeTitle(title: String) {
        composeTestRule.onNodeWithTag("addTitleField").assertExists().performTextReplacement(title)
        composeTestRule.waitForIdle()
    }

    fun selectDate(calendar: Calendar) {
        composeTestRule.onNodeWithText("Date").performClick()
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
        composeTestRule.onNodeWithText(label).performClick()
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
        composeTestRule.onNodeWithText("Frequency").performClick()
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
}
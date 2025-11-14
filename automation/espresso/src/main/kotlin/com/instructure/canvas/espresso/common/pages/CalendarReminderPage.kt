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
package com.instructure.canvas.espresso.common.pages

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView
import com.instructure.espresso.scrollTo
import com.instructure.pandautils.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.anything
import java.util.Calendar

class CalendarReminderPage(private val composeTestRule: ComposeTestRule) {
    private val reminderTitle = "Reminder"
    private val reminderDescription = "Add due date reminder notifications about this assignment on this device."
    private val reminderAdd = "Add reminder"

    fun assertReminderSectionDisplayed() {
        composeTestRule.onNodeWithText(reminderTitle).isDisplayed()
        composeTestRule.onNodeWithText(reminderDescription).isDisplayed()
        composeTestRule.onNodeWithContentDescription(reminderAdd).isDisplayed()
    }

    fun clickBeforeReminderOption(text: String) {
        waitForView(withText(text)).scrollTo().click()
        composeTestRule.waitForIdle()
    }

    fun clickCustomReminderOption() {
        onData(anything()).inRoot(isDialog()).atPosition(6).perform(click())
    }

    fun clickAddReminder() {
        composeTestRule.onNodeWithContentDescription(reminderAdd).performClick()
    }

    fun assertReminderDisplayedWithText(text: String) {
        composeTestRule.onNodeWithText(text).isDisplayed()
    }

    fun removeReminder() {
        composeTestRule.onNode(
            hasContentDescription("Remove")
        ).performClick()
        Thread.sleep(1000)
        onView(withText(R.string.yes)).scrollTo().click()
    }

    fun assertReminderNotDisplayedWithText(text: String) {
        onView(withText(text)).check(doesNotExist())
    }

    fun selectDate(calendar: Calendar) {
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))

        onView(withId(android.R.id.button1)).perform(click())
    }

    fun selectTime(calendar: Calendar) {
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))

        onView(withId(android.R.id.button1)).perform(click())
    }
}
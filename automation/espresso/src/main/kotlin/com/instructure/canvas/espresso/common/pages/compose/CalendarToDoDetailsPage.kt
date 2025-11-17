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
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
import com.instructure.canvasapi2.utils.DateHelper
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForView
import com.instructure.espresso.scrollTo
import com.instructure.pandautils.R
import org.hamcrest.Matchers
import org.hamcrest.Matchers.anything
import java.util.Calendar
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

    fun assertReminderSectionDisplayed() {
        composeTestRule.onNodeWithText(R.string.reminderTitle.toString()).isDisplayed()
        composeTestRule.onNodeWithText(R.string.reminderDescription.toString()).isDisplayed()
        composeTestRule.onNodeWithContentDescription(R.string.a11y_addReminder.toString()).isDisplayed()
    }

    fun clickBeforeReminderOption(reminderText: String) {
        waitForView(withText(reminderText)).scrollTo().click()
        composeTestRule.waitForIdle()
    }

    fun clickCustomReminderOption() {
        onData(anything()).inRoot(isDialog()).atPosition(6).perform(click())
    }

    fun clickAddReminder() {
        composeTestRule.onNodeWithContentDescription("Add reminder").performClick()
    }

    fun assertReminderDisplayedWithText(reminderText: String) {
        composeTestRule.onNodeWithText(reminderText).isDisplayed()
    }

    fun removeReminder() {
        composeTestRule.onNode(
            hasContentDescription("Remove")
        ).performClick()
        Thread.sleep(1000)
        waitForView(withText(com.instructure.pandautils.R.string.yes)).scrollTo().click()
    }

    fun assertReminderNotDisplayedWithText(reminderText: String) {
        onView(withText(reminderText)).check(doesNotExist())
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
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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.espresso.page.BasePage
import org.hamcrest.Matchers
import java.util.Calendar


class ReminderPage: BasePage() {
    fun selectDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(year, monthOfYear, dayOfMonth))

        onView(withId(android.R.id.button1)).perform(click())
    }

    fun selectDate(calendar: Calendar) {
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))

        onView(withId(android.R.id.button1)).perform(click())
    }

    fun selectTime(hours: Int, minutes: Int) {
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(hours, minutes))

        onView(withId(android.R.id.button1)).perform(click())
    }

    fun selectTime(calendar: Calendar) {
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))

        onView(withId(android.R.id.button1)).perform(click())
    }
}
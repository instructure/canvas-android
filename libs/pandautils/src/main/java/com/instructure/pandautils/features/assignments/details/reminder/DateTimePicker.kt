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
package com.instructure.pandautils.features.assignments.details.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class DateTimePicker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val calendar = Calendar.getInstance()

    private var year: Int
        get() = calendar.get(Calendar.YEAR)
        set(value) = calendar.set(Calendar.YEAR, value)

    private var month: Int
        get() = calendar.get(Calendar.MONTH)
        set(value) = calendar.set(Calendar.MONTH, value)

    private var day: Int
        get() = calendar.get(Calendar.DAY_OF_MONTH)
        set(value) = calendar.set(Calendar.DAY_OF_MONTH, value)

    private var hour: Int
        get() = calendar.get(Calendar.HOUR_OF_DAY)
        set(value) = calendar.set(Calendar.HOUR_OF_DAY, value)

    private var minute: Int
        get() = calendar.get(Calendar.MINUTE)
        set(value) = calendar.set(Calendar.MINUTE, value)

    fun show(dateTimeSelected: (Calendar) -> Unit) {
        showDatePicker(dateTimeSelected)
    }

    private fun showDatePicker(dateTimeSelected: (Calendar) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, day -> onDateSet(year, month, day, dateTimeSelected) },
            year,
            month,
            day
        ).show()
    }

    private fun showTimePicker(dateTimeSelected: (Calendar) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute -> onTimeSet(hour, minute, dateTimeSelected) },
            hour,
            minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    private fun onDateSet(year: Int, month: Int, day: Int, dateTimeSelected: (Calendar) -> Unit) {
        this.year = year
        this.month = month
        this.day = day

        showTimePicker(dateTimeSelected)
    }

    private fun onTimeSet(hourOfDay: Int, minute: Int, dateTimeSelected: (Calendar) -> Unit) {
        this.hour = hourOfDay
        this.minute = minute

        dateTimeSelected(calendar)
    }
}
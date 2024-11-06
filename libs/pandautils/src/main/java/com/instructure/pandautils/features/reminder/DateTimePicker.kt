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
package com.instructure.pandautils.features.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import java.util.Calendar

class DateTimePicker {
    private var calendar = Calendar.getInstance()

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

    private var selectedDate = MutableStateFlow<Calendar?>(null)

    private fun initPicker() {
        selectedDate = MutableStateFlow(null)
        calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun show(context: Context): Flow<Calendar> {
        initPicker()
        showDatePicker(context)
        return selectedDate.filterNotNull()
    }

    private fun showDatePicker(context: Context) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int -> onDateSet(context, year, month, day) },
            year,
            month,
            day
        ).show()
    }

    private fun showTimePicker(context: Context) {
        TimePickerDialog(
            context,
            { _: TimePicker, hour: Int, minute: Int -> onTimeSet(hour, minute) },
            hour,
            minute,
            DateFormat.is24HourFormat(context)
        ).show()
    }

    private fun onDateSet(context: Context, year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day

        showTimePicker(context)
    }

    private fun onTimeSet(hourOfDay: Int, minute: Int) {
        this.hour = hourOfDay
        this.minute = minute

        selectedDate.tryEmit(calendar)
    }

    companion object {
        fun getInstance(): DateTimePicker {
            return DateTimePicker()
        }
    }
}
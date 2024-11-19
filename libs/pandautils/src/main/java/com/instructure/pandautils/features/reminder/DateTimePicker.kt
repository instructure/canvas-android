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

import android.content.Context
import com.instructure.pandautils.compose.getDatePickerDialog
import com.instructure.pandautils.compose.getTimePickerDialog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
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

    private fun initPicker() {
        calendar = Calendar.getInstance()
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun show(context: Context) = callbackFlow<Calendar> {
        initPicker()

        showDatePicker(context, { trySend(it) }, { close() })

        awaitClose()
    }

    private fun showDatePicker(context: Context, onDateSelected: (Calendar) -> Unit, onCancel: () -> Unit) {
        getDatePickerDialog(
            context,
            LocalDate.of(year, month, day),
            { onDateSet(context, it.year, it.monthValue + 1, it.dayOfMonth, onDateSelected, onCancel) },
            onCancel,
            {}
        ).show()
    }

    private fun showTimePicker(context: Context, onDateSelected: (Calendar) -> Unit, onCancel: () -> Unit) {
        getTimePickerDialog(
            context,
            LocalTime.of(hour, minute),
            { onTimeSet(it.hour, it.minute, onDateSelected) },
            onCancel,
            {}
        ).show()
    }

    private fun onDateSet(context: Context, year: Int, month: Int, day: Int, onDateSelected: (Calendar) -> Unit, onCancel: () -> Unit) {
        this@DateTimePicker.year = year
        this@DateTimePicker.month = month
        this@DateTimePicker.day = day

        showTimePicker(context, onDateSelected, onCancel)
    }

    private fun onTimeSet(hourOfDay: Int, minute: Int, onDateSelected: (Calendar) -> Unit) {
        this.hour = hourOfDay
        this.minute = minute

        onDateSelected(calendar)
    }

    companion object {
        fun getInstance(): DateTimePicker {
            return DateTimePicker()
        }
    }
}
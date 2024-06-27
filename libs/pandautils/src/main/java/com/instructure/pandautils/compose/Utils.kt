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

package com.instructure.pandautils.compose

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import com.instructure.pandautils.utils.ThemePrefs
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime


fun getDatePickerDialog(
    context: Context,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
): DatePickerDialog {
    return DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onDateSelected(
                LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )
            )
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    ).apply {
        setOnShowListener {
            getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }
        setOnDismissListener {
            onDismiss()
        }
        setOnCancelListener {
            onCancel()
        }
    }
}

fun getTimePickerDialog(
    context: Context,
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
): TimePickerDialog {
    return TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(
                LocalTime.of(
                    hourOfDay,
                    minute
                )
            )
        },
        time.hour,
        time.minute,
        false
    ).apply {
        setOnShowListener {
            getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.textButtonColor)
            getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.textButtonColor)
        }

        setOnDismissListener {
            onDismiss()
        }

        setOnCancelListener {
            onCancel()
        }
    }
}

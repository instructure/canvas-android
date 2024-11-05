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

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.toast
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dateTimePicker: DateTimePicker
) {
    fun showDateTimePicker(scope: LifecycleCoroutineScope) {
        dateTimePicker.show {
            setReminder(it, scope)
        }
    }

    private fun setReminder(calendar: Calendar, scope: LifecycleCoroutineScope) {
        val alarmTimeInMillis = calendar.timeInMillis

        if (alarmTimeInMillis < System.currentTimeMillis()) {
            context.toast(R.string.reminderInPast)
            return
        }
    }
}
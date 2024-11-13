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
import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.showThemed
import com.instructure.pandautils.utils.toFormattedString
import com.instructure.pandautils.utils.toast
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderManager(
    private val dateTimePicker: DateTimePicker,
    private val reminderRepository: ReminderRepository
) {
    suspend fun showBeforeDueDateReminderDialog(
        context: Context,
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String,
        @ColorInt color: Int
    ) {
        showBeforeDueDateReminderDialog(context, color).collect { calendar ->
            createReminder(context, calendar, userId, contentId, contentName, contentHtmlUrl)
        }
    }

    private fun showBeforeDueDateReminderDialog(
        context: Context,
        @ColorInt color: Int,
    ) = callbackFlow<Calendar> {
        val choices = listOf(
            ReminderChoice.Minute(5),
            ReminderChoice.Minute(15),
            ReminderChoice.Minute(30),
            ReminderChoice.Hour(1),
            ReminderChoice.Day(1),
            ReminderChoice.Week(1),
            ReminderChoice.Custom,
        )

        AlertDialog.Builder(context)
            .setTitle(R.string.reminderTitle)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                close()
                dialog.dismiss()
            }
            .setSingleChoiceItems(
                choices.map {
                    if (it is ReminderChoice.Custom) {
                        it.getText(context.resources)
                    } else {
                        context.getString(R.string.reminderBefore, it.getText(context.resources))
                    }
                }.toTypedArray(), -1
            ) { dialog, which ->
                if (choices[which] is ReminderChoice.Custom) {
                    this.launch {
                        showCustomReminderDialog(context).collect { calendar ->
                            trySend(calendar)
                            close()
                        }
                    }
                    dialog.dismiss()
                } else {
                    trySend(choices[which].getCalendar())
                    close()
                    dialog.dismiss()
                }
            }
            .showThemed(color)

        awaitClose()
    }

    suspend fun showCustomReminderDialog(
        context: Context,
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String
    ) {
        showCustomReminderDialog(context).collect { calendar ->
            createReminder(context, calendar, userId, contentId, contentName, contentHtmlUrl)
        }
    }

    private fun showCustomReminderDialog(
        context: Context,
    ) = callbackFlow<Calendar> {
        dateTimePicker.show(context)
            .onEach { selectedDateTime ->
                trySend(selectedDateTime)
                close()
            }
            .onCompletion { close() }
            .collect()

        awaitClose()
    }

    private suspend fun createReminder(
        context: Context,
        calendar: Calendar,
        userId: Long,
        contentId: Long,
        contentName: String,
        contentHtmlUrl: String
    ) {
        val alarmTimeInMillis = calendar.timeInMillis
        if (reminderRepository.isReminderAlreadySetForTime(userId, contentId, calendar.timeInMillis)) {
            context.toast(R.string.reminderAlreadySet)
            return
        }

        if (alarmTimeInMillis < System.currentTimeMillis()) {
            context.toast(R.string.reminderInPast)
            return
        }

        val dateTimeString = calendar.time.toFormattedString()

        reminderRepository.createReminder(
            userId,
            contentId,
            contentName,
            contentHtmlUrl,
            dateTimeString,
            alarmTimeInMillis
        )
    }
}

sealed class ReminderChoice {
    data class Minute(val quantity: Int) : ReminderChoice()
    data class Hour(val quantity: Int) : ReminderChoice()
    data class Day(val quantity: Int) : ReminderChoice()
    data class Week(val quantity: Int) : ReminderChoice()
    data object Custom : ReminderChoice()

    fun getText(resources: Resources) = when (this) {
        is Minute -> resources.getQuantityString(R.plurals.reminderMinute, quantity, quantity)
        is Hour -> resources.getQuantityString(R.plurals.reminderHour, quantity, quantity)
        is Day -> resources.getQuantityString(R.plurals.reminderDay, quantity, quantity)
        is Week -> resources.getQuantityString(R.plurals.reminderWeek, quantity, quantity)
        is Custom -> resources.getString(R.string.reminderCustom)
    }

    private fun getTimeInMillis() = when (this) {
        is Minute -> quantity * 60 * 1000L
        is Hour -> quantity * 60 * 60 * 1000L
        is Day -> quantity * 24 * 60 * 60 * 1000L
        is Week -> quantity * 7 * 24 * 60 * 60 * 1000L
        else -> 0
    }

    fun getCalendar(): Calendar = Calendar.getInstance().apply {
        add(Calendar.MILLISECOND, this@ReminderChoice.getTimeInMillis().toInt())
    }
}

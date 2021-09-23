/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.features.settings.calendarsync

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.instructure.canvasapi2.apis.PlannerAPI
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.utils.date.RealDateTimeProvider
import com.instructure.student.util.StudentPrefs
import java.util.*

class CalendarSyncWorker(private val appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    private val dateTimeProvider = RealDateTimeProvider()

    private val plannerManager = PlannerManager(PlannerAPI)

    override suspend fun doWork(): Result {
        try {
            val today = Date(dateTimeProvider.getCalendar().timeInMillis)
            val endDateCalendar = dateTimeProvider.getCalendar()
            endDateCalendar.add(Calendar.DAY_OF_YEAR, 7)
            val endDate = Date(endDateCalendar.timeInMillis)

            val plannerItems = plannerManager.getPlannerItemsAsync(
                true,
                today.toApiString(),
                endDate.toApiString()
            )
                .await()
                .dataOrNull

            val syncedCalendarId = StudentPrefs.syncedCalendarId

            plannerItems?.forEach {
                if (StudentPrefs.eventIds.containsKey(it.plannableId)) {
                    val eventId = StudentPrefs.eventIds[it.plannableId] ?: -1
                    val isEventPresent = queryEvent(eventId)
                    if (isEventPresent) {
                        updateCalendarEvent(syncedCalendarId, it, eventId)
                    } else {
                        insertCalendarEvent(syncedCalendarId, it)
                    }
                } else {
                    insertCalendarEvent(syncedCalendarId, it)
                }
            }

            val currentTime = dateTimeProvider.getCalendar().timeInMillis
            StudentPrefs.lastCalendarSync = currentTime

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    private fun queryEvent(eventId: Long): Boolean {
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection = "(${CalendarContract.Events._ID} = ?) AND (deleted = 0)"
        val selectionArgs = arrayOf(eventId.toString())

        val eventProjection: Array<String> = arrayOf(CalendarContract.Events._ID)
        val cur: Cursor? =
            appContext.contentResolver.query(uri, eventProjection, selection, selectionArgs, null)

        return cur?.count ?: 0 > 0
    }

    private fun updateCalendarEvent(calID: Long, plannerItem: PlannerItem, eventId: Long?) {
        val values = ContentValues().apply {
            put(
                CalendarContract.Events.DTSTART,
                plannerItem.plannable.startAt?.time ?: plannerItem.plannable.dueAt?.time
                ?: dateTimeProvider.getCalendar().timeInMillis
            )
            put(
                CalendarContract.Events.DTEND,
                plannerItem.plannable.endAt?.time ?: plannerItem.plannable.dueAt?.time
                ?: dateTimeProvider.getCalendar().timeInMillis
            )
            put(
                CalendarContract.Events.TITLE,
                "${plannerItem.contextName} - ${plannerItem.plannable.title}"
            )
            put(CalendarContract.Events.DESCRIPTION, createUrl(plannerItem))
            put(CalendarContract.Events.CALENDAR_ID, calID)
            put(CalendarContract.Events.EVENT_TIMEZONE, "Mountain Time")
        }

        val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId!!)
        appContext.contentResolver.update(updateUri, values, null, null)
    }

    private fun insertCalendarEvent(calID: Long, plannerItem: PlannerItem) {
        val values = ContentValues().apply {
            put(
                CalendarContract.Events.DTSTART,
                plannerItem.plannable.startAt?.time ?: plannerItem.plannable.dueAt?.time
                ?: dateTimeProvider.getCalendar().timeInMillis
            )
            put(
                CalendarContract.Events.DTEND,
                plannerItem.plannable.endAt?.time ?: plannerItem.plannable.dueAt?.time
                ?: dateTimeProvider.getCalendar().timeInMillis
            )
            put(
                CalendarContract.Events.TITLE,
                "${plannerItem.contextName} - ${plannerItem.plannable.title}"
            )
            put(CalendarContract.Events.DESCRIPTION, createUrl(plannerItem))
            put(CalendarContract.Events.CALENDAR_ID, calID)
            put(CalendarContract.Events.EVENT_TIMEZONE, "Mountain Time")
        }
        val uri: Uri? =
            appContext.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

        // get the event ID that is the last element in the Uri
        val eventID: Long = uri?.lastPathSegment?.toLong() ?: -1

        val eventIds = StudentPrefs.eventIds
        val newEventIds = eventIds.plus(plannerItem.plannableId to eventID)
        StudentPrefs.eventIds = newEventIds
    }

    private fun createUrl(plannerItem: PlannerItem): String {
        return if (plannerItem.htmlUrl?.contains(ApiPrefs.fullDomain) == true) {
            plannerItem.htmlUrl!!
        } else {
            "${ApiPrefs.fullDomain}${plannerItem.htmlUrl}"
        }
    }
}
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

import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.instructure.canvasapi2.managers.PlannerManager
import com.instructure.canvasapi2.models.PlannableType
import com.instructure.canvasapi2.models.PlannerItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.pandautils.R
import com.instructure.pandautils.utils.date.DateTimeProvider
import com.instructure.student.util.StudentPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
private val CALENDAR_PROJECTION: Array<String> = arrayOf(
    CalendarContract.Calendars._ID,                     // 0
    CalendarContract.Calendars.ACCOUNT_NAME,            // 1
    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
    CalendarContract.Calendars.OWNER_ACCOUNT,            // 3
    CalendarContract.Calendars.ACCOUNT_TYPE            // 4
)

// The indices for the projection array above.
private const val PROJECTION_ID_INDEX: Int = 0
private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3
private const val PROJECTION_ACCOUNT_TYPE_INDEX: Int = 4

private const val CALENDAR_WORKER_TAG = "calendarWork"

@HiltViewModel
class CalendarSyncViewModel @Inject constructor(
    private val application: Application,
    private val plannerManager: PlannerManager,
    private val dateTimeProvider: DateTimeProvider,
    private val apiPrefs: ApiPrefs,
    private val workManager: WorkManager
) : ViewModel() {

    val loading: LiveData<Boolean>
        get() = _loading
    private val _loading = MutableLiveData<Boolean>(false)

    val data: LiveData<List<CalendarItemViewModel>>
        get() = _data
    private val _data = MutableLiveData<List<CalendarItemViewModel>>(emptyList())

    val syncEnabled: LiveData<Boolean>
        get() = _syncEnabled
    private val _syncEnabled = MutableLiveData<Boolean>(false)

    val lastSynced: LiveData<String>
        get() = _lastSynced
    private val _lastSynced = MutableLiveData<String>("")

    private val simpleDateFormat = SimpleDateFormat("dd. MM. hh:mm aa", Locale.getDefault())

    init {
        val syncedCalendarId = StudentPrefs.syncedCalendarId
        val isSyncEnabled = syncedCalendarId != -1L
        _syncEnabled.value = isSyncEnabled

        if (isSyncEnabled) {
            _loading.value = true
            val calendars = getCalendars()
                .map {
                    CalendarItemViewModel(
                        it,
                        syncedCalendarId == it.calID
                    ) { calendarClicked(it.calID) }
                }
            _data.postValue(calendars)

            val lastSync = StudentPrefs.lastCalendarSync
            if (lastSync > -1L) {
                _lastSynced.value = simpleDateFormat.format(Date(lastSync))
            }

            _loading.value = false
        }
    }

    fun syncSwitchChanged(checked: Boolean) {
        if (checked) {
            _loading.value = true

            _syncEnabled.value = true
            enableSync()

            _loading.value = false
        } else {
            _syncEnabled.value = false
            StudentPrefs.syncedCalendarId = -1

            _lastSynced.value = ""
            StudentPrefs.lastCalendarSync = -1
            disableSync()

            _loading.value = false
        }
    }

    private fun enableSync() {
        viewModelScope.launch {
            val calendars = getCalendars()
                .map { CalendarItemViewModel(it, false) { calendarClicked(it.calID) } }
            _data.postValue(calendars)
        }
    }

    private fun getCalendars(): List<CalendarViewData> {
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val cur: Cursor? =
            application.contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)

        val calendars = mutableListOf<CalendarViewData>()

        while (cur?.moveToNext() == true) {
            // Get the field values
            val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
            val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
            val accountType: String = cur.getString(PROJECTION_ACCOUNT_TYPE_INDEX)

            calendars.add(CalendarViewData(calID, displayName, accountName, ownerName, accountType))
        }

        return calendars
    }

    private fun calendarClicked(calID: Long) {
        _loading.value = true
        viewModelScope.launch {
            try {
                StudentPrefs.syncedCalendarId = calID

                data.value?.forEach { it.unselect() }

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

                plannerItems?.forEach {
                    if (StudentPrefs.eventIds.containsKey(it.plannableId)) {
                        val eventId = StudentPrefs.eventIds[it.plannableId] ?: -1
                        val isEventPresent = queryEvent(eventId)
                        if (isEventPresent) {
                            updateCalendarEvent(calID, it, eventId)
                        } else {
                            insertCalendarEvent(calID, it)
                        }
                    } else {
                        insertCalendarEvent(calID, it)
                    }
                }

                val currentTime = dateTimeProvider.getCalendar().timeInMillis
                _lastSynced.value = simpleDateFormat.format(Date(currentTime))
                StudentPrefs.lastCalendarSync = currentTime

                workManager.cancelAllWorkByTag(CALENDAR_WORKER_TAG)

                val workRequest: WorkRequest =
                    PeriodicWorkRequestBuilder<CalendarSyncWorker>(5, TimeUnit.MINUTES)
                        .addTag(CALENDAR_WORKER_TAG)
                        .build()

                workManager.enqueue(workRequest)

                _loading.value = false
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    private fun queryEvent(eventId: Long): Boolean {
        val uri: Uri = CalendarContract.Events.CONTENT_URI
        val selection = "(${CalendarContract.Events._ID} = ?) AND (deleted = 0)"
        val selectionArgs = arrayOf(eventId.toString())

        val eventProjection: Array<String> = arrayOf(CalendarContract.Events._ID)
        val cur: Cursor? =
            application.contentResolver.query(uri, eventProjection, selection, selectionArgs, null)

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
            put(CalendarContract.Events.TITLE, createEventTitle(plannerItem))
            put(CalendarContract.Events.DESCRIPTION, createUrl(plannerItem))
            put(CalendarContract.Events.CALENDAR_ID, calID)
            put(CalendarContract.Events.EVENT_TIMEZONE, "Mountain Time")
        }

        val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId!!)
        application.contentResolver.update(updateUri, values, null, null)
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
            put(CalendarContract.Events.TITLE, createEventTitle(plannerItem))
            put(CalendarContract.Events.DESCRIPTION, createUrl(plannerItem))
            put(CalendarContract.Events.CALENDAR_ID, calID)
            put(CalendarContract.Events.EVENT_TIMEZONE, "Mountain Time")
        }
        val uri: Uri? =
            application.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

        // get the event ID that is the last element in the Uri
        val eventID: Long = uri?.lastPathSegment?.toLong() ?: -1

        val eventIds = StudentPrefs.eventIds
        val newEventIds = eventIds.plus(plannerItem.plannableId to eventID)
        StudentPrefs.eventIds = newEventIds
    }

    private fun createEventTitle(plannerItem: PlannerItem): String {
        return when (plannerItem.plannableType) {
            PlannableType.ANNOUNCEMENT -> application.getString(R.string.calendarSync_announcement, plannerItem.contextName, plannerItem.plannable.title)
            PlannableType.DISCUSSION_TOPIC -> application.getString(R.string.calendarSync_discussion_topic, plannerItem.contextName, plannerItem.plannable.title)
            PlannableType.CALENDAR_EVENT -> application.getString(R.string.calendarSync_calendar_event, plannerItem.contextName, plannerItem.plannable.title)
            PlannableType.ASSIGNMENT -> application.getString(R.string.calendarSync_assignment, plannerItem.contextName, plannerItem.plannable.title)
            PlannableType.PLANNER_NOTE -> application.getString(R.string.calendarSync_planner_note, plannerItem.contextName, plannerItem.plannable.title)
            PlannableType.QUIZ -> application.getString(R.string.calendarSync_quiz, plannerItem.contextName, plannerItem.plannable.title)
            PlannableType.TODO -> application.getString(R.string.calendarSync_todo, plannerItem.contextName, plannerItem.plannable.title)
            PlannableType.WIKI_PAGE -> application.getString(R.string.calendarSync_page, plannerItem.contextName, plannerItem.plannable.title)
        }
    }

    private fun createUrl(plannerItem: PlannerItem): String {
        return if (plannerItem.htmlUrl?.contains(apiPrefs.fullDomain) == true) {
            plannerItem.htmlUrl!!
        } else {
            "${apiPrefs.fullDomain}${plannerItem.htmlUrl}"
        }
    }

    private fun disableSync() {
        _data.postValue(emptyList())

        workManager.cancelAllWorkByTag(CALENDAR_WORKER_TAG)
    }
}
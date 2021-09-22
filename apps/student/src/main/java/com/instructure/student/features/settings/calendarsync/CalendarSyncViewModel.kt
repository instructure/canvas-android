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
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
private val EVENT_PROJECTION: Array<String> = arrayOf(
    CalendarContract.Calendars._ID,                     // 0
    CalendarContract.Calendars.ACCOUNT_NAME,            // 1
    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 2
    CalendarContract.Calendars.OWNER_ACCOUNT            // 3
)

// The indices for the projection array above.
private const val PROJECTION_ID_INDEX: Int = 0
private const val PROJECTION_ACCOUNT_NAME_INDEX: Int = 1
private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 2
private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 3

@HiltViewModel
class CalendarSyncViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    val data: LiveData<List<CalendarItemViewModel>>
        get() = _data
    private val _data = MutableLiveData<List<CalendarItemViewModel>>(emptyList())

    fun syncSwitchChanged(checked: Boolean) {
        if (checked) enableSync() else disableSync()
    }

    private fun enableSync() {
        viewModelScope.launch {
            val calendars = getCalendars()
                .map { CalendarItemViewModel(it) }
            _data.postValue(calendars)
        }
    }

    private fun getCalendars(): List<CalendarViewData> {
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val cur: Cursor? = application.contentResolver.query(uri, EVENT_PROJECTION, null, null, null)

        val calendars = mutableListOf<CalendarViewData>()

        while (cur?.moveToNext() == true) {
            // Get the field values
            val calID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
            val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)

            calendars.add(CalendarViewData(calID, displayName, accountName, ownerName))
        }

        return calendars
    }

    private fun disableSync() {
        _data.postValue(emptyList())
    }
}
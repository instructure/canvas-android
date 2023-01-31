/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instructure.pandautils.mvvm.Event

class InboxSharedViewModel : ViewModel() {

    val events: LiveData<Event<InboxFilterAction>>
        get() = _events
    private val _events = MutableLiveData<Event<InboxFilterAction>>()

    fun selectContextId(id: Long) {
        _events.value = Event(InboxFilterAction.FilterSelected(id))
    }

    fun clearFilter() {
        _events.value = Event(InboxFilterAction.FilterCleared)
    }
}

sealed class InboxFilterAction {
    data class FilterSelected(val id: Long) : InboxFilterAction()
    object FilterCleared : InboxFilterAction()
}

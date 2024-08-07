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

package com.instructure.pandautils.features.calendar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CalendarSharedEvents {
    private val _events = MutableSharedFlow<SharedCalendarAction>()
    val events = _events.asSharedFlow()

    fun sendEvent(coroutineScope: CoroutineScope, event: SharedCalendarAction) {
        coroutineScope.launch {
            delay(event.delay)
            _events.emit(event)
        }
    }

    fun filtersClosed(coroutineScope: CoroutineScope, changed: Boolean) {
        sendEvent(coroutineScope, SharedCalendarAction.FiltersClosed(changed))
    }
}

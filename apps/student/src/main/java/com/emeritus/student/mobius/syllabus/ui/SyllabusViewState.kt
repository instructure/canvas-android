/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.emeritus.student.mobius.syllabus.ui

sealed class SyllabusViewState {
    object Loading : SyllabusViewState()

    data class Loaded(
        val syllabus: String? = null,
        val eventsState: EventsViewState? = null
    ) : SyllabusViewState()
}

sealed class EventsViewState(val visibility: EventsVisibility) {
    object Empty : EventsViewState(EventsVisibility(empty = true))
    object Error : EventsViewState(EventsVisibility(error = true))
    data class Loaded(val events: List<ScheduleItemViewState>) : EventsViewState(EventsVisibility(list = true))
}

data class EventsVisibility(
    val empty: Boolean = false,
    val error: Boolean = false,
    val list: Boolean = false
)

data class ScheduleItemViewState(
    val id: String,
    val title: String,
    val date: String,
    val iconRes: Int,
    val color: Int
)

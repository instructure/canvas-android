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
package com.instructure.student.mobius.syllabus

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.DataResult

sealed class SyllabusEvent {
    object PullToRefresh : SyllabusEvent()
    data class DataLoaded(val events: DataResult<List<ScheduleItem>>) : SyllabusEvent()
    data class AssignmentClicked(val assignmentId: Long) : SyllabusEvent()
}

sealed class SyllabusEffect {
    data class LoadData(val contextId: Long, val forceNetwork: Boolean) : SyllabusEffect()
    data class ShowAssignmentView(val assignment: Assignment, val canvasContext: CanvasContext) : SyllabusEffect()
}

data class SyllabusModel(
    val isLoading: Boolean = false,
    val canvasContext: CanvasContext,
    val syllabus: ScheduleItem,
    val events: DataResult<List<ScheduleItem>>? = null
)

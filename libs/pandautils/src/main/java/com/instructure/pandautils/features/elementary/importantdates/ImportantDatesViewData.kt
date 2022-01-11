/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.elementary.importantdates

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.mvvm.ItemViewModel
import java.util.*

data class ImportantDatesViewData(
        val itemViewModels: List<ItemViewModel>
)

data class ImportantDatesHeaderViewData(val title: String)

sealed class ImportantDatesAction {
    data class OpenCourse(val course: Course) : ImportantDatesAction()
    data class OpenAssignment(val canvasContext: CanvasContext, val assignmentId: Long) : ImportantDatesAction()
    data class OpenCalendarEvent(val canvasContext: CanvasContext, val scheduleItemId: Long) : ImportantDatesAction()
    data class OpenQuiz(val canvasContext: CanvasContext, val htmlUrl: String) : ImportantDatesAction()
    data class OpenDiscussion(val canvasContext: CanvasContext, val id: Long, val title: String) : ImportantDatesAction()
}
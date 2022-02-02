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

import androidx.annotation.DrawableRes
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.elementary.importantdates.itemviewmodels.ImportantDatesHeaderItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel
import java.util.*

data class ImportantDatesViewData(
        val itemViewModels: List<ImportantDatesHeaderItemViewModel>
)

data class ImportantDatesHeaderViewData(val title: String)

data class ImportantDatesItemViewData(
        val scheduleItemId: Long,
        val title: String,
        val courseName: String,
        @DrawableRes val icon: Int,
        val courseColor: String,
)

sealed class ImportantDatesAction {
    data class OpenAssignment(val canvasContext: CanvasContext, val assignmentId: Long) : ImportantDatesAction()
    data class OpenCalendarEvent(val canvasContext: CanvasContext, val scheduleItem: ScheduleItem) : ImportantDatesAction()
    data class ShowToast(val toast: String) : ImportantDatesAction()
}

enum class ImportantDatesViewType(val viewType: Int) {
    HEADER(0),
    ITEM(1)
}
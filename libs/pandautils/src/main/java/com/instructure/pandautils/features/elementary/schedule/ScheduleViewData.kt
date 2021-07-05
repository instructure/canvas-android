/*
 * Copyright (C) 2021 - present Instructure, Inc.
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

package com.instructure.pandautils.features.elementary.schedule

import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.mvvm.ItemViewModel

data class ScheduleViewData(val itemViewModels: List<ItemViewModel>)

data class ScheduleCourseViewData(
        val courseName: String,
        val openable: Boolean,
        val courseColor: String,
        val imageUrl: String,
        val plannerItems: List<ItemViewModel>
)

sealed class ScheduleAction {

}
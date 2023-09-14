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

package com.instructure.pandautils.features.dashboard.edit

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardCourseItemViewModel
import com.instructure.pandautils.features.dashboard.edit.itemviewmodels.EditDashboardGroupItemViewModel
import com.instructure.pandautils.mvvm.ItemViewModel

data class EditDashboardViewData(val items: List<ItemViewModel>)

sealed class EditDashboardItemAction {
    data class FavoriteCourse(val itemViewModel: EditDashboardCourseItemViewModel) : EditDashboardItemAction()
    data class FavoriteGroup(val itemViewModel: EditDashboardGroupItemViewModel) : EditDashboardItemAction()
    data class UnfavoriteCourse(val itemViewModel: EditDashboardCourseItemViewModel) : EditDashboardItemAction()
    data class UnfavoriteGroup(val itemViewModel: EditDashboardGroupItemViewModel) : EditDashboardItemAction()
    data class OpenItem(val canvasContext: CanvasContext?) : EditDashboardItemAction()
    data class OpenCourse(val id: Long) : EditDashboardItemAction()
    data class OpenGroup(val id: Long) : EditDashboardItemAction()
    data class ShowSnackBar(val res: Int) : EditDashboardItemAction()
}

enum class EditDashboardItemViewType(val viewType: Int) {
    COURSE(0),
    GROUP(1),
    HEADER(2),
    DESCRIPTION(3),
    ENROLLMENT(4),
    NOTE(5),
}
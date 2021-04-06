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

package com.instructure.student.features.dashboard.edit

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Group

data class EditDashboardViewData(val items: List<EditDashboardItemViewModel>)

sealed class EditDashboardItemAction {
    data class FavoriteItem(val itemViewModel: EditDashboardItemViewModel) : EditDashboardItemAction()
    data class UnfavoriteItem(val itemViewModel: EditDashboardItemViewModel) : EditDashboardItemAction()
    data class OpenItem(val model: CanvasContext) : EditDashboardItemAction()
}

enum class EditDashboardItemViewType(val viewType: Int) {
    GROUP(0),
    COURSE(1)
}
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
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.pandautils.utils.isGroup
import com.instructure.student.R

class EditDashboardItemViewModel(val id: Long, val name: String?, var isFavorite: Boolean, val subtitle: String?, val termTitle: String?, val type: CanvasContext.Type, private val onClick: (EditDashboardItemAction) -> Unit) : ItemViewModel {

    override val layoutId: Int = R.layout.viewholder_edit_dashboard

    override val viewType: Int
        get() = if (type == CanvasContext.Type.GROUP) EditDashboardItemViewType.GROUP.viewType else EditDashboardItemViewType.COURSE.viewType

    fun onClick() {
        if (type == CanvasContext.Type.COURSE) {
            onClick(EditDashboardItemAction.OpenCourse(id))
        }
        if (type == CanvasContext.Type.GROUP) {
            onClick(EditDashboardItemAction.OpenGroup(id))
        }
    }

    fun onFavoriteClick() {
        if (isFavorite) {
            onClick(EditDashboardItemAction.UnfavoriteItem(this))
        } else {
            onClick(EditDashboardItemAction.FavoriteItem(this))
        }
    }
}
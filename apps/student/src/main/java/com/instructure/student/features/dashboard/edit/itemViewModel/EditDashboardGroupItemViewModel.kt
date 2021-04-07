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

package com.instructure.student.features.dashboard.edit.itemViewModel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.student.features.dashboard.edit.EditDashboardItemAction
import com.instructure.student.R
import com.instructure.student.features.dashboard.edit.EditDashboardItemViewType

class EditDashboardGroupItemViewModel(val id: Long, val name: String?, @get:Bindable var isFavorite: Boolean, val subtitle: String?, val termTitle: String?, private val actionHandler: (EditDashboardItemAction) -> Unit) : ItemViewModel, BaseObservable() {
    override val layoutId: Int = R.layout.viewholder_edit_dashboard_group

    override val viewType: Int = EditDashboardItemViewType.GROUP.viewType

    fun onClick() {
        actionHandler(EditDashboardItemAction.OpenGroup(id))
    }

    fun onFavoriteClick() {
        if (isFavorite) {
            actionHandler(EditDashboardItemAction.UnfavoriteGroup(this))
        } else {
            actionHandler(EditDashboardItemAction.FavoriteGroup(this))
        }
    }
}
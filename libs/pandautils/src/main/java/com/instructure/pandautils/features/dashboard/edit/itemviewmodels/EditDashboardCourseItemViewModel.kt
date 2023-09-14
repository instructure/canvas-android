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

package com.instructure.pandautils.features.dashboard.edit.itemviewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.edit.EditDashboardItemAction
import com.instructure.pandautils.features.dashboard.edit.EditDashboardItemViewType
import com.instructure.pandautils.mvvm.ItemViewModel

class EditDashboardCourseItemViewModel(
    val id: Long,
    val name: String?,
    @get:Bindable var isFavorite: Boolean,
    val favoritableOnline: Boolean,
    val openable: Boolean,
    val termTitle: String,
    val online: Boolean,
    val availableOffline: Boolean,
    val enabled: Boolean,
    private val actionHandler: (EditDashboardItemAction) -> Unit
) : ItemViewModel, BaseObservable() {

    override val layoutId: Int = R.layout.viewholder_edit_dashboard_course

    override val viewType: Int = EditDashboardItemViewType.COURSE.viewType

    val favoritable: Boolean
        get() = favoritableOnline && online

    fun onClick() {
        if (!openable) {
            actionHandler(EditDashboardItemAction.ShowSnackBar(R.string.unauthorized))
            return
        }

        actionHandler(EditDashboardItemAction.OpenCourse(id))
    }

    fun onFavoriteClick() {
        when {
            !online -> actionHandler(EditDashboardItemAction.ShowSnackBar(R.string.coursesCannotBeFavoritedOffline))
            !favoritableOnline -> actionHandler(EditDashboardItemAction.ShowSnackBar(R.string.inactive_courses_cant_be_added_to_dashboard))
            isFavorite -> actionHandler(EditDashboardItemAction.UnfavoriteCourse(this))
            else -> actionHandler(EditDashboardItemAction.FavoriteCourse(this))
        }
    }
}
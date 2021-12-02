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

package com.instructure.pandautils.features.dashboard.notifications.itemviewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.AnnouncementViewData
import com.instructure.pandautils.mvvm.ItemViewModel

class AnnouncementItemViewModel(
    val data: AnnouncementViewData,
    val dismiss: (AnnouncementItemViewModel, Long) -> Unit,
    val open: (String, String) -> Unit,
    @get:Bindable var inProgress: Boolean = false
) : ItemViewModel, BaseObservable() {
    override val layoutId: Int = R.layout.item_dashboard_announcement

    fun dismiss() {
        dismiss(this, data.id)
    }

    fun open() {
        open(data.subject, data.message)
    }
}
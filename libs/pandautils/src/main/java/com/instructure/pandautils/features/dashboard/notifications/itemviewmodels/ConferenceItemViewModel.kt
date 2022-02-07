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
import com.instructure.canvasapi2.models.Conference
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.notifications.ConferenceViewData
import com.instructure.pandautils.mvvm.ItemViewModel

class ConferenceItemViewModel(
    val data: ConferenceViewData,
    @get:Bindable var isJoining: Boolean = false,
    val handleJoin: (ConferenceItemViewModel, Conference) -> Unit,
    val handleDismiss: (Conference) -> Unit
) : ItemViewModel, BaseObservable() {
    override val layoutId: Int = R.layout.item_dashboard_conference

    fun handleJoin() {
        if (!isJoining) {
            handleJoin(this, data.conference)
        }
    }

    fun handleDismiss() {
        if (!isJoining) {
            handleDismiss(data.conference)
        }
    }
}
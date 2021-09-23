/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.student.features.settings.calendarsync

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.student.R

class CalendarItemViewModel(
    val calendarViewData: CalendarViewData,
    @get:Bindable var selected: Boolean,
    private val calendarClicked: () -> Unit
): BaseObservable(), ItemViewModel {

    override val layoutId: Int = R.layout.item_calendar

    val subtitle: String = "${calendarViewData.accountType} - ${calendarViewData.accountName}"

    fun onClick() {
        calendarClicked()
        selected = true
        notifyChange()
    }

    fun unselect() {
        selected = false
        notifyChange()
    }
}
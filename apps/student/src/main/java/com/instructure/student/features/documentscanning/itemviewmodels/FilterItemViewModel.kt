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

package com.instructure.student.features.documentscanning.itemviewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.BR
import com.instructure.student.features.documentscanning.FilterItemViewData
import com.instructure.pandautils.mvvm.ItemViewModel
import com.instructure.student.R

class FilterItemViewModel(
        val data: FilterItemViewData,
        @get:Bindable var selected: Boolean,
        val onSelect: (FilterItemViewModel) -> Unit
) : ItemViewModel, BaseObservable() {
    override val layoutId: Int = R.layout.item_document_scanning_filter

    fun select() {
        if (!selected) {
            selected = true
            notifyPropertyChanged(BR.selected)
            onSelect(this)
        }
    }
}
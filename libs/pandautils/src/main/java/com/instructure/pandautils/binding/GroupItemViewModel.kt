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

package com.instructure.pandautils.binding

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.BR
import com.instructure.pandautils.mvvm.ItemViewModel

abstract class GroupItemViewModel(
    val collapsable: Boolean,
    @get:Bindable open var collapsed: Boolean = collapsable,
    var items: List<ItemViewModel>
) : ItemViewModel, BaseObservable() {

    open fun toggleItems() {
        collapsed = !collapsed
        notifyPropertyChanged(BR.collapsed)
    }

    fun getAllVisibleItems(): List<ItemViewModel> {
        val result = mutableListOf<ItemViewModel>()
        items.forEach {
            result += it
            if (it is GroupItemViewModel && !it.collapsed) {
                result += it.getAllVisibleItems()
            }
        }
        return result
    }
}
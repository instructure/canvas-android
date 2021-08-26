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

package com.instructure.pandautils.features.elementary.schedule

import android.view.LayoutInflater
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.instructure.pandautils.adapters.itemdecorations.StickyHeaderInterface
import com.instructure.pandautils.adapters.itemdecorations.StickyHeaderItemDecoration
import com.instructure.pandautils.binding.BindableRecyclerViewAdapter
import com.instructure.pandautils.databinding.ItemScheduleDayHeaderBinding
import com.instructure.pandautils.features.elementary.schedule.itemviewmodels.ScheduleDayGroupItemViewModel

class ScheduleRecyclerViewAdapter : BindableRecyclerViewAdapter(), StickyHeaderInterface {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(StickyHeaderItemDecoration(this))
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var startPosition = itemPosition
        var headerPosition = 0
        do {
            if (isHeader(startPosition)) {
                headerPosition = startPosition
                break
            }
            startPosition -= 1
        } while (startPosition >= 0)
        return headerPosition
    }

    override fun getHeaderBinding(
        headerPosition: Int,
        parent: RecyclerView,
        hasChildInContact: Boolean
    ): ViewDataBinding {
        val binding = ItemScheduleDayHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.setVariable(BR.itemViewModel, itemViewModels[headerPosition])
        binding.hasDivider = hasChildInContact
        binding.invalidateAll()
        return binding
    }

    override fun isHeader(itemPosition: Int): Boolean {
        if (itemPosition == RecyclerView.NO_POSITION) return false
        return itemViewModels[itemPosition] is ScheduleDayGroupItemViewModel
    }
}
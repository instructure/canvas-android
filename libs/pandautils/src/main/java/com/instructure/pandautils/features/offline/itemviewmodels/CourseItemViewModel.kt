/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.pandautils.features.offline.itemviewmodels

import android.widget.CompoundButton.OnCheckedChangeListener
import com.instructure.pandautils.R
import com.instructure.pandautils.binding.GroupItemViewModel
import com.instructure.pandautils.features.offline.CourseItemViewData
import com.instructure.pandautils.features.offline.OfflineItemViewModelType
import com.instructure.pandautils.mvvm.ItemViewModel

data class CourseItemViewModel(
    val data: CourseItemViewData,
    val courseId: Long,
    val onCheckedChanged: (Boolean, CourseItemViewModel) -> Unit
) : GroupItemViewModel(collapsable = true, collapsed = data.collapsed, items = data.tabs) {
    override val layoutId = R.layout.item_offline_course
    override val viewType = OfflineItemViewModelType.COURSE.viewType

    val onCheckChanged = OnCheckedChangeListener { cb, checked ->
        if (cb.isPressed) onCheckedChanged(checked, this)
    }

    override fun areContentsTheSame(other: ItemViewModel): Boolean {
        return other is CourseItemViewModel
                && other.courseId == this.courseId
                && other.data == this.data
    }

    override fun areItemsTheSame(other: ItemViewModel): Boolean {
        return other is CourseItemViewModel && other.courseId == this.courseId
    }
}

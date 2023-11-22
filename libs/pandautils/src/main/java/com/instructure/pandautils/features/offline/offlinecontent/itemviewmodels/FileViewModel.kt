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

package com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels

import android.widget.CompoundButton
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.offlinecontent.FileViewData
import com.instructure.pandautils.features.offline.offlinecontent.OfflineItemViewModelType
import com.instructure.pandautils.mvvm.ItemViewModel

data class FileViewModel(
    val data: FileViewData,
    val courseId: Long,
    val fileId: Long,
    val fileUrl: String?,
    val tabId: String,
    val onCheckedChanged: (Boolean, FileViewModel) -> Unit
) : ItemViewModel {
    override val layoutId = R.layout.item_offline_file
    override val viewType = OfflineItemViewModelType.FILE.viewType

    val onCheckChanged = CompoundButton.OnCheckedChangeListener { cb, checked ->
        if (cb.isPressed) onCheckedChanged(checked, this)
    }

    fun onRowClicked() {
        data.checked = !data.checked
        onCheckedChanged(data.checked, this)
    }

    override fun areContentsTheSame(other: ItemViewModel): Boolean {
        return other is FileViewModel
                && other.courseId == this.courseId
                && other.tabId == this.tabId
                && other.data == this.data
    }

    override fun areItemsTheSame(other: ItemViewModel): Boolean {
        return other is FileViewModel
                && other.courseId == this.courseId
                && other.tabId == this.tabId
    }
}

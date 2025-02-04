/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 */

package com.instructure.pandautils.features.offline.offlinecontent

import androidx.databinding.BaseObservable
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.FileViewModel

data class OfflineContentViewData(
    val storageInfo: StorageInfo,
    val courseItems: List<CourseItemViewModel>,
    val selectedCount: Int
) : BaseObservable()

data class StorageInfo(
    val otherAppsReservedPercent: Int,
    val allAppsReservedPercent: Int,
    val storageInfoText: String,
    val contentDescription: String
)

data class CourseItemViewData(
    var fullContentSync: Boolean,
    val title: String,
    val size: String,
    val tabs: List<CourseTabViewModel>
) : BaseObservable() {

    fun checkedState(): Int {
        return when {
            fullContentSync -> MaterialCheckBox.STATE_CHECKED
            tabs.isNotEmpty() && tabs.all { it.data.synced } -> MaterialCheckBox.STATE_CHECKED
            tabs.any { it.data.synced || it.data.files.any { file -> file.data.checked } } -> MaterialCheckBox.STATE_INDETERMINATE
            else -> MaterialCheckBox.STATE_UNCHECKED
        }
    }
}

data class CourseTabViewData(
    var synced: Boolean,
    val title: String,
    val size: String,
    val files: List<FileViewModel>
) : BaseObservable() {

    fun checkedState(): Int {
        return when {
            synced -> MaterialCheckBox.STATE_CHECKED
            files.isNotEmpty() && files.all { it.data.checked } -> MaterialCheckBox.STATE_CHECKED
            files.any { it.data.checked } -> MaterialCheckBox.STATE_INDETERMINATE
            else -> MaterialCheckBox.STATE_UNCHECKED
        }
    }
}

data class FileViewData(
    var checked: Boolean,
    val title: String,
    val size: String
) : BaseObservable()

enum class OfflineItemViewModelType(val viewType: Int) {
    COURSE(1),
    COURSE_TAB(2),
    FILE(3),
    EMPTY_COURSE_CONTENT(4)
}

sealed class OfflineContentAction {
    object Back : OfflineContentAction()
    data class Dialog(
        val title: String,
        val message: String,
        val positive: String,
        val positiveCallback: () -> Unit
    ) : OfflineContentAction()
}

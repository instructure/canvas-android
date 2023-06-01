package com.instructure.pandautils.features.offline

import androidx.databinding.BaseObservable
import com.google.android.material.checkbox.MaterialCheckBox
import com.instructure.pandautils.features.offline.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.FileViewModel

data class OfflineContentViewData(
    val storageInfo: StorageInfo,
    val courseItems: List<CourseItemViewModel>,
    val selectedCount: Int
) : BaseObservable()

data class StorageInfo(val otherAppsReservedPercent: Int, val allAppsReservedPercent: Int, val storageInfoText: String)

data class CourseItemViewData(
    val fullContentSync: Boolean,
    val title: String,
    val size: String,
    val tabs: List<CourseTabViewModel>
) : BaseObservable() {

    fun checkedState(): Int {
        return when {
            fullContentSync -> MaterialCheckBox.STATE_CHECKED
            tabs.all { it.data.checked } -> MaterialCheckBox.STATE_CHECKED
            tabs.any { it.data.checked } -> MaterialCheckBox.STATE_INDETERMINATE
            else -> MaterialCheckBox.STATE_UNCHECKED
        }
    }
}

data class CourseTabViewData(
    val checked: Boolean,
    val title: String,
    val size: String,
    val files: List<FileViewModel>
) : BaseObservable() {

    fun checkedState(): Int {
        return when{
            checked -> MaterialCheckBox.STATE_CHECKED
            files.all { it.data.checked } -> MaterialCheckBox.STATE_CHECKED
            files.any { it.data.checked } -> MaterialCheckBox.STATE_INDETERMINATE
            else -> MaterialCheckBox.STATE_UNCHECKED
        }
    }
}

data class FileViewData(
    val checked: Boolean,
    val title: String,
    val size: String
) : BaseObservable()

enum class OfflineItemViewModelType(val viewType: Int) {
    COURSE(1),
    COURSE_TAB(2),
    FILE(3)
}

sealed class OfflineContentAction {
    object Back : OfflineContentAction()
}

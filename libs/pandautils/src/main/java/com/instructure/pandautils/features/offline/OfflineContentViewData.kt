package com.instructure.pandautils.features.offline

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.instructure.pandautils.features.offline.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.FileViewModel

data class OfflineContentViewData(
    val storageInfo: StorageInfo,
    val courseItems: List<CourseItemViewModel>,
    @get:Bindable var selectedCount: Int
) : BaseObservable()

data class StorageInfo(val otherPercent: Int, val canvasPercent: Int, val storageInfoText: String)

data class CourseItemViewData(
    @Bindable var checked: Boolean,
    val title: String,
    val size: String,
    val collapsed: Boolean,
    val tabs: List<CourseTabViewModel>
) : BaseObservable()

data class CourseTabViewData(
    @Bindable var checked: Boolean,
    val title: String,
    val size: String,
    val files: List<FileViewModel>
) : BaseObservable()

data class FileViewData(
    @Bindable var checked: Boolean,
    val title: String,
    val size: String
) : BaseObservable()

enum class OfflineItemViewModelType(val viewType: Int) {
    COURSE(1),
    COURSE_TAB(2),
    FILE(3)
}

sealed class OfflineContentAction {

}

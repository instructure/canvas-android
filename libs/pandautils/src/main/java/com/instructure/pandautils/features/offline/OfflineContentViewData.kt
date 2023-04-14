package com.instructure.pandautils.features.offline

import androidx.databinding.BaseObservable
import com.instructure.pandautils.features.offline.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.FileViewModel

data class OfflineContentViewData(
    val storageInfo: StorageInfo,
    val courseItems: List<CourseItemViewModel>
) : BaseObservable()

data class StorageInfo(val otherPercent: Int, val canvasPercent: Int, val storageInfoText: String)

data class CourseItemViewData(val title: String, val size: String, val collapsed: Boolean, val tabs: List<CourseTabViewModel>)

data class CourseTabViewData(val title: String, val size: String, val courseId: Long, val files: List<FileViewModel>)

data class FileViewData(val title: String, val size: String)

enum class OfflineItemViewModelType(val viewType: Int) {
    COURSE(1),
    COURSE_TAB(2),
    FILE(3)
}

sealed class OfflineContentAction {

}

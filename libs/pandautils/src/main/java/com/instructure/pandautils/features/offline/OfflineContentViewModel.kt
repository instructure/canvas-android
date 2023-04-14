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

package com.instructure.pandautils.features.offline

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Environment
import android.text.format.Formatter
import androidx.lifecycle.*
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.FileViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class OfflineContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val resources: Resources,
    private val offlineContentRepository: OfflineContentRepository
) : ViewModel() {

    val course = savedStateHandle.get<Course>(Const.CANVAS_CONTEXT)

    val state: LiveData<ViewState>
        get() = _state
    private val _state = MutableLiveData<ViewState>()

    val data: LiveData<OfflineContentViewData>
        get() = _data
    private val _data = MutableLiveData<OfflineContentViewData>()

    val events: LiveData<Event<OfflineContentAction>>
        get() = _events
    private val _events = MutableLiveData<Event<OfflineContentAction>>()

    init {
        loadData()
    }

    private fun loadData() {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            val storageInfo = getStorageInfo()
            val coursesData = getCoursesData(course?.id)
            val data = OfflineContentViewData(storageInfo, coursesData)
            _data.postValue(data)
            _state.postValue(ViewState.Success)
        }
    }

    private suspend fun getCoursesData(courseId: Long? = null): List<CourseItemViewModel> {
        val courses = offlineContentRepository.getCourses(courseId)
        return courses.map { course ->
            val tabs = offlineContentRepository.getTabs(course.id)
            val files = offlineContentRepository.getCourseFiles(course.id)
            val size = Formatter.formatShortFileSize(context, files.sumOf { it.size })

            CourseItemViewModel(
                CourseItemViewData(
                    course.name, size, false, tabs.map { tab ->
                        CourseTabViewModel(
                            if (tab.tabId == Tab.FILES_ID) {
                                CourseTabViewData(tab.label.orEmpty(), size, course.id, files.map {
                                    val fileSize = Formatter.formatShortFileSize(context, it.size)
                                    FileViewModel(FileViewData(it.displayName.orEmpty(), fileSize))
                                })
                            } else {
                                CourseTabViewData(tab.label.orEmpty(), "", course.id, emptyList())
                            }
                        )
                    }
                )
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun getStorageInfo(): StorageInfo {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        val appInfo = packageInfo.applicationInfo

        val appSize = File(appInfo.publicSourceDir).length()
        val dataDirSize = getDirSize(File(appInfo.dataDir))
        val cacheDirSize = getDirSize(context.cacheDir)
        val externalCacheDirSize = getDirSize(context.externalCacheDir)
        val filesDirSize = getDirSize(context.filesDir)
        val externalFilesDirSize = getDirSize(context.getExternalFilesDir(null))

        val totalAppSpace = appSize + dataDirSize + cacheDirSize + externalCacheDirSize + filesDirSize + externalFilesDirSize
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        val totalSpace = externalStorageDirectory.totalSpace
        val freeSpace = totalSpace - externalStorageDirectory.freeSpace
        val otherAppsSpace = freeSpace - appSize

        val otherPercent = if (totalSpace > 0) (otherAppsSpace.toFloat() / totalSpace * 100).toInt() else 0
        val canvasPercent = if (totalSpace > 0) (totalAppSpace.toFloat() / totalSpace * 100).toInt().coerceAtLeast(1) + otherPercent else 0
        val storageIndoText = resources.getString(
            R.string.offline_content_storage_info,
            Formatter.formatShortFileSize(context, freeSpace),
            Formatter.formatShortFileSize(context, totalSpace),
        )

        return StorageInfo(otherPercent, canvasPercent, storageIndoText)
    }

    private fun getDirSize(directory: File?): Long {
        if (directory == null || !directory.isDirectory) return 0
        var size: Long = 0
        val files = directory.listFiles() ?: return 0
        for (file in files) size += if (file.isDirectory) getDirSize(file) else file.length()
        return size
    }
}

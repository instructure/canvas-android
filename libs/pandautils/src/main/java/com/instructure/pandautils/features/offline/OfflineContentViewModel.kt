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
import androidx.databinding.Observable
import androidx.lifecycle.*
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.BR
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

    private val onCheckChangedCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (propertyId == BR.checked) {
                updateSelectedItems()
            }
        }
    }

    init {
        loadData(false)
    }

    private fun loadData(forceNetwork: Boolean) {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            val storageInfo = getStorageInfo()
            val coursesData = getCoursesData(course?.id, forceNetwork)
            val data = OfflineContentViewData(storageInfo, coursesData, 0)
            _data.postValue(data)
            _state.postValue(ViewState.Success)
        }
    }

    private suspend fun getCoursesData(courseId: Long? = null, forceNetwork: Boolean): List<CourseItemViewModel> {
        val courses = if (courseId == null) {
            offlineContentRepository.getCourses(forceNetwork)
        } else {
            listOf(offlineContentRepository.getCourse(courseId, forceNetwork))
        }

        return courses.map { course ->
            val tabs = offlineContentRepository.getTabs(course.id, forceNetwork)
            val files = offlineContentRepository.getCourseFiles(course.id, forceNetwork)
            val size = Formatter.formatShortFileSize(context, files.sumOf { it.size })
            createCourseItemViewModel(course, size, tabs, files)
        }
    }

    private fun createCourseItemViewModel(course: Course, size: String, tabs: List<Tab>, files: List<FileFolder>) = CourseItemViewModel(
        CourseItemViewData(false, course.name, size, false, tabs.map { tab ->
            createTabViewModel(course.id, tab, size, files)
        }).apply {
            removeOnPropertyChangedCallback(onCheckChangedCallback)
            addOnPropertyChangedCallback(onCheckChangedCallback)
        }, course.id
    )

    private fun createTabViewModel(courseId: Long, tab: Tab, size: String, files: List<FileFolder>) = CourseTabViewModel(
        if (tab.tabId == Tab.FILES_ID) {
            CourseTabViewData(false, tab.label.orEmpty(), size, files.map { fileFolder ->
                createFileViewModel(fileFolder, courseId, tab.tabId)
            }).apply {
                removeOnPropertyChangedCallback(onCheckChangedCallback)
                addOnPropertyChangedCallback(onCheckChangedCallback)
            }
        } else {
            CourseTabViewData(false, tab.label.orEmpty(), "", emptyList())
        }, courseId, tab.tabId
    ) { courseTabViewModel ->
        _data.value?.courseItems?.find { it.courseId == courseTabViewModel.courseId }?.apply {
            data.checked = data.tabs.all { it.data.checked }
            data.notifyPropertyChanged(BR.checked)
        }
    }

    private fun createFileViewModel(fileFolder: FileFolder, courseId: Long, tabId: String): FileViewModel {
        val fileSize = Formatter.formatShortFileSize(context, fileFolder.size)
        return FileViewModel(FileViewData(false, fileFolder.displayName.orEmpty(), fileSize).apply {
            removeOnPropertyChangedCallback(onCheckChangedCallback)
            addOnPropertyChangedCallback(onCheckChangedCallback)
        }, courseId, tabId) { fileViewModel ->
            _data.value?.courseItems
                ?.find { it.courseId == fileViewModel.courseId }?.data?.tabs
                ?.find { it.tabId == fileViewModel.tabId }
                ?.apply {
                    data.checked = data.files.all { it.data.checked }
                    data.notifyPropertyChanged(BR.checked)
                }
        }
    }

    private fun updateSelectedItems() {
        val selectedTabs = mutableListOf<CourseTabViewModel>()
        val selectedFiles = mutableListOf<FileViewModel>()

        _data.value?.courseItems?.forEach { courseItemViewModel ->
            selectedTabs += courseItemViewModel.data.tabs.filter { it.data.checked && it.tabId != Tab.FILES_ID }
            courseItemViewModel.data.tabs.forEach { courseTabViewModel ->
                selectedFiles += courseTabViewModel.data.files.filter { it.data.checked }
            }
        }

        _data.value?.selectedCount = selectedTabs.size + selectedFiles.size
        _data.value?.notifyPropertyChanged(BR.selectedCount)
    }

    fun onSyncClicked() {

    }

    fun onRefresh() {
        loadData(true)
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

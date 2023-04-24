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
import android.content.res.Resources
import android.text.format.Formatter
import androidx.lifecycle.*
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.FileViewModel
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.StorageUtils
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val resources: Resources,
    private val offlineContentRepository: OfflineContentRepository,
    private val storageUtils: StorageUtils
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
        loadData(false)
    }

    private fun loadData(forceNetwork: Boolean) {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            try {
                val storageInfo = getStorageInfo()
                val coursesData = getCoursesData(course?.id, forceNetwork)
                val data = OfflineContentViewData(storageInfo, coursesData, 0)
                _data.postValue(data)
                _state.postValue(ViewState.Success)
            } catch (ex: Exception) {
                _state.postValue(ViewState.Error(resources.getString(R.string.offline_content_loading_error)))
            }
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
        CourseItemViewData(false, course.name, size, tabs.map { tab ->
            createTabViewModel(course.id, tab, size, files)
        }), course.id, this.course == null
    ) { checked, item ->
        val courseViewModel = data.value?.courseItems?.find { it == item } ?: return@CourseItemViewModel
        val newTabs = courseViewModel.data.tabs.map { tab ->
            val newFiles = tab.data.files.map { it.copy(data = it.data.copy(checked = checked)) }
            tab.copy(data = tab.data.copy(checked = checked, files = newFiles))
        }
        val newCourseViewModel = courseViewModel.copy(data = item.data.copy(checked = checked, tabs = newTabs))
        val newList = _data.value?.courseItems?.toMutableList()?.apply {
            replaceAll { if (it == item) newCourseViewModel else it }
        }.orEmpty()
        val selectedCount = getSelectedItemCount(newList)
        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    private fun createTabViewModel(courseId: Long, tab: Tab, size: String, files: List<FileFolder>) = CourseTabViewModel(
        if (tab.tabId == Tab.FILES_ID) {
            CourseTabViewData(false, tab.label.orEmpty(), size, files.map { fileFolder ->
                createFileViewModel(fileFolder, courseId, tab.tabId)
            })
        } else {
            CourseTabViewData(false, tab.label.orEmpty(), "", emptyList())
        }, courseId, tab.tabId, false
    ) { checked, item ->
        val courseViewModel = data.value?.courseItems?.find { it.courseId == item.courseId } ?: return@CourseTabViewModel
        val tabViewModel = courseViewModel.data.tabs.find { it == item } ?: return@CourseTabViewModel
        val newFiles = tabViewModel.data.files.map { it.copy(data = it.data.copy(checked = checked)) }
        val newTabViewModel = tabViewModel.copy(data = tabViewModel.data.copy(checked = checked, files = newFiles))
        val newTabs = courseViewModel.data.tabs.toMutableList().apply { replaceAll { if (it == item) newTabViewModel else it } }
        val newCourseViewModel = courseViewModel.copy(data = courseViewModel.data.copy(checked = newTabs.all { it.data.checked }, tabs = newTabs))
        val newList = _data.value?.courseItems?.toMutableList()?.apply {
            replaceAll { if (it == courseViewModel) newCourseViewModel else it }
        }.orEmpty()
        val selectedCount = getSelectedItemCount(newList)
        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    private fun createFileViewModel(fileFolder: FileFolder, courseId: Long, tabId: String): FileViewModel {
        val fileSize = Formatter.formatShortFileSize(context, fileFolder.size)
        return FileViewModel(FileViewData(false, fileFolder.displayName.orEmpty(), fileSize), courseId, tabId) { checked, item ->
            val courseViewModel = data.value?.courseItems?.find { it.courseId == item.courseId } ?: return@FileViewModel
            val tabViewModel = courseViewModel.data.tabs.find { it.tabId == item.tabId } ?: return@FileViewModel
            val fileViewModel = tabViewModel.data.files.find { it == item } ?: return@FileViewModel
            val newFile = fileViewModel.copy(data = fileViewModel.data.copy(checked = checked))
            val newFiles = tabViewModel.data.files.toMutableList().apply { replaceAll { if (it == item) newFile else it } }
            val newTabViewModel = tabViewModel.copy(data = tabViewModel.data.copy(checked = newFiles.all { it.data.checked }, files = newFiles))
            val newTabs = courseViewModel.data.tabs.toMutableList().apply { replaceAll { if (it.tabId == item.tabId) newTabViewModel else it } }
            val newCourseViewModel = courseViewModel.copy(data = courseViewModel.data.copy(checked = newTabs.all { it.data.checked }, tabs = newTabs))
            val newList = _data.value?.courseItems?.toMutableList()?.apply {
                replaceAll { if (it == courseViewModel) newCourseViewModel else it }
            }.orEmpty()
            val selectedCount = getSelectedItemCount(newList)
            _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
        }
    }

    private fun getSelectedItemCount(courses: List<CourseItemViewModel>): Int {
        val selectedTabs = mutableListOf<CourseTabViewModel>()
        val selectedFiles = mutableListOf<FileViewModel>()
        courses.forEach { courseItemViewModel ->
            selectedTabs += courseItemViewModel.data.tabs.filter { it.data.checked && it.tabId != Tab.FILES_ID }
            courseItemViewModel.data.tabs.forEach { courseTabViewModel ->
                selectedFiles += courseTabViewModel.data.files.filter { it.data.checked }
            }
        }

        return selectedTabs.size + selectedFiles.size
    }

    fun toggleSelection() {
        val shouldCheck = _data.value?.selectedCount.orDefault() == 0
        val newList = _data.value?.courseItems?.map { courseItemViewModel ->
            val newTabs = courseItemViewModel.data.tabs.map { tab ->
                val newFiles = tab.data.files.map { it.copy(data = it.data.copy(checked = shouldCheck)) }
                tab.copy(data = tab.data.copy(checked = shouldCheck, files = newFiles))
            }
            courseItemViewModel.copy(data = courseItemViewModel.data.copy(checked = shouldCheck, tabs = newTabs))
        }.orEmpty()
        val selectedCount = getSelectedItemCount(newList)
        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    fun onSyncClicked() {

    }

    fun onRefresh() {
        loadData(true)
    }

    private fun getStorageInfo(): StorageInfo {
        val appSize = storageUtils.getAppSize()
        val totalSpace = storageUtils.getTotalSpace()
        val usedSpace = totalSpace - storageUtils.getFreeSpace()
        val otherAppsSpace = usedSpace - appSize
        val otherPercent = if (totalSpace > 0) (otherAppsSpace.toFloat() / totalSpace * 100).toInt() else 0
        val canvasPercent = if (totalSpace > 0) (appSize.toFloat() / totalSpace * 100).toInt().coerceAtLeast(1) + otherPercent else 0
        val storageInfoText = resources.getString(
            R.string.offline_content_storage_info,
            Formatter.formatShortFileSize(context, usedSpace),
            Formatter.formatShortFileSize(context, totalSpace),
        )

        return StorageInfo(otherPercent, canvasPercent, storageInfoText)
    }
}

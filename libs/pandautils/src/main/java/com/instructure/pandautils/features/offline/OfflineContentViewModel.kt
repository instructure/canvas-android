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
import android.text.format.Formatter
import androidx.lifecycle.*
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.itemviewmodels.FileViewModel
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.offline.entities.CourseSyncSettingsEntity
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.StorageUtils
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

private val ALLOWED_TAB_IDS = listOf(Tab.ASSIGNMENTS_ID, Tab.PAGES_ID, Tab.FILES_ID)

@HiltViewModel
class OfflineContentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val offlineContentRepository: OfflineContentRepository,
    private val storageUtils: StorageUtils,
    private val offlineSyncHelper: OfflineSyncHelper
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

    private val syncSettings = mutableMapOf<Long, CourseSyncSettingsEntity>()

    init {
        loadData()
    }

    private fun loadData() {
        _state.postValue(ViewState.Loading)
        viewModelScope.launch {
            try {
                val storageInfo = getStorageInfo()
                val coursesData = getCoursesData(course?.id)
                val data = OfflineContentViewData(storageInfo, coursesData, 0)
                _data.postValue(data)
                _state.postValue(ViewState.Success)
            } catch (ex: Exception) {
                _state.postValue(ViewState.Error(context.getString(R.string.offline_content_loading_error)))
            }
        }
    }

    private suspend fun getCourseSyncSettings(courseId: Long): CourseSyncSettingsEntity {
        val courseSettings = offlineContentRepository.findCourseSyncSettings(courseId)
        syncSettings[courseSettings.courseId] = courseSettings
        return courseSettings
    }

    private suspend fun getCoursesData(courseId: Long? = null): List<CourseItemViewModel> {
        val courses = if (courseId == null) {
            offlineContentRepository.getCourses()
        } else {
            listOf(offlineContentRepository.getCourse(courseId))
        }

        return courses.map { course ->
            val courseSettings = getCourseSyncSettings(course.id)
            val tabs = course.tabs?.filter { it.tabId in ALLOWED_TAB_IDS }.orEmpty()
            val files = if (tabs.any { it.tabId == Tab.FILES_ID }) offlineContentRepository.getCourseFiles(course.id) else emptyList()
            val size = Formatter.formatShortFileSize(context, files.sumOf { it.size })
            createCourseItemViewModel(course, size, tabs, files, courseSettings)
        }
    }

    private fun createCourseItemViewModel(
        course: Course,
        size: String,
        tabs: List<Tab>,
        files: List<FileFolder>,
        courseSyncSettingsEntity: CourseSyncSettingsEntity
    ) = CourseItemViewModel(
        data = CourseItemViewData(
            checked = courseSyncSettingsEntity.fullContentSync,
            title = course.name,
            size = size,
            tabs = tabs.map { tab ->
                val isFilesTab = tab.tabId == Tab.FILES_ID
                createTabViewModel(
                    course.id,
                    tab,
                    if (isFilesTab) size else "",
                    if (isFilesTab) files else emptyList(),
                    courseSyncSettingsEntity
                )
            }
        ),
        courseId = course.id,
        collapsed = this.course == null,
        onCheckedChanged = this::onCourseCheckedChanged
    )

    private fun onCourseCheckedChanged(checked: Boolean, courseItemViewModel: CourseItemViewModel) {
        val courseViewModel = data.value?.courseItems?.find { it == courseItemViewModel } ?: return

        val newTabs = courseViewModel.data.tabs.map { tab ->
            val newFiles = tab.data.files.map { it.copy(data = it.data.copy(checked = checked)) }
            tab.copy(
                data = tab.data.copy(
                    checked = checked,
                    files = newFiles
                )
            )
        }

        val newCourseViewModel =
            courseViewModel.copy(
                data = courseItemViewModel.data.copy(
                    checked = checked,
                    tabs = newTabs
                )
            )

        val newList =
            _data.value?.courseItems?.map { if (it == courseItemViewModel) newCourseViewModel else it }
                .orEmpty()

        val selectedCount = getSelectedItemCount(newList)

        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)

        viewModelScope.launch {
            updateCourse(courseItemViewModel.courseId, checked)
        }
    }

    private suspend fun updateCourse(courseId: Long, checked: Boolean) {
        val courseSyncSettings = syncSettings[courseId]
        val updated = courseSyncSettings?.copy(
            fullContentSync = checked,
            assignments = checked,
            pages = checked,
            grades = checked
        )

        if (updated != null) {
            updateCourseSettings(updated)
        }
    }

    private fun createTabViewModel(
        courseId: Long,
        tab: Tab,
        size: String,
        files: List<FileFolder>,
        courseSyncSettingsEntity: CourseSyncSettingsEntity
    ) =
        CourseTabViewModel(
            data = CourseTabViewData(
                checked = courseSyncSettingsEntity.isTabSelected(tab.tabId),
                title = tab.label.orEmpty(),
                size = size,
                files = files.map { fileFolder ->
                    createFileViewModel(fileFolder, courseId, tab.tabId)
                }),
            courseId = courseId,
            tabId = tab.tabId,
            collapsed = false,
            onCheckedChanged = this::onTabCheckedChanged
        )

    private fun onTabCheckedChanged(checked: Boolean, tabItemViewModel: CourseTabViewModel) {
        val courseViewModel = data.value?.courseItems?.find { it.courseId == tabItemViewModel.courseId } ?: return
        val tabViewModel = courseViewModel.data.tabs.find { it == tabItemViewModel } ?: return

        val newFiles = tabViewModel.data.files.map { it.copy(data = it.data.copy(checked = checked)) }

        val newTabViewModel = tabViewModel.copy(data = tabViewModel.data.copy(checked = checked, files = newFiles))

        val newTabs = courseViewModel.data.tabs.map { if (it == tabItemViewModel) newTabViewModel else it }

        val newCourseViewModel = courseViewModel.copy(
            data = courseViewModel.data.copy(
                checked = newTabs.all { it.data.checked },
                tabs = newTabs
            )
        )

        val newList = _data.value?.courseItems?.map { if (it == courseViewModel) newCourseViewModel else it }.orEmpty()

        val selectedCount = getSelectedItemCount(newList)

        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)

        viewModelScope.launch {
            syncSettings[tabItemViewModel.courseId]?.copy(
                fullContentSync = newTabs.all { it.data.checked }
            )?.let {
                updateCourseSettings(it)
            }
            updateTab(tabItemViewModel.courseId, tabItemViewModel.tabId, checked)
        }
    }

    private suspend fun updateTab(courseId: Long, tabId: String, checked: Boolean) {
        val courseSyncSettings = syncSettings[courseId]
        val updated = when (tabId) {
            Tab.GRADES_ID -> courseSyncSettings?.copy(grades = checked)
            Tab.PAGES_ID -> courseSyncSettings?.copy(pages = checked)
            Tab.ASSIGNMENTS_ID -> courseSyncSettings?.copy(assignments = checked)
            else -> courseSyncSettings
        }

        if (updated != null) {
            updateCourseSettings(updated)
        }
    }

    private suspend fun updateCourseSettings(updated: CourseSyncSettingsEntity) {
        offlineContentRepository.updateCourseSyncSettings(updated)
        syncSettings[updated.courseId] = offlineContentRepository.findCourseSyncSettings(updated.courseId)
    }

    private fun createFileViewModel(fileFolder: FileFolder, courseId: Long, tabId: String): FileViewModel {
        val fileSize = Formatter.formatShortFileSize(context, fileFolder.size)
        return FileViewModel(FileViewData(false, fileFolder.displayName.orEmpty(), fileSize), courseId, tabId) { checked, item ->
            val courseViewModel = data.value?.courseItems?.find { it.courseId == item.courseId } ?: return@FileViewModel
            val tabViewModel = courseViewModel.data.tabs.find { it.tabId == item.tabId } ?: return@FileViewModel
            val fileViewModel = tabViewModel.data.files.find { it == item } ?: return@FileViewModel
            val newFile = fileViewModel.copy(data = fileViewModel.data.copy(checked = checked))
            val newFiles = tabViewModel.data.files.map { if (it == item) newFile else it }
            val newTabViewModel = tabViewModel.copy(data = tabViewModel.data.copy(checked = newFiles.all { it.data.checked }, files = newFiles))
            val newTabs = courseViewModel.data.tabs.map { if (it.tabId == item.tabId) newTabViewModel else it }
            val newCourseViewModel = courseViewModel.copy(data = courseViewModel.data.copy(checked = newTabs.all { it.data.checked }, tabs = newTabs))
            val newList = _data.value?.courseItems?.map { if (it == courseViewModel) newCourseViewModel else it }.orEmpty()
            val selectedCount = getSelectedItemCount(newList)
            _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
        }
    }

    private fun getSelectedItemCount(courses: List<CourseItemViewModel>): Int {
        val selectedTabs = courses.flatMap { it.data.tabs }.count { it.data.checked && it.tabId != Tab.FILES_ID }
        val selectedFiles = courses.flatMap { it.data.tabs }.flatMap { it.data.files }.count { it.data.checked }
        return selectedTabs + selectedFiles
    }

    fun toggleSelection() {
        val shouldCheck = _data.value?.selectedCount.orDefault() == 0
        val newList = _data.value?.courseItems?.map { courseItemViewModel ->
            courseItemViewModel.copy(data = courseItemViewModel.data.copy(
                checked = shouldCheck,
                tabs = courseItemViewModel.data.tabs.map { tab ->
                    tab.copy(data = tab.data.copy(
                        checked = shouldCheck,
                        files = tab.data.files.map { it.copy(data = it.data.copy(checked = shouldCheck)) }
                    ))
                }
            ))
        }.orEmpty()
        val selectedCount = getSelectedItemCount(newList)
        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    fun onSyncClicked() {
        viewModelScope.launch {
            offlineSyncHelper.syncCourses(syncSettings.keys.toList())
        }
    }

    fun onRefresh() {
        loadData()
    }

    private fun getStorageInfo(): StorageInfo {
        val appSize = storageUtils.getAppSize()
        val totalSpace = storageUtils.getTotalSpace()
        val usedSpace = totalSpace - storageUtils.getFreeSpace()
        val otherAppsSpace = usedSpace - appSize
        val otherPercent = if (totalSpace > 0) (otherAppsSpace.toFloat() / totalSpace * 100).toInt() else 0
        val canvasPercent = if (totalSpace > 0) (appSize.toFloat() / totalSpace * 100).toInt().coerceAtLeast(1) + otherPercent else 0
        val storageInfoText = context.getString(
            R.string.offline_content_storage_info,
            Formatter.formatShortFileSize(context, usedSpace),
            Formatter.formatShortFileSize(context, totalSpace),
        )

        return StorageInfo(otherPercent, canvasPercent, storageInfoText)
    }
}

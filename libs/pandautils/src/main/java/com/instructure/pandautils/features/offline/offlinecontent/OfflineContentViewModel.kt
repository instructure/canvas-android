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

package com.instructure.pandautils.features.offline.offlinecontent

import android.content.Context
import android.content.LocusId
import android.text.format.Formatter
import androidx.lifecycle.*
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.Tab
import com.instructure.pandautils.R
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.CourseItemViewModel
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.CourseTabViewModel
import com.instructure.pandautils.features.offline.offlinecontent.itemviewmodels.FileViewModel
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.room.offline.entities.FileSyncSettingsEntity
import com.instructure.pandautils.room.offline.model.CourseSyncSettingsWithFiles
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

    private val syncSettingsMap = mutableMapOf<Long, CourseSyncSettingsWithFiles>()

    private val courseMap = mutableMapOf<Long, Course>()
    private val courseFilesMap = mutableMapOf<Long, Map<Long, FileFolder>>()
    private val courseSelectedFilesMap = mutableMapOf<Long, MutableSet<Long>>()

    init {
        loadData()
    }

    private fun loadData(isRefresh: Boolean = false) {
        _state.postValue(ViewState.Loading)
        if (isRefresh) {
            _data.value = _data.value?.copy(courseItems = emptyList(), selectedCount = 0)
        }
        viewModelScope.launch {
            try {
                val storageInfo = getStorageInfo()
                courseMap.putAll(getCourses(course?.id).associateBy { it.id })
                courseFilesMap.putAll(getCourseFiles(courseMap.values.toList()))
                syncSettingsMap.putAll(getCourseSyncSettings(courseMap.values.toList()))
                courseSelectedFilesMap.putAll(getSelectedFiles(courseMap.values.toList()))
                val coursesData = createCourseItemViewModels()
                val data = OfflineContentViewData(storageInfo, coursesData, 0)
                _data.postValue(data)
                _state.postValue(ViewState.Success)
            } catch (ex: Exception) {
                _state.postValue(ViewState.Error(context.getString(R.string.offline_content_loading_error)))
            }
        }
    }

    private fun getSelectedFiles(courses: List<Course>): List<Pair<Long, MutableSet<Long>>> {
        return courses.map { course ->
            val courseFilesMap = courseFilesMap[course.id] ?: throw IllegalStateException()
            val syncSettings = syncSettingsMap[course.id] ?: throw IllegalStateException()
            val fileSyncSettings = syncSettings.files

            if (syncSettings.courseSyncSettings.fullFileSync) {
                course.id to courseFilesMap.values.map { it.id }.toMutableSet()
            } else {
                course.id to fileSyncSettings.map {
                    it.id
                }.toMutableSet()
            }
        }
    }

    private suspend fun getCourseSyncSettings(courses: List<Course>): List<Pair<Long, CourseSyncSettingsWithFiles>> {
        return courses.map {
            it.id to offlineContentRepository.findCourseSyncSettings(it.id)
        }
    }

    private suspend fun getCourses(courseId: Long?): List<Course> {
        return if (courseId == null) {
            offlineContentRepository.getCourses()
        } else {
            listOf(offlineContentRepository.getCourse(courseId))
        }
    }

    private suspend fun getCourseFiles(courses: List<Course>): List<Pair<Long, Map<Long, FileFolder>>> {
        return courses.map { course ->
            val files =
                if (course.tabs?.any { it.tabId == Tab.FILES_ID } == true) {
                    offlineContentRepository.getCourseFiles(
                        course.id

                    )
                } else emptyList()

            val filesMap = files.associateBy { it.id }
            course.id to filesMap
        }
    }

    private fun createCourseItemViewModels(): List<CourseItemViewModel> {
        val courses = courseMap.values

        return courses.map { course ->
            createCourseItemViewModel(course.id)
        }
    }

    private fun createCourseItemViewModel(
        courseId: Long
    ): CourseItemViewModel {
        val course = courseMap[courseId] ?: throw IllegalStateException()
        val courseSyncSettingsWithFiles = syncSettingsMap[courseId] ?: throw IllegalStateException()

        val files = courseFilesMap[course.id]?.values ?: emptyList()
        val size = Formatter.formatShortFileSize(context, files.sumOf { it.size })
        val tabs = course.tabs?.filter { it.tabId in ALLOWED_TAB_IDS }.orEmpty()


        val collapsed = _data.value?.courseItems?.find { it.courseId == courseId }?.collapsed ?: (this.course == null)

        return CourseItemViewModel(
            data = CourseItemViewData(
                fullContentSync = courseSyncSettingsWithFiles.courseSyncSettings.fullContentSync,
                title = course.name,
                size = size,
                tabs = tabs.map { tab ->
                    createTabViewModel(
                        course.id,
                        tab
                    )
                }
            ),
            courseId = course.id,
            collapsed = collapsed,
            onCheckedChanged = this::onCourseCheckedChanged
        )
    }

    private fun onCourseCheckedChanged(checked: Boolean, courseItemViewModel: CourseItemViewModel) {
        data.value?.courseItems?.find { it == courseItemViewModel } ?: return

        toggleCourse(courseItemViewModel.courseId, checked)

        val newCourseViewModel = createCourseItemViewModel(courseItemViewModel.courseId)
        val newList =
            _data.value?.courseItems?.map { if (it == courseItemViewModel) newCourseViewModel else it }
                .orEmpty()

        val selectedCount = getSelectedItemCount(newList)

        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    private fun createTabViewModel(
        courseId: Long,
        tab: Tab
    ): CourseTabViewModel {
        val isFilesTab = tab.tabId == Tab.FILES_ID
        val courseSyncSettingsWithFiles = syncSettingsMap[courseId] ?: throw IllegalArgumentException()
        val files = if (isFilesTab) courseFilesMap[courseId]?.values.orEmpty() else emptyList()
        val size = if (isFilesTab) Formatter.formatShortFileSize(context, files.sumOf { it.size }) else null

        val collapsed = _data.value?.courseItems?.find { it.courseId == courseId }?.data?.tabs?.find { it.tabId == Tab.FILES_ID }?.collapsed ?: false

        return CourseTabViewModel(
            data = CourseTabViewData(
                synced = courseSyncSettingsWithFiles.courseSyncSettings.isTabSelected(tab.tabId),
                title = tab.label.orEmpty(),
                size = size ?: "",
                files = files.map { fileFolder ->
                    createFileViewModel(
                        fileFolder,
                        courseId,
                        tab.tabId,
                        courseSyncSettingsWithFiles
                    )
                }),
            courseId = courseId,
            tabId = tab.tabId,
            collapsed = collapsed,
            onCheckedChanged = this::onTabCheckedChanged
        )
    }

    private fun createFileViewModel(
        fileFolder: FileFolder,
        courseId: Long,
        tabId: String,
        syncSettingsWithFiles: CourseSyncSettingsWithFiles
    ): FileViewModel {
        val fileSize = Formatter.formatShortFileSize(context, fileFolder.size)
        val isChecked = syncSettingsWithFiles.courseSyncSettings.fullFileSync || courseSelectedFilesMap[courseId]?.contains(fileFolder.id) ?: false

        return FileViewModel(
            data = FileViewData(
                checked = isChecked,
                title = fileFolder.displayName.orEmpty(),
                size = fileSize
            ),
            courseId = courseId,
            fileId = fileFolder.id,
            fileUrl = fileFolder.url,
            tabId = tabId,
            onCheckedChanged = this::fileCheckedChanged
        )
    }

    private fun onTabCheckedChanged(checked: Boolean, tabItemViewModel: CourseTabViewModel) {
        val courseViewModel = data.value?.courseItems?.find { it.courseId == tabItemViewModel.courseId } ?: return

        updateTab(tabItemViewModel.courseId, tabItemViewModel.tabId, checked)
        val newCourseViewModel = createCourseItemViewModel(courseViewModel.courseId)
        val newList = _data.value?.courseItems?.map { if (it == courseViewModel) newCourseViewModel else it }.orEmpty()

        val selectedCount = getSelectedItemCount(newList)

        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    private fun updateTab(courseId: Long, tabId: String, checked: Boolean) {
        val courseSettingWithFiles = syncSettingsMap[courseId] ?: return
        val courseSyncSettings = courseSettingWithFiles.courseSyncSettings
        var updated = when (tabId) {
            Tab.GRADES_ID -> courseSyncSettings.copy(grades = checked)
            Tab.PAGES_ID -> courseSyncSettings.copy(pages = checked)
            Tab.ASSIGNMENTS_ID -> courseSyncSettings.copy(assignments = checked)
            Tab.FILES_ID -> {
                toggleAllFiles(courseId, checked)
                courseSyncSettings.copy(fullFileSync = checked)
            }
            else -> courseSyncSettings
        }
        updated = updated.copy(fullContentSync = updated.allTabsEnabled)

        val updatedSyncSettings = courseSettingWithFiles.copy(
            courseSyncSettings = updated
        )

        syncSettingsMap[courseId] = updatedSyncSettings
    }

    private fun fileCheckedChanged(checked: Boolean, item: FileViewModel) {
        val courseViewModel = data.value?.courseItems?.find { it.courseId == item.courseId } ?: return

        updateFile(checked, courseViewModel.courseId, item.fileId)

        val newCourseViewModel = createCourseItemViewModel(item.courseId)
        val newList = _data.value?.courseItems?.map { if (it == courseViewModel) newCourseViewModel else it }.orEmpty()
        val selectedCount = getSelectedItemCount(newList)
        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    private fun updateFile(checked: Boolean, courseId: Long, fileId: Long) {
        if (checked) {
            courseSelectedFilesMap[courseId]?.add(fileId)
        } else {
            courseSelectedFilesMap[courseId]?.remove(fileId)
        }

        val syncSettings = syncSettingsMap[courseId] ?: return

        var updated = syncSettings.courseSyncSettings.copy(
            fullFileSync = courseSelectedFilesMap[courseId]?.size == courseFilesMap[courseId]?.size
        )
        updated = updated.copy(fullContentSync = updated.allTabsEnabled)

        syncSettingsMap[courseId] = syncSettings.copy(courseSyncSettings = updated)
    }
    private fun getSelectedItemCount(courses: List<CourseItemViewModel>): Int {
        val selectedTabs = courses.flatMap { it.data.tabs }.count { it.data.synced && it.tabId != Tab.FILES_ID }
        val selectedFiles = courses.flatMap { it.data.tabs }.flatMap { it.data.files }.count { it.data.checked }
        return selectedTabs + selectedFiles
    }

    fun toggleSelection() {
        val shouldCheck = _data.value?.selectedCount.orDefault() == 0
        _data.value?.courseItems?.forEach {
            toggleCourse(it.courseId, shouldCheck)
        }

        val newList = createCourseItemViewModels()
        val selectedCount = getSelectedItemCount(newList)
        _data.value = _data.value?.copy(courseItems = newList, selectedCount = selectedCount)
    }

    private fun toggleCourse(courseId: Long, shouldCheck: Boolean) {
        val syncSettingsWithFiles = syncSettingsMap[courseId] ?: return
        val courseSyncSettings = syncSettingsWithFiles.courseSyncSettings.copy(
            fullContentSync = shouldCheck,
            assignments = shouldCheck,
            pages = shouldCheck,
            grades = shouldCheck,
            syllabus = shouldCheck,
            fullFileSync = shouldCheck
        )

        val updatedSyncSettings = syncSettingsWithFiles.copy(
            courseSyncSettings = courseSyncSettings
        )

        toggleAllFiles(courseId, shouldCheck)

        syncSettingsMap[courseId] = updatedSyncSettings
    }

    private fun toggleAllFiles(courseId: Long, shouldCheck: Boolean) {
        if (shouldCheck) {
            courseSelectedFilesMap[courseId]?.addAll(courseFilesMap[courseId]?.values.orEmpty().map { it.id })
        } else {
            courseSelectedFilesMap[courseId]?.clear()
        }
    }

    fun onSyncClicked() {
        viewModelScope.launch {
            saveSettings()
            offlineSyncHelper.syncCourses(syncSettingsMap.keys.toList())
            _events.postValue(Event(OfflineContentAction.Back))
        }
    }

    private suspend fun saveSettings() {
        syncSettingsMap.forEach { courseSettingsEntry ->
            val courseId = courseSettingsEntry.key
            val fileSettings = courseSelectedFilesMap[courseId].orEmpty().mapNotNull { fileId ->
                courseFilesMap[courseId]?.get(fileId)?.let {
                    FileSyncSettingsEntity(it.id, courseId, it.url)
                }
            }
            offlineContentRepository.updateCourseSyncSettings(courseSettingsEntry.key, courseSettingsEntry.value.courseSyncSettings, fileSettings)
        }
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
        val canvasPercent =
            if (totalSpace > 0) (appSize.toFloat() / totalSpace * 100).toInt().coerceAtLeast(1) + otherPercent else 0
        val storageInfoText = context.getString(
            R.string.offline_content_storage_info,
            Formatter.formatShortFileSize(context, usedSpace),
            Formatter.formatShortFileSize(context, totalSpace),
        )

        return StorageInfo(otherPercent, canvasPercent, storageInfoText)
    }
}


/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.horizon.features.account.manageofflinecontent

import android.content.Context
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.domain.usecase.GetCoursesWithFilesUseCase
import com.instructure.horizon.domain.usecase.GetDeviceStorageUseCase
import com.instructure.horizon.domain.usecase.GetSelectedSyncFileIdsUseCase
import com.instructure.horizon.domain.usecase.SaveOfflineSyncPlanUseCase
import com.instructure.horizon.features.account.manageofflinecontent.syncinprogress.SyncInProgressUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.offline.FileSyncPaths
import com.instructure.horizon.offline.sync.HorizonAggregateProgressObserver
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageOfflineContentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCoursesWithFilesUseCase: GetCoursesWithFilesUseCase,
    private val getDeviceStorageUseCase: GetDeviceStorageUseCase,
    private val getSelectedSyncFileIdsUseCase: GetSelectedSyncFileIdsUseCase,
    private val saveOfflineSyncPlanUseCase: SaveOfflineSyncPlanUseCase,
    private val syncHelper: HorizonOfflineSyncHelper,
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val aggregateProgressObserver: HorizonAggregateProgressObserver,
    private val apiPrefs: ApiPrefs,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ManageOfflineContentUiState(
            onSelectAllClick = ::onSelectAllClick,
            onDismissInsufficientStorageWarning = ::dismissInsufficientStorageWarning,
        )
    )
    val uiState = _uiState.asStateFlow()

    val syncingUiState = _uiState
        .map { state ->
            SyncInProgressUiState(
                courses = state.courses
                    .filter { it.offlineState != CourseOfflineState.NONE }
                    .map { course -> course.copy(files = course.files.filter { it.isSelected }) },
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SyncInProgressUiState())

    private var hasLocalSyncedFiles = false

    init {
        loadData()
        observeSyncPlan()
        observeSyncProgress()
    }

    private fun observeSyncPlan() {
        courseSyncPlanDao.findAllFlow()
            .onEach { plans ->
                _uiState.update { state ->
                    state.copy(hasSyncedContent = plans.isNotEmpty() || hasLocalSyncedFiles)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSyncProgress() {
        aggregateProgressObserver.progressData
            .onEach { data ->
                _uiState.update { it.copy(isSyncActive = data.isActive) }
            }
            .launchIn(viewModelScope)
    }

    fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = LoadingState(isLoading = true, onRefresh = ::loadData)) }
            val coursesWithFiles = getCoursesWithFilesUseCase()
            val previouslySelectedCourseIds = courseSyncPlanDao.findAll().map { it.courseId }.toSet()
            val previouslySelectedFileIds = getSelectedSyncFileIdsUseCase()

            val courses = coursesWithFiles.map { courseData ->
                val courseWasPreviouslySynced = courseData.courseId in previouslySelectedCourseIds
                val files = courseData.files.map { file ->
                    val fileSelected = !file.isLocked && (file.isSynced || file.fileId in previouslySelectedFileIds)
                    OfflineFileItemUiState(
                        fileId = file.fileId,
                        fileName = file.displayName,
                        fileSize = file.size,
                        isSelected = fileSelected,
                        isLocked = file.isLocked,
                        onSelectionChanged = { selected -> updateFileSelection(courseData.courseId, file.fileId, selected) },
                    )
                }
                val selectableFiles = files.filterNot { it.isLocked }
                val anyFileSelected = selectableFiles.any { it.isSelected }
                val allFilesSelected = selectableFiles.isNotEmpty() && selectableFiles.all { it.isSelected }
                OfflineCourseItemUiState(
                    courseId = courseData.courseId,
                    courseName = courseData.courseName,
                    offlineState = when {
                        courseWasPreviouslySynced && !anyFileSelected -> CourseOfflineState.ALL
                        allFilesSelected -> CourseOfflineState.ALL
                        anyFileSelected || courseWasPreviouslySynced -> CourseOfflineState.PARTIAL
                        else -> CourseOfflineState.NONE
                    },
                    files = files,
                    onToggleExpanded = { toggleCourseExpanded(courseData.courseId) },
                    onOfflineStateChanged = { state -> updateCourseOfflineState(courseData.courseId, state) },
                )
            }

            val canvasBytes = computeCanvasFootprint()
            val storageData = getDeviceStorageUseCase()
            val usedBytes = (storageData.totalBytes - storageData.availableBytes).coerceAtLeast(0L)
            val canvasBytesClamped = canvasBytes.coerceAtMost(usedBytes)
            val otherAppBytes = (usedBytes - canvasBytesClamped).coerceAtLeast(0L)

            hasLocalSyncedFiles = coursesWithFiles.any { course -> course.files.any { it.isSynced } }
            val hasSyncedContent = previouslySelectedCourseIds.isNotEmpty() || hasLocalSyncedFiles

            _uiState.update { state ->
                state.copy(
                    loadingState = LoadingState(isLoading = false, onRefresh = ::loadData),
                    courses = courses,
                    hasSyncedContent = hasSyncedContent,
                    storageCanvasBytes = canvasBytesClamped,
                    storageOtherAppBytes = otherAppBytes,
                    storageTotalBytes = storageData.totalBytes,
                    storageUsedLabel = Formatter.formatShortFileSize(context, usedBytes),
                    storageTotalLabel = Formatter.formatShortFileSize(context, storageData.totalBytes),
                )
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = LoadingState(isLoading = false, isError = true, onRefresh = ::loadData))
            }
        }
    }

    suspend fun onSyncConfirmed(): Boolean {
        val selectedCourses = _uiState.value.courses
            .filter { it.offlineState != CourseOfflineState.NONE }

        if (selectedCourses.isEmpty()) return false

        val plannedBytes = selectedCourses
            .flatMap { it.files }
            .filter { it.isSelected }
            .sumOf { it.fileSize }
        val storage = getDeviceStorageUseCase()
        if (plannedBytes > 0 && plannedBytes > storage.availableBytes) {
            _uiState.update { state ->
                state.copy(
                    insufficientStorageWarning = InsufficientStorageWarning(
                        requiredLabel = Formatter.formatShortFileSize(context, plannedBytes),
                        availableLabel = Formatter.formatShortFileSize(context, storage.availableBytes),
                    )
                )
            }
            return false
        }

        saveOfflineSyncPlanUseCase(
            SaveOfflineSyncPlanUseCase.Params(
                courses = selectedCourses.map { course ->
                    SaveOfflineSyncPlanUseCase.Params.Course(
                        courseId = course.courseId,
                        courseName = course.courseName,
                        syncFiles = course.files.any { it.isSelected },
                        files = course.files.filter { it.isSelected }.map { file ->
                            SaveOfflineSyncPlanUseCase.Params.File(
                                fileId = file.fileId,
                                fileName = file.fileName,
                                size = file.fileSize,
                            )
                        },
                    )
                },
            )
        )

        syncHelper.syncCoursesNow(selectedCourses.map { it.courseId })
        syncHelper.applyPeriodicSchedule()
        return true
    }

    private fun dismissInsufficientStorageWarning() {
        _uiState.update { it.copy(insufficientStorageWarning = null) }
    }

    private fun onSelectAllClick() {
        _uiState.update { state ->
            val anySelected = state.courses.any { it.offlineState != CourseOfflineState.NONE }
            val targetState = if (anySelected) CourseOfflineState.NONE else CourseOfflineState.ALL
            val targetSelected = !anySelected
            state.copy(
                courses = state.courses.map { course ->
                    course.copy(
                        offlineState = targetState,
                        files = course.files.map { file ->
                            if (file.isLocked) file else file.copy(isSelected = targetSelected)
                        },
                    )
                }
            )
        }
    }

    private fun toggleCourseExpanded(courseId: Long) {
        _uiState.update { state ->
            state.copy(
                courses = state.courses.map { course ->
                    if (course.courseId == courseId) course.copy(isExpanded = !course.isExpanded) else course
                }
            )
        }
    }

    private fun updateCourseOfflineState(courseId: Long, offlineState: CourseOfflineState) {
        _uiState.update { state ->
            state.copy(
                courses = state.courses.map { course ->
                    if (course.courseId == courseId) {
                        val selected = offlineState != CourseOfflineState.NONE
                        course.copy(
                            offlineState = offlineState,
                            files = course.files.map { file ->
                                if (file.isLocked) file else file.copy(isSelected = selected)
                            },
                        )
                    } else course
                }
            )
        }
    }

    private fun updateFileSelection(courseId: Long, fileId: Long, selected: Boolean) {
        _uiState.update { state ->
            state.copy(
                courses = state.courses.map { course ->
                    if (course.courseId == courseId) {
                        val targetFile = course.files.firstOrNull { it.fileId == fileId }
                        if (targetFile?.isLocked == true) return@map course
                        val updatedFiles = course.files.map { file ->
                            if (file.fileId == fileId) file.copy(isSelected = selected) else file
                        }
                        val selectableFiles = updatedFiles.filterNot { it.isLocked }
                        val allSelected = selectableFiles.isNotEmpty() && selectableFiles.all { it.isSelected }
                        val noneSelected = selectableFiles.none { it.isSelected }
                        course.copy(
                            files = updatedFiles,
                            offlineState = when {
                                allSelected -> CourseOfflineState.ALL
                                noneSelected -> CourseOfflineState.NONE
                                else -> CourseOfflineState.PARTIAL
                            },
                        )
                    } else course
                }
            )
        }
    }

    private suspend fun computeCanvasFootprint(): Long = withContext(Dispatchers.IO) {
        val userId = apiPrefs.user?.id ?: return@withContext 0L
        val dir = FileSyncPaths.userDir(context.filesDir, userId)
        if (!dir.exists()) return@withContext 0L
        dir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }
}

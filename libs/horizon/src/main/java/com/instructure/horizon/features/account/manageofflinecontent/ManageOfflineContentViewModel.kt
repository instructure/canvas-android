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
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.database.dao.HorizonCourseSyncPlanDao
import com.instructure.horizon.database.dao.HorizonFileSyncPlanDao
import com.instructure.horizon.database.entity.HorizonCourseSyncPlanEntity
import com.instructure.horizon.database.entity.HorizonFileSyncPlanEntity
import com.instructure.horizon.domain.usecase.GetCoursesWithFilesUseCase
import com.instructure.horizon.domain.usecase.GetDeviceStorageUseCase
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.offline.sync.HorizonOfflineSyncHelper
import com.instructure.horizon.offline.sync.HorizonProgressState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageOfflineContentViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getCoursesWithFilesUseCase: GetCoursesWithFilesUseCase,
    private val getDeviceStorageUseCase: GetDeviceStorageUseCase,
    private val courseSyncPlanDao: HorizonCourseSyncPlanDao,
    private val fileSyncPlanDao: HorizonFileSyncPlanDao,
    private val syncHelper: HorizonOfflineSyncHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ManageOfflineContentUiState(
            onSelectAllClick = ::onSelectAllClick,
            onSyncClick = ::onSyncClick,
        )
    )
    val uiState = _uiState.asStateFlow()

    val syncingUiState = _uiState
        .map { state ->
            SyncingContentUiState(
                courses = state.courses
                    .filter { it.offlineState != CourseOfflineState.NONE }
                    .map { course -> course.copy(files = course.files.filter { it.isSelected }) },
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SyncingContentUiState())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = LoadingState(isLoading = true)) }
            val coursesWithFiles = getCoursesWithFilesUseCase()

            val courses = coursesWithFiles.map { courseData ->
                val totalSizeBytes = courseData.files.sumOf { it.size }
                OfflineCourseItemUiState(
                    courseId = courseData.courseId,
                    courseName = courseData.courseName,
                    courseSizeLabel = if (courseData.files.isNotEmpty()) Formatter.formatShortFileSize(context, totalSizeBytes) else "",
                    offlineState = when {
                        courseData.files.isEmpty() -> CourseOfflineState.NONE
                        courseData.files.all { it.isSynced } -> CourseOfflineState.ALL
                        courseData.files.any { it.isSynced } -> CourseOfflineState.PARTIAL
                        else -> CourseOfflineState.NONE
                    },
                    files = courseData.files.map { file ->
                        OfflineFileItemUiState(
                            fileId = file.fileId,
                            fileName = file.displayName,
                            fileSizeLabel = Formatter.formatShortFileSize(context, file.size),
                            isSelected = file.isSynced,
                            onSelectionChanged = { selected -> updateFileSelection(courseData.courseId, file.fileId, selected) },
                        )
                    },
                    onToggleExpanded = { toggleCourseExpanded(courseData.courseId) },
                    onOfflineStateChanged = { state -> updateCourseOfflineState(courseData.courseId, state) },
                )
            }

            val canvasBytes = coursesWithFiles.sumOf { course ->
                course.files.filter { it.isSynced }.sumOf { it.size }
            }
            val storageData = getDeviceStorageUseCase()
            val otherAppBytes = ((storageData.totalBytes - storageData.availableBytes) - canvasBytes).coerceAtLeast(0L)

            _uiState.update { state ->
                state.copy(
                    loadingState = LoadingState(isLoading = false),
                    courses = courses,
                    storageCanvasBytes = canvasBytes,
                    storageOtherAppBytes = otherAppBytes,
                    storageTotalBytes = storageData.totalBytes,
                    storageUsedLabel = Formatter.formatShortFileSize(context, canvasBytes + otherAppBytes),
                    storageTotalLabel = Formatter.formatShortFileSize(context, storageData.totalBytes),
                )
            }
        } catch {
            _uiState.update { it.copy(loadingState = LoadingState(isLoading = false, isError = true)) }
        }
    }

    private fun onSyncClick() {
        viewModelScope.launch {
            val selectedCourses = _uiState.value.courses
                .filter { it.offlineState != CourseOfflineState.NONE }

            if (selectedCourses.isEmpty()) return@launch

            courseSyncPlanDao.deleteAll()
            fileSyncPlanDao.deleteAll()

            for (course in selectedCourses) {
                courseSyncPlanDao.upsert(
                    HorizonCourseSyncPlanEntity(
                        courseId = course.courseId,
                        courseName = course.courseName,
                        syncFiles = course.files.any { it.isSelected },
                        state = HorizonProgressState.PENDING,
                    )
                )
                for (file in course.files.filter { it.isSelected }) {
                    fileSyncPlanDao.upsert(
                        HorizonFileSyncPlanEntity(
                            fileId = file.fileId,
                            courseId = course.courseId,
                            fileName = file.fileName,
                            state = HorizonProgressState.PENDING,
                        )
                    )
                }
            }

            syncHelper.syncCourses(selectedCourses.map { it.courseId })
        }
    }

    private fun onSelectAllClick() {
        _uiState.update { state ->
            val allSelected = state.courses.isNotEmpty() && state.courses.all { it.offlineState == CourseOfflineState.ALL }
            val targetState = if (allSelected) CourseOfflineState.NONE else CourseOfflineState.ALL
            val targetSelected = !allSelected
            state.copy(
                courses = state.courses.map { course ->
                    course.copy(
                        offlineState = targetState,
                        files = course.files.map { it.copy(isSelected = targetSelected) },
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
                            files = course.files.map { it.copy(isSelected = selected) },
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
                        val updatedFiles = course.files.map { file ->
                            if (file.fileId == fileId) file.copy(isSelected = selected) else file
                        }
                        val allSelected = updatedFiles.all { it.isSelected }
                        val noneSelected = updatedFiles.none { it.isSelected }
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
}

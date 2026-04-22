/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.data.repository.course.CourseRepository
import com.instructure.pandautils.features.dashboard.DashboardNavigationEvent
import com.instructure.pandautils.room.appdatabase.daos.DashboardFileUploadDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val observeUploadsUseCase: ObserveUploadsUseCase,
    private val observeSyncProgressUseCase: ObserveSyncProgressUseCase,
    private val dismissUploadUseCase: DismissUploadUseCase,
    private val dismissSyncProgressUseCase: DismissSyncProgressUseCase,
    private val courseRepository: CourseRepository,
    private val dashboardFileUploadDao: DashboardFileUploadDao,
    private val apiPrefs: ApiPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProgressUiState(
            onRefresh = ::refresh,
            onUploadClick = ::onUploadClick,
            onUploadDismiss = ::onUploadDismiss,
            onSyncClick = ::onSyncClick,
            onSyncDismiss = ::onSyncDismiss,
            onClearSnackbar = ::clearSnackbar
        )
    )
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<DashboardNavigationEvent.Progress>()
    val navigationEvents: SharedFlow<DashboardNavigationEvent.Progress> = _navigationEvents.asSharedFlow()

    init {
        observeProgress()
    }

    private fun observeProgress() {
        viewModelScope.launch {
            combine(
                observeUploadsUseCase(),
                observeSyncProgressUseCase()
            ) { uploads, syncProgress ->
                Pair(uploads, syncProgress)
            }.collect { (uploads, syncProgress) ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        uploadItems = uploads,
                        syncProgress = syncProgress
                    )
                }
            }
        }
    }

    private fun refresh() {
        // Progress is observed reactively, no explicit refresh needed
    }

    private fun onUploadClick(item: UploadProgressItem) {
        viewModelScope.launch {
            when (item.state) {
                UploadState.SUCCEEDED -> handleSucceededUpload(item)
                else -> _navigationEvents.emit(DashboardNavigationEvent.Progress.OpenProgressDialog(item.workerId))
            }
        }
    }

    private suspend fun handleSucceededUpload(item: UploadProgressItem) {
        if (item.courseId != null && item.assignmentId != null && item.attemptId != null) {
            val courseResult = courseRepository.getCourse(item.courseId, false)
            val course = courseResult.dataOrNull
            if (course != null) {
                dashboardFileUploadDao.deleteByWorkerId(item.workerId.toString())
                _navigationEvents.emit(
                    DashboardNavigationEvent.Progress.NavigateToSubmissionDetails(
                        course,
                        item.assignmentId,
                        item.attemptId
                    )
                )
            } else {
                _navigationEvents.emit(DashboardNavigationEvent.Progress.OpenProgressDialog(item.workerId))
            }
        } else if (item.folderId != null) {
            dashboardFileUploadDao.deleteByWorkerId(item.workerId.toString())
            apiPrefs.user?.let { user ->
                _navigationEvents.emit(DashboardNavigationEvent.Progress.NavigateToMyFiles(user, item.folderId))
            }
        } else {
            dashboardFileUploadDao.deleteByWorkerId(item.workerId.toString())
            _navigationEvents.emit(DashboardNavigationEvent.Progress.OpenProgressDialog(item.workerId))
        }
    }

    private fun onUploadDismiss(item: UploadProgressItem) {
        viewModelScope.launch {
            dismissUploadUseCase(item.workerId)
        }
    }

    private fun onSyncClick() {
        viewModelScope.launch {
            _navigationEvents.emit(DashboardNavigationEvent.Progress.OpenSyncProgress)
        }
    }

    private fun onSyncDismiss() {
        viewModelScope.launch {
            dismissSyncProgressUseCase()
        }
    }

    private fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
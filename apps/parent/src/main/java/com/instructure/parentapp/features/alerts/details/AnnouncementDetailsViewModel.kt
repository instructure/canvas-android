/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.features.alerts.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandares.R
import com.instructure.pandautils.utils.FileDownloader
import com.instructure.pandautils.utils.studentColor
import com.instructure.parentapp.util.ParentPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnouncementDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val repository: AnnouncementDetailsRepository,
    private val parentPrefs: ParentPrefs,
    private val fileDownloader: FileDownloader
) : ViewModel() {

    private val courseId = savedStateHandle.get<Long>(AnnouncementDetailsFragment.COURSE_ID) ?: -1
    private val announcementId =
        savedStateHandle.get<Long>(AnnouncementDetailsFragment.ANNOUNCEMENT_ID) ?: -1

    private val _uiState = MutableStateFlow(AnnouncementDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(isLoading = true)
        }
        loadData()
    }

    fun handleAction(action: AnnouncementDetailsAction) {
        when (action) {
            is AnnouncementDetailsAction.Refresh -> {
                _uiState.update {
                    it.copy(isRefreshing = true)
                }
                loadData(true)
            }
            is AnnouncementDetailsAction.OpenAttachment -> {
                viewModelScope.launch {
                    fileDownloader.downloadFileToDevice(action.attachment)
                }
            }
            AnnouncementDetailsAction.SnackbarDismissed -> {
                _uiState.update {
                    it.copy(showErrorSnack = false)
                }
            }
        }
    }

    private fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    studentColor = parentPrefs.currentStudent.studentColor
                )
            }
            if (courseId == -1L) {
                fetchGlobalAnnouncement(forceNetwork)
            } else {
                fetchCourseAnnouncement(forceNetwork)
            }
        } catch {
            if (uiState.value.pageTitle == null && uiState.value.announcementTitle == null && uiState.value.message == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isError = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        showErrorSnack = true
                    )
                }
            }
        }
    }

    private suspend fun fetchGlobalAnnouncement(forceNetwork: Boolean = false) {
        val globalAnnouncement = repository.getGlobalAnnouncement(announcementId, forceNetwork)
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                isError = false,
                pageTitle = context.getString(R.string.globalAnnouncementPageTitle),
                announcementTitle = globalAnnouncement.subject,
                message = globalAnnouncement.message,
                postedDate = globalAnnouncement.startDate,
            )
        }
    }

    private suspend fun fetchCourseAnnouncement(forceNetwork: Boolean = false) {
        coroutineScope {
            val course = async { repository.getCourse(courseId, forceNetwork) }
            val announcement = async {
                repository.getCourseAnnouncement(
                    courseId,
                    announcementId,
                    forceNetwork
                )
            }

            _uiState.update {
                with(announcement.await()) {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isError = false,
                        pageTitle = course.await()?.name,
                        announcementTitle = title,
                        message = message,
                        postedDate = postedDate,
                        attachment = attachments.getOrNull(0)?.toAttachment()
                    )
                }
            }
        }
    }
}

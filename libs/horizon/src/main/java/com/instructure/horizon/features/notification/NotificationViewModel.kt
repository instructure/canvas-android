/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.notification

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.format
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NotificationRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(
        NotificationUiState(
            LoadingState(
                onRefresh = ::refresh,
                onErrorSnackbarDismiss = ::dismissSnackbar
            ),
            decreasePageIndex = ::decreasePageIndex,
            increasePageIndex = ::increasePageIndex,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        initData()
    }

    private fun initData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(screenState = LoadingState(isLoading = true))
            }
            loadData()
            _uiState.update {
                it.copy(screenState = LoadingState(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(screenState = LoadingState(isLoading = false, isError = true, errorMessage = context.getString(
                    R.string.notificationsFailedToLoad
                )))
            }
        }
    }

    private suspend fun loadData(forceRefresh: Boolean = false) {
        val notifications = repository.getNotifications(forceRefresh)
        val items = notifications.map {
            NotificationItem(
                categoryLabel = getNotificationItemCategoryLabel(it),
                title = getNotificationItemTitle(it),
                date = it.updatedDate?.format("MMM dd").orEmpty()
            )
        }
        _uiState.update {
            it.copy(
                allNotificationItems = items,
                pagedNotificationItems = items.chunked(10),
            )
        }
    }

    private fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(screenState = LoadingState(isRefreshing = true))
            }
            loadData(true)
            _uiState.update {
                it.copy(screenState = LoadingState(isRefreshing = false))
            }
        } catch {
            _uiState.update {
                it.copy(screenState = LoadingState(isRefreshing = false, errorSnackbar = context.getString(
                    R.string.notificationsFailedToRefresh
                )))
            }
        }
    }

    private suspend  fun getNotificationItemCategoryLabel(streamItem: StreamItem): String {
        val courseName = repository.getCourse(streamItem.courseId).name
        if (isNotificationItemScored(streamItem)) {
            return context.getString(R.string.notificationsAssignmentScoredCategoryLabel)
        }
        if (isDueDateChanged(streamItem)) {
            return context.getString(R.string.notificationsDueDateChangedCategoryLabel)
        }
        if (isGradingWeightChanged(streamItem)) {
            return context.getString(R.string.notificationsScoringWeightChangedCategoryLabel)
        }
        if (streamItem.contextType == CanvasContext.Type.COURSE) {
            return context.getString(
                R.string.notificationsAnnouncementFromCetegoryLabel,
                courseName
            )
        }

        return streamItem.notificationCategory
    }

    private suspend fun getNotificationItemTitle(streamItem: StreamItem): String {
        val courseName = repository.getCourse(streamItem.courseId).name
        if (isNotificationItemScored(streamItem)) {
            return context.getString(R.string.notificationsScoredItemTitle, streamItem.title)
        }
        if (isDueDateChanged(streamItem)) {
            return formatDueDateTitle(streamItem, courseName)
        }
        if (isAssignmentCreated(streamItem)) {
            return streamItem.title?.replace(", $courseName", "").orEmpty()
        }
        if (isGradingWeightChanged(streamItem)) {
            return formatGradingWeightChangeTitle(streamItem)
        }
        return streamItem.title.orEmpty()
    }

    private fun increasePageIndex() {
        _uiState.update {
            it.copy(currentPageIndex = it.currentPageIndex + 1)
        }
    }

    private fun decreasePageIndex() {
        _uiState.update {
            it.copy(currentPageIndex = it.currentPageIndex - 1)
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = LoadingState(errorSnackbar = null))
        }
    }

    // TODO: There is no API support for handling categories and titles.
    // TODO: For now the Web and iOS logic is copied, but it won't work if language support is introduced.
    // TODO: This should be handled in the API.
    private fun isNotificationItemScored(streamItem: StreamItem): Boolean {
        return streamItem.grade != null || streamItem.score != -1.0
    }

    private fun isDueDateChanged(streamItem: StreamItem): Boolean {
        return streamItem.notificationCategory == "Due Date"
                && streamItem.title?.contains("Assignment Due Date Changed").orDefault()
    }

    private fun isAssignmentCreated(streamItem: StreamItem): Boolean {
        return streamItem.notificationCategory == "Due Date"
                && streamItem.title?.contains("Assignment Created").orDefault()
    }

    private fun isGradingWeightChanged(streamItem: StreamItem): Boolean {
        return streamItem.notificationCategory == "Grading Policies"
                || streamItem.title?.contains("Grading Weight Changed").orDefault()
    }

    private fun formatDueDateTitle(streamItem: StreamItem, courseName: String): String {
        val assignmentName = streamItem.title
            ?.replace("Assignment Due Date Changed: ", "")
            ?.replace(", $courseName", "")

        val dateComponent = streamItem.getMessage(context)?.split("\n\n")?.firstOrNull()
        val date = dateComponent.orEmpty()

        return context.getString(R.string.notificationsDueOnTitle, assignmentName, date)
    }

    private fun formatGradingWeightChangeTitle(streamItem: StreamItem): String {
        val courseNameFromTitle = streamItem.title?.replace("Grade Weight Changed: ", "")
        return context.getString(R.string.notificationsScoreWeightChangedTitle, courseNameFromTitle)
    }
}
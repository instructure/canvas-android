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
import com.instructure.canvasapi2.models.StreamItem
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NotificationRepository,
    private val dashboardEventHandler: DashboardEventHandler
): ViewModel() {
    private val _uiState = MutableStateFlow(
        NotificationUiState(
            LoadingState(
                onRefresh = ::refresh,
                onSnackbarDismiss = ::dismissSnackbar
            ),
            onItemRead = ::markAsRead,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        initData()
    }

    private fun initData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = true))
            }
            loadData()
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                    R.string.notificationsFailedToLoad
                )))
            }
        }
    }

    private suspend fun loadData(forceRefresh: Boolean = false) {
        val userNotifications = repository.getNotifications(forceRefresh).map {
            NotificationItem(
                id = it.id,
                category = getNotificationItemCategoryLabel(it),
                title = getNotificationItemTitle(it),
                courseLabel = if (it.isCourseNotification()) getCourseName(it.courseId) else null,
                date = it.updatedDate,
                isRead = it.isReadState,
                deepLink = if (it.assignment?.htmlUrl != null) {
                    it.assignment?.htmlUrl!!
                } else {
                    it.htmlUrl
                }
            )
        }

        _uiState.update {
            it.copy(
                notificationItems = (userNotifications).sortedByDescending { item -> item.date }
            )
        }
    }

    private fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isRefreshing = true))
            }
            loadData(true)
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isRefreshing = false))
            }
        } catch {
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isRefreshing = false, snackbarMessage = context.getString(
                    R.string.notificationsFailedToRefresh
                )))
            }
        }
    }

    private fun getNotificationItemCategoryLabel(streamItem: StreamItem): NotificationItemCategory {
        if (streamItem.isNotificationItemScored()) {
            return NotificationItemCategory(
                context.getString(R.string.notificationsScoreChangedCategoryLabel),
                StatusChipColor.Violet
            )
        }
        if (streamItem.isGradingPeriodNotification()) {
            return NotificationItemCategory(
                context.getString(R.string.notificationsScoreCategoryLabel),
                StatusChipColor.Violet
            )
        }
        if (streamItem.isDueDateNotification()) {
            return NotificationItemCategory(
                context.getString(R.string.notificationsDueDateCategoryLabel),
                StatusChipColor.Honey
            )
        }
        if (streamItem.isCourseNotification()) {
            return NotificationItemCategory(
                context.getString(R.string.notificationsAnnouncementCategoryLabel),
                StatusChipColor.Sky
            )
        }

        return NotificationItemCategory(
            streamItem.notificationCategory,
            StatusChipColor.Honey
        )
    }

    private fun getNotificationItemTitle(streamItem: StreamItem): String {
        if (streamItem.isNotificationItemScored()) {
            return context.getString(R.string.notificationsScoredItemTitle, streamItem.title)
        }
        return streamItem.title.orEmpty()
    }

    private suspend fun getCourseName(courseId: Long): String {
        return repository.getCourse(courseId).name
    }

    private fun markAsRead(id: Long) {
        val item = _uiState.value.notificationItems.firstOrNull { it.id == id } ?: return
        if (item.isRead) return
        _uiState.update { state ->
            state.copy(
                notificationItems = state.notificationItems.map {
                    if (it.id == id) it.copy(isRead = true) else it
                }
            )
        }
        viewModelScope.launch {
            dashboardEventHandler.postEvent(DashboardEvent.NotificationsRefresh)
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }
}
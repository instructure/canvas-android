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
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.domain.usecase.GetNotificationStreamUseCase
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.model.NotificationStreamItem
import com.instructure.horizon.offline.HorizonOfflineState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getNotificationStreamUseCase: GetNotificationStreamUseCase,
    private val offlineState: HorizonOfflineState,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        NotificationUiState(
            LoadingState(
                onRefresh = ::refresh,
                onSnackbarDismiss = ::dismissSnackbar
            ),
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        initData()
        offlineState.isOffline
            .drop(1)
            .onEach { initData() }
            .launchIn(viewModelScope)
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
        val items = getNotificationStreamUseCase(GetNotificationStreamUseCase.Params(forceRefresh))
            .map { it.toUiItem() }
            .sortedByDescending { it.date }

        _uiState.update {
            it.copy(notificationItems = items)
        }
    }

    private fun refresh() {
        if (offlineState.isOffline.value) {
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
            return
        }
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

    private fun NotificationStreamItem.toUiItem(): NotificationItem {
        return NotificationItem(
            category = categoryLabel(),
            title = displayTitle(),
            courseLabel = if (isCourseNotification) courseName else null,
            date = updatedAt,
            isRead = isRead,
            deepLink = deepLink,
        )
    }

    private fun NotificationStreamItem.categoryLabel(): NotificationItemCategory {
        return when {
            isScored -> NotificationItemCategory(
                context.getString(R.string.notificationsScoreChangedCategoryLabel),
                StatusChipColor.Violet,
            )
            isGradingPolicy -> NotificationItemCategory(
                context.getString(R.string.notificationsScoreCategoryLabel),
                StatusChipColor.Violet,
            )
            isDueDate -> NotificationItemCategory(
                context.getString(R.string.notificationsDueDateCategoryLabel),
                StatusChipColor.Honey,
            )
            isCourseNotification -> NotificationItemCategory(
                context.getString(R.string.notificationsAnnouncementCategoryLabel),
                StatusChipColor.Sky,
            )
            else -> NotificationItemCategory(notificationCategory, StatusChipColor.Honey)
        }
    }

    private fun NotificationStreamItem.displayTitle(): String {
        return if (isScored) {
            context.getString(R.string.notificationsScoredItemTitle, title)
        } else {
            title
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }
}

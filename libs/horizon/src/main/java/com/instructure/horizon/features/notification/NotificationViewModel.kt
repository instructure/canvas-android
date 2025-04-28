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
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.format
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
    private val _uiState = MutableStateFlow(NotificationUiState(LoadingState(
        onRefresh = ::refresh,
        onErrorSnackbarDismiss = ::dismissSnackbar
    )))
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
                categoryLabel = it.type,
                title = it.title.orEmpty(),
                date = it.updatedDate?.format("MMM dd").orEmpty()
            )
        }
        _uiState.update {
            it.copy(notificationItems = items)
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

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = LoadingState(errorSnackbar = null))
        }
    }
}
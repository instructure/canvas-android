/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.poll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val themePrefs: ThemePrefs,
    private val dashboardEventHandler: DashboardEventHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(onSnackbarDismiss = ::dismissSnackbar))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.tryLaunch {
            loadUnreadCount()
            loadLogo()
        } catch {

        }

        viewModelScope.launch {
            dashboardEventHandler.events.collect { event ->
                when (event) {
                    is DashboardEvent.RefreshRequested -> {
                        _uiState.update { it.copy(externalShouldRefresh = true) }
                        refresh()
                        _uiState.update { it.copy(externalShouldRefresh = true) }
                    }
                    is DashboardEvent.ShowSnackbar -> showSnackbar(event.message)
                }
            }
        }
    }

    private suspend fun loadLogo() {
        // We need to poll for the logo URL because the Dashboard already starts to load when the canvas theme is not yet applied at the first launch.
        poll(
            pollInterval = 50,
            maxAttempts = 10,
            block = { _uiState.update { it.copy(logoUrl = themePrefs.mobileLogoUrl) } },
            validate = { themePrefs.mobileLogoUrl.isNotEmpty() }
        )
    }

    private suspend fun loadUnreadCount() {
        val unreadCounts = dashboardRepository.getUnreadCounts(true)
        val unreadConversations = unreadCounts.firstOrNull { it.type == "Conversation" }?.unreadCount ?: 0
        val unreadNotifications = unreadCounts.filter { it.type == "Message" }.sumOf { it.unreadCount }
        _uiState.update {
            it.copy(
                unreadCountState = DashboardUnreadState(
                    unreadConversations = unreadConversations,
                    unreadNotifications = unreadNotifications
                )
            )
        }
    }

    fun refresh() {
        viewModelScope.tryLaunch {
            loadUnreadCount()
        } catch {

        }
    }

    private fun showSnackbar(message: String) {
        _uiState.update {
            it.copy(snackbarMessage = message)
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(snackbarMessage = null)
        }
    }
}
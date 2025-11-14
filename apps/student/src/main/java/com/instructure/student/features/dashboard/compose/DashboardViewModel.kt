/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.features.dashboard.widget.usecase.EnsureDefaultWidgetsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetMetadataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val networkStateProvider: NetworkStateProvider,
    private val ensureDefaultWidgetsUseCase: EnsureDefaultWidgetsUseCase,
    private val observeWidgetMetadataUseCase: ObserveWidgetMetadataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            onRefresh = ::onRefresh,
            onRetry = ::onRetry
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _refreshSignal = MutableSharedFlow<Unit>()
    val refreshSignal = _refreshSignal.asSharedFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                launch { ensureDefaultWidgetsUseCase(Unit) }
                observeWidgetMetadataUseCase(Unit).collect { widgets ->
                    val visibleWidgets = widgets.filter { it.isVisible }
                    _uiState.update { it.copy(loading = false, error = null, widgets = visibleWidgets) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    private fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(refreshing = true, error = null) }
            try {
                _refreshSignal.emit(Unit)
                _uiState.update { it.copy(refreshing = false, error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(refreshing = false, error = e.message) }
            }
        }
    }

    private fun onRetry() {
        loadDashboard()
    }
}

/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.compose

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.usecase.EnsureDefaultWidgetsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetMetadataUseCase
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.NetworkStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val networkStateProvider: NetworkStateProvider,
    private val ensureDefaultWidgetsUseCase: EnsureDefaultWidgetsUseCase,
    private val observeWidgetMetadataUseCase: ObserveWidgetMetadataUseCase,
    private val analytics: Analytics,
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase,
    private val crashlytics: FirebaseCrashlytics
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

    private val _snackbarMessage = MutableSharedFlow<SnackbarMessage>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    private var widgetVisibilityTracked = false

    init {
        loadDashboard()
        observeNetworkState()
        observeConfig()
    }

    fun showSnackbar(message: String, actionLabel: String? = null, action: (() -> Unit)? = null) {
        viewModelScope.launch {
            _snackbarMessage.emit(SnackbarMessage(message, actionLabel, action))
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                ensureDefaultWidgetsUseCase(Unit)
                combine(
                    observeWidgetMetadataUseCase(Unit),
                    networkStateProvider.isOnlineLiveData.asFlow()
                ) { widgets, isOnline ->
                    val visibleWidgets = widgets.filter { it.isVisible }
                    val filtered = if (isOnline) visibleWidgets
                    else visibleWidgets.filter { it.id in OFFLINE_VISIBLE_WIDGETS }
                    Triple(filtered, isOnline, widgets)
                }.collect { (filteredWidgets, isOnline, allWidgets) ->
                    _uiState.update { it.copy(loading = false, error = null, widgets = filteredWidgets, isOnline = isOnline) }

                    if (!widgetVisibilityTracked) {
                        trackWidgetVisibility(allWidgets)
                        widgetVisibilityTracked = true
                    }
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

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkStateProvider.isOnlineLiveData.asFlow()
                .collect {
                    _refreshSignal.emit(Unit)
                }
        }
    }

    private fun trackWidgetVisibility(widgets: List<WidgetMetadata>) {
        val bundle = Bundle().apply {
            widgets.filter { it.isEditable }.forEach { widget ->
                val position = if (widget.isVisible) widget.position.toString() else "-1"
                putString(widget.id, position)
            }
        }
        analytics.logEvent(AnalyticsEventConstants.DASHBOARD_WIDGET_VISIBILITY, bundle)
    }

    private fun observeConfig() {
        viewModelScope.launch {
            observeGlobalConfigUseCase(Unit)
                .catch { crashlytics.recordException(it) }
                .collect { config ->
                    val themedColor = ColorKeeper.createThemedColor(config.backgroundColor)
                    _uiState.update { it.copy(color = themedColor) }
                }
        }
    }

    companion object {
        private val OFFLINE_VISIBLE_WIDGETS = setOf(
            WidgetMetadata.WIDGET_ID_COURSES,
            WidgetMetadata.WIDGET_ID_COURSE_INVITATIONS,
            WidgetMetadata.WIDGET_ID_INSTITUTIONAL_ANNOUNCEMENTS
        )
    }
}

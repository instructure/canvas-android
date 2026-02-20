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

package com.instructure.pandautils.features.dashboard.customize

import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.RemoteConfigParam
import com.instructure.canvasapi2.utils.RemoteConfigPrefs
import com.instructure.canvasapi2.utils.RemoteConfigUtils
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetConfigUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveWidgetMetadataUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.SwapWidgetPositionsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ToggleWidgetVisibilityUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.UpdateWidgetConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizeDashboardViewModel @Inject constructor(
    private val observeWidgetMetadataUseCase: ObserveWidgetMetadataUseCase,
    private val swapWidgetPositionsUseCase: SwapWidgetPositionsUseCase,
    private val toggleWidgetVisibilityUseCase: ToggleWidgetVisibilityUseCase,
    private val observeWidgetConfigUseCase: ObserveWidgetConfigUseCase,
    private val updateWidgetConfigUseCase: UpdateWidgetConfigUseCase,
    private val resources: Resources,
    private val apiPrefs: ApiPrefs,
    private val remoteConfigUtils: RemoteConfigUtils,
    private val remoteConfigPrefs: RemoteConfigPrefs,
    private val analytics: Analytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CustomizeDashboardUiState(
            onMoveUp = this::moveWidgetUp,
            onMoveDown = this::moveWidgetDown,
            onToggleVisibility = this::toggleVisibility,
            onToggleDashboardRedesign = this::toggleDashboardRedesign,
            onUpdateSetting = this::updateSetting,
            feedbackUrl = remoteConfigPrefs.getString(RemoteConfigParam.DASHBOARD_FEEDBACK_URL.rc_name, "").orEmpty()
        )
    )
    val uiState: StateFlow<CustomizeDashboardUiState> = _uiState.asStateFlow()

    init {
        loadWidgets()
        loadDashboardRedesignFlag()
    }

    private fun loadDashboardRedesignFlag() {
        val isDashboardRedesignEnabled = remoteConfigUtils.getBoolean(RemoteConfigParam.DASHBOARD_REDESIGN)
        _uiState.update { it.copy(isDashboardRedesignEnabled = isDashboardRedesignEnabled) }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun loadWidgets() {
        viewModelScope.launch {
            observeWidgetConfigUseCase(WidgetMetadata.WIDGET_ID_GLOBAL).collect { settingItems ->
                _uiState.update { it.copy(globalSettings = settingItems) }
            }
        }
        viewModelScope.launch {
            observeWidgetMetadataUseCase(Unit)
                .map { metadata ->
                    metadata
                        .filter { it.isEditable }
                        .sortedWith(compareBy({ !it.isVisible }, { it.position }))
                }
                .flatMapLatest { editableMetadata ->
                    combineWidgetsWithConfigs(editableMetadata)
                }
                .catch { e ->
                    _uiState.update { it.copy(loading = false, error = e.message) }
                }
                .collect { widgetItems ->
                    _uiState.update {
                        it.copy(
                            widgets = widgetItems,
                            loading = false,
                            error = null
                        )
                    }
                }
        }
    }

    private fun combineWidgetsWithConfigs(metadata: List<WidgetMetadata>) =
        if (metadata.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(
                metadata.map { meta ->
                    observeWidgetConfigUseCase(meta.id)
                        .map { settings ->
                            WidgetItem(
                                metadata = meta,
                                displayName = getDisplayName(meta.id),
                                settings = settings
                            )
                        }
                        .catch { e ->
                            e.printStackTrace()
                            emit(
                                WidgetItem(
                                    metadata = meta,
                                    displayName = getDisplayName(meta.id),
                                    settings = emptyList()
                                )
                            )
                        }
                }
            ) { it.toList() }
        }

    private fun moveWidgetUp(widgetId: String) {
        val widgets = _uiState.value.widgets
        val currentIndex = widgets.indexOfFirst { it.metadata.id == widgetId }

        if (currentIndex > 0) {
            val currentWidget = widgets[currentIndex].metadata
            val previousWidget = widgets[currentIndex - 1].metadata

            viewModelScope.launch {
                swapWidgetPositionsUseCase(
                    SwapWidgetPositionsUseCase.Params(currentWidget.id, previousWidget.id)
                )
            }
        }
    }

    private fun moveWidgetDown(widgetId: String) {
        val widgets = _uiState.value.widgets
        val currentIndex = widgets.indexOfFirst { it.metadata.id == widgetId }

        if (currentIndex < widgets.size - 1) {
            val currentWidget = widgets[currentIndex].metadata
            val nextWidget = widgets[currentIndex + 1].metadata

            viewModelScope.launch {
                swapWidgetPositionsUseCase(
                    SwapWidgetPositionsUseCase.Params(currentWidget.id, nextWidget.id)
                )
            }
        }
    }

    private fun toggleVisibility(widgetId: String) {
        val widgetsMetadata = _uiState.value.widgets.map { it.metadata }

        viewModelScope.launch {
            toggleWidgetVisibilityUseCase(
                ToggleWidgetVisibilityUseCase.Params(widgetId, widgetsMetadata)
            )
        }
    }

    private fun toggleDashboardRedesign(enabled: Boolean) {
        remoteConfigPrefs.putString(RemoteConfigParam.DASHBOARD_REDESIGN.rc_name, enabled.toString())
        _uiState.update { it.copy(isDashboardRedesignEnabled = enabled) }
    }

    private fun getDisplayName(widgetId: String): String {
        return when (widgetId) {
            WidgetMetadata.WIDGET_ID_WELCOME -> resources.getString(R.string.widget_hello, apiPrefs.user?.shortName)
            WidgetMetadata.WIDGET_ID_FORECAST -> resources.getString(R.string.widget_weekly_summary)
            WidgetMetadata.WIDGET_ID_COURSES -> resources.getString(R.string.courses_and_groups)
            WidgetMetadata.WIDGET_ID_TODO -> resources.getString(R.string.widget_toDo)
            else -> widgetId
        }
    }

    private fun updateSetting(widgetId: String, key: String, value: Any) {
        viewModelScope.launch {
            updateWidgetConfigUseCase(
                UpdateWidgetConfigUseCase.Params(
                    widgetId = widgetId,
                    key = key,
                    value = value
                )
            )
        }
    }

    fun trackDashboardSurvey(selectedOption: String) {
        val bundle = Bundle().apply {
            putString(AnalyticsParamConstants.SELECTED_REASON, selectedOption)
        }
        analytics.logEvent(AnalyticsEventConstants.DASHBOARD_SURVEY_SUBMITTED, bundle)
    }
}
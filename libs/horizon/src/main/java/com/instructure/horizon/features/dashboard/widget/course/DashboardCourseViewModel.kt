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
package com.instructure.horizon.features.dashboard.widget.course

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.domain.usecase.GetDashboardCoursesUseCase
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.features.dashboard.widget.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.model.DashboardNextModuleItem
import com.instructure.horizon.offline.HorizonOfflineViewModel
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import com.instructure.pandautils.utils.formatIsoDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardCourseViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getDashboardCoursesUseCase: GetDashboardCoursesUseCase,
    private val dashboardEventHandler: DashboardEventHandler,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : HorizonOfflineViewModel(networkStateProvider, featureFlagProvider) {

    private val _uiState = MutableStateFlow(DashboardCourseUiState(onRefresh = ::onRefresh))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()

        viewModelScope.launch {
            dashboardEventHandler.events.collect { event ->
                when (event) {
                    is DashboardEvent.ProgressRefresh -> onRefresh()
                    else -> { /* No-op */ }
                }
            }
        }
    }

    override fun onNetworkRestored() {
        loadData()
    }

    override fun onNetworkLost() {
        // Offline banner is handled by DashboardViewModel; no action needed here
    }

    private fun loadData() {
        _uiState.update { it.copy(state = DashboardItemState.LOADING) }
        viewModelScope.tryLaunch {
            fetchData()
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
        }
    }

    private fun onRefresh(onFinished: () -> Unit = {}) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            fetchData()
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
            onFinished()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
            onFinished()
        }
    }

    private suspend fun fetchData() {
        val data = getDashboardCoursesUseCase()

        val courseCardStates = data.enrollments.mapToDashboardCourseCardState(
            context,
            programs = data.programs,
            nextModuleForCourse = { courseId ->
                data.nextModuleItemByCourseId[courseId]?.let { mapToModuleItemState(it) }
            },
        )

        val programCardStates = data.unenrolledPrograms.mapToDashboardCourseCardState(context)

        _uiState.update {
            it.copy(
                programs = DashboardPaginatedWidgetCardState(programCardStates),
                courses = courseCardStates,
            )
        }
    }

    private fun mapToModuleItemState(moduleItem: DashboardNextModuleItem): DashboardCourseCardModuleItemState {
        return DashboardCourseCardModuleItemState(
            moduleItemTitle = moduleItem.title,
            moduleItemType = moduleItem.type,
            dueDate = moduleItem.dueDate,
            estimatedDuration = moduleItem.estimatedDuration?.formatIsoDuration(context),
            onClickAction = CardClickAction.NavigateToModuleItem(moduleItem.courseId, moduleItem.moduleItemId),
        )
    }
}

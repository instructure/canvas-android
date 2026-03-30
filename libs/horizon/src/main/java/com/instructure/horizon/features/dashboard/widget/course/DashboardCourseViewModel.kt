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
package com.instructure.horizon.features.dashboard.widget.course

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.type.EnrollmentWorkflowState
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.domain.usecase.AcceptCourseInviteParams
import com.instructure.horizon.domain.usecase.AcceptCourseInviteUseCase
import com.instructure.horizon.domain.usecase.GetEnrollmentsUseCase
import com.instructure.horizon.domain.usecase.GetModuleItemsUseCase
import com.instructure.horizon.domain.usecase.GetProgramsUseCase
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.features.dashboard.widget.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.model.LearningObjectType
import com.instructure.horizon.offline.HorizonOfflineViewModel
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
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
    private val getEnrollmentsUseCase: GetEnrollmentsUseCase,
    private val getProgramsUseCase: GetProgramsUseCase,
    private val getModuleItemsUseCase: GetModuleItemsUseCase,
    private val acceptCourseInviteUseCase: AcceptCourseInviteUseCase,
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
        var enrollments = getEnrollmentsUseCase()
        val programs = getProgramsUseCase()

        val invitations = enrollments.filter { it.state == EnrollmentWorkflowState.invited }
        if (invitations.isNotEmpty()) {
            invitations.forEach { enrollment ->
                acceptCourseInviteUseCase(
                    AcceptCourseInviteParams(
                        courseId = enrollment.course?.id?.toLongOrNull() ?: return@forEach,
                        enrollmentId = enrollment.id?.toLongOrNull() ?: return@forEach,
                    )
                )
            }
            enrollments = getEnrollmentsUseCase()
        }

        val courseCardStates = enrollments.mapToDashboardCourseCardState(
            context,
            programs = programs,
            nextModuleForCourse = { courseId -> fetchNextModuleState(courseId) },
        )

        val programCardStates = programs
            .filter { program -> program.sortedRequirements.none { it.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED } }
            .mapToDashboardCourseCardState(context)

        _uiState.update {
            it.copy(
                programs = DashboardPaginatedWidgetCardState(programCardStates),
                courses = courseCardStates,
            )
        }
    }

    private suspend fun fetchNextModuleState(courseId: Long?): DashboardCourseCardModuleItemState? {
        if (courseId == null) return null
        val modules = getModuleItemsUseCase(courseId)
        val nextModuleItem = modules.flatMap { it.items }.firstOrNull() ?: return null
        val formattedDuration = nextModuleItem.estimatedDuration?.formatIsoDuration(context)
        return DashboardCourseCardModuleItemState(
            moduleItemTitle = nextModuleItem.title.orEmpty(),
            moduleItemType = if (nextModuleItem.quizLti) LearningObjectType.ASSESSMENT
                             else LearningObjectType.fromApiString(nextModuleItem.type.orEmpty()),
            dueDate = nextModuleItem.moduleDetails?.dueDate,
            estimatedDuration = formattedDuration,
            onClickAction = CardClickAction.NavigateToModuleItem(courseId, nextModuleItem.id),
        )
    }
}

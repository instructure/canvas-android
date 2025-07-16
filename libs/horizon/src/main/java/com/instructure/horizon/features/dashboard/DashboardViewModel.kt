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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.managers.DashboardCourse
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.canvasapi2.models.ModuleObject
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.model.LearningObjectType
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.formatIsoDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    @ApplicationContext private val context: Context,
    private val themePrefs: ThemePrefs
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(DashboardUiState(loadingState = LoadingState(onRefresh = ::refresh, onSnackbarDismiss = ::dismissSnackbar)))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            loadData(forceNetwork = false)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }
    }

    fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            loadData(forceNetwork = true)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false)) }
        }
    }

    private suspend fun loadData(forceNetwork: Boolean) {
        _uiState.update { it.copy(logoUrl = themePrefs.mobileLogoUrl) }
        val courses = dashboardRepository.getDashboardCourses(forceNetwork = forceNetwork)
        if (courses.isSuccess) {
            val coursesResult = courses.dataOrThrow
            val courseUiStates = coursesResult.map { course ->
                viewModelScope.async {
                    mapCourse(course, forceNetwork)
                }
            }.awaitAll().filterNotNull()
            _uiState.update { it.copy(coursesUiState = courseUiStates, loadingState = it.loadingState.copy(isError = false)) }
        } else {
            handleError()
        }
    }

    private suspend fun mapCourse(dashboardCourse: DashboardCourse, forceNetwork: Boolean): DashboardCourseUiState? {
        val nextModuleId = dashboardCourse.nextUpModuleId
        val nextModuleItemId = dashboardCourse.nextUpModuleItemId
        return if (nextModuleId != null && nextModuleItemId != null) {
            createCourseUiState(dashboardCourse)
        } else if (dashboardCourse.course.progress == 0.0) {

            val modules = dashboardRepository.getFirstPageModulesWithItems(
                dashboardCourse.course.courseId,
                forceNetwork = forceNetwork
            )

            if (modules.isSuccess) {
                val nextModuleItemResult = modules.dataOrThrow.flatMap { module -> module.items }.firstOrNull()
                val nextModuleResult = modules.dataOrThrow.find { module -> module.id == nextModuleItemResult?.moduleId }

                if (nextModuleItemResult == null || nextModuleResult == null) {
                    return null
                }
                createCourseUiState(dashboardCourse.course, nextModuleResult, nextModuleItemResult)
            } else {
                handleError()
                null
            }
        } else if (dashboardCourse.course.progress == 100.0) {
            DashboardCourseUiState(
                courseId = dashboardCourse.course.courseId,
                courseName = dashboardCourse.course.courseName,
                courseProgress = dashboardCourse.course.progress,
                completed = true,
                progressLabel = getProgressLabel(dashboardCourse.course.progress),
            )
        } else {
            handleError()
            null
        }
    }

    private fun createCourseUiState(
        dashboardCourse: DashboardCourse
    ) = DashboardCourseUiState(
        courseId = dashboardCourse.course.courseId,
        courseName = dashboardCourse.course.courseName,
        courseProgress = dashboardCourse.course.progress,
        nextModuleName = dashboardCourse.nextUpModuleTitle ?: "",
        nextModuleItemId = dashboardCourse.nextUpModuleItemId,
        nextModuleItemName = dashboardCourse.nextUpModuleItemTitle ?: "",
        progressLabel = getProgressLabel(dashboardCourse.course.progress),
        remainingTime = dashboardCourse.nextModuleItemEstimatedDuration?.formatIsoDuration(context),
        learningObjectType = if (dashboardCourse.isNewQuiz) LearningObjectType.ASSESSMENT else LearningObjectType.fromApiString(
            dashboardCourse.nextModuleItemType.orEmpty()
        ),
        dueDate = dashboardCourse.nextModuleItemDueDate
    )

    private fun createCourseUiState(
        course: CourseWithProgress,
        nextModule: ModuleObject?,
        nextModuleItem: ModuleItem
    ) = DashboardCourseUiState(
        courseId = course.courseId,
        courseName = course.courseName,
        courseProgress = course.progress,
        nextModuleName = nextModule?.name ?: "",
        nextModuleItemId = nextModuleItem.id,
        nextModuleItemName = nextModuleItem.title ?: "",
        progressLabel = getProgressLabel(course.progress),
        remainingTime = nextModuleItem.estimatedDuration?.formatIsoDuration(context),
        learningObjectType = if (nextModuleItem.quizLti) LearningObjectType.ASSESSMENT else LearningObjectType.fromApiString(nextModuleItem.type.orEmpty()),
        dueDate = nextModuleItem.moduleDetails?.dueDate
    )

    private fun getProgressLabel(progress: Double): String {
        return when (progress) {
            0.0 -> {
                context.getString(R.string.learningObject_pillStatusNotStarted).uppercase()
            }

            100.0 -> {
                context.getString(R.string.learningObject_pillStatusCompleted).uppercase()
            }

            else -> {
                context.getString(R.string.learningObject_pillStatusInProgress).uppercase()
            }
        }
    }

    private fun handleError() {
        _uiState.update {
            if (it.coursesUiState.isEmpty()) {
                it.copy(loadingState = it.loadingState.copy(isError = true))
            } else {
                it.copy(loadingState = it.loadingState.copy(snackbarMessage = context.getString(R.string.errorOccurred)))
            }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(snackbarMessage = null))
        }
    }
}
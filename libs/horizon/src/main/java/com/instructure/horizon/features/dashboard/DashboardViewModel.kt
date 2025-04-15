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
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.model.LearningObjectType
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(DashboardUiState(loadingState = LoadingState(onRefresh = ::refresh, onErrorSnackbarDismiss = ::dismissSnackbar)))
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
        val courses = dashboardRepository.getCoursesWithProgress(forceNetwork = forceNetwork)
        if (courses.isSuccess) {
            val coursesResult = courses.dataOrThrow.filter { it.progress != null && it.nextUpModuleId != null && it.nextUpModuleItemId != null }
            val courseUiStates = coursesResult.map { course ->
                viewModelScope.async {
                    val nextModuleId = course.nextUpModuleId
                    val nextModuleItemId = course.nextUpModuleItemId
                    if (nextModuleId != null && nextModuleItemId != null) {
                        val nextModule = dashboardRepository.getNextModule(
                            course.course.id,
                            nextModuleId,
                            forceNetwork = true
                        )
                        val nextModuleItem = dashboardRepository.getNextModuleItem(
                            course.course.id,
                            nextModuleId,
                            nextModuleItemId,
                            forceNetwork = true
                        )
                        if (nextModuleItem.isSuccess) {
                            val nextModuleResult = nextModule.dataOrNull
                            val nextModuleItemResult = nextModuleItem.dataOrThrow
                            DashboardCourseUiState(
                                courseId = course.course.id,
                                courseName = course.course.name,
                                courseProgress = course.progress ?: 0.0,
                                nextModuleName = nextModuleResult?.name ?: "",
                                nextModuleItemName = nextModuleItemResult.title ?: "",
                                progressLabel = getProgressLabel(course.progress ?: 0.0),
                                remainingTime = nextModuleItemResult.estimatedDuration?.formatIsoDuration(context),
                                learningObjectType = LearningObjectType.fromApiString(nextModuleItemResult.type.orEmpty()),
                                dueDate = nextModuleItemResult.moduleDetails?.dueDate,
                                onClick = { onCourseClick(course.course.id) }
                            )
                        } else {
                            handleError()
                            null
                        }
                    } else {
                        handleError()
                        null
                    }
                }
            }.awaitAll().filterNotNull()
            _uiState.update { it.copy(coursesUiState = courseUiStates, loadingState = it.loadingState.copy(isError = false)) }
        } else {
            handleError()
        }
    }

    private fun getProgressLabel(progress: Double): String {
        return when (progress) {
            in 0.0..<100.0 -> {
                context.getString(R.string.learningObject_pillStatusInProgress).uppercase()
            }

            100.0 -> {
                context.getString(R.string.learningObject_pillStatusCompleted).uppercase()
            }

            else -> {
                context.getString(R.string.learningObject_pillStatusNotStarted).uppercase()
            }
        }
    }

    private fun handleError() {
        _uiState.update {
            if (it.coursesUiState.isEmpty()) {
                it.copy(loadingState = it.loadingState.copy(isError = true))
            } else {
                it.copy(loadingState = it.loadingState.copy(errorSnackbar = context.getString(R.string.errorOccurred)))
            }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(errorSnackbar = null))
        }
    }

    private fun onCourseClick(courseId: Long) {
        // TODO Navigate to module item
    }
}
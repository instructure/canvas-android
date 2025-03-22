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
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
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

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData(forceNetwork = false)
    }

    fun refresh() {
        loadData(forceNetwork = true)
    }

    private fun loadData(forceNetwork: Boolean) {
        viewModelScope.tryLaunch {
            val courses = dashboardRepository.getCoursesWithProgress(forceNetwork = forceNetwork)
            if (courses.isSuccess) {
                val coursesResult = courses.dataOrThrow
                val courseUiStates = coursesResult.map { course ->
                    async {
                        val nextModule = dashboardRepository.getNextModule(course.course.id, course.nextUpModuleId, forceNetwork = true)
                        val nextModuleItem = dashboardRepository.getNextModuleItem(
                            course.course.id,
                            course.nextUpModuleId,
                            course.nextUpModuleItemId,
                            forceNetwork = true
                        )
                        if (nextModule.isSuccess && nextModuleItem.isSuccess) {
                            val nextModuleResult = nextModule.dataOrThrow
                            val nextModuleItemResult = nextModuleItem.dataOrThrow
                            DashboardCourseUiState(
                                courseId = course.course.id,
                                courseName = course.course.name,
                                courseProgress = course.progress,
                                nextModuleName = nextModuleResult.name ?: "",
                                nextModuleItemName = nextModuleItemResult.title ?: "",
                                progressLabel = "In progress",
                                remainingTime = nextModuleItemResult.estimatedDuration?.formatIsoDuration(context),
                                learningObjectType = "Assignment", // TODO: get learning object type
                                dueDate = nextModuleItemResult.moduleDetails?.dueDate
                            )
                        } else {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
                _uiState.update { it.copy(coursesUiState = courseUiStates) }
                Log.d("asdasd", "moduleItems: $courseUiStates")
            }
        } catch {
            Log.d("asdasd", "exception occurred $it")
        }
    }
}
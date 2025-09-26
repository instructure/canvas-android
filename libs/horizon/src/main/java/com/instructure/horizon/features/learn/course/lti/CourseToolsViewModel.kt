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
package com.instructure.horizon.features.learn.course.lti

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CourseToolsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CourseToolsRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(
        CourseToolsUiState()
    )
    val uiState = _uiState.asStateFlow()

    fun loadState(courseId: Long) {
        _uiState.update {
            it.copy(
                screenState = it.screenState.copy(isLoading = true, onRefresh = ::refresh, onSnackbarDismiss = ::dismissSnackbar),
                courseId = courseId,
            )
        }
        viewModelScope.tryLaunch {
            getData(courseId)
            _uiState.update {
                it.copy(screenState = it.screenState.copy(isLoading = false))
            }
        } catch { _ ->
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(isLoading = false, isError = true, errorMessage = context.getString(
                        R.string.failedToLoadScores
                    )),
                )
            }
        }
    }

    private suspend fun getData(courseId: Long, forceRefresh: Boolean = false) {
        val tools = repository.getExternalTools(courseId, forceRefresh)
        if (tools.isNotEmpty()) {
            val toolItems = tools.map { tool ->
                LtiToolItem(
                    title = tool.courseNavigation?.text ?: tool.name.orEmpty(),
                    iconUrl = tool.iconUrl,
                    url = tool.url.orEmpty(),
                )
            }
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(isError = false, errorMessage = null),
                    ltiTools = toolItems
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    screenState = it.screenState.copy(isError = true, errorMessage = context.getString(
                        R.string.tools_noLtiTools
                    )),
                )
            }
        }
    }

    private fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = true)) }
            getData(uiState.value.courseId, forceRefresh = true)
            _uiState.update { it.copy(screenState = it.screenState.copy(isRefreshing = false)) }
        } catch {
            _uiState.update { it.copy(screenState = it.screenState.copy(snackbarMessage = context.getString(R.string.errorOccurred), isRefreshing = false)) }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(screenState = it.screenState.copy(snackbarMessage = null))
        }
    }
}
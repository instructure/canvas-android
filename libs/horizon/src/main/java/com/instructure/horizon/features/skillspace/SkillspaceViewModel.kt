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
package com.instructure.horizon.features.skillspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SkillspaceViewModel @Inject constructor(
    private val repository: SkillspaceRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(SkillspaceUiState(
        loadingState = LoadingState(onRefresh = ::refreshData)
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = true))
            }

            fetchUrl()

            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false))
            }
        } catch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false, isError = true)
                )
            }
        }
    }

    private suspend fun fetchUrl() {
        repository.getAuthenticatedSession()?.sessionUrl?.let { url ->
            _uiState.update { it.copy(webviewUrl = url) }
        }
        repository.getEmbeddedSkillspaceUrl().let { url ->
            _uiState.update { it.copy(webviewUrl = url) }
        }

    }

    private fun refreshData() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isRefreshing = true))
            }

            fetchUrl()

            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isRefreshing = false))
            }
        } catch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isRefreshing = false)
                )
            }
        }
    }
}
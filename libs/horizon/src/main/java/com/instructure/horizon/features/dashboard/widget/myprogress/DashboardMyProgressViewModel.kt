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
package com.instructure.horizon.features.dashboard.widget.myprogress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.myprogress.card.DashboardMyProgressCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardMyProgressViewModel @Inject constructor(
    private val repository: DashboardMyProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardMyProgressUiState(
            onRefresh = ::refresh
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadMyProgressData()
    }

    private fun loadMyProgressData(forceNetwork: Boolean = false) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            val data = repository.getLearningStatusData(forceNetwork = forceNetwork)?.data.orEmpty()
            val moduleCountCompleted = data.sumOf { it.moduleCountCompleted ?: 0}

            _uiState.update {
                it.copy(
                    state = DashboardItemState.SUCCESS,
                    cardState = DashboardMyProgressCardState(
                        moduleCountCompleted = moduleCountCompleted
                    )
                )
            }
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
        }
    }

    private fun refresh(onComplete: () -> Unit) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            loadMyProgressData(forceNetwork = true)
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
            onComplete()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
            onComplete()
        }
    }
}

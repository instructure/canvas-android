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
package com.instructure.horizon.features.dashboard.widget.skilloverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.skilloverview.card.DashboardSkillOverviewCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DashboardSkillOverviewViewModel @Inject constructor(
    private val repository: DashboardSkillOverviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DashboardSkillOverviewUiState(
            onRefresh = ::refresh
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.tryLaunch {
            loadSkillOverviewData()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
        }
    }

    private suspend fun loadSkillOverviewData(forceNetwork: Boolean = false) {
        _uiState.update { it.copy(state = DashboardItemState.LOADING) }
        val skills = repository.getSkills(completedOnly = true, forceNetwork = forceNetwork)

        val completedSkillCount = skills.size

        _uiState.update {
            it.copy(
                state = DashboardItemState.SUCCESS,
                cardState = DashboardSkillOverviewCardState(
                    completedSkillCount = completedSkillCount
                )
            )
        }
    }

    private fun refresh(onComplete: () -> Unit) {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(state = DashboardItemState.LOADING) }
            loadSkillOverviewData(forceNetwork = true)
            _uiState.update { it.copy(state = DashboardItemState.SUCCESS) }
            onComplete()
        } catch {
            _uiState.update { it.copy(state = DashboardItemState.ERROR) }
            onComplete()
        }
    }
}

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
package com.instructure.pandautils.features.speedgrader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderBottomSheetViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedGraderBottomSheetUiState(onTabSelected = this::selectTab))
    val uiState = _uiState.asStateFlow()

    private fun selectTab(tabOrdinal: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTab = tabOrdinal) }
        }
    }
}

data class SpeedGraderBottomSheetUiState(
    val selectedTab: Int = SpeedGraderTab.GRADE.ordinal,
    val onTabSelected: (Int) -> Unit
)
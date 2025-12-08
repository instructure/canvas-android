/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.dashboard.widget.welcome

import androidx.lifecycle.ViewModel
import com.instructure.pandautils.features.dashboard.widget.welcome.usecase.GetWelcomeGreetingUseCase
import com.instructure.pandautils.features.dashboard.widget.welcome.usecase.GetWelcomeMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WelcomeWidgetViewModel @Inject constructor(
    private val getWelcomeGreetingUseCase: GetWelcomeGreetingUseCase,
    private val getWelcomeMessageUseCase: GetWelcomeMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeWidgetUiState())
    val uiState: StateFlow<WelcomeWidgetUiState> = _uiState.asStateFlow()

    init {
        loadWelcomeContent()
    }

    fun refresh() {
        loadWelcomeContent()
    }

    private fun loadWelcomeContent() {
        _uiState.update {
            it.copy(
                greeting = getWelcomeGreetingUseCase(),
                message = getWelcomeMessageUseCase()
            )
        }
    }
}

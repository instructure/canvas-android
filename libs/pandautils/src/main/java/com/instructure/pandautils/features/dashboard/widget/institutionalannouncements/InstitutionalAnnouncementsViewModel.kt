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
package com.instructure.pandautils.features.dashboard.widget.institutionalannouncements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.domain.usecase.accountnotification.LoadInstitutionalAnnouncementsParams
import com.instructure.pandautils.domain.usecase.accountnotification.LoadInstitutionalAnnouncementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstitutionalAnnouncementsViewModel @Inject constructor(
    private val loadInstitutionalAnnouncementsUseCase: LoadInstitutionalAnnouncementsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InstitutionalAnnouncementsUiState(
            onRefresh = ::loadAnnouncements
        )
    )
    val uiState: StateFlow<InstitutionalAnnouncementsUiState> = _uiState.asStateFlow()

    init {
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            try {
                val announcements = loadInstitutionalAnnouncementsUseCase(
                    LoadInstitutionalAnnouncementsParams(forceRefresh = true)
                )
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = false,
                        announcements = announcements
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = true
                    )
                }
            }
        }
    }
}
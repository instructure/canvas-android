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

package com.instructure.pandautils.features.dashboard.widget.conferences

import android.content.res.Resources
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.domain.usecase.conference.LoadLiveConferencesUseCase
import com.instructure.pandautils.domain.usecase.session.GetAuthenticatedSessionUseCase
import com.instructure.pandautils.models.ConferenceDashboardBlacklist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConferencesViewModel @Inject constructor(
    private val loadLiveConferencesUseCase: LoadLiveConferencesUseCase,
    private val getAuthenticatedSessionUseCase: GetAuthenticatedSessionUseCase,
    private val conferenceDashboardBlacklist: ConferenceDashboardBlacklist,
    private val conferencesWidgetRouter: ConferencesWidgetRouter,
    private val apiPrefs: ApiPrefs,
    private val resources: Resources
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConferencesUiState(
            onRefresh = ::loadConferences,
            onJoinConference = ::joinConference,
            onDismissConference = ::dismissConference,
            onClearSnackbar = ::clearSnackbar
        )
    )
    val uiState: StateFlow<ConferencesUiState> = _uiState.asStateFlow()

    init {
        loadConferences()
    }

    private fun loadConferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            try {
                val conferences = loadLiveConferencesUseCase(LoadLiveConferencesUseCase.Params(forceRefresh = true))
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = false,
                        conferences = conferences
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

    private fun joinConference(activity: FragmentActivity, conference: ConferenceItem) {
        if (_uiState.value.joiningConferenceId != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(joiningConferenceId = conference.id) }

            val targetUrl = conference.joinUrl
                ?: "${apiPrefs.fullDomain}${conference.canvasContext.toAPIString()}/conferences/${conference.id}/join"

            val url = try {
                getAuthenticatedSessionUseCase(GetAuthenticatedSessionUseCase.Params(targetUrl))
            } catch (e: Throwable) {
                targetUrl
            }

            conferencesWidgetRouter.launchConference(activity, conference.canvasContext, url)

            delay(3000)
            _uiState.update { it.copy(joiningConferenceId = null) }
        }
    }

    private fun dismissConference(conference: ConferenceItem) {
        val blacklist = conferenceDashboardBlacklist.conferenceDashboardBlacklist + conference.id.toString()
        conferenceDashboardBlacklist.conferenceDashboardBlacklist = blacklist

        val updatedConferences = _uiState.value.conferences.filter { it.id != conference.id }
        _uiState.update {
            it.copy(
                conferences = updatedConferences,
                snackbarMessage = SnackbarMessage(
                    message = resources.getString(R.string.conferencesWidgetDismissed)
                )
            )
        }
    }

    private fun clearSnackbar() {
        _uiState.update {
            it.copy(
                snackbarMessage = null,
                onClearSnackbar = ::clearSnackbar
            )
        }
    }
}
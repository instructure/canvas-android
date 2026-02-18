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
 *
 */
package com.instructure.pandautils.features.dashboard.widget.courseinvitation

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import com.instructure.pandautils.domain.usecase.enrollment.HandleCourseInvitationParams
import com.instructure.pandautils.domain.usecase.enrollment.HandleCourseInvitationUseCase
import com.instructure.pandautils.domain.usecase.enrollment.LoadCourseInvitationsParams
import com.instructure.pandautils.domain.usecase.enrollment.LoadCourseInvitationsUseCase
import com.instructure.pandautils.features.dashboard.widget.usecase.ObserveGlobalConfigUseCase
import com.instructure.pandautils.utils.ColorKeeper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseInvitationsViewModel @Inject constructor(
    private val loadCourseInvitationsUseCase: LoadCourseInvitationsUseCase,
    private val handleCourseInvitationUseCase: HandleCourseInvitationUseCase,
    private val observeGlobalConfigUseCase: ObserveGlobalConfigUseCase,
    private val crashlytics: FirebaseCrashlytics,
    private val resources: Resources
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CourseInvitationsUiState(
            onRefresh = ::loadInvitations,
            onAcceptInvitation = ::acceptInvitation,
            onDeclineInvitation = ::declineInvitation,
            onClearSnackbar = ::clearSnackbar
        )
    )
    val uiState: StateFlow<CourseInvitationsUiState> = _uiState.asStateFlow()

    init {
        loadInvitations()
        observeConfig()
    }

    private fun loadInvitations() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            try {
                val invitations = loadCourseInvitationsUseCase(LoadCourseInvitationsParams(forceRefresh = true))
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = false,
                        invitations = invitations
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

    private fun acceptInvitation(invitation: CourseInvitation) {
        handleInvitation(invitation, accept = true)
    }

    private fun declineInvitation(invitation: CourseInvitation) {
        handleInvitation(invitation, accept = false)
    }

    private fun clearSnackbar() {
        _uiState.update {
            it.copy(
                snackbarMessage = null,
                onClearSnackbar = ::clearSnackbar
            )
        }
    }

    private fun handleInvitation(invitation: CourseInvitation, accept: Boolean) {
        viewModelScope.launch {
            val optimisticInvitations = _uiState.value.invitations.filter { it.enrollmentId != invitation.enrollmentId }
            _uiState.update { it.copy(invitations = optimisticInvitations) }

            try {
                handleCourseInvitationUseCase(
                    HandleCourseInvitationParams(
                        courseId = invitation.courseId,
                        enrollmentId = invitation.enrollmentId,
                        accept = accept
                    )
                )
                val message = if (accept) {
                    resources.getString(R.string.courseInvitationAccepted, invitation.courseName)
                } else {
                    resources.getString(R.string.courseInvitationDeclined, invitation.courseName)
                }
                _uiState.update {
                    it.copy(
                        snackbarMessage = SnackbarMessage(message = message)
                    )
                }
            } catch (e: Exception) {
                val restoredInvitations = (_uiState.value.invitations + invitation).sortedBy { it.enrollmentId }
                _uiState.update {
                    it.copy(
                        invitations = restoredInvitations,
                        snackbarMessage = SnackbarMessage(
                            message = resources.getString(R.string.errorOccurred),
                            actionLabel = resources.getString(R.string.retry),
                            action = { handleInvitation(invitation, accept) }
                        )
                    )
                }
            }
        }
    }

    private fun observeConfig() {
        viewModelScope.launch {
            observeGlobalConfigUseCase(Unit)
                .catch { crashlytics.recordException(it) }
                .collect { config ->
                    val themedColor = ColorKeeper.createThemedColor(config.backgroundColor)
                    _uiState.update { it.copy(color = themedColor) }
                }
        }
    }
}
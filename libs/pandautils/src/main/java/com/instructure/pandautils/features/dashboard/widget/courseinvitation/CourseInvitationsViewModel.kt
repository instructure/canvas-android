package com.instructure.pandautils.features.dashboard.widget.courseinvitation

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import com.instructure.pandautils.domain.usecase.enrollment.HandleCourseInvitationParams
import com.instructure.pandautils.domain.usecase.enrollment.HandleCourseInvitationUseCase
import com.instructure.pandautils.domain.usecase.enrollment.LoadCourseInvitationsParams
import com.instructure.pandautils.domain.usecase.enrollment.LoadCourseInvitationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseInvitationsViewModel @Inject constructor(
    private val loadCourseInvitationsUseCase: LoadCourseInvitationsUseCase,
    private val handleCourseInvitationUseCase: HandleCourseInvitationUseCase,
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
                snackbarAction = null,
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
                _uiState.update {
                    it.copy(
                        snackbarMessage = if (accept) {
                            resources.getString(R.string.courseInvitationAccepted, invitation.courseName)
                        } else {
                            resources.getString(R.string.courseInvitationDeclined, invitation.courseName)
                        },
                        snackbarAction = null
                    )
                }
            } catch (e: Exception) {
                val restoredInvitations = (_uiState.value.invitations + invitation).sortedBy { it.enrollmentId }
                _uiState.update {
                    it.copy(
                        invitations = restoredInvitations,
                        snackbarMessage = resources.getString(R.string.errorOccurred),
                        snackbarAction = { handleInvitation(invitation, accept) }
                    )
                }
            }
        }
    }
}
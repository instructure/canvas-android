package com.instructure.pandautils.features.dashboard.widget.courseinvitation

import com.instructure.pandautils.domain.models.enrollment.CourseInvitation

data class CourseInvitationsUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val invitations: List<CourseInvitation> = emptyList(),
    val snackbarMessage: String? = null,
    val snackbarAction: (() -> Unit)? = null,
    val onRefresh: () -> Unit = {},
    val onAcceptInvitation: (CourseInvitation) -> Unit = {},
    val onDeclineInvitation: (CourseInvitation) -> Unit = {}
)
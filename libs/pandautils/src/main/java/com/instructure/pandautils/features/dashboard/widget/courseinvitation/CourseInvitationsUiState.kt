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

import com.instructure.pandautils.compose.SnackbarMessage
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import com.instructure.pandautils.features.dashboard.widget.GlobalConfig
import com.instructure.pandautils.utils.ThemedColor

data class CourseInvitationsUiState(
    val loading: Boolean = true,
    val error: Boolean = false,
    val invitations: List<CourseInvitation> = emptyList(),
    val snackbarMessage: SnackbarMessage? = null,
    val onRefresh: () -> Unit = {},
    val onAcceptInvitation: (CourseInvitation) -> Unit = {},
    val onDeclineInvitation: (CourseInvitation) -> Unit = {},
    val onClearSnackbar: () -> Unit = {},
    val color: ThemedColor = ThemedColor(GlobalConfig.DEFAULT_COLOR)
)
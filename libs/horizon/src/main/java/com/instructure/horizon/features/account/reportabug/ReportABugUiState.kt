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
package com.instructure.horizon.features.account.reportabug

data class ReportABugUiState(
    val selectedTopic: String? = null,
    val isTopicMenuOpen: Boolean = false,
    val topicError: String? = null,
    val subject: String = "",
    val description: String = "",
    val subjectError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val shouldNavigateBack: Boolean = false,
    val onTopicSelected: (String) -> Unit = {},
    val onTopicMenuOpenChanged: (Boolean) -> Unit = {},
    val onSubjectChanged: (String) -> Unit = {},
    val onDescriptionChanged: (String) -> Unit = {},
    val onSubmit: () -> Unit = {},
    val onSnackbarDismissed: () -> Unit = {}
)

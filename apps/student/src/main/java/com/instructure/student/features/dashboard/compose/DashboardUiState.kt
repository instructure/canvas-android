/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.student.features.dashboard.compose

import com.instructure.pandautils.features.dashboard.widget.WidgetMetadata

data class DashboardUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val refreshing: Boolean = false,
    val widgets: List<WidgetMetadata> = emptyList(),
    val onRefresh: () -> Unit = {},
    val onRetry: () -> Unit = {}
)

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null
)
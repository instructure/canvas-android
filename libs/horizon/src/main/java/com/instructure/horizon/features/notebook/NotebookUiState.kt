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
package com.instructure.horizon.features.notebook

import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.platform.LoadingState

data class NotebookUiState(
    val loadingState: LoadingState = LoadingState(),
    val isLoadingMore: Boolean = false,
    val selectedFilter: NotebookType? = null,
    val onFilterSelected: (NotebookType?) -> Unit = {},
    val selectedCourse: CourseWithProgress? = null,
    val onCourseSelected: (CourseWithProgress?) -> Unit = {},
    val courses: List<CourseWithProgress> = emptyList(),
    val notes: List<Note> = emptyList(),
    val hasNextPage: Boolean = false,
    val loadNextPage: () -> Unit = {},
    val showTopBar: Boolean = false,
    val showFilters: Boolean = false,
    val navigateToEdit: Boolean = false,
    val showNoteTypeFilter: Boolean = true,
    val showCourseFilter: Boolean = true,
)
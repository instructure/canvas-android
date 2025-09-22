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
package com.instructure.horizon.features.learn.course.note

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.instructure.horizon.features.notebook.NotebookScreen
import com.instructure.horizon.features.notebook.NotebookViewModel

@Composable
fun CourseNotesScreen(
    courseId: Long,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    val viewModel: NotebookViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.updateCourseId(courseId)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        NotebookScreen(
            mainNavController,
            state
        )
    }
}
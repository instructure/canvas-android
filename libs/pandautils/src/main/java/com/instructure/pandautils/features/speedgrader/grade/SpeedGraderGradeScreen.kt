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
package com.instructure.pandautils.features.speedgrader.grade

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.pandautils.features.speedgrader.grade.comments.SpeedGraderCommentsScreen
import com.instructure.pandautils.features.speedgrader.grade.grading.SpeedGraderGradingScreen
import com.instructure.pandautils.features.speedgrader.grade.rubric.SpeedGraderRubricContent
import com.instructure.pandautils.features.speedgrader.grade.rubric.SpeedGraderRubricViewModel

@Composable
fun SpeedGraderGradeScreen() {
    val speedGraderRubricViewModel = hiltViewModel<SpeedGraderRubricViewModel>()
    val speedGraderRubricUiState by speedGraderRubricViewModel.uiState.collectAsState()
    var commentsExpanded by rememberSaveable { mutableStateOf(false) }
    var commentsPressed by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val showRubric = speedGraderRubricUiState.loading || speedGraderRubricUiState.criterions.isNotEmpty()
        if (!commentsPressed) {
            commentsExpanded = speedGraderRubricUiState.criterions.isEmpty()
        }
        SpeedGraderGradingScreen()
        SpeedGraderCommentsScreen(
            expanded = commentsExpanded,
            onExpandToggle = {
                commentsExpanded = !commentsExpanded
                commentsPressed = true
            }
        )
        if (showRubric) {
            SpeedGraderRubricContent(uiState = speedGraderRubricUiState)
        }
    }
}

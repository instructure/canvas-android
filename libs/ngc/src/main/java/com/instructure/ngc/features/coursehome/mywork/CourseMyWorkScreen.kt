/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */

package com.instructure.ngc.features.coursehome.mywork

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.LocalDesignSystem
import com.instructure.pandautils.features.grades.GradesScreen
import com.instructure.pandautils.features.grades.GradesViewModel
import com.instructure.pandautils.utils.ColorKeeper

@Composable
fun CourseMyWorkScreen(courseId: Long, modifier: Modifier = Modifier) {
    val gradesViewModel: GradesViewModel = hiltViewModel()
    val uiState by gradesViewModel.uiState.collectAsState()
    val courseColor = ColorKeeper.getOrGenerateColor(Course(id = courseId)).light

    CompositionLocalProvider(LocalDesignSystem provides DesignSystem.InstUI) {
        GradesScreen(
            uiState = uiState,
            actionHandler = gradesViewModel::handleAction,
            canvasContextColor = courseColor,
            appBarUiState = null,
            applyInsets = true,
            onFilterUpdated = gradesViewModel::onFilterUpdated,
        )
    }
}
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.pandautils.features.speedgrader.SpeedGraderSharedViewModel
import com.instructure.pandautils.features.speedgrader.grade.grading.SpeedGraderGradingScreen
import com.instructure.pandautils.features.speedgrader.grade.rubric.SpeedGraderRubricScreen
import com.instructure.pandautils.utils.getFragmentActivity

@Composable
fun SpeedGraderGradeScreen() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SpeedGraderGradingScreen()
        SpeedGraderRubricScreen()
    }
    val activity = LocalContext.current.getFragmentActivity()
    val speedGraderSharedViewModel: SpeedGraderSharedViewModel = viewModel(viewModelStoreOwner = activity)
    val selectedAttemptId = speedGraderSharedViewModel.selectedAttemptId.collectAsStateWithLifecycle(null)

}

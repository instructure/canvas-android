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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.instructure.pandautils.features.speedgrader.grade.grading.SpeedGraderGradingScreen

@Composable
fun SpeedGraderGradeScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        SpeedGraderGradingScreen()
    }
    val activity = LocalContext.current.getFragmentActivity()
    val speedGraderSharedViewModel: SpeedGraderSharedViewModel = viewModel(viewModelStoreOwner = activity)
    val selectedAttemptId = speedGraderSharedViewModel.selectedAttemptId.collectAsStateWithLifecycle(null)

}

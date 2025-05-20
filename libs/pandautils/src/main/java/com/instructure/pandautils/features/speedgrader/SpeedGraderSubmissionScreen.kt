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
package com.instructure.pandautils.features.speedgrader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowWidthSizeClass
import com.instructure.pandautils.compose.composables.DraggableResizableLayout
import com.instructure.pandautils.compose.composables.HorizontalDraggableResizableLayout
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentScreen

@Composable
fun SpeedGraderSubmissionScreen(assignmentId: Long, submissionId: Long) {

    val horizontal = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    if (horizontal) {
        HorizontalDraggableResizableLayout(
            modifier = Modifier,
            leftContent = {
                NavHost(
                    navController = rememberNavController(),
                    modifier = Modifier.fillMaxSize(),
                    startDestination = "speedGraderContent/$assignmentId/$submissionId"
                ) {
                    speedGraderContentScreen()
                }
            },
            rightContent = {
                Text(text = "Right Content")
            }
        )
    } else {
        DraggableResizableLayout(
            modifier = Modifier,
            topContent = {
                NavHost(
                    navController = rememberNavController(),
                    modifier = Modifier.fillMaxSize(),
                    startDestination = "speedGraderContent/$assignmentId/$submissionId"
                ) {
                    speedGraderContentScreen()
                }
            },
            bottomContent = {
                Text(text = "Bottom Content")
            }
        )
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun SpeedGraderSubmissionScreenTabletPreview() {
    SpeedGraderSubmissionScreen(1L, 1L)
}

@Preview
@Composable
fun SpeedGraderSubmissionScreenPhonePreview() {
    SpeedGraderSubmissionScreen(2L, 2L)
}

@Preview(device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
fun SpeedGraderSubmissionScreenPhoneLandscapePreview() {
    SpeedGraderSubmissionScreen(3L, 3L)
}

private fun NavGraphBuilder.speedGraderContentScreen() {
    composable(
        route = "speedGraderContent/{assignmentId}/{submissionId}",
        arguments = listOf(
            navArgument("assignmentId") { type = NavType.LongType },
            navArgument("submissionId") { type = NavType.LongType }
        )
    ) {
        SpeedGraderContentScreen()
    }
}
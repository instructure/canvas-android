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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowWidthSizeClass
import com.instructure.pandautils.compose.composables.AnchorPoints
import com.instructure.pandautils.compose.composables.DraggableResizableLayout
import com.instructure.pandautils.compose.composables.HorizontalDraggableResizableLayout
import com.instructure.pandautils.features.speedgrader.content.SpeedGraderContentScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedGraderSubmissionScreen(assignmentId: Long, submissionId: Long) {

    val horizontal =
        currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

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
                NavHost(
                    navController = rememberNavController(),
                    modifier = Modifier.fillMaxSize(),
                    startDestination = "speedGraderBottomSheet/$assignmentId/$submissionId"
                ) {
                    speedGraderBottomSheet()
                }
            }
        )
    } else {
        val density = LocalDensity.current
        val defaultDecayAnimationSpec = rememberSplineBasedDecay<Float>()
        val initialAnchor = AnchorPoints.TOP
        val velocityThresholdDps = 125.dp
        val positionalThresholdFraction = 0.5f
        val snapAnimationSpec = spring<Float>(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioNoBouncy
        )
        val confirmValueChange: (AnchorPoints) -> Boolean = { newAnchor ->
            true
        }

        val velocityThresholdPx =
            remember(velocityThresholdDps) { with(density) { velocityThresholdDps.toPx() } }

        val anchoredDraggableState = remember(initialAnchor, confirmValueChange) {
            AnchoredDraggableState(
                initialValue = initialAnchor,
                anchors = DraggableAnchors { },
                positionalThreshold = { distance -> distance * positionalThresholdFraction },
                velocityThreshold = { velocityThresholdPx },
                snapAnimationSpec = snapAnimationSpec,
                decayAnimationSpec = defaultDecayAnimationSpec,
                confirmValueChange = confirmValueChange
            )
        }
        DraggableResizableLayout(
            anchoredDraggableState = anchoredDraggableState,
            modifier = Modifier,
            minTopHeightDp = 64.dp,
            minBottomHeightDp = 96.dp,
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
                NavHost(
                    navController = rememberNavController(),
                    modifier = Modifier.fillMaxSize(),
                    startDestination = "speedGraderBottomSheet/$assignmentId/$submissionId"
                ) {
                    speedGraderBottomSheet(anchoredDraggableState)
                }
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

@OptIn(ExperimentalFoundationApi::class)
private fun NavGraphBuilder.speedGraderBottomSheet(anchoredDraggableState: AnchoredDraggableState<AnchorPoints>? = null) {
    composable(
        route = "speedGraderBottomSheet/{assignmentId}/{submissionId}",
        arguments = listOf(
            navArgument("assignmentId") { type = NavType.LongType },
            navArgument("submissionId") { type = NavType.LongType }
        )
    ) {
        SpeedGraderBottomSheet(
            anchoredDraggableState = anchoredDraggableState,
            assignmentId = it.arguments?.getLong("assignmentId") ?: 0L,
            submissionId = it.arguments?.getLong("submissionId") ?: 0L
        )
    }
}
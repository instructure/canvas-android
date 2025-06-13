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

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.AnchorPoints
import com.instructure.pandautils.features.speedgrader.comments.SpeedGraderCommentsScreen
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradeScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedGraderBottomSheet(
    anchoredDraggableState: AnchoredDraggableState<AnchorPoints>?,
    assignmentId: Long,
    submissionId: Long
) {
    val navController = rememberNavController()
    val startDestination = SpeedGraderTab.GRADE
    var selectedTab by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    Column {
        PrimaryTabRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            selectedTabIndex = selectedTab,
            containerColor = colorResource(R.color.backgroundLightest),
            contentColor = colorResource(R.color.textInfo),
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = false),
                    width = Dp.Unspecified,
                    height = 1.5.dp,
                    color = colorResource(R.color.textInfo)
                )
            }
        ) {
            SpeedGraderTab.entries.forEach { tab ->
                Tab(
                    text = {
                        Text(
                            stringResource(id = tab.title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    selected = selectedTab == tab.ordinal,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (anchoredDraggableState?.currentValue == AnchorPoints.TOP) {
                            coroutineScope.launch {
                                anchoredDraggableState.animateTo(AnchorPoints.MIDDLE)
                            }
                        }
                        val route = tab.route.replace("{assignmentId}", assignmentId.toString())
                            .replace("{submissionId}", submissionId.toString())
                        navController.navigate(route) {
                            popUpTo(route) { inclusive = true }
                        }
                        selectedTab = tab.ordinal
                    }
                )
            }
        }
        SpeedGraderBottomSheetNavHost(
            navController = navController,
            startDestination = startDestination.route
                .replace("{assignmentId}", assignmentId.toString())
                .replace("{submissionId}", submissionId.toString())
        )
    }
}

@Composable
fun SpeedGraderBottomSheetNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = SpeedGraderTab.GRADE.route,
            arguments = listOf(
                navArgument("assignmentId") { type = NavType.LongType },
                navArgument("submissionId") { type = NavType.LongType }
            )
        ) {
            SpeedGraderGradeScreen()
        }

        composable(
            route = SpeedGraderTab.DETAILS.route,
            arguments = listOf(
                navArgument("assignmentId") { type = NavType.LongType },
                navArgument("submissionId") { type = NavType.LongType }
            )
        ) {
            SpeedGraderCommentsScreen()
        }
    }
}

enum class SpeedGraderTab(
    val route: String,
    @StringRes val title: Int
) {
    GRADE("speedGraderGrade/{assignmentId}/{submissionId}", R.string.speedGraderGradeTabTitle),
    DETAILS(
        "speedGraderDetails/{assignmentId}/{submissionId}",
        R.string.speedGraderCommentsTabTitle
    ),
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun SpeedGraderBottomSheetPreview() {
    SpeedGraderBottomSheet(null, 1L, 1L)
}

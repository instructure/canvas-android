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
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instructure.pandautils.R
import com.instructure.pandautils.features.speedgrader.details.SpeedGraderDetailsScreen
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradeScreen

@Composable
fun SpeedGraderBottomSheet(assignmentId: Long, submissionId: Long) {

    val navController = rememberNavController()
    val startDestination = SpeedGraderTab.GRADE
    var selectedTab by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Column {
        PrimaryTabRow(
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
                    text = { Text(stringResource(id = tab.title)) },
                    selected = selectedTab == tab.ordinal,
                    onClick = {
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
            SpeedGraderDetailsScreen()
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
        R.string.speedGraderDetailsTabTitle
    ),
}

@Preview
@Composable
private fun SpeedGraderBottomSheetPreview() {
    SpeedGraderBottomSheet(1L, 1L)
}

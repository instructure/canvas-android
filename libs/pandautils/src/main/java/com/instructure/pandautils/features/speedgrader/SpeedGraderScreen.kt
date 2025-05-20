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

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.composables.CanvasAppBar

@Composable
fun SpeedGraderScreen(
    uiState: SpeedGraderUiState,
    sharedViewModel: SpeedGraderSharedViewModel,
    navigationActionClick: () -> Unit
) {

    val pagerState = rememberPagerState(pageCount = { uiState.submissionIds.size })
    val viewPagerEnabled by sharedViewModel.viewPagerEnabled.collectAsState(initial = true)


    LaunchedEffect(Unit) {
        pagerState.scrollToPage(uiState.selectedItem)
    }

    Scaffold(
        topBar = {
            CanvasAppBar(
                title = uiState.assignmentName,
                subtitle = uiState.courseName,
                backgroundColor = Color(uiState.courseColor),
                navigationActionClick = navigationActionClick,
                navIconRes = R.drawable.ic_back_arrow,
                textColor = colorResource(id = R.color.textLightest),
            )
        },
    ) { padding ->
        HorizontalPager(modifier = Modifier.padding(padding), state = pagerState, userScrollEnabled = viewPagerEnabled) { page ->
            val submissionId = uiState.submissionIds[page]
            NavHost(navController = rememberNavController(), startDestination = "${uiState.assignmentId}/submission/$submissionId") {
                submissionScreen()
            }
        }
    }
}

fun NavGraphBuilder.submissionScreen() {
    composable(
        route = "{assignmentId}/submission/{submissionId}",
        arguments = listOf(
            navArgument("assignmentId") { type = NavType.LongType },
            navArgument("submissionId") { type = NavType.LongType }
        )
    ) {
        SpeedGraderSubmissionScreen(
            assignmentId = it.arguments?.getLong("assignmentId") ?: 0L,
            submissionId = it.arguments?.getLong("submissionId") ?: 0L
        )
    }
}
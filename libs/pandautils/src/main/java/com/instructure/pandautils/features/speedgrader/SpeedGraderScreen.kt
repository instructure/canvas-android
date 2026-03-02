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

import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.CanvasAppBar
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.utils.getFragmentActivity
import kotlinx.coroutines.launch

@Composable
fun SpeedGraderScreen(
    uiState: SpeedGraderUiState,
    sharedViewModel: SpeedGraderSharedViewModel,
    navigationActionClick: () -> Unit
) {
    val context = LocalContext.current
    val window = (context.getFragmentActivity()).window

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val viewPagerEnabled by sharedViewModel.viewPagerEnabled.collectAsState(initial = true)

    val errorEvent by sharedViewModel.errorState.collectAsState(initial = null)

    LaunchedEffect(errorEvent) {
        errorEvent?.let {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = it.message,
                    actionLabel = context.getString(R.string.retry),
                    duration = SnackbarDuration.Long
                )

                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        it.retryAction?.invoke()
                    }

                    SnackbarResult.Dismissed -> {
                        // Do nothing on dismiss
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val originalMode = window?.attributes?.softInputMode
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        onDispose {
            if (originalMode != null) {
                window.setSoftInputMode(originalMode)
            }
        }
    }

    CanvasScaffold(
        backgroundColor = colorResource(id = R.color.backgroundLightest),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    actionColor = LocalCourseColor.current
                )
            }
        },
        topBar = {
            CanvasAppBar(
                title = uiState.assignmentName,
                subtitle = uiState.courseName,
                backgroundColor = LocalCourseColor.current,
                navigationActionClick = navigationActionClick,
                navIconRes = R.drawable.ic_back_arrow,
                textColor = colorResource(id = R.color.textLightest),
                modifier = Modifier.testTag("speedGraderAppBar"),
                actions = {
                    IconButton(onClick = {
                        uiState.navigateToPostPolicy(context)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_eye_filled),
                            tint = colorResource(R.color.textLightest),
                            contentDescription = stringResource(R.string.a11y_contentDescription_postPolicy)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.loading -> {
                Loading(modifier = Modifier.fillMaxSize())
            }

            else -> {
                val pagerState = rememberPagerState(
                    pageCount = { uiState.submissionIds.size },
                    initialPage = uiState.selectedItem
                )

                HorizontalPager(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.displayCutout)
                        .padding(padding),
                    state = pagerState,
                    userScrollEnabled = viewPagerEnabled
                ) { page ->
                    uiState.onPageChange(page)
                    val submissionId = uiState.submissionIds[page]
                    NavHost(
                        navController = rememberNavController(),
                        startDestination = "${uiState.courseId}/assignments/${uiState.assignmentId}/submission/$submissionId"
                    ) {
                        submissionScreen()
                    }
                }
            }
        }
    }
}

fun NavGraphBuilder.submissionScreen() {
    composable(
        route = "{courseId}/assignments/{assignmentId}/submission/{submissionId}",
        arguments = listOf(
            navArgument("courseId") { type = NavType.LongType },
            navArgument("assignmentId") { type = NavType.LongType },
            navArgument("submissionId") { type = NavType.LongType },
        )
    ) {
        SpeedGraderSubmissionScreen()
    }
}
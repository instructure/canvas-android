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

package com.instructure.ngc.features.submission

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.navigation.NavigableSupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.navigation.TopBar
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronDown
import com.instructure.instui.token.icon.chevronUp
import com.instructure.ngc.R
import com.instructure.ngc.features.submission.supportingpane.SubmissionSupportingPane
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NGCSubmissionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NGCSubmissionViewModel = hiltViewModel(),
) {
    val widthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val twoPane = widthSizeClass != WindowWidthSizeClass.COMPACT
    var selectedSegment by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TopBar(
            title = "Submit Assignment",
            navigationIcon = {
                TextButton(
                    text = "Back",
                    onClick = onNavigateBack,
                )
            },
        )
        if (twoPane) {
            // Expanded width (e.g. landscape / tablet): show the supporting content as a side pane.
            val navigator = rememberSupportingPaneScaffoldNavigator()
            NavigableSupportingPaneScaffold(
                navigator = navigator,
                mainPane = {
                    AnimatedPane {
                        MainPaneContent(courseId = viewModel.courseId, assignmentId = viewModel.assignmentId)
                    }
                },
                supportingPane = {
                    AnimatedPane {
                        SubmissionSupportingPane(
                            selectedIndex = selectedSegment,
                            onSegmentSelected = { selectedSegment = it },
                        )
                    }
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            // Compact width (e.g. portrait phone): surface the supporting content as a bottom sheet.
            val scaffoldState = rememberBottomSheetScaffoldState()
            val scope = rememberCoroutineScope()
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 96.dp,
                sheetContent = {
                    SubmissionSupportingPane(
                        selectedIndex = selectedSegment,
                        onSegmentSelected = { selectedSegment = it },
                        headerTrailing = {
                            val isExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
                            val chevronIcon = if (isExpanded) InstUIIcons.chevronDown else InstUIIcons.chevronUp
                            val chevronContentDescription = stringResource(
                                if (isExpanded) R.string.a11y_submissionSupportingPaneCollapse
                                else R.string.a11y_submissionSupportingPaneExpand
                            )
                            Icon(
                                imageVector = chevronIcon,
                                contentDescription = chevronContentDescription,
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        if (isExpanded) {
                                            scaffoldState.bottomSheetState.partialExpand()
                                        } else {
                                            scaffoldState.bottomSheetState.expand()
                                        }
                                    }
                                },
                            )
                        },
                    )
                },
                modifier = Modifier.weight(1f),
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    MainPaneContent(courseId = viewModel.courseId, assignmentId = viewModel.assignmentId)
                }
            }
        }
    }
}

@Composable
private fun MainPaneContent(courseId: Long, assignmentId: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Submission content\ncourse $courseId · assignment $assignmentId")
    }
}


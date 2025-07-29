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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.core.layout.WindowWidthSizeClass
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.LocalCourseColor
import com.instructure.pandautils.compose.composables.AnchorPoints
import com.instructure.pandautils.features.speedgrader.details.SpeedGraderDetailsScreen
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradeScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpeedGraderBottomSheet(anchoredDraggableState: AnchoredDraggableState<AnchorPoints>?) {
    val viewModel: SpeedGraderBottomSheetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    SpeedGraderBottomSheetContent(uiState, anchoredDraggableState)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpeedGraderBottomSheetContent(
    uiState: SpeedGraderBottomSheetUiState,
    anchoredDraggableState: AnchoredDraggableState<AnchorPoints>?
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val horizontal = windowClass != WindowWidthSizeClass.COMPACT

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Column {
        PrimaryTabRow(
            modifier = Modifier
                .padding(horizontal = if (horizontal) 0.dp else 16.dp, vertical = 9.dp)
                .requiredHeight(64.dp),
            selectedTabIndex = uiState.selectedTab,
            containerColor = colorResource(R.color.backgroundLightestElevated),
            contentColor = LocalCourseColor.current,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        uiState.selectedTab,
                        matchContentSize = false
                    ),
                    width = Dp.Unspecified,
                    height = 1.5.dp,
                    color = LocalCourseColor.current
                )
            }
        ) {
            SpeedGraderTab.entries.forEach { tab ->
                Tab(
                    text = {
                        Text(
                            stringResource(id = tab.title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("speedGraderTab-${stringResource(id = tab.title)}")
                        )
                    },
                    selected = uiState.selectedTab == tab.ordinal,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (anchoredDraggableState?.currentValue == AnchorPoints.BOTTOM) {
                            coroutineScope.launch {
                                anchoredDraggableState.animateTo(AnchorPoints.MIDDLE)
                            }
                        }
                        uiState.onTabSelected(tab.ordinal)
                    }
                )
            }
        }
        when (uiState.selectedTab) {
            SpeedGraderTab.GRADE.ordinal -> {
                SpeedGraderGradeScreen()
            }

            SpeedGraderTab.DETAILS.ordinal -> {
                SpeedGraderDetailsScreen()
            }
        }
    }
}

enum class SpeedGraderTab(
    val route: String,
    @StringRes val title: Int
) {
    GRADE(
        "speedGraderGrade/{courseId}/assignments/{assignmentId}/{submissionId}",
        R.string.speedGraderGradeTabTitle
    ),
    DETAILS(
        "speedGraderDetails/{courseId}/assignments/{assignmentId}/{submissionId}",
        R.string.speedGraderDetailsTabTitle
    ),
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun SpeedGraderBottomSheetPreview() {
    CanvasTheme(courseColor = Color.Blue) {
        SpeedGraderBottomSheetContent(uiState = SpeedGraderBottomSheetUiState(0, {}), null)
    }
}

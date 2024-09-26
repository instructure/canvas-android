/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.grades

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.NoRippleInteractionSource
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesScreen
import com.instructure.pandautils.utils.DisplayGrade


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GradesScreen(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit
) {
    CanvasTheme {
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
        ) { padding ->
            if (uiState.gradePreferencesUiState.show) {
                GradePreferencesDialog(
                    uiState = uiState,
                    actionHandler = actionHandler
                )
            }

            val pullRefreshState = rememberPullRefreshState(
                refreshing = uiState.isRefreshing,
                onRefresh = {
                    actionHandler(GradesAction.Refresh)
                }
            )
            Box(
                modifier = Modifier
                    .padding(padding)
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    uiState.isError -> {
                        ErrorContent(
                            errorMessage = stringResource(id = R.string.errorLoadingGrades),
                            retryClick = {
                                actionHandler(GradesAction.Refresh)
                            }, modifier = Modifier.fillMaxSize()
                        )
                    }

                    uiState.isLoading -> {
                        Loading(
                            color = Color(uiState.canvasContextColor),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("loading")
                        )
                    }

                    else -> {
                        GradesScreenContent(
                            uiState = uiState,
                            userColor = uiState.canvasContextColor,
                            actionHandler = actionHandler
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = uiState.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("pullRefreshIndicator"),
                    contentColor = Color(uiState.canvasContextColor)
                )
            }
        }
    }
}

@Composable
private fun GradePreferencesDialog(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit
) {
    FullScreenDialog(
        onDismissRequest = {
            actionHandler(GradesAction.HideGradePreferences)
        }
    ) {
        GradePreferencesScreen(
            uiState = uiState.gradePreferencesUiState,
            onPreferenceChangeSaved = {
                actionHandler(GradesAction.GradePreferencesUpdated(it))
                actionHandler(GradesAction.HideGradePreferences)
            },
            navigationActionClick = {
                actionHandler(GradesAction.HideGradePreferences)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GradesScreenContent(
    uiState: GradesUiState,
    userColor: Int,
    actionHandler: (GradesAction) -> Unit
) {
    val lazyListState = rememberLazyListState()

    val shouldShowNewText by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .semantics(true) {}
                    .weight(1f),
                shape = RoundedCornerShape(6.dp),
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = shouldShowNewText && uiState.onlyGradedAssignmentsSwitchEnabled,
                        label = "GradeCardTextAnimation",
                        transitionSpec = {
                            if (targetState) {
                                slideInVertically { it } togetherWith slideOutVertically { -it }
                            } else {
                                slideInVertically { -it } togetherWith slideOutVertically { it }
                            }
                        }
                    ) {
                        Text(
                            text = if (it) {
                                stringResource(id = R.string.gradesBasedOnGraded)
                            } else {
                                stringResource(id = R.string.gradesTotal)
                            },
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.textDark),
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )
                    }

                    Text(
                        text = uiState.gradeText.orEmpty(),
                        fontSize = 22.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        actionHandler(GradesAction.ShowGradePreferences)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (uiState.gradePreferencesUiState.isDefault) {
                            R.drawable.ic_filter
                        } else {
                            R.drawable.ic_filter_active
                        }
                    ),
                    contentDescription = stringResource(id = R.string.gradesFilterContentDescription),
                    tint = Color(userColor),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        LazyColumn(
            state = lazyListState
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            actionHandler(GradesAction.OnlyGradedAssignmentsSwitchCheckedChange(!uiState.onlyGradedAssignmentsSwitchEnabled))
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.gradesBasedOnGraded),
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.textDarkest)
                    )
                    Switch(
                        interactionSource = NoRippleInteractionSource(),
                        checked = uiState.onlyGradedAssignmentsSwitchEnabled,
                        onCheckedChange = {
                            actionHandler(GradesAction.OnlyGradedAssignmentsSwitchCheckedChange(it))
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }

            if (uiState.items.isEmpty()) {
                item {
                    EmptyContent(
                        emptyTitle = stringResource(id = R.string.gradesEmptyTitle),
                        emptyMessage = stringResource(id = R.string.gradesEmptyMessage),
                        imageRes = R.drawable.ic_panda_space,
                        modifier = Modifier.fillParentMaxHeight(.8f)
                    )
                }
            } else {
                uiState.items.forEach {
                    stickyHeader {
                        Column(
                            modifier = Modifier
                                .background(colorResource(id = R.color.backgroundLightest))
                                .clickable {
                                    actionHandler(GradesAction.HeaderClick(it.id))
                                }
                        ) {
                            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                            ) {
                                Text(
                                    text = it.name,
                                    color = colorResource(id = R.color.textDark),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_down),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(if (it.expanded) 180f else 0f)
                                )
                            }
                            Divider(color = colorResource(id = R.color.backgroundMedium), thickness = .5.dp)
                        }
                    }

                    if (it.expanded) {
                        items(it.assignments) { assignment ->
                            AssignmentItem(assignment, actionHandler, userColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentItem(
    uiState: AssignmentUiState,
    actionHandler: (GradesAction) -> Unit,
    userColor: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                actionHandler(GradesAction.AssignmentClick(uiState.id))
            }
            .padding(12.dp)
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            painter = painterResource(id = uiState.iconRes),
            contentDescription = null,
            tint = Color(userColor),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column {
            Text(
                text = uiState.name,
                color = colorResource(id = R.color.textDarkest),
                fontSize = 16.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.dueDate.orEmpty(),
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp
                )
                if (uiState.submissionStateLabel != SubmissionStateLabel.NONE) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        Modifier
                            .height(16.dp)
                            .width(1.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(colorResource(id = R.color.borderMedium))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = uiState.submissionStateLabel.iconRes),
                        contentDescription = null,
                        tint = colorResource(id = uiState.submissionStateLabel.colorRes),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = uiState.submissionStateLabel.labelRes),
                        color = colorResource(id = uiState.submissionStateLabel.colorRes),
                        fontSize = 14.sp
                    )
                }
            }
            val gradeText = uiState.displayGrade.text
            if (gradeText.isNotEmpty()) {
                Text(
                    text = gradeText,
                    color = Color(userColor),
                    fontSize = 16.sp,
                    modifier = Modifier.semantics {
                        contentDescription = uiState.displayGrade.contentDescription
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GradesScreenPreview() {
    GradesScreen(
        uiState = GradesUiState(
            isLoading = false,
            items = listOf(
                AssignmentGroupUiState(
                    id = 1,
                    name = "Assignment Group 1",
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 1",
                            dueDate = "Due Date",
                            displayGrade = DisplayGrade("100%", ""),
                            submissionStateLabel = SubmissionStateLabel.NOT_SUBMITTED
                        ),
                        AssignmentUiState(
                            id = 2,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 2",
                            dueDate = "Due Date",
                            displayGrade = DisplayGrade("Complete", ""),
                            submissionStateLabel = SubmissionStateLabel.GRADED
                        )
                    ),
                    expanded = true
                ),
            )
        ),
        actionHandler = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun AssignmentItem1Preview() {
    AssignmentItem(
        uiState = AssignmentUiState(
            id = 1,
            iconRes = R.drawable.ic_assignment,
            name = "Assignment 1",
            dueDate = "Due Date",
            displayGrade = DisplayGrade("100%", ""),
            submissionStateLabel = SubmissionStateLabel.LATE
        ),
        actionHandler = {},
        userColor = android.graphics.Color.RED
    )
}

@Preview(showBackground = true)
@Composable
private fun GradesScreenEmptyPreview() {
    GradesScreen(
        uiState = GradesUiState(
            isLoading = false,
            items = emptyList()
        ),
        actionHandler = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun GradesScreenErrorPreview() {
    GradesScreen(
        uiState = GradesUiState(
            isLoading = false,
            isError = true
        ),
        actionHandler = {}
    )
}
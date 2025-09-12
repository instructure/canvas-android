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

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.NoRippleInteractionSource
import com.instructure.pandautils.compose.composables.CanvasSwitch
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.GroupHeader
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesScreen
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.announceAccessibilityText
import com.instructure.pandautils.utils.drawableId
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GradesScreen(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit
) {
    CanvasTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val localCoroutineScope = rememberCoroutineScope()
        uiState.snackbarMessage?.let {
            LaunchedEffect(Unit) {
                localCoroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(it)
                    if (result == SnackbarResult.Dismissed) {
                        actionHandler(GradesAction.SnackbarDismissed)
                    }
                }
            }
        }
        Scaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.testTag("snackbarHost")) },
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
                    actionHandler(GradesAction.Refresh())
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
                                actionHandler(GradesAction.Refresh())
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
        val context = LocalContext.current
        val gradePreferencesUpdatedAnnouncement = stringResource(R.string.a11y_gradesFilterUpdatedAnnouncement)
        GradePreferencesScreen(
            uiState = uiState.gradePreferencesUiState,
            onPreferenceChangeSaved = { gradingPeriod, sortBy ->
                actionHandler(GradesAction.GradePreferencesUpdated(gradingPeriod, sortBy))
                actionHandler(GradesAction.HideGradePreferences)
                announceAccessibilityText(context, gradePreferencesUpdatedAnnouncement)
            },
            navigationActionClick = {
                actionHandler(GradesAction.HideGradePreferences)
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
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

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Column {
        if (isPortrait) {
            GradesCard(
                uiState = uiState,
                userColor = userColor,
                shouldShowNewText = shouldShowNewText,
                actionHandler = actionHandler
            )
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.testTag("gradesList"),
            contentPadding = PaddingValues(bottom = 64.dp)
        ) {
            item {
                if (!isPortrait) {
                    GradesCard(
                        uiState = uiState,
                        userColor = userColor,
                        shouldShowNewText = false,
                        actionHandler = actionHandler
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
                        .toggleable(
                            value = uiState.onlyGradedAssignmentsSwitchEnabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            actionHandler(GradesAction.OnlyGradedAssignmentsSwitchCheckedChange(!uiState.onlyGradedAssignmentsSwitchEnabled))
                        }
                        .semantics {
                            role = Role.Switch
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.gradesBasedOnGraded),
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.textDarkest),
                        modifier = Modifier.testTag("basedOnGradedAssignmentsLabel")
                    )
                    CanvasSwitch(
                        interactionSource = NoRippleInteractionSource(),
                        checked = uiState.onlyGradedAssignmentsSwitchEnabled,
                        onCheckedChange = {
                            actionHandler(GradesAction.OnlyGradedAssignmentsSwitchCheckedChange(it))
                        },
                        color = Color(uiState.canvasContextColor),
                        modifier = Modifier
                            .height(24.dp)
                            .semantics {
                                invisibleToUser()
                            }
                    )
                }

                if (uiState.items.isEmpty()) {
                    EmptyContent()
                }
            }

            uiState.items.forEach {
                stickyHeader {
                    GroupHeader(
                        name = it.name,
                        expanded = it.expanded,
                        onClick = {
                            actionHandler(GradesAction.GroupHeaderClick(it.id))
                        }
                    )
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

@Composable
private fun GradesCard(
    uiState: GradesUiState,
    userColor: Int,
    shouldShowNewText: Boolean,
    actionHandler: (GradesAction) -> Unit
) {
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
            backgroundColor = colorResource(id = R.color.backgroundLightestElevated),
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
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .testTag("gradesCardText")
                    )
                }

                if (uiState.isGradeLocked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock_lined),
                        contentDescription = stringResource(id = R.string.gradeLockedContentDescription),
                        tint = colorResource(id = R.color.textDarkest),
                        modifier = Modifier
                            .size(24.dp)
                            .semantics {
                                drawableId = R.drawable.ic_lock_lined
                            }
                    )
                } else {
                    Text(
                        text = uiState.gradeText,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Right,
                        color = colorResource(id = R.color.textDarkest),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .selectable(!uiState.gradePreferencesUiState.isDefault) {
                    actionHandler(GradesAction.ShowGradePreferences)
                }
                .semantics {
                    role = Role.Button
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
}

@Composable
private fun EmptyContent() {
    EmptyContent(
        emptyTitle = stringResource(id = R.string.gradesEmptyTitle),
        emptyMessage = stringResource(id = R.string.gradesEmptyMessage),
        imageRes = R.drawable.ic_panda_space,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssignmentItem(
    uiState: AssignmentUiState,
    actionHandler: (GradesAction) -> Unit,
    userColor: Int,
    modifier: Modifier = Modifier
) {
    val iconRotation by animateFloatAsState(
        targetValue = if (uiState.checkpointsExpanded) 180f else 0f,
        label = "expandedIconRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                actionHandler(GradesAction.AssignmentClick(uiState.id))
            }
            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp)
            .semantics {
                role = Role.Button
                testTag = "assignmentItem"
            }
    ) {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                painter = painterResource(id = uiState.iconRes),
                contentDescription = null,
                tint = Color(userColor),
                modifier = Modifier
                    .size(24.dp)
                    .semantics {
                        drawableId = uiState.iconRes
                    }
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = uiState.name,
                    color = colorResource(id = R.color.textDarkest),
                    fontSize = 16.sp
                )
                if (uiState.checkpoints.isNotEmpty()) {
                    uiState.checkpoints.forEach {
                        Text(
                            text = it.dueDate,
                            color = colorResource(id = R.color.textDark),
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("assignmentDueDate")
                        )
                    }
                    SubmissionState(uiState.submissionStateLabel, "submissionStateLabel")
                } else {
                    FlowRow {
                        Text(
                            text = uiState.dueDate,
                            color = colorResource(id = R.color.textDark),
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("assignmentDueDate")
                        )
                        if (uiState.submissionStateLabel != SubmissionStateLabel.None) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                Modifier
                                    .height(16.dp)
                                    .width(1.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(colorResource(id = R.color.borderMedium))
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            SubmissionState(uiState.submissionStateLabel, "submissionStateLabel")
                        }
                    }
                }
                val gradeText = uiState.displayGrade.text
                if (gradeText.isNotEmpty()) {
                    Text(
                        text = gradeText,
                        color = Color(userColor),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .semantics {
                                contentDescription = uiState.displayGrade.contentDescription
                            }
                            .testTag("gradeText")
                    )
                }
                AnimatedVisibility(visible = uiState.checkpointsExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        uiState.checkpoints.forEach {
                            CheckpointItem(it, userColor)
                        }
                    }
                }
            }
        }
        if (uiState.checkpoints.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .requiredSize(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        actionHandler(GradesAction.ToggleCheckpointsExpanded(uiState.id))
                    }
                    .semantics {
                        testTag = "expandDiscussionCheckpoint"
                        role = Role.Button
                    }
            ) {
                val expandButtonContentDescription = stringResource(
                    if (uiState.checkpointsExpanded) {
                        R.string.content_description_collapse_content_with_param
                    } else {
                        R.string.content_description_expand_content_with_param
                    },
                    stringResource(R.string.a11y_discussion_checkpoints)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    tint = colorResource(id = R.color.textDarkest),
                    contentDescription = expandButtonContentDescription,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(iconRotation)
                )
            }
        }
    }
}

@Composable
private fun SubmissionState(submissionStateLabel: SubmissionStateLabel, testTag: String) {
    if (submissionStateLabel != SubmissionStateLabel.None) {
        Row {
            Icon(
                painter = painterResource(id = submissionStateLabel.iconRes),
                contentDescription = null,
                tint = colorResource(id = submissionStateLabel.colorRes),
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterVertically)
                    .semantics {
                        drawableId = submissionStateLabel.iconRes
                    }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (submissionStateLabel) {
                    is SubmissionStateLabel.Predefined -> stringResource(id = submissionStateLabel.labelRes)
                    is SubmissionStateLabel.Custom -> submissionStateLabel.label
                },
                color = colorResource(id = submissionStateLabel.colorRes),
                fontSize = 14.sp,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckpointItem(
    discussionCheckpointUiState: DiscussionCheckpointUiState,
    userColor: Int
) {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics(true) {}
    ) {
        Text(
            text = discussionCheckpointUiState.name,
            color = colorResource(id = R.color.textDarkest),
            fontSize = 16.sp
        )
        FlowRow {
            Text(
                text = discussionCheckpointUiState.dueDate,
                color = colorResource(id = R.color.textDark),
                fontSize = 14.sp,
                modifier = Modifier.testTag("checkpointDueDate")
            )
            if (discussionCheckpointUiState.submissionStateLabel != SubmissionStateLabel.None) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    Modifier
                        .height(16.dp)
                        .width(1.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(colorResource(id = R.color.borderMedium))
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                SubmissionState(discussionCheckpointUiState.submissionStateLabel, "checkpointSubmissionStateLabel")
            }
        }
        val gradeText = discussionCheckpointUiState.displayGrade.text
        if (gradeText.isNotEmpty()) {
            Text(
                text = gradeText,
                color = Color(userColor),
                fontSize = 16.sp,
                modifier = Modifier
                    .semantics {
                        contentDescription = discussionCheckpointUiState.displayGrade.contentDescription
                    }
                    .testTag("checkpointGradeText")
            )
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
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted
                        ),
                        AssignmentUiState(
                            id = 2,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 2",
                            dueDate = "Due Date",
                            displayGrade = DisplayGrade("Complete", ""),
                            submissionStateLabel = SubmissionStateLabel.Graded
                        )
                    ),
                    expanded = true
                )
            ),
            gradeText = "96% A"
        ),
        actionHandler = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun AssignmentItemPreview() {
    AssignmentItem(
        uiState = AssignmentUiState(
            id = 1,
            iconRes = R.drawable.ic_assignment,
            name = "Assignment 1",
            dueDate = "Due Date",
            displayGrade = DisplayGrade("100%", ""),
            submissionStateLabel = SubmissionStateLabel.Late
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
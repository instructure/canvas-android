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
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.NoRippleInteractionSource
import com.instructure.pandautils.compose.composables.CanvasSwitch
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.GroupHeader
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesScreen
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.announceAccessibilityText
import com.instructure.pandautils.utils.drawableId
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GradesScreen(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit,
    canvasContextColor: Int,
    appBarUiState: AppBarUiState? = null,
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
            topBar = {
                appBarUiState?.let {
                    CanvasThemedAppBar(
                        title = it.title,
                        subtitle = it.subtitle,
                        navigationActionClick = it.navigationActionClick,
                        backgroundColor = Color(color = canvasContextColor),
                        actions = {
                            FilterIcon(uiState, actionHandler, ThemePrefs.primaryTextColor)
                        }
                    )
                }
            }
        ) { padding ->
            if (uiState.gradePreferencesUiState.show) {
                GradePreferencesDialog(
                    uiState = uiState,
                    actionHandler = actionHandler,
                    canvasContextColor = canvasContextColor
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
                            color = Color(color = canvasContextColor),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("loading")
                        )
                    }

                    else -> {
                        GradesScreenContent(
                            uiState = uiState,
                            contextColor = canvasContextColor,
                            actionHandler = actionHandler,
                            canvasContextColor = canvasContextColor,
                            showFilterIconOnGradesCard = appBarUiState == null
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = uiState.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("pullRefreshIndicator"),
                    contentColor = Color(color = canvasContextColor)
                )
            }
        }
    }
}

@Composable
private fun GradePreferencesDialog(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit,
    canvasContextColor: Int
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
            },
            canvasContextColor = canvasContextColor
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun GradesScreenContent(
    uiState: GradesUiState,
    contextColor: Int,
    actionHandler: (GradesAction) -> Unit,
    showFilterIconOnGradesCard: Boolean,
    canvasContextColor: Int
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
                contextColor = contextColor,
                shouldShowNewText = shouldShowNewText,
                actionHandler = actionHandler,
                showFilterIcon = showFilterIconOnGradesCard
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
                        contextColor = contextColor,
                        shouldShowNewText = false,
                        actionHandler = actionHandler,
                        showFilterIcon = showFilterIconOnGradesCard
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
                        color = Color(color = canvasContextColor),
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
                        AssignmentItem(assignment, actionHandler, contextColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun GradesCard(
    uiState: GradesUiState,
    contextColor: Int,
    shouldShowNewText: Boolean,
    actionHandler: (GradesAction) -> Unit,
    showFilterIcon: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 16.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = if (showFilterIcon) 0.dp else 16.dp
            )
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

        if (showFilterIcon) {
            FilterIcon(uiState, actionHandler, contextColor)
        }
    }
}

@Composable
private fun FilterIcon(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit,
    tint: Int
) {
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
            tint = Color(color = tint),
            modifier = Modifier.size(24.dp)
        )
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                actionHandler(GradesAction.AssignmentClick(uiState.id))
            }
            .padding(12.dp)
            .semantics {
                role = Role.Button
                testTag = "assignmentItem"
            }
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            painter = painterResource(id = uiState.iconRes),
            contentDescription = null,
            tint = Color(color = userColor),
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
            FlowRow {
                Text(
                    text = uiState.dueDate,
                    color = colorResource(id = R.color.textDark),
                    fontSize = 14.sp,
                    modifier = modifier.testTag("assignmentName")
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
                    Icon(
                        painter = painterResource(id = uiState.submissionStateLabel.iconRes),
                        contentDescription = null,
                        tint = colorResource(id = uiState.submissionStateLabel.colorRes),
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.CenterVertically)
                            .semantics {
                                drawableId = uiState.submissionStateLabel.iconRes
                            }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (uiState.submissionStateLabel) {
                            is SubmissionStateLabel.Predefined -> stringResource(id = uiState.submissionStateLabel.labelRes)
                            is SubmissionStateLabel.Custom -> uiState.submissionStateLabel.label
                        },
                        color = colorResource(id = uiState.submissionStateLabel.colorRes),
                        fontSize = 14.sp
                    )
                }
            }
            val gradeText = uiState.displayGrade.text
            if (gradeText.isNotEmpty()) {
                Text(
                    text = gradeText,
                    color = Color(color = userColor),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .semantics {
                            contentDescription = uiState.displayGrade.contentDescription
                        }
                        .testTag("gradeText")
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
        actionHandler = {},
        canvasContextColor = android.graphics.Color.RED
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
            submissionStateLabel = SubmissionStateLabel.Late
        ),
        actionHandler = {},
        userColor = android.graphics.Color.RED
    )
}

@Preview(showBackground = true)
@Composable
private fun GradesScreenEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    GradesScreen(
        uiState = GradesUiState(
            isLoading = false,
            items = emptyList()
        ),
        actionHandler = {},
        canvasContextColor = android.graphics.Color.RED
    )
}

@Preview(showBackground = true)
@Composable
private fun GradesScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    GradesScreen(
        uiState = GradesUiState(
            isLoading = false,
            isError = true
        ),
        actionHandler = {},
        canvasContextColor = android.graphics.Color.RED
    )
}
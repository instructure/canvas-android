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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
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
import com.instructure.pandautils.compose.composables.CheckpointItem
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.GroupHeader
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SearchBar
import com.instructure.pandautils.compose.composables.SubmissionState
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesScreen
import com.instructure.pandautils.utils.DisplayGrade
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
                        contentColor = colorResource(id = R.color.textLightest),
                        actions = {
                            FilterIcon(uiState, actionHandler, colorResource(id = R.color.textLightest))
                            if (it.bookmarkable) {
                                var overflowMenuExpanded by remember { mutableStateOf(false) }
                                OverflowMenu(
                                    modifier = Modifier.background(color = colorResource(id = R.color.backgroundLightestElevated)),
                                    showMenu = overflowMenuExpanded,
                                    onDismissRequest = {
                                        overflowMenuExpanded = !overflowMenuExpanded
                                    },
                                    iconColor = colorResource(R.color.textLightest)
                                ) {
                                    DropdownMenuItem(
                                        modifier = Modifier.background(
                                            color = colorResource(id = R.color.backgroundLightestElevated)
                                        ),
                                        onClick = {
                                            overflowMenuExpanded = !overflowMenuExpanded
                                            it.addBookmarkClick()
                                        },
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.addBookmark),
                                            color = colorResource(id = R.color.textDarkest)
                                        )
                                    }
                                }
                            }
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

            uiState.whatIfScoreDialogData?.let { dialogData ->
                WhatIfScoreDialog(
                    dialogData = dialogData,
                    onDismiss = {
                        actionHandler(GradesAction.HideWhatIfScoreDialog)
                    },
                    onConfirm = { score ->
                        actionHandler(GradesAction.UpdateWhatIfScore(dialogData.assignmentId, score))
                    },
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
                                hideFromAccessibility()
                            }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
                        .toggleable(
                            value = uiState.showWhatIfScore,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            actionHandler(GradesAction.ShowWhatIfScoreSwitchCheckedChange(!uiState.showWhatIfScore))
                        }
                        .semantics {
                            role = Role.Switch
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.showWhatIfScore),
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.textDarkest),
                        modifier = Modifier.testTag("showWhatIfScoreLabel")
                    )
                    CanvasSwitch(
                        interactionSource = NoRippleInteractionSource(),
                        checked = uiState.showWhatIfScore,
                        onCheckedChange = {
                            actionHandler(GradesAction.ShowWhatIfScoreSwitchCheckedChange(it))
                        },
                        color = Color(color = canvasContextColor),
                        modifier = Modifier
                            .height(24.dp)
                            .semantics {
                                hideFromAccessibility()
                            }
                    )
                }

                AnimatedVisibility(
                    visible = uiState.isSearchExpanded,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    val focusRequester = remember { FocusRequester() }

                    LaunchedEffect(uiState.isSearchExpanded) {
                        if (uiState.isSearchExpanded) {
                            focusRequester.requestFocus()
                        }
                    }

                    SearchBar(
                        icon = R.drawable.ic_search_white_24dp,
                        searchQuery = uiState.searchQuery,
                        tintColor = colorResource(R.color.textDarkest),
                        placeholder = stringResource(R.string.search),
                        collapsable = false,
                        onSearch = {
                            actionHandler(GradesAction.SearchQueryChanged(it))
                        },
                        onClear = {
                            actionHandler(GradesAction.SearchQueryChanged(""))
                        },
                        onQueryChange = {
                            actionHandler(GradesAction.SearchQueryChanged(it))
                        },
                        modifier = Modifier
                            .testTag("searchField")
                            .focusRequester(focusRequester)
                    )
                }

                if (uiState.items.isEmpty()) {
                    if (uiState.searchQuery.length >= 3) {
                        EmptySearchContent()
                    } else {
                        EmptyContent()
                    }
                }
            }

            uiState.items.forEach { item ->
                stickyHeader {
                    GroupHeader(
                        name = item.name,
                        expanded = item.expanded,
                        onClick = {
                            actionHandler(GradesAction.GroupHeaderClick(item.id))
                        }
                    )
                }

                items(
                    items = item.assignments,
                    key = { assignment -> assignment.id }
                ) { assignment ->
                    AnimatedVisibility(
                        visible = item.expanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        AssignmentItem(assignment, actionHandler, contextColor, uiState.showWhatIfScore)
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
            backgroundColor = if (uiState.showWhatIfScore) {
                Color(color = contextColor)
            } else {
                colorResource(id = R.color.backgroundLightestElevated)
            },
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val whatIf = uiState.showWhatIfScore
                val onlyGraded = uiState.onlyGradedAssignmentsSwitchEnabled
                AnimatedContent(
                    targetState = shouldShowNewText && (onlyGraded || whatIf),
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
                        text = when {
                            !it -> stringResource(id = R.string.gradesTotal)
                            whatIf && onlyGraded -> stringResource(id = R.string.gradesBasedOnGradedAndWhatIf)
                            whatIf -> stringResource(id = R.string.whatIfScoreLabel)
                            onlyGraded -> stringResource(id = R.string.gradesBasedOnGraded)
                            else -> stringResource(id = R.string.gradesTotal)
                        },
                        fontSize = 14.sp,
                        color = if (uiState.showWhatIfScore) {
                            colorResource(id = R.color.textLightest)
                        } else {
                            colorResource(id = R.color.textDark)
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .testTag("gradesCardText")
                    )
                }

                if (uiState.isGradeLocked) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock_lined),
                        contentDescription = stringResource(id = R.string.gradeLockedContentDescription),
                        tint = if (uiState.showWhatIfScore) {
                            colorResource(id = R.color.textLightest)
                        } else {
                            colorResource(id = R.color.textDarkest)
                        },
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
                        color = if (uiState.showWhatIfScore) {
                            colorResource(id = R.color.textLightest)
                        } else {
                            colorResource(id = R.color.textDarkest)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        if (showFilterIcon) {
            FilterIcon(uiState, actionHandler, Color(color = contextColor))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        actionHandler(GradesAction.ToggleSearch)
                    }
                    .semantics {
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_white_24dp),
                    contentDescription = stringResource(id = R.string.search),
                    tint = Color(color = contextColor),
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("searchIcon")
                )
            }
        }
    }
}

@Composable
private fun FilterIcon(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit,
    tint: Color
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
            tint = tint,
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

@Composable
private fun EmptySearchContent() {
    EmptyContent(
        emptyTitle = stringResource(id = R.string.noMatchingAssignments),
        emptyMessage = stringResource(id = R.string.noMatchingAssignmentsDescription),
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
    contextColor: Int,
    showWhatIfScore: Boolean,
    modifier: Modifier = Modifier
) {
    val hasWhatIfScore = showWhatIfScore && uiState.whatIfScore != null

    val iconRotation by animateFloatAsState(
        targetValue = if (uiState.checkpointsExpanded) 180f else 0f,
        label = "expandedIconRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (hasWhatIfScore) {
                    Color(color = contextColor)
                } else {
                    Color.Transparent
                }
            )
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
                tint = if (hasWhatIfScore) {
                    colorResource(id = R.color.textDarkest)
                } else {
                    Color(contextColor)
                },
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
                    color = if (hasWhatIfScore) {
                        colorResource(id = R.color.textDarkest)
                    } else {
                        colorResource(id = R.color.textDarkest)
                    },
                    fontSize = 16.sp
                )
                if (uiState.checkpoints.isNotEmpty()) {
                    uiState.checkpoints.forEach {
                        Text(
                            text = it.dueDate,
                            color = if (hasWhatIfScore) {
                                colorResource(id = R.color.textDarkest)
                            } else {
                                colorResource(id = R.color.textDark)
                            },
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("assignmentDueDate")
                        )
                    }
                    SubmissionState(
                        submissionStateLabel = uiState.submissionStateLabel,
                        testTag = "submissionStateLabel",
                        colorOverride = if (hasWhatIfScore) R.color.textDarkest else null
                    )
                } else {
                    FlowRow {
                        Text(
                            text = uiState.dueDate,
                            color = if (hasWhatIfScore) {
                                colorResource(id = R.color.textDarkest)
                            } else {
                                colorResource(id = R.color.textDark)
                            },
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
                            SubmissionState(
                                submissionStateLabel = uiState.submissionStateLabel,
                                testTag = "submissionStateLabel",
                                colorOverride = if (hasWhatIfScore) R.color.textDarkest else null
                            )
                        }
                    }
                }
                val gradeText = uiState.displayGrade.text
                if (gradeText.isNotEmpty()) {
                    Text(
                        text = gradeText,
                        color = if (hasWhatIfScore) {
                            colorResource(id = R.color.textDarkest)
                        } else {
                            Color(contextColor)
                        },
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
                            CheckpointItem(
                                discussionCheckpointUiState = it,
                                contextColor = Color(contextColor),
                                colorOverride = if (hasWhatIfScore) R.color.textDarkest else null
                            )
                        }
                    }
                }
            }
        }
        if (showWhatIfScore) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .requiredSize(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        actionHandler(GradesAction.ShowWhatIfScoreDialog(uiState.id))
                    }
                    .semantics {
                        testTag = "editWhatIfScore"
                        role = Role.Button
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    tint = if (hasWhatIfScore) {
                        colorResource(id = R.color.textDarkest)
                    } else {
                        colorResource(id = R.color.textDarkest)
                    },
                    contentDescription = stringResource(id = R.string.editWhatIfScore),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (uiState.checkpoints.isNotEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.Top)
                    .requiredSize(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        actionHandler(GradesAction.ToggleCheckpointsExpanded(uiState.id))
                    }
                    .semantics {
                        testTag = "expandDiscussionCheckpoints"
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
                    tint = if (hasWhatIfScore) {
                        colorResource(id = R.color.textDarkest)
                    } else {
                        colorResource(id = R.color.textDarkest)
                    },
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
private fun WhatIfScoreDialog(
    dialogData: WhatIfScoreDialogData,
    onDismiss: () -> Unit,
    onConfirm: (Double?) -> Unit,
    canvasContextColor: Int
) {
    var whatIfScoreText by remember { mutableStateOf(dialogData.whatIfScore?.toString().orEmpty()) }

    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.editWhatIfScoreTitle),
                color = colorResource(id = R.color.textDarkest)
            )
        },
        text = {
            Column {
                if (dialogData.currentScoreText.isNotEmpty()) {
                    Text(
                        text = dialogData.currentScoreText,
                        color = colorResource(id = R.color.textDark),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                OutlinedTextField(
                    value = whatIfScoreText,
                    onValueChange = { whatIfScoreText = it },
                    label = {
                        Text(
                            text = stringResource(id = R.string.whatIfScoreLabel),
                            color = colorResource(id = R.color.textDark)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = colorResource(id = R.color.textDarkest),
                        focusedBorderColor = Color(canvasContextColor),
                        unfocusedBorderColor = colorResource(id = R.color.borderMedium),
                        cursorColor = Color(canvasContextColor),
                        focusedLabelColor = Color(canvasContextColor)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("whatIfScoreInput")
                )
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(whatIfScoreText.toDoubleOrNull())
                },
                modifier = Modifier.testTag("doneButton")
            ) {
                Text(
                    text = stringResource(id = R.string.done),
                    color = Color(canvasContextColor)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancelButton")
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    color = Color(canvasContextColor)
                )
            }
        },
        backgroundColor = colorResource(R.color.backgroundLightestElevated)
    )
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
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = 0.0
                        ),
                        AssignmentUiState(
                            id = 2,
                            iconRes = R.drawable.ic_assignment,
                            name = "Assignment 2",
                            dueDate = "Due Date",
                            displayGrade = DisplayGrade("Complete", ""),
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            score = 0.0,
                            maxScore = 0.0,
                            whatIfScore = 0.0
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
private fun AssignmentItemPreview() {
    AssignmentItem(
        uiState = AssignmentUiState(
            id = 1,
            iconRes = R.drawable.ic_assignment,
            name = "Assignment 1",
            dueDate = "Due Date",
            displayGrade = DisplayGrade("100%", ""),
            submissionStateLabel = SubmissionStateLabel.Late,
            score = 0.0,
            maxScore = 0.0,
            whatIfScore = 0.0
        ),
        actionHandler = {},
        contextColor = android.graphics.Color.RED,
        showWhatIfScore = false
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
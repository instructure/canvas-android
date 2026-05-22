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
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.instui.compose.text.Text as InstUIText
import com.instructure.instui.token.component.InstUIText as InstUITextTokens
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.grades.components.NGCGradesFilterScreen
import com.instructure.pandautils.compose.NoRippleInteractionSource
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SearchBarLive
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.DSSectionHeader
import com.instructure.pandautils.designsystem.DSSeparator
import com.instructure.pandautils.designsystem.LocalDesignSystem
import com.instructure.pandautils.features.grades.components.GradesAssignmentItem
import com.instructure.pandautils.features.grades.components.GradesCardContent
import com.instructure.pandautils.features.grades.components.GradesToggleRow
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesScreen
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.announceAccessibilityText
import com.instructure.pandautils.utils.drawableId
import com.instructure.pandautils.utils.orDefault
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

private val FadeInAnimation = fadeIn(animationSpec = tween(300))
private val FadeOutAnimation = fadeOut(animationSpec = tween(300))

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GradesScreen(
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit,
    canvasContextColor: Int,
    appBarUiState: AppBarUiState? = null,
    applyInsets: Boolean = true,
    onFilterUpdated: (AssignmentListSelectedFilters) -> Unit = {},
) {
    val isInstUI = LocalDesignSystem.current == DesignSystem.InstUI
    val themeWrapper: @Composable (@Composable () -> Unit) -> Unit = if (isInstUI) {
        { content -> content() }
    } else {
        { content -> CanvasTheme { content() } }
    }

    themeWrapper {
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
        CanvasScaffold(
            backgroundColor = colorResource(id = R.color.backgroundLightest),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.testTag("snackbarHost")) },
            contentWindowInsets = if (applyInsets) WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                .union(WindowInsets.ime) else WindowInsets(0, 0, 0, 0),
            topBar = {
                appBarUiState?.let {
                    CanvasThemedAppBar(
                        title = it.title,
                        subtitle = it.subtitle,
                        navigationActionClick = it.navigationActionClick,
                        navIconRes = if (uiState.isSearchExpanded) null else R.drawable.ic_back_arrow,
                        backgroundColor = Color(color = canvasContextColor),
                        contentColor = colorResource(id = R.color.textLightest),
                        actions = {
                            if (!uiState.isLoading) {
                                if (!uiState.isSearchExpanded) {
                                    FilterIcon(uiState, actionHandler, colorResource(id = R.color.textLightest))
                                }
                                SearchBarLive(
                                    icon = R.drawable.ic_search_white_24dp,
                                    tintColor = colorResource(R.color.backgroundLightest),
                                    placeholder = stringResource(R.string.search),
                                    query = uiState.searchQuery,
                                    queryChanged = {
                                        actionHandler(GradesAction.SearchQueryChanged(it))
                                    },
                                    expanded = uiState.isSearchExpanded,
                                    onExpand = {
                                        actionHandler(GradesAction.SearchQueryChanged(""))
                                        actionHandler(GradesAction.ToggleSearch)
                                    }
                                )
                                if (!uiState.isSearchExpanded && it.bookmarkable) {
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
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
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
                        }
                    )
                }
            }
        ) { padding ->
            if (uiState.gradePreferencesUiState.show) {
                if (uiState.filter != null) {
                    GradesFilterDialog(
                        filter = uiState.filter,
                        canvasContextColor = canvasContextColor,
                        onFilterUpdated = onFilterUpdated,
                        onDismiss = { actionHandler(GradesAction.HideGradePreferences) },
                    )
                } else {
                    GradePreferencesDialog(
                        uiState = uiState,
                        actionHandler = actionHandler,
                        canvasContextColor = canvasContextColor
                    )
                }
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
                            showActionsOnCard = appBarUiState == null,
                            scaffoldPadding = padding,
                            modifier = if (uiState.gradePreferencesUiState.show) Modifier.padding(
                                bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
                            ) else Modifier
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
private fun GradesFilterDialog(
    filter: GradesFilterUiState,
    canvasContextColor: Int,
    onFilterUpdated: (AssignmentListSelectedFilters) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = filter.filterOptions ?: return
    FullScreenDialog(onDismissRequest = onDismiss) {
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
            window.statusBarColor = canvasContextColor
            WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = false
        }
        NGCGradesFilterScreen(
            courseName = filter.courseName,
            contextColor = Color(canvasContextColor),
            assignmentFilterOptions = options.assignmentFilters,
            assignmentStatusFilterOptions = options.assignmentStatusFilters,
            assignmentGroupByOptions = options.groupByOptions,
            gradingPeriodOptions = options.gradingPeriodOptions,
            selectedOptions = filter.selectedFilters,
            onFilterChange = onFilterUpdated,
            onBackPressed = onDismiss,
        )
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
    showActionsOnCard: Boolean,
    canvasContextColor: Int,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(0.dp),
) {
    val lazyListState = rememberLazyListState()

    val shouldShowNewText by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    Column(modifier = modifier.fillMaxSize()) {
        if (isPortrait) {
            SearchCardTransition(
                uiState = uiState,
                contextColor = contextColor,
                shouldShowNewText = shouldShowNewText,
                actionHandler = actionHandler,
                showActionsOnCard = showActionsOnCard
            )
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.testTag("gradesList"),
            contentPadding = scaffoldPadding
        ) {
            item {
                if (!isPortrait) {
                    SearchCardTransition(
                        uiState = uiState,
                        contextColor = contextColor,
                        shouldShowNewText = false,
                        actionHandler = actionHandler,
                        showActionsOnCard = showActionsOnCard
                    )
                }

                GradesToggleRow(
                    label = stringResource(id = R.string.gradesBasedOnGraded),
                    checked = uiState.onlyGradedAssignmentsSwitchEnabled,
                    onCheckedChange = {
                        actionHandler(GradesAction.OnlyGradedAssignmentsSwitchCheckedChange(it))
                    },
                    contextColor = Color(color = canvasContextColor),
                    testTagLabel = "basedOnGradedAssignmentsLabel",
                    testTagSwitch = "basedOnGradedAssignmentsSwitch",
                )

                if (uiState.isWhatIfGradingEnabled) {

                    DSSeparator(modifier = Modifier.padding(horizontal = 16.dp))

                    GradesToggleRow(
                        label = stringResource(id = R.string.showWhatIfScore),
                        checked = uiState.showWhatIfScore,
                        onCheckedChange = {
                            actionHandler(GradesAction.ShowWhatIfScoreSwitchCheckedChange(it))
                        },
                        contextColor = Color(color = canvasContextColor),
                        testTagLabel = "showWhatIfScoreLabel",
                        testTagSwitch = "showWhatIfScoreSwitch",
                    )
                }

                if (uiState.gradePreferencesUiState.gradingPeriods.isNotEmpty()) {
                    DSSeparator(modifier = Modifier.padding(horizontal = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InstUIText(
                            text = stringResource(id = R.string.gradePreferencesHeaderGradingPeriod),
                            style = InstUITextTokens.content,
                            color = InstUISemanticColors.Text.base(),
                            modifier = Modifier.testTag("gradingPeriodLabel")
                        )
                        InstUIText(
                            text = uiState.gradePreferencesUiState.selectedGradingPeriod?.title
                                ?: stringResource(id = R.string.allGradingPeriods),
                            style = InstUITextTokens.content,
                            color = InstUISemanticColors.Text.muted(),
                            modifier = Modifier.testTag("gradingPeriodName")
                        )
                    }
                }

                if (uiState.items.isEmpty()) {
                    GradesEmptyContent(
                        titleRes = if (uiState.searchQuery.length >= 3) {
                            R.string.noMatchingAssignments
                        } else {
                            R.string.gradesEmptyTitle
                        },
                        messageRes = if (uiState.searchQuery.length >= 3) {
                            R.string.noMatchingAssignmentsDescription
                        } else {
                            R.string.gradesEmptyMessage
                        }
                    )
                }
            }

            uiState.items.forEach { item ->
                stickyHeader {
                    DSSectionHeader(
                        label = item.name,
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
                        GradesAssignmentItem(assignment, actionHandler, contextColor, uiState.showWhatIfScore)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchCardTransition(
    uiState: GradesUiState,
    contextColor: Int,
    shouldShowNewText: Boolean,
    actionHandler: (GradesAction) -> Unit,
    showActionsOnCard: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = !(showActionsOnCard && uiState.isSearchExpanded),
            enter = FadeInAnimation,
            exit = FadeOutAnimation
        ) {
            GradesCard(
                uiState = uiState,
                contextColor = contextColor,
                shouldShowNewText = shouldShowNewText,
                actionHandler = actionHandler,
                showActionsOnCard = showActionsOnCard
            )
        }

        AnimatedVisibility(
            visible = showActionsOnCard && uiState.isSearchExpanded,
            enter = FadeInAnimation,
            exit = FadeOutAnimation
        ) {
            SearchField(
                uiState = uiState,
                contextColor = contextColor,
                actionHandler = actionHandler
            )
        }
    }
}

@Composable
private fun SearchField(
    uiState: GradesUiState,
    contextColor: Int,
    actionHandler: (GradesAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.backgroundLightest))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            LaunchedEffect(uiState.isSearchExpanded) {
                if (uiState.isSearchExpanded) {
                    focusRequester.requestFocus()
                }
            }

            Card(
                shape = RoundedCornerShape(28.dp),
                backgroundColor = colorResource(R.color.backgroundLightestElevated),
                elevation = 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .testTag("searchField")
                    .border(1.dp, colorResource(R.color.borderMedium), RoundedCornerShape(28.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_white_24dp),
                        contentDescription = null,
                        tint = colorResource(R.color.textDark),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = {
                            actionHandler(GradesAction.SearchQueryChanged(it))
                        },
                        textStyle = TextStyle(
                            color = colorResource(R.color.textDarkest),
                            fontSize = 16.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    ) { innerTextField ->
                        if (uiState.searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search),
                                color = colorResource(R.color.textDark),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }

                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.searchQuery.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color = Color(contextColor))
                                    .clickable {
                                        actionHandler(GradesAction.SearchQueryChanged(""))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_close),
                                    contentDescription = stringResource(R.string.a11y_searchBarClearButton),
                                    tint = colorResource(R.color.backgroundLightest),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.close),
                tint = Color(contextColor),
                modifier = Modifier
                    .size(24.dp)
                    .clickable {
                        actionHandler(GradesAction.ToggleSearch)
                    }
            )
        }
    }
}

@Composable
private fun GradesCard(
    uiState: GradesUiState,
    contextColor: Int,
    shouldShowNewText: Boolean,
    actionHandler: (GradesAction) -> Unit,
    showActionsOnCard: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 16.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = if (showActionsOnCard) 0.dp else 16.dp
            )
    ) {
        GradesCardContent(
            uiState = uiState,
            contextColor = contextColor,
            shouldShowNewText = shouldShowNewText,
            modifier = Modifier.weight(1f),
        )

        if (showActionsOnCard) {
            AnimatedVisibility(
                visible = !uiState.isSearchExpanded,
                enter = FadeInAnimation,
                exit = FadeOutAnimation
            ) {
                FilterIcon(uiState, actionHandler, Color(color = contextColor))
            }
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
                    painter = painterResource(
                        id = if (uiState.isSearchExpanded) R.drawable.ic_close else R.drawable.ic_search_white_24dp
                    ),
                    contentDescription = stringResource(id = if (uiState.isSearchExpanded) R.string.close else R.string.search),
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
private fun GradesEmptyContent(
    titleRes: Int,
    messageRes: Int
) {
    EmptyContent(
        emptyTitle = stringResource(id = titleRes),
        emptyMessage = stringResource(id = messageRes),
        imageRes = R.drawable.ic_panda_space,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp)
    )
}

@Composable
private fun WhatIfScoreDialog(
    dialogData: WhatIfScoreDialogData,
    onDismiss: () -> Unit,
    onConfirm: (Double?) -> Unit,
    canvasContextColor: Int
) {
    var whatIfScoreText by remember { mutableStateOf(dialogData.whatIfScore?.toString().orEmpty()) }
    var warningMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        awaitFrame()
        focusRequester.requestFocus()
    }

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
                    onValueChange = {
                        val newValue = it
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            whatIfScoreText = newValue
                            val score = newValue.toDoubleOrNull()
                            warningMessage = if (score != null && dialogData.maxScore != null && score > dialogData.maxScore) {
                                context.getString(R.string.whatIfScoreExceedsMaximumWarning)
                            } else {
                                null
                            }
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.whatIfScoreLabel),
                            color = colorResource(id = R.color.textDark)
                        )
                    },
                    trailingIcon = {
                        if (whatIfScoreText.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    whatIfScoreText = ""
                                    warningMessage = null
                                },
                                modifier = Modifier.testTag("clearWhatIfScoreButton")
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_reply),
                                    contentDescription = stringResource(id = R.string.clearWhatIfScore),
                                    tint = colorResource(id = R.color.textDark)
                                )
                            }
                        }
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
                        .focusRequester(focusRequester)
                        .testTag("whatIfScoreInput")
                )
                warningMessage?.let { warning ->
                    Text(
                        text = warning,
                        color = colorResource(id = R.color.textWarning),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val score = whatIfScoreText.toDoubleOrNull()
                    onConfirm(score)
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
    GradesAssignmentItem(
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
private fun GradesScreenWhatIfPreview() {
    ContextKeeper.appContext = LocalContext.current
    GradesScreen(
        uiState = GradesUiState(
            isLoading = false,
            isWhatIfGradingEnabled = true,
            showWhatIfScore = true,
            items = listOf(
                AssignmentGroupUiState(
                    id = 1,
                    name = "Assignments",
                    assignments = listOf(
                        AssignmentUiState(
                            id = 1,
                            iconRes = R.drawable.ic_assignment,
                            name = "Essay Assignment",
                            dueDate = "Due Nov 15, 2024 at 11:59 PM",
                            displayGrade = DisplayGrade("85/100", "85 out of 100 points"),
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            score = 85.0,
                            maxScore = 100.0,
                            whatIfScore = 95.0
                        ),
                        AssignmentUiState(
                            id = 2,
                            iconRes = R.drawable.ic_quiz,
                            name = "Midterm Exam",
                            dueDate = "Due Nov 20, 2024 at 2:00 PM",
                            displayGrade = DisplayGrade("72/100", "72 out of 100 points"),
                            submissionStateLabel = SubmissionStateLabel.Graded,
                            score = 72.0,
                            maxScore = 100.0,
                            whatIfScore = null
                        ),
                        AssignmentUiState(
                            id = 3,
                            iconRes = R.drawable.ic_assignment,
                            name = "Final Project",
                            dueDate = "Due Dec 1, 2024 at 11:59 PM",
                            displayGrade = DisplayGrade("-/150", "Not graded, 150 points possible"),
                            submissionStateLabel = SubmissionStateLabel.NotSubmitted,
                            score = null,
                            maxScore = 150.0,
                            whatIfScore = 140.0
                        )
                    ),
                    expanded = true
                )
            ),
            gradeText = "89% B+",
            onlyGradedAssignmentsSwitchEnabled = true
        ),
        actionHandler = {},
        canvasContextColor = 0xFF9C27B0.toInt(),
        appBarUiState = AppBarUiState(title = "Grades", subtitle = "Course Name", {}, false, {})
    )
}

@Preview(showBackground = true)
@Composable
private fun AssignmentItemWhatIfPreview() {
    GradesAssignmentItem(
        uiState = AssignmentUiState(
            id = 1,
            iconRes = R.drawable.ic_assignment,
            name = "Essay Assignment",
            dueDate = "Due Nov 15, 2024 at 11:59 PM",
            displayGrade = DisplayGrade("85/100", "85 out of 100 points"),
            submissionStateLabel = SubmissionStateLabel.Graded,
            score = 85.0,
            maxScore = 100.0,
            whatIfScore = 95.0
        ),
        actionHandler = {},
        contextColor = 0xFF9C27B0.toInt(),
        showWhatIfScore = true
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

@Preview(showBackground = true)
@Composable
private fun WhatIfScoreDialogPreview() {
    WhatIfScoreDialog(
        dialogData = WhatIfScoreDialogData(
            assignmentId = 1,
            assignmentName = "Essay Assignment",
            currentScoreText = "Score 55 out of 100",
            whatIfScore = 75.0,
            maxScore = 100.0
        ),
        onDismiss = {},
        onConfirm = {},
        canvasContextColor = 0xFF2B7ABC.toInt()
    )
}

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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowInsetsControllerCompat
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.instui.compose.container.Tray
import com.instructure.instui.compose.indicator.Icon as InstUIIcon
import com.instructure.instui.compose.input.TextInput
import com.instructure.instui.compose.input.TextInputValidation
import com.instructure.instui.compose.list.SectionHeader
import com.instructure.instui.compose.list.Separator
import com.instructure.instui.compose.navigation.TopBar
import com.instructure.instui.compose.text.Heading
import com.instructure.instui.compose.text.HeadingLevel
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.arrowLeft
import com.instructure.instui.token.icon.listFilter
import com.instructure.instui.token.icon.notebook
import com.instructure.instui.token.icon.search
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.compose.CanvasTheme
import com.instructure.pandautils.compose.composables.CanvasDivider
import com.instructure.pandautils.compose.composables.CanvasScaffold
import com.instructure.pandautils.compose.composables.CanvasThemedAppBar
import com.instructure.pandautils.compose.composables.EmptyContent
import com.instructure.pandautils.compose.composables.ErrorContent
import com.instructure.pandautils.compose.composables.FullScreenDialog
import com.instructure.pandautils.compose.composables.GroupHeader
import com.instructure.pandautils.compose.composables.Loading
import com.instructure.pandautils.compose.composables.OverflowMenu
import com.instructure.pandautils.compose.composables.SearchBarLive
import com.instructure.pandautils.compose.composables.SubmissionStateLabel
import com.instructure.pandautils.designsystem.DesignSystem
import com.instructure.pandautils.designsystem.LocalDesignSystem
import com.instructure.pandautils.features.assignments.list.filter.AssignmentListSelectedFilters
import com.instructure.pandautils.features.grades.components.GradesAssignmentItem
import com.instructure.pandautils.features.grades.components.GradesCardContent
import com.instructure.pandautils.features.grades.components.GradesToggleRow
import com.instructure.pandautils.features.grades.components.NGCGradesFilterScreen
import com.instructure.pandautils.features.grades.gradepreferences.GradePreferencesScreen
import com.instructure.pandautils.utils.DisplayGrade
import com.instructure.pandautils.utils.announceAccessibilityText
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.instructure.instui.compose.text.Text as InstUIText
import com.instructure.instui.token.component.InstUIText as InstUITextTokens

private val FadeInAnimation = fadeIn(animationSpec = tween(300))
private val FadeOutAnimation = fadeOut(animationSpec = tween(300))

// Extra pause after the What-If bottom sheet settles open before focusing the input / raising the
// keyboard, so the keyboard doesn't animate in while the sheet is still visually settling.
private const val WHAT_IF_KEYBOARD_DELAY_MS = 250L

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
                    if (isInstUI) {
                        NGCGradesTopBar(
                            appBar = it,
                            uiState = uiState,
                            actionHandler = actionHandler,
                            canvasContextColor = canvasContextColor,
                        )
                        return@let
                    }
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
                val onDismiss = { actionHandler(GradesAction.HideWhatIfScoreDialog) }
                val onConfirm = { score: Double? ->
                    actionHandler(GradesAction.UpdateWhatIfScore(dialogData.assignmentId, score))
                }
                if (isInstUI) {
                    WhatIfScoreSheet(
                        dialogData = dialogData,
                        onDismiss = onDismiss,
                        onConfirm = onConfirm,
                        canvasContextColor = canvasContextColor,
                    )
                } else {
                    WhatIfScoreDialog(
                        dialogData = dialogData,
                        onDismiss = onDismiss,
                        onConfirm = onConfirm,
                        canvasContextColor = canvasContextColor
                    )
                }
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
private fun NGCGradesTopBar(
    appBar: AppBarUiState,
    uiState: GradesUiState,
    actionHandler: (GradesAction) -> Unit,
    canvasContextColor: Int,
) {
    val contentColor = InstUISemanticColors.Text.onColor()
    TopBar(
        title = if (uiState.isLoading) "" else appBar.title,
        subtitle = if (uiState.isLoading) null else appBar.subtitle,
        containerColor = Color(color = canvasContextColor),
        contentColor = contentColor,
        navigationIcon = {
            if (!uiState.isSearchExpanded) {
                ToolbarIcon(
                    imageVector = InstUIIcons.arrowLeft,
                    contentDescription = stringResource(id = R.string.back),
                    tint = contentColor,
                    onClick = appBar.navigationActionClick,
                )
            }
        },
        actions = {
            if (!uiState.isLoading) {
                if (!uiState.isSearchExpanded) {
                    appBar.onNotebookClick?.let { onNotebookClick ->
                        ToolbarIcon(
                            imageVector = InstUIIcons.notebook,
                            contentDescription = stringResource(id = R.string.gradesNotebookContentDescription),
                            tint = contentColor,
                            onClick = onNotebookClick,
                        )
                    }
                    ToolbarIcon(
                        imageVector = InstUIIcons.listFilter,
                        contentDescription = stringResource(id = R.string.gradesFilterContentDescription),
                        tint = contentColor,
                        onClick = { actionHandler(GradesAction.ShowGradePreferences) },
                    )
                }
                SearchBarLive(
                    iconVector = InstUIIcons.search,
                    tintColor = contentColor,
                    placeholder = stringResource(R.string.search),
                    query = uiState.searchQuery,
                    queryChanged = { actionHandler(GradesAction.SearchQueryChanged(it)) },
                    expanded = uiState.isSearchExpanded,
                    onExpand = {
                        actionHandler(GradesAction.SearchQueryChanged(""))
                        actionHandler(GradesAction.ToggleSearch)
                    }
                )
            }
        },
    )
}

@Composable
private fun ToolbarIcon(
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(InstUILayoutSizes.Size.Interactive.height_lg)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        contentAlignment = Alignment.Center
    ) {
        InstUIIcon(
            imageVector = imageVector,
            tint = tint,
            contentDescription = contentDescription,
        )
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
    if (LocalDesignSystem.current == DesignSystem.InstUI) {
        NGCGradesScreenContent(
            uiState = uiState,
            contextColor = contextColor,
            actionHandler = actionHandler,
            showActionsOnCard = showActionsOnCard,
            canvasContextColor = canvasContextColor,
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    } else {
        LegacyGradesScreenContent(
            uiState = uiState,
            contextColor = contextColor,
            actionHandler = actionHandler,
            showActionsOnCard = showActionsOnCard,
            canvasContextColor = canvasContextColor,
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun LegacyGradesScreenContent(
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

                    CanvasDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    GradesToggleRow(
                        label = stringResource(id = R.string.showWhatIfScore),
                        checked = uiState.showWhatIfScore,
                        onCheckedChange = {
                            actionHandler(GradesAction.ShowWhatIfScoreSwitchCheckedChange(it))
                        },
                        contextColor = Color(color = canvasContextColor),
                        testTagLabel = "showWhatIfScoreLabel",
                        testTagSwitch = "showWhatIfScoreSwitch",
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }

                if (uiState.gradePreferencesUiState.gradingPeriods.isNotEmpty()) {
                    CanvasDivider(modifier = Modifier.padding(horizontal = 16.dp))

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
                    val selectedGradingPeriod = uiState.gradePreferencesUiState.selectedGradingPeriod
                    when {
                        uiState.searchQuery.length >= 3 -> GradesEmptyContent(
                            titleRes = R.string.noMatchingAssignments,
                            messageRes = R.string.noMatchingAssignmentsDescription
                        )

                        selectedGradingPeriod?.title != null -> GradesEmptyContent(
                            titleRes = R.string.gradesEmptyTitle,
                            messageRes = R.string.gradesEmptyMessageForPeriod,
                            messageArg = selectedGradingPeriod.title
                        )

                        else -> GradesEmptyContent(
                            titleRes = R.string.gradesEmptyTitle,
                            messageRes = R.string.gradesEmptyMessage
                        )
                    }
                }
            }

            uiState.items.forEach { item ->
                stickyHeader(key = "group_${item.id}", contentType = "groupHeader") {
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
                    key = { assignment -> assignment.id },
                    contentType = { "assignment" }
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun NGCGradesScreenContent(
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

    Column(
        modifier = modifier
            .fillMaxSize()
            // Protect content from the display cutout AND the side system navigation bar on
            // notched/landscape devices. Horizontal only — the top/bottom insets are already
            // handled by the top bar / scaffold, so including them here would add a gap below
            // the toolbar. safeDrawing covers both the camera cutout and the nav bar edges.
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        SearchCardTransition(
            uiState = uiState,
            contextColor = contextColor,
            shouldShowNewText = shouldShowNewText,
            actionHandler = actionHandler,
            showActionsOnCard = showActionsOnCard
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .testTag("gradesList")
                .imePadding(),
            contentPadding = scaffoldPadding
        ) {
            if (uiState.gradePreferencesUiState.gradingPeriods.isNotEmpty()) {
                item {
                    NGCGradingPeriodRow(uiState)
                }
            }

            item {
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

                    Separator(modifier = Modifier.padding(horizontal = 16.dp))

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
                // Stable key so the header survives item-count changes on expand/collapse; without
                // it Compose recreates the header and the chevron's animateFloatAsState rotation
                // snaps instead of animating (the legacy list keys its header for the same reason).
                stickyHeader(key = "groupHeader_${item.id}") {
                    SectionHeader(
                        label = item.name,
                        expanded = item.expanded,
                        onClick = {
                            actionHandler(GradesAction.GroupHeaderClick(item.id))
                        },
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
private fun NGCGradingPeriodRow(uiState: GradesUiState) {
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
                        cursorBrush = SolidColor(Color(contextColor)),
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
    messageRes: Int,
    messageArg: String? = null
) {
    EmptyContent(
        emptyTitle = stringResource(id = titleRes),
        emptyMessage = if (messageArg != null) {
            stringResource(id = messageRes, messageArg)
        } else {
            stringResource(id = messageRes)
        },
        imageRes = R.drawable.ic_panda_space,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp)
    )
}

private val whatIfScoreInputRegex = Regex("^\\d*\\.?\\d{0,2}$")

/**
 * Result of validating a What-If score input change against [whatIfScoreInputRegex] and the
 * assignment's max score. Returns null when [text] fails the input mask, signalling the caller to
 * ignore the change and keep the previous field value.
 */
internal data class WhatIfScoreInputResult(
    val text: String,
    val score: Double?,
    val isOverMax: Boolean,
)

/**
 * Shared What-If input validation logic used by both the InstUI sheet and the Academic dialog, so
 * the two design-system-specific wrappers apply identical rules while rendering with their own widgets.
 */
internal fun validateWhatIfScoreInput(newValue: String, maxScore: Double?): WhatIfScoreInputResult? {
    if (newValue.isNotEmpty() && !newValue.matches(whatIfScoreInputRegex)) return null
    val score = newValue.toDoubleOrNull()
    val isOverMax = score != null && maxScore != null && score > maxScore
    return WhatIfScoreInputResult(newValue, score, isOverMax)
}

/**
 * InstUI What-If score input body (current-score line + validated field + over-max warning).
 * Uses InstUI text/color/spacing tokens; only the course-colored accents stay app-specific.
 */
@Composable
private fun InstUIWhatIfScoreInput(
    dialogData: WhatIfScoreDialogData,
    focusRequester: FocusRequester,
    onScoreChange: (Double?) -> Unit,
) {
    var whatIfScoreText by remember { mutableStateOf(dialogData.whatIfScore?.toString().orEmpty()) }
    var isOverMax by remember { mutableStateOf(false) }

    if (dialogData.currentScoreText.isNotEmpty()) {
        InstUIText(
            text = dialogData.currentScoreText,
            style = InstUITextTokens.contentSmall,
            color = InstUISemanticColors.Text.muted(),
            modifier = Modifier.padding(bottom = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
        )
    }
    // Reuse the shared InstUI TextInput (same component the URL submission uses); the over-max
    // case surfaces through its Error validation (message + red border) rather than a separate line.
    TextInput(
        value = whatIfScoreText,
        onValueChange = { newValue ->
            validateWhatIfScoreInput(newValue, dialogData.maxScore)?.let { result ->
                whatIfScoreText = result.text
                isOverMax = result.isOverMax
                onScoreChange(result.score)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("whatIfScoreInput"),
        label = stringResource(id = R.string.whatIfScoreLabel),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        validation = if (isOverMax) {
            TextInputValidation.Error(stringResource(id = R.string.whatIfScoreExceedsMaximumWarning))
        } else {
            TextInputValidation.None
        },
    )
}

/**
 * Academic What-If score input body (current-score line + validated field + over-max warning).
 * Keeps the legacy resource-based colors/icon; the Academic look is unchanged by the InstUI split.
 */
@Composable
private fun AcademicWhatIfScoreInput(
    dialogData: WhatIfScoreDialogData,
    canvasContextColor: Int,
    onScoreChange: (Double?) -> Unit,
) {
    var whatIfScoreText by remember { mutableStateOf(dialogData.whatIfScore?.toString().orEmpty()) }
    var isOverMax by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        awaitFrame()
        focusRequester.requestFocus()
    }

    if (dialogData.currentScoreText.isNotEmpty()) {
        Text(
            text = dialogData.currentScoreText,
            color = colorResource(id = R.color.textDark),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
    OutlinedTextField(
        value = whatIfScoreText,
        onValueChange = { newValue ->
            validateWhatIfScoreInput(newValue, dialogData.maxScore)?.let { result ->
                whatIfScoreText = result.text
                isOverMax = result.isOverMax
                onScoreChange(result.score)
            }
        },
        label = {
            Text(
                text = stringResource(id = R.string.whatIfScoreLabel),
                color = colorResource(id = R.color.textDark),
            )
        },
        trailingIcon = {
            if (whatIfScoreText.isNotEmpty()) {
                IconButton(
                    onClick = {
                        whatIfScoreText = ""
                        isOverMax = false
                        onScoreChange(null)
                    },
                    modifier = Modifier.testTag("clearWhatIfScoreButton"),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reply),
                        contentDescription = stringResource(id = R.string.clearWhatIfScore),
                        tint = colorResource(id = R.color.textDark),
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
            focusedLabelColor = Color(canvasContextColor),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("whatIfScoreInput"),
    )
    if (isOverMax) {
        Text(
            text = stringResource(id = R.string.whatIfScoreExceedsMaximumWarning),
            color = colorResource(id = R.color.textWarning),
            fontSize = 12.sp,
            modifier = Modifier
                .padding(top = 4.dp)
                .testTag("whatIfScoreWarning"),
        )
    }
}

/**
 * InstUI What-If wrapper: an InstUI [Tray] hosting [InstUIWhatIfScoreInput] and InstUI text/tokens.
 * Replaces the previous raw `ModalBottomSheet`-based sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhatIfScoreSheet(
    dialogData: WhatIfScoreDialogData,
    onDismiss: () -> Unit,
    onConfirm: (Double?) -> Unit,
    canvasContextColor: Int
) {
    var score by remember { mutableStateOf(dialogData.whatIfScore) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }

    // Only focus (and raise the keyboard for) the input once the tray has fully settled open,
    // instead of immediately on composition — avoids the keyboard racing the sheet's slide-up
    // animation. Wait for the settled Expanded state, then hold an extra beat so the sheet is
    // visibly at rest before the keyboard animates in.
    LaunchedEffect(Unit) {
        snapshotFlow { sheetState.currentValue }.first { it == SheetValue.Expanded }
        delay(WHAT_IF_KEYBOARD_DELAY_MS)
        focusRequester.requestFocus()
    }

    Tray(
        onDismiss = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Protect content from the display cutout and side nav bar on notched/landscape devices.
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                // Keep the Cancel/Done row clear of the keyboard in landscape.
                .imePadding()
                .padding(horizontal = InstUILayoutSizes.Spacing.SpaceXl.spaceXl)
                .padding(bottom = InstUILayoutSizes.Spacing.Space2xl.space2xl),
        ) {
            Heading(
                text = dialogData.assignmentName,
                level = HeadingLevel.H3,
                modifier = Modifier
                    .padding(bottom = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
                    .testTag("whatIfSheetAssignmentName"),
            )
            InstUIWhatIfScoreInput(
                dialogData = dialogData,
                focusRequester = focusRequester,
                onScoreChange = { score = it },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancelButton"),
                ) {
                    InstUIText(text = stringResource(id = R.string.cancel), color = Color(canvasContextColor))
                }
                TextButton(
                    onClick = { onConfirm(score) },
                    modifier = Modifier.testTag("doneButton"),
                ) {
                    InstUIText(text = stringResource(id = R.string.done), color = Color(canvasContextColor))
                }
            }
        }
    }
}

/**
 * Academic What-If wrapper: keeps the legacy `AlertDialog` + resource colors, unchanged in look.
 */
@Composable
private fun WhatIfScoreDialog(
    dialogData: WhatIfScoreDialogData,
    onDismiss: () -> Unit,
    onConfirm: (Double?) -> Unit,
    canvasContextColor: Int
) {
    var score by remember { mutableStateOf(dialogData.whatIfScore) }

    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.editWhatIfScoreTitle),
                color = colorResource(id = R.color.textDarkest)
            )
        },
        text = {
            Column {
                AcademicWhatIfScoreInput(
                    dialogData = dialogData,
                    canvasContextColor = canvasContextColor,
                    onScoreChange = { score = it },
                )
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(score) },
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

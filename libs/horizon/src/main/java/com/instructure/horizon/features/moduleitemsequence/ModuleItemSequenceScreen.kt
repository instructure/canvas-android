/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.instructure.horizon.features.moduleitemsequence

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.SHOULD_REFRESH_DASHBOARD
import com.instructure.horizon.features.moduleitemsequence.content.DummyContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.LockedContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.assignment.AssignmentDetailsScreen
import com.instructure.horizon.features.moduleitemsequence.content.assignment.AssignmentDetailsViewModel
import com.instructure.horizon.features.moduleitemsequence.content.file.FileDetailsContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.file.FileDetailsViewModel
import com.instructure.horizon.features.moduleitemsequence.content.link.ExternalLinkContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.link.ExternalLinkUiState
import com.instructure.horizon.features.moduleitemsequence.content.lti.ExternalToolContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.lti.ExternalToolViewModel
import com.instructure.horizon.features.moduleitemsequence.content.page.PageDetailsContentScreen
import com.instructure.horizon.features.moduleitemsequence.content.page.PageDetailsViewModel
import com.instructure.horizon.features.moduleitemsequence.progress.ProgressScreen
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonIconPosition
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.molecules.SpinnerSize
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.toPx
import kotlin.math.abs

@Composable
fun ModuleItemSequenceScreen(navController: NavHostController, uiState: ModuleItemSequenceUiState) {
    val activity = LocalContext.current.getActivityOrNull()
    if (activity != null) ViewStyler.setStatusBarColor(activity, ThemePrefs.brandColor, true)
    if (uiState.progressScreenState.visible) ProgressScreen(uiState.progressScreenState, uiState.loadingState)
    Scaffold(containerColor = HorizonColors.Surface.institution(), bottomBar = {
        ModuleItemSequenceBottomBar(
            showNextButton = uiState.currentPosition < uiState.items.size - 1,
            showPreviousButton = uiState.currentPosition > 0,
            showNotebookButton = uiState.currentItem?.moduleItemContent is ModuleItemContent.Page,
            showAssignmentToolsButton = uiState.currentItem?.moduleItemContent is ModuleItemContent.Assignment,
            onNextClick = uiState.onNextClick,
            onPreviousClick = uiState.onPreviousClick,
            onAssignmentToolsClick = uiState.onAssignmentToolsClick,
        )
    }) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            ModuleItemSequenceContent(uiState = uiState, navController = navController, onBackPressed = {
                navController.popBackStack()
            })
            val markAsDoneState = uiState.currentItem?.markAsDoneUiState
            if (markAsDoneState != null && !uiState.currentItem.isLoading) {
                MarkAsDoneButton(markAsDoneState)
            }
        }
    }
}

@Composable
private fun BoxScope.MarkAsDoneButton(markAsDoneState: MarkAsDoneUiState, modifier: Modifier = Modifier) {
    Box(
        modifier
            .align(Alignment.BottomEnd)
            .padding(end = 24.dp, bottom = 16.dp)
            .background(color = HorizonColors.Surface.pagePrimary(), shape = HorizonCornerRadius.level6)
            .animateContentSize()
    ) {
        if (markAsDoneState.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(color = HorizonColors.Surface.pagePrimary(), shape = HorizonCornerRadius.level6)
            ) {
                Spinner(
                    size = SpinnerSize.EXTRA_SMALL,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 22.dp, vertical = 10.dp),
                )
            }
        } else {
            Button(
                label = stringResource(id = if (markAsDoneState.isDone) R.string.modulePager_done else R.string.modulePager_markAsDone),
                color = ButtonColor.Ghost,
                onClick = if (markAsDoneState.isDone) markAsDoneState.onMarkAsNotDoneClick else markAsDoneState.onMarkAsDoneClick,
                iconPosition = ButtonIconPosition.Start(
                    iconRes = if (markAsDoneState.isDone) R.drawable.check_box else R.drawable.check_box_outline_blank,
                )
            )
        }
    }
}

@Composable
private fun ModuleItemSequenceContent(
    uiState: ModuleItemSequenceUiState,
    navController: NavHostController,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentScrollState = rememberScrollState()
    var isCollapsedByScroll by remember { mutableStateOf(false) }
    var isCollapsed by remember { mutableStateOf(false) }

    LaunchedEffect(contentScrollState) {
        snapshotFlow { contentScrollState.value }
            .collect { offset ->
                if (offset > 100.toPx) {
                    if (!isCollapsedByScroll) {
                        isCollapsedByScroll = true
                        isCollapsed = true
                    }
                } else {
                    isCollapsedByScroll = false
                    isCollapsed = false
                }
            }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier.animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing,
                    delayMillis = if (contentScrollState.value > 0) 0 else 200,
                )
            )
        ) {
            ModuleHeaderContainer(
                uiState = uiState,
                modifier = Modifier
                    .conditional(isCollapsedByScroll) {
                        clickable {
                            isCollapsed = !isCollapsed
                        }
                    }
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
                    .then(
                        if (isCollapsed) Modifier.height(0.dp) else Modifier.wrapContentHeight()
                    ),
                onBackPressed = onBackPressed,
                buttonsEnabled = !isCollapsed
            )
        }
        LoadingStateWrapper(
            loadingState = uiState.loadingState,
            containerColor = Color.Transparent,
            modifier = Modifier.conditional(uiState.loadingState.isLoading || uiState.loadingState.isError) {
                background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level5)
            }
        ) {
            if (uiState.currentPosition != -1) {
                val homeEntry =
                    remember(navController.currentBackStackEntry) { navController.getBackStackEntry(MainNavigationRoute.Home.route) }
                LaunchedEffect(Unit) {
                    homeEntry.savedStateHandle[SHOULD_REFRESH_DASHBOARD] = true
                }

                val pagerState = rememberPagerState(initialPage = uiState.currentPosition, pageCount = { uiState.items.size })
                LaunchedEffect(key1 = uiState.currentPosition) {
                    isCollapsedByScroll = false
                    isCollapsed = false
                    contentScrollState.scrollTo(0)
                    if (abs(uiState.currentPosition - pagerState.currentPage) > 1) {
                        pagerState.scrollToPage(uiState.currentPosition)
                    } else {
                        pagerState.animateScrollToPage(uiState.currentPosition)
                    }
                }

                ModuleItemPager(pagerState = pagerState) { page ->
                    val moduleItemUiState = uiState.items[page]
                    ModuleItemContentScreen(
                        moduleItemUiState,
                        scrollState = contentScrollState,
                        uiState.openAssignmentTools,
                        uiState.assignmentToolsOpened
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleHeaderContainer(
    uiState: ModuleItemSequenceUiState,
    onBackPressed: () -> Unit,
    buttonsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            if (buttonsEnabled) {
                IconButton(iconRes = R.drawable.arrow_back, color = IconButtonColor.INSTITUTION, onClick = onBackPressed)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.currentItem?.moduleName.orEmpty(),
                    style = HorizonTypography.p3,
                    color = HorizonColors.Text.surfaceColored(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                HorizonSpace(SpaceSize.SPACE_4)
                Text(
                    text = uiState.currentItem?.moduleItemName.orEmpty(),
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Text.surfaceColored(),
                    textAlign = TextAlign.Center
                )
            }
            if (buttonsEnabled) {
                IconButton(
                    iconRes = R.drawable.list_alt,
                    color = IconButtonColor.INSTITUTION,
                    onClick = uiState.onProgressClick
                )
            }
        }
        if (!uiState.currentItem?.detailTags.isNullOrEmpty()) {
            HorizonSpace(SpaceSize.SPACE_24)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val separatedFlowRowItems = uiState.currentItem?.detailTags.orEmpty().flatMapIndexed { index, item ->
                    if (index < uiState.currentItem?.detailTags?.lastIndex.orDefault()) listOf(item, "|") else listOf(item)
                }
                separatedFlowRowItems.forEach {
                    Text(text = it, style = HorizonTypography.p2, color = HorizonColors.Text.surfaceColored())
                }
            }
        }
        if (uiState.currentItem?.pillText != null) {
            HorizonSpace(SpaceSize.SPACE_12)
            Pill(label = uiState.currentItem.pillText, type = PillType.INVERSE, case = PillCase.TITLE)
        }
    }
}

@Composable
private fun ModuleItemPager(pagerState: PagerState, modifier: Modifier = Modifier, content: @Composable ColumnScope.(Int) -> Unit = { }) {
    HorizontalPager(
        pageSpacing = 32.dp,
        state = pagerState,
        beyondViewportPageCount = 0,
        pageSize = PageSize.Fill,
        modifier = modifier,
        userScrollEnabled = false
    ) { page ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level5)
        ) {
            content(page)
        }
    }
}

@Composable
private fun ModuleItemContentScreen(
    moduleItemUiState: ModuleItemUiState,
    scrollState: ScrollState,
    openAssignmentTools: Boolean,
    assignmentToolsOpened: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (moduleItemUiState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level5),
            contentAlignment = Alignment.Center
        ) {
            Spinner()
        }
    } else {
        // TODO We only need this condition and the DummyContentScreen until we have all the content implemented.
        if (moduleItemUiState.moduleItemContent is ModuleItemContent.Locked ||
            moduleItemUiState.moduleItemContent is ModuleItemContent.Page ||
            moduleItemUiState.moduleItemContent is ModuleItemContent.ExternalLink ||
            moduleItemUiState.moduleItemContent is ModuleItemContent.ExternalTool ||
            moduleItemUiState.moduleItemContent is ModuleItemContent.File ||
            moduleItemUiState.moduleItemContent is ModuleItemContent.Assignment
        ) {
            NavHost(rememberNavController(), startDestination = moduleItemUiState.moduleItemContent.routeWithArgs, modifier = modifier) {
                composable(
                    route = ModuleItemContent.Assignment.ROUTE, arguments = listOf(
                        navArgument(Const.COURSE_ID) { type = NavType.LongType },
                        navArgument(ModuleItemContent.Assignment.ASSIGNMENT_ID) { type = NavType.LongType }
                    )) {
                    val viewModel = hiltViewModel<AssignmentDetailsViewModel>()
                    val uiState by viewModel.uiState.collectAsState()
                    LaunchedEffect(openAssignmentTools) {
                        if (openAssignmentTools) {
                            viewModel.openAssignmentTools()
                            assignmentToolsOpened()
                        }
                    }
                    AssignmentDetailsScreen(
                        uiState = uiState,
                        scrollState = scrollState
                    )
                }
                composable(
                    route = ModuleItemContent.Page.ROUTE, arguments = listOf(
                        navArgument(Const.COURSE_ID) { type = NavType.LongType },
                        navArgument(ModuleItemContent.Page.PAGE_URL) { type = NavType.StringType }
                    )) {
                    val viewModel = hiltViewModel<PageDetailsViewModel>()
                    val uiState by viewModel.uiState.collectAsState()
                    PageDetailsContentScreen(
                        uiState = uiState,
                        scrollState = scrollState
                    )
                }
                composable(
                    route = ModuleItemContent.ExternalLink.ROUTE, arguments = listOf(
                        navArgument(ModuleItemContent.ExternalLink.TITLE) { type = NavType.StringType },
                        navArgument(ModuleItemContent.ExternalLink.URL) { type = NavType.StringType }
                    )) {
                    val title = Uri.decode(it.arguments?.getString(ModuleItemContent.ExternalLink.TITLE).orEmpty())
                    val url = Uri.decode(it.arguments?.getString(ModuleItemContent.ExternalLink.URL).orEmpty())
                    val uiState = ExternalLinkUiState(title, url)
                    ExternalLinkContentScreen(uiState)
                }
                composable(
                    ModuleItemContent.File.ROUTE, arguments = listOf(
                        navArgument(Const.COURSE_ID) { type = NavType.LongType },
                        navArgument(ModuleItemContent.File.FILE_URL) { type = NavType.StringType },
                        navArgument(Const.MODULE_ITEM_ID) { type = NavType.LongType },
                        navArgument(Const.MODULE_ID) { type = NavType.LongType }
                    )) {
                    val viewModel = hiltViewModel<FileDetailsViewModel>()
                    val uiState by viewModel.uiState.collectAsState()
                    FileDetailsContentScreen(
                        uiState = uiState,
                        modifier = modifier
                    )
                }
                composable(
                    route = ModuleItemContent.ExternalTool.ROUTE, arguments = listOf(
                        navArgument(Const.COURSE_ID) { type = NavType.LongType },
                        navArgument(ModuleItemContent.ExternalTool.URL) { type = NavType.StringType },
                        navArgument(ModuleItemContent.ExternalTool.EXTERNAL_URL) { type = NavType.StringType }
                    )) {
                    val viewModel = hiltViewModel<ExternalToolViewModel>()
                    val uiState by viewModel.uiState.collectAsState()
                    ExternalToolContentScreen(uiState = uiState)
                }
                composable(
                    ModuleItemContent.Locked.ROUTE, arguments = listOf(
                        navArgument(ModuleItemContent.Locked.LOCK_EXPLANATION) { type = NavType.StringType }
                    )) {
                    val lockExplanation = Uri.decode(it.arguments?.getString(ModuleItemContent.Locked.LOCK_EXPLANATION).orEmpty())
                    LockedContentScreen(
                        lockExplanation = lockExplanation,
                        scrollState = scrollState
                    )
                }
            }
        } else {
            DummyContentScreen(
                moduleItemName = moduleItemUiState.moduleItemName,
                moduleItemType = moduleItemUiState.moduleItemContent!!::class.simpleName.orEmpty(),
                scrollState = scrollState
            )
        }
    }
}

@Composable
private fun ModuleItemSequenceBottomBar(
    showNextButton: Boolean,
    showPreviousButton: Boolean,
    showNotebookButton: Boolean,
    showAssignmentToolsButton: Boolean,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onAssignmentToolsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(shadowElevation = HorizonElevation.level4, color = HorizonColors.Surface.pagePrimary()) {
        Row(modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            if (showPreviousButton) IconButton(
                iconRes = R.drawable.chevron_left,
                color = IconButtonColor.INVERSE,
                elevation = HorizonElevation.level4,
                onClick = onPreviousClick
            )
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                IconButton(iconRes = R.drawable.ai, color = IconButtonColor.AI, elevation = HorizonElevation.level4)
                if (showNotebookButton) IconButton(
                    iconRes = R.drawable.menu_book_notebook,
                    color = IconButtonColor.INVERSE,
                    elevation = HorizonElevation.level4
                )
                if (showAssignmentToolsButton) IconButton(
                    iconRes = R.drawable.more_vert,
                    color = IconButtonColor.INVERSE,
                    elevation = HorizonElevation.level4,
                    onClick = onAssignmentToolsClick
                )
            }
            if (showNextButton) IconButton(
                iconRes = R.drawable.chevron_right,
                color = IconButtonColor.INVERSE,
                elevation = HorizonElevation.level4,
                onClick = onNextClick
            )
        }
    }
}

@Composable
@Preview
private fun ModuleItemSequenceScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    ModuleItemSequenceScreen(
        navController = rememberNavController(),
        uiState = ModuleItemSequenceUiState(
            items = listOf(
                ModuleItemUiState(
                    moduleName = "Module Name",
                    moduleItemName = "Module Item Name. Make this at least two lines long",
                    moduleItemId = 1L,
                    detailTags = listOf("XX Mins", "Due XX/XX", "X Points Possible", "Unlimited Attempts Allowed"),
                    pillText = "Pill Text",
                    moduleItemContent = ModuleItemContent.Assignment(courseId = 1, assignmentId = 1L)
                )
            ), currentPosition = 0, currentItem = ModuleItemUiState(
                moduleName = "Module Name",
                moduleItemName = "Module Item Name. Make this at least two lines long",
                moduleItemId = 1L,
                detailTags = listOf("XX Mins", "Due XX/XX", "X Points Possible", "Unlimited Attempts Allowed"),
                pillText = "Pill Text",
                moduleItemContent = ModuleItemContent.Assignment(courseId = 1, assignmentId = 1L)
            )
        )
    )
}

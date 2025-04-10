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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.Pill
import com.instructure.horizon.horizonui.molecules.PillCase
import com.instructure.horizon.horizonui.molecules.PillType
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.orDefault
import kotlin.math.abs

@Composable
fun ModuleItemSequenceScreen(navController: NavHostController, uiState: ModuleItemSequenceUiState) {
    val activity = LocalContext.current.getActivityOrNull()
    if (activity != null) ViewStyler.setStatusBarColor(activity, ThemePrefs.brandColor, true)
    val pagerState = rememberPagerState(initialPage = uiState.currentPosition, pageCount = { uiState.items.size })
    LaunchedEffect(key1 = uiState.currentPosition) {
        if (abs(uiState.currentPosition - pagerState.currentPage) > 1) {
            pagerState.scrollToPage(uiState.currentPosition)
        } else {
            pagerState.animateScrollToPage(uiState.currentPosition)
        }
    }
    Scaffold(containerColor = HorizonColors.Surface.institution(), bottomBar = {
        ModuleItemSequenceBottomBar(
            showNextButton = uiState.currentPosition < uiState.items.size - 1,
            showPreviousButton = uiState.currentPosition > 0,
            onNextClick = uiState.onNextClick,
            onPreviousClick = uiState.onPreviousClick
        )
    }) { contentPadding ->
        ModuleItemSequenceContent(modifier = Modifier.padding(contentPadding), uiState = uiState, pagerState = pagerState, onBackPressed = {
            navController.popBackStack()
        })
    }
}

@Composable
private fun ModuleItemSequenceContent(
    uiState: ModuleItemSequenceUiState,
    pagerState: PagerState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.animateContentSize(
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            ModuleHeaderContainer(
                uiState = uiState,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                onBackPressed = onBackPressed
            )
        }
        LoadingStateWrapper(
            loadingState = uiState.loadingState,
            containerColor = Color.Transparent,
            modifier = Modifier.conditional(uiState.loadingState.isLoading || uiState.loadingState.isError) {
                background(color = HorizonColors.Surface.pageSecondary(), shape = HorizonCornerRadius.level5)
            }
        ) {
            ModuleItemPager(pagerState = pagerState) { page ->
                val moduleItem = uiState.items[page]
                Text(
                    text = "${moduleItem.moduleItemName}\n type: ${moduleItem.moduleItemContent!!::class.simpleName}",
                    style = HorizonTypography.h2,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ModuleHeaderContainer(uiState: ModuleItemSequenceUiState, onBackPressed: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            IconButton(iconRes = R.drawable.arrow_back, color = IconButtonColor.INSTITUTION, onClick = onBackPressed)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.currentItem?.moduleName.orEmpty(),
                    style = HorizonTypography.p3,
                    color = HorizonColors.Text.surfaceColored(),
                    textAlign = TextAlign.Center
                )
                HorizonSpace(SpaceSize.SPACE_4)
                Text(
                    text = uiState.currentItem?.moduleItemName.orEmpty(),
                    style = HorizonTypography.labelLargeBold,
                    color = HorizonColors.Text.surfaceColored(),
                    textAlign = TextAlign.Center
                )
            }
            IconButton(iconRes = R.drawable.list_alt, color = IconButtonColor.INSTITUTION)
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
private fun ModuleItemSequenceBottomBar(
    showNextButton: Boolean,
    showPreviousButton: Boolean,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonModifier = Modifier.shadow(HorizonElevation.level4, shape = CircleShape)
    Surface(shadowElevation = HorizonElevation.level4, color = HorizonColors.Surface.pagePrimary()) {
        Row(modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            if (showPreviousButton) IconButton(
                iconRes = R.drawable.chevron_left,
                color = IconButtonColor.INVERSE,
                modifier = buttonModifier,
                onClick = onPreviousClick
            )
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                IconButton(iconRes = R.drawable.ai, color = IconButtonColor.AI, modifier = buttonModifier)
                IconButton(iconRes = R.drawable.menu_book_notebook, color = IconButtonColor.INVERSE, modifier = buttonModifier)
            }
            if (showNextButton) IconButton(
                iconRes = R.drawable.chevron_right,
                color = IconButtonColor.INVERSE,
                modifier = buttonModifier,
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
                    moduleItemContent = ModuleItemContent.Assignment(assignmentId = 1L)
                )
            ), currentPosition = 0, currentItem = ModuleItemUiState(
                moduleName = "Module Name",
                moduleItemName = "Module Item Name. Make this at least two lines long",
                moduleItemId = 1L,
                detailTags = listOf("XX Mins", "Due XX/XX", "X Points Possible", "Unlimited Attempts Allowed"),
                pillText = "Pill Text",
                moduleItemContent = ModuleItemContent.Assignment(assignmentId = 1L)
            )
        )
    )
}

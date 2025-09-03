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
package com.instructure.horizon.features.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.instructure.canvasapi2.managers.CourseWithProgress
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.course.CourseDetailsScreen
import com.instructure.horizon.features.learn.course.CourseDetailsUiState
import com.instructure.horizon.features.learn.program.ProgramDetailsScreen
import com.instructure.horizon.features.learn.program.ProgramDetailsViewModel
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.inputs.common.InputDropDownPopup
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull
import kotlinx.coroutines.delay

@Composable
fun LearnScreen(state: LearnUiState, mainNavController: NavHostController, homeNavController: NavHostController) {

    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(Unit) {
        if (activity != null) ViewStyler.setStatusBarColor(activity, ContextCompat.getColor(activity, R.color.surface_pagePrimary))
    }

    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.screenState.isError -> ErrorContent(state.screenState.errorMessage.orEmpty())
                state.screenState.isLoading -> LoadingContent()
                else -> if (state.learningItems.isEmpty()) {
                    LearnScreenEmptyContent(state)
                } else {
                    LearnScreenWrapper(state, mainNavController, homeNavController, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(errorText: String) {
    Text(text = errorText, style = HorizonTypography.h3)
}

@Composable
private fun LoadingContent() {
    Spinner()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LearnScreenEmptyContent(state: LearnUiState) {
    LoadingStateWrapper(state.screenState) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.learnEmptyMessage),
                style = HorizonTypography.h3,
                color = HorizonColors.Text.body(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LearnScreenWrapper(
    state: LearnUiState,
    mainNavController: NavHostController,
    homeNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(top = 24.dp)) {
            state.selectedLearningItem?.parentItem?.let { parentItem ->
                val programName = parentItem.title
                val text = stringResource(R.string.learnScreen_partOfProgram, programName)
                val annotatedString = buildAnnotatedString {
                    append(text)
                    val startIndex = text.indexOf(programName)
                    if (startIndex >= 0) {
                        pushStringAnnotation(tag = "PROGRAM_DETAILS", annotation = "PROGRAM_DETAILS")
                        addStyle(
                            style = HorizonTypography.buttonTextLarge.copy(
                                textDecoration = TextDecoration.Underline
                            ).toSpanStyle(),
                            start = startIndex,
                            end = startIndex + (programName.length)
                        )
                        pop()
                    }
                }
                Text(text = annotatedString, style = HorizonTypography.p1, modifier = Modifier.padding(horizontal = 24.dp).clickable {
                    state.onSelectedLearningItemChanged(parentItem)
                })
                HorizonSpace(SpaceSize.SPACE_16)
            }
            DropDownTitle(
                learningItems = state.learningItems,
                selectedItem = state.selectedLearningItem ?: LearningItem.CourseItem(
                    CourseWithProgress(
                        courseId = -1, courseName = "", courseSyllabus = "", progress = 0.0
                    )
                ),
                onSelect = { state.onSelectedLearningItemChanged(it) },
            )
            when {
                (state.selectedLearningItem is LearningItem.CourseItem) -> {
                    CourseDetailsScreen(CourseDetailsUiState(state.selectedLearningItem.courseWithProgress), mainNavController)
                }

                (state.selectedLearningItem is LearningItem.ProgramDetails) -> {
                    val programDetailsViewModel = hiltViewModel<ProgramDetailsViewModel>()
                    LaunchedEffect(state.selectedLearningItem) {
                        programDetailsViewModel.loadProgramDetails(state.selectedLearningItem.program, state.selectedLearningItem.courses)
                    }
                    val programDetailsState by programDetailsViewModel.state.collectAsState()
                    ProgramDetailsScreen(programDetailsState, homeNavController = homeNavController)
                }

                else -> {
                    // No-op, should never happen
                }
            }
        }
    }
}

@Composable
private fun DropDownTitle(learningItems: List<LearningItem>, selectedItem: LearningItem, onSelect: (LearningItem) -> Unit) {
    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp)
    ) {
        val showDropDown = learningItems.size > 1

        val localDensity = LocalDensity.current
        var heightInPx by remember { mutableIntStateOf(0) }
        var width by remember { mutableStateOf(0.dp) }
        var isMenuOpen by remember { mutableStateOf(false) }
        val iconRotation by animateFloatAsState(
            if (isMenuOpen) 180f else 0f,
            label = "DropDownIconRotation"
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    heightInPx = it.size.height
                    width = with(localDensity) { it.size.width.toDp() }
                }
                .clickable(enabled = showDropDown) { isMenuOpen = !isMenuOpen }
        ) {
            AnimatedContent(
                selectedItem,
                label = "SelectedCourseName",
                modifier = Modifier
                    .weight(1f, fill = false)
            ) { selectedItem ->
                Text(
                    text = selectedItem.title,
                    style = HorizonTypography.h3,
                    color = HorizonColors.Text.title(),
                )
            }

            HorizonSpace(SpaceSize.SPACE_8)

            if (showDropDown) {
                Icon(
                    painter = painterResource(R.drawable.keyboard_arrow_down),
                    contentDescription = null,
                    tint = HorizonColors.Icon.default(),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(iconRotation)
                )
            }
        }

        var options by remember { mutableStateOf(learningItems) }

        LaunchedEffect(selectedItem, isMenuOpen) {
            if (!options.contains(selectedItem) && !isMenuOpen) {
                delay(300) // Delay to allow menu to close before changing options
                val newOptions =
                    (learningItems.find { it is LearningItem.ProgramGroupItem && it.items.contains(selectedItem) } as LearningItem.ProgramGroupItem?)?.items
                        ?: learningItems
                options = newOptions
            }
        }

        InputDropDownPopup(
            isMenuOpen = isMenuOpen,
            options = options,
            width = width,
            verticalOffsetPx = heightInPx,
            closeMenuAfterSelection = { it.closeOnClick },
            optionEnabled = { it.clickable },
            onMenuOpenChanged = { isMenuOpen = it },
            onOptionSelected = { selectedItem ->
                when (selectedItem) {
                    is LearningItem.ProgramGroupItem -> options = selectedItem.items
                    is LearningItem.BackToAllItems -> options = learningItems
                    else -> onSelect(options.first { it == selectedItem })
                }
            },
            item = { learningItem ->
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .conditional(learningItem == selectedItem) {
                                background(HorizonColors.Surface.inversePrimary())
                            }
                            .conditional(learningItem is LearningItem.LockedCourseItem) {
                                alpha(0.5f)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        if (learningItem is LearningItem.BackToAllItems) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back),
                                contentDescription = null,
                                tint = HorizonColors.Icon.default(),
                                modifier = Modifier.size(24.dp)
                            )
                            HorizonSpace(SpaceSize.SPACE_8)
                        }
                        val itemTextColor = when (learningItem) {
                            selectedItem -> HorizonColors.Surface.pageSecondary()
                            is LearningItem.ProgramHeaderItem -> HorizonColors.Text.surfaceInverseSecondary()
                            else -> HorizonColors.Text.body()
                        }
                        Text(
                            text = learningItem.titleInDropdown,
                            style = if (learningItem is LearningItem.ProgramHeaderItem) HorizonTypography.labelLargeBold else HorizonTypography.buttonTextLarge,
                            color = itemTextColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (learningItem is LearningItem.LockedCourseItem) {
                            HorizonSpace(SpaceSize.SPACE_8)
                            Icon(
                                painter = painterResource(R.drawable.lock),
                                contentDescription = null,
                                tint = HorizonColors.Icon.default(),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (learningItem is LearningItem.ProgramGroupItem) {
                            HorizonSpace(SpaceSize.SPACE_8)
                            Icon(
                                painter = painterResource(R.drawable.arrow_forward),
                                contentDescription = null,
                                tint = HorizonColors.Icon.default(),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    if (learningItem is LearningItem.ProgramHeaderItem) {
                        HorizonDivider(color = HorizonColors.Surface.divider())
                    }
                }
            },
        )
    }
}

@Composable
@Preview
fun LearnScreenLoadingPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(isLoading = true),
        selectedLearningItem = null
    )
    LearnScreen(state, rememberNavController(), rememberNavController())
}

@Composable
@Preview
fun LearnScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState(isError = true, errorMessage = "Error loading course"),
        selectedLearningItem = null
    )
    LearnScreen(state, rememberNavController(), rememberNavController())
}

@Composable
@Preview
fun LearnScreenEmptyContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = LearnUiState(
        screenState = LoadingState()
    )
    LearnScreen(state, rememberNavController(), rememberNavController())
}

@Composable
@Preview
private fun LearnScreenContentPreview() {
    ContextKeeper.appContext = LocalContext.current
    val course = CourseWithProgress(
        courseId = 123,
        courseName = "Course Name",
        courseSyllabus = "Course Overview",
        progress = 0.5,
    )
    val state = LearnUiState(
        screenState = LoadingState(),
        learningItems = listOf(LearningItem.CourseItem(course)),
        selectedLearningItem = LearningItem.CourseItem(course)
    )
    LearnScreen(state, rememberNavController(), rememberNavController())
}
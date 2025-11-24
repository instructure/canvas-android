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
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.instructure.horizon.features.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NotebookAppBar
import com.instructure.horizon.features.notebook.common.composable.NotebookHighlightedText
import com.instructure.horizon.features.notebook.common.composable.NotebookPill
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.DropdownChip
import com.instructure.horizon.horizonui.molecules.DropdownItem
import com.instructure.horizon.horizonui.molecules.HorizonDivider
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.CollapsableHeaderScreen
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.localisedFormat

@Composable
fun NotebookScreen(
    mainNavController: NavHostController,
    viewModel: NotebookViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(Unit) {
        if (activity != null) ViewStyler.setStatusBarColor(
            activity,
            ContextCompat.getColor(activity, R.color.surface_pagePrimary)
        )
    }

    val scrollState = rememberLazyListState()

    CollapsableHeaderScreen(
        modifier = Modifier.background(HorizonColors.Surface.pagePrimary()),
        headerContent = {
            if (state.showTopBar) {
                NotebookAppBar(
                    navigateBack = { mainNavController.popBackStack() },
                    centeredTitle = true
                )
            }
        },
        bodyContent = {
            LoadingStateWrapper(state.loadingState) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if ((state.showNoteTypeFilter || state.showCourseFilter) && (state.courses.isNotEmpty() || state.selectedCourse != null || state.selectedFilter != null)) {
                        FilterContent(
                            state,
                            scrollState,
                            Modifier
                                .clip(HorizonCornerRadius.level5)
                                .background(HorizonColors.Surface.pageSecondary()),
                        )
                    }

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(HorizonColors.Surface.pageSecondary()),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 2.dp,
                            bottom = 16.dp
                        )
                    ) {
                        if (state.notes.isEmpty()) {
                            item {
                                if (state.selectedCourse != null || state.selectedFilter != null) {
                                    EmptyFilteredContent()
                                } else {
                                    EmptyContent()
                                }
                            }
                        } else {
                            items(state.notes) { note ->
                                Column {
                                    NoteContent(note) {
                                        if (state.navigateToEdit) {
                                            mainNavController.navigate(
                                                NotebookRoute.EditNotebook(
                                                    noteId = note.id,
                                                    highlightedTextStartOffset = note.highlightedText.range.startOffset,
                                                    highlightedTextEndOffset = note.highlightedText.range.endOffset,
                                                    highlightedTextStartContainer = note.highlightedText.range.startContainer,
                                                    highlightedTextEndContainer = note.highlightedText.range.endContainer,
                                                    textSelectionStart = note.highlightedText.textPosition.start,
                                                    textSelectionEnd = note.highlightedText.textPosition.end,
                                                    highlightedText = note.highlightedText.selectedText,
                                                    noteType = note.type.name,
                                                    userComment = note.userText
                                                )
                                            )
                                        } else {
                                            mainNavController.navigate(
                                                MainNavigationRoute.ModuleItemSequence(
                                                    courseId = note.courseId,
                                                    moduleItemAssetType = note.objectType.value,
                                                    moduleItemAssetId = note.objectId,
                                                )
                                            )
                                        }
                                    }

                                    if (state.notes.lastOrNull() != note) {
                                        HorizonSpace(SpaceSize.SPACE_4)
                                    }
                                }
                            }

                            if (state.hasNextPage) {
                                item {
                                    if (state.isLoadingMore) {
                                        LoadingContent()
                                    } else {
                                        Button(
                                            label = stringResource(R.string.showMore),
                                            height = ButtonHeight.SMALL,
                                            width = ButtonWidth.FILL,
                                            color = ButtonColor.WhiteWithOutline,
                                            onClick = { state.loadNextPage() },
                                            modifier = Modifier.padding(vertical = 24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun FilterContent(
    state: NotebookUiState,
    scrollState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val defaultBackgroundColor = HorizonColors.PrimitivesGrey.grey12()
    val importantBgColor = HorizonColors.PrimitivesSea.sea12()
    val confusingBgColor = HorizonColors.PrimitivesRed.red12()

    // Type filter items
    val allNotesItem = DropdownItem(
        value = null as NotebookType?,
        label = context.getString(R.string.notebookTypeAllNotes),
        iconRes = R.drawable.menu,
        iconTint = HorizonColors.Icon.default(),
        backgroundColor = defaultBackgroundColor
    )

    val typeItems = remember {
        listOf(
            allNotesItem,
            DropdownItem(
                value = NotebookType.Important,
                label = context.getString(NotebookType.Important.labelRes),
                iconRes = NotebookType.Important.iconRes,
                iconTint = Color(context.getColor(NotebookType.Important.color)),
                backgroundColor = importantBgColor
            ),
            DropdownItem(
                value = NotebookType.Confusing,
                label = context.getString(NotebookType.Confusing.labelRes),
                iconRes = NotebookType.Confusing.iconRes,
                iconTint = Color(context.getColor(NotebookType.Confusing.color)),
                backgroundColor = confusingBgColor
            )
        )
    }

    // Course filter items
    val allCoursesItem = DropdownItem<CourseWithProgress?>(
        value = null,
        label = context.getString(R.string.notebookFilterCoursePlaceholder),
        iconRes = null,
        iconTint = null,
        backgroundColor = defaultBackgroundColor
    )

    val courseItems = remember(state.courses) {
        listOf(allCoursesItem) + state.courses.map { course ->
            DropdownItem(
                value = course,
                label = course.courseName,
                iconRes = null,
                iconTint = null,
                backgroundColor = defaultBackgroundColor
            )
        }
    }

    val selectedTypeItem =
        if (state.selectedFilter == null) allNotesItem else typeItems.find { it.value == state.selectedFilter }

    val selectedCourseItem =
        if (state.selectedCourse == null) allCoursesItem else courseItems.find { it.value == state.selectedCourse }


        Column {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.showCourseFilter) {
                    DropdownChip(
                        items = courseItems,
                        selectedItem = selectedCourseItem,
                        onItemSelected = { item -> state.onCourseSelected(item?.value) },
                        placeholder = stringResource(R.string.notebookFilterCoursePlaceholder),
                        dropdownWidth = 178.dp,
                        verticalPadding = 6.dp,
                        modifier = Modifier.weight(1f, false)
                    )
                }

                if (state.showNoteTypeFilter) {
                    DropdownChip(
                        items = typeItems,
                        selectedItem = selectedTypeItem,
                        onItemSelected = { item -> state.onFilterSelected(item?.value) },
                        placeholder = stringResource(R.string.notebookFilterTypePlaceholder),
                        dropdownWidth = 178.dp,
                        verticalPadding = 6.dp
                    )
                }
            }

            if (scrollState.canScrollBackward) {
                HorizonDivider()
            }
        }
}

@Composable
private fun LoadingContent() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Spinner(color = HorizonColors.Surface.institution())
    }
}

@Composable
private fun NoteContent(
    note: Note,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizonShadow(
                elevation = HorizonElevation.level4,
                shape = HorizonCornerRadius.level2,
                clip = true
            )
            .background(
                color = HorizonColors.PrimitivesWhite.white10(),
                shape = HorizonCornerRadius.level2,
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = note.updatedAt.localisedFormat("MMM d, yyyy"),
                style = HorizonTypography.labelSmall,
                color = HorizonColors.Text.timestamp()
            )

            HorizonSpace(SpaceSize.SPACE_16)

            NotebookHighlightedText(
                text = note.highlightedText.selectedText,
                type = note.type,
                maxLines = 3,
            )

            HorizonSpace(SpaceSize.SPACE_16)

            if (note.userText.isNotEmpty()) {
                Text(
                    text = note.userText,
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                HorizonSpace(SpaceSize.SPACE_16)
            }

            NotebookPill(note.type)
        }
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.notesEmptyContentTitle),
            style = HorizonTypography.sh2,
            color = HorizonColors.Text.body()
        )
        HorizonSpace(size = SpaceSize.SPACE_8)
        Text(
            text = stringResource(R.string.notesEmptyContentBody),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.dataPoint()
        )
    }
}

@Composable
private fun EmptyFilteredContent(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.notesEmptyFilteredContentTitle),
            style = HorizonTypography.sh2,
            color = HorizonColors.Text.body()
        )
        HorizonSpace(size = SpaceSize.SPACE_8)
        Text(
            text = stringResource(R.string.notesEmptyFilteredContentBody),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.dataPoint()
        )
    }
}

@Composable
private fun ErrorContent(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.notesErrorContentTitle),
            style = HorizonTypography.sh2,
            color = HorizonColors.Text.body()
        )
        HorizonSpace(size = SpaceSize.SPACE_8)
        Text(
            text = stringResource(R.string.notesErrorContentBody),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.dataPoint()
        )
    }
}

@Composable
@Preview
private fun NotebookScreenEmptyPreview() {
    ContextKeeper.appContext = LocalContext.current
    EmptyContent()
}

@Composable
@Preview
private fun NotebookScreenEmptyFilteredPreview() {
    ContextKeeper.appContext = LocalContext.current
    EmptyFilteredContent()
}

@Composable
@Preview
private fun NotebookScreenErrorPreview() {
    ContextKeeper.appContext = LocalContext.current
    ErrorContent()
}

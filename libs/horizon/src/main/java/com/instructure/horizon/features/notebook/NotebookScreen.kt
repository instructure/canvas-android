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
package com.instructure.horizon.features.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NoteDeleteConfirmationDialog
import com.instructure.horizon.features.notebook.common.composable.NotebookAppBar
import com.instructure.horizon.features.notebook.common.composable.NotebookHighlightedText
import com.instructure.horizon.features.notebook.common.composable.NotebookTypeSelect
import com.instructure.horizon.features.notebook.common.composable.toNotebookLocalisedDateFormat
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonBorder
import com.instructure.horizon.horizonui.foundation.horizonBorderShadow
import com.instructure.horizon.horizonui.molecules.Button
import com.instructure.horizon.horizonui.molecules.ButtonColor
import com.instructure.horizon.horizonui.molecules.ButtonHeight
import com.instructure.horizon.horizonui.molecules.ButtonWidth
import com.instructure.horizon.horizonui.molecules.DropdownChip
import com.instructure.horizon.horizonui.molecules.DropdownItem
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.LoadingIconButton
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.horizonui.organisms.scaffolds.CollapsableHeaderScreen
import com.instructure.horizon.horizonui.platform.LoadingStateWrapper
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.horizon.util.plus
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.localisedFormat

@Composable
fun NotebookScreen(
    navController: NavHostController,
    viewModel: NotebookViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    NotebookScreen(
        navController = navController,
        state = state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
    navController: NavHostController,
    state: NotebookUiState
) {
    val scrollState = rememberLazyListState()

    NoteDeleteConfirmationDialog(
        showDialog = state.showDeleteConfirmationForNote != null,
        dismissDialog = { state.updateShowDeleteConfirmation(null) },
        onDeleteSelected = {
            state.deleteNote(state.showDeleteConfirmationForNote)
        }
    )

    CollapsableHeaderScreen(
        statusBarColor = HorizonColors.Surface.pagePrimary(),
        navigationBarColor = HorizonColors.Surface.cardPrimary(),
        modifier = Modifier
            .background(HorizonColors.Surface.pagePrimary()),
        headerContent = { contentPadding ->
            if (state.showTopBar) {
                if (state.showCourseFilter) {
                    NotebookAppBar(
                        navigateBack = { navController.popBackStack() },
                        centeredTitle = true,
                        modifier = Modifier.padding(contentPadding)
                    )
                } else {
                    NotebookAppBar(
                        onClose = { navController.popBackStack() },
                        centeredTitle = false,
                        modifier = Modifier.padding(contentPadding)
                    )
                }

            }
        },
        bodyContent = { contentPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val topPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection),
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(layoutDirection)
            )
            val bottomPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding()
            )

            LoadingStateWrapper(
                state.loadingState,
                modifier = Modifier.padding(topPadding)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if ((state.showNoteTypeFilter || state.showCourseFilter)) {
                        FilterContent(
                            state,
                            scrollState,
                            Modifier
                                .background(HorizonColors.Surface.pagePrimary())
                                .clip(HorizonCornerRadius.level5)
                                .conditional(scrollState.canScrollBackward) {
                                    horizonBorderShadow(
                                        HorizonColors.Surface.inversePrimary(),
                                        bottom = 1.dp,
                                    )
                                }
                                .background(HorizonColors.Surface.pageSecondary())
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = scrollState,
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(HorizonColors.Surface.pageSecondary()),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 2.dp,
                            bottom = 16.dp
                        ) + bottomPadding
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
                                    val courseName = if (state.showCourseFilter) {
                                        state.courses.firstOrNull { it.courseId == note.courseId }?.courseName
                                    } else null
                                    NoteContent(
                                        note,
                                        courseName,
                                        state.deleteLoadingNote,
                                        onDeleteClick = {
                                            state.updateShowDeleteConfirmation(note)
                                        }) {
                                        if (state.navigateToEdit) {
                                            navController.navigate(
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
                                                    userComment = note.userText,
                                                    updatedAt = note.updatedAt.toNotebookLocalisedDateFormat()
                                                )
                                            )
                                        } else {
                                            navController.navigate(
                                                MainNavigationRoute.ModuleItemSequence(
                                                    courseId = note.courseId,
                                                    moduleItemAssetType = note.objectType.value,
                                                    moduleItemAssetId = note.objectId,
                                                    scrollToNoteId = note.id
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

    val selectedCourseItem =
        if (state.selectedCourse == null) allCoursesItem else courseItems.find { it.value == state.selectedCourse }


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
                NotebookTypeSelect(
                    state.selectedFilter,
                    state.onFilterSelected,
                    false,
                    true,
                    Modifier.conditional(!state.showCourseFilter) { // TalkBack hack to fix focus handling
                        semantics(true) {}
                    }
                )
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
    courseName: String?,
    deleteLoading: Note?,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizonBorder(
                colorResource(note.type.highlightColor),
                6.dp,
                1.dp,
                1.dp,
                6.dp,
                16.dp
            )
            .background(
                color = HorizonColors.PrimitivesWhite.white10(),
                shape = HorizonCornerRadius.level2,
            )
            .clip(HorizonCornerRadius.level2)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            val typeName = stringResource(note.type.labelRes)
            NotebookTypeSelect(
                note.type,
                verticalPadding = 2.dp,
                onSelect = {},
                showIcons = true,
                enabled = false,
                showAllOption = false,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = typeName
                }
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

                HorizonSpace(SpaceSize.SPACE_8)
            }

            Row {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ){
                    Text(
                        text = note.updatedAt.localisedFormat("MMM d, yyyy"),
                        style = HorizonTypography.labelMediumBold,
                        color = HorizonColors.Text.timestamp(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (courseName != null) {
                        Text(
                            text = courseName,
                            style = HorizonTypography.labelMediumBold,
                            color = HorizonColors.Text.timestamp(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                LoadingIconButton(
                    iconRes = R.drawable.delete,
                    contentDescription = stringResource(R.string.a11y_notebookDeleteNoteButtonContentDescription),
                    color = IconButtonColor.InverseDanger,
                    size = IconButtonSize.SMALL,
                    onClick = { onDeleteClick() },
                    loading = note == deleteLoading,
                    modifier = Modifier.align(Alignment.Bottom)
                )
            }
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

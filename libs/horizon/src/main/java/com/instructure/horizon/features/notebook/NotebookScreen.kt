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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.managers.NoteHighlightedData
import com.instructure.canvasapi2.managers.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.NoteObjectType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NotebookAppBar
import com.instructure.horizon.features.notebook.common.composable.NotebookHighlightedText
import com.instructure.horizon.features.notebook.common.composable.NotebookPill
import com.instructure.horizon.features.notebook.common.composable.NotebookTypeSelect
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.format
import java.util.Date

@Composable
fun NotebookScreen(
    mainNavController: NavHostController,
    state: NotebookUiState,
) {
    val scrollState = rememberLazyListState()
    Scaffold(
        containerColor = HorizonColors.Surface.pagePrimary(),
        topBar = {
            if (state.showTopBar) {
                NotebookAppBar(
                    navigateBack = { mainNavController.popBackStack() },
                    modifier = Modifier.conditional(scrollState.canScrollBackward) {
                        horizonShadow(
                            elevation = HorizonElevation.level1,
                        )
                    }
                )
            }
        },
    ) { padding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .padding(padding),
            contentPadding = PaddingValues(24.dp)
        ) {
            if (state.showFilters && state.notes.isNotEmpty()) {
                item {
                    FilterContent(
                        state.selectedFilter,
                        state.onFilterSelected
                    )
                }
            }

            if (state.notes.isNotEmpty()){
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.notebookNotesLabel),
                            style = HorizonTypography.labelLargeBold,
                            color = HorizonColors.Text.title()
                        )

                        HorizonSpace(SpaceSize.SPACE_12)
                    }
                }
            }

            if (state.isLoading) {
                item {
                    LoadingContent()
                }
            } else if (state.notes.isEmpty()) {
                item {
                    EmptyContent()
                }
            } else {
                items(state.notes) { note ->
                    Column {
                        NoteContent(note) {
                            mainNavController.navigate(
                                MainNavigationRoute.ModuleItemSequence(
                                    courseId = note.courseId,
                                    moduleItemAssetType = note.objectType.value,
                                    moduleItemAssetId = note.objectId,
                                )
                            )
                        }

                        if (state.notes.lastOrNull() != note) {
                            HorizonSpace(SpaceSize.SPACE_12)
                        }
                    }
                }

                item {
                    Column {
                        HorizonSpace(SpaceSize.SPACE_24)

                        NotesPager(
                            canNavigateBack = state.hasPreviousPage,
                            canNavigateForward = state.hasNextPage,
                            isLoading = state.isLoading,
                            onNavigateBack = state.loadPreviousPage,
                            onNavigateForward = state.loadNextPage
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookBottomDialog(
    courseId: Long,
    objectFilter: Pair<String, String>,
    mainNavController: NavHostController,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel<NotebookViewModel>()
    val state by viewModel.uiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        containerColor = HorizonColors.Surface.pagePrimary(),
        onDismissRequest = { onDismiss() },
        dragHandle = null,
        sheetState = bottomSheetState,
    ) {
        LaunchedEffect(courseId, objectFilter) {
            state.updateContent(courseId, objectFilter)
        }

        NotebookScreen(
            mainNavController = mainNavController,
            state = state
        )

    }
}

@Composable
private fun FilterContent(
    selectedFilter: NotebookType?,
    onFilterSelected: (NotebookType?) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.notebookFilterLabel),
            style = HorizonTypography.labelLargeBold,
            color = HorizonColors.Text.title()
        )

        HorizonSpace(SpaceSize.SPACE_12)

        Row {
            NotebookTypeSelect(
                type = NotebookType.Important,
                isSelected = selectedFilter == NotebookType.Important,
                onSelect = { onFilterSelected(if (selectedFilter == NotebookType.Important) null else NotebookType.Important) },
                modifier = Modifier.weight(1f)
            )

            HorizonSpace(SpaceSize.SPACE_12)

            NotebookTypeSelect(
                type = NotebookType.Confusing,
                isSelected = selectedFilter == NotebookType.Confusing,
                onSelect = { onFilterSelected(if (selectedFilter == NotebookType.Confusing) null else NotebookType.Confusing) },
                modifier = Modifier.weight(1f)
            )
        }

        HorizonSpace(SpaceSize.SPACE_24)
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
                text = note.updatedAt.format("MMM d, yyyy"),
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
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
    ) {
        Text(
            text = stringResource(R.string.notebookEmptyContentMessage),
            style = HorizonTypography.p1,
            color = HorizonColors.Text.body(),
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 36.dp)
        )
    }
}

@Composable
private fun NotesPager(
    canNavigateBack: Boolean,
    canNavigateForward: Boolean,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
) {
    if (canNavigateBack || canNavigateForward) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                iconRes = R.drawable.chevron_left,
                color = IconButtonColor.Black,
                size = IconButtonSize.SMALL,
                onClick = onNavigateBack,
                enabled = canNavigateBack && !isLoading
            )

            HorizonSpace(SpaceSize.SPACE_8)

            IconButton(
                iconRes = R.drawable.chevron_right,
                color = IconButtonColor.Black,
                size = IconButtonSize.SMALL,
                onClick = onNavigateForward,
                enabled = canNavigateForward && !isLoading
            )
        }
    }
}

@Composable
@Preview
private fun NotebookScreenPreview() {
    ContextKeeper.appContext = LocalContext.current
    val state = NotebookUiState(
        isLoading = false,
        showFilters = true,
        showTopBar = true,
        selectedFilter = NotebookType.Important,
        notes = listOf(
            Note(
                id = "1",
                courseId = 123L,
                objectId = "456",
                objectType = NoteObjectType.PAGE,
                userText = "This is a note about an assignment.",
                highlightedText = NoteHighlightedData("Important part of the assignment.", NoteHighlightedDataRange(0, 0, "", ""), NoteHighlightedDataTextPosition(0, 0)),
                updatedAt = Date(),
                type = NotebookType.Important
            ),
            Note(
                id = "2",
                courseId = 123L,
                objectId = "789",
                objectType = NoteObjectType.PAGE,
                userText = "This is a note about another assignment.",
                highlightedText = NoteHighlightedData("Confusing part of the assignment.", NoteHighlightedDataRange(0, 0, "", ""), NoteHighlightedDataTextPosition(0, 0)),
                updatedAt = Date(),
                type = NotebookType.Confusing
            )
        ),
        updateContent = { _, _ -> }
    )

    NotebookScreen(
        mainNavController = NavHostController(LocalContext.current),
        state = state
    )
}
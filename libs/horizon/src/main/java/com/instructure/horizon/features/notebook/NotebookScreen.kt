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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.navigation.NavHostController
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedData
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataRange
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteHighlightedDataTextPosition
import com.instructure.canvasapi2.managers.graphql.horizon.redwood.NoteObjectType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NotebookAppBar
import com.instructure.horizon.features.notebook.common.composable.NotebookHighlightedText
import com.instructure.horizon.features.notebook.common.composable.NotebookPill
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonElevation
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.HorizonTypography
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.horizon.horizonui.foundation.horizonShadow
import com.instructure.horizon.horizonui.molecules.DropdownChip
import com.instructure.horizon.horizonui.molecules.DropdownItem
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.compose.modifiers.conditional
import com.instructure.pandautils.utils.localisedFormat
import java.util.Date

@Composable
fun NotebookScreen(
    mainNavController: NavHostController,
    state: NotebookUiState,
    onDismiss: (() -> Unit)? = null,
    onNoteSelected: ((Note) -> Unit)? = null,
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
                            elevation = HorizonElevation.level2,
                        )
                    }
                )
            } else if (onDismiss != null) {
                NotebookAppBar(
                    onClose = { onDismiss() },
                    modifier = Modifier.conditional(scrollState.canScrollBackward) {
                        horizonShadow(
                            elevation = HorizonElevation.level2,
                        )
                    }
                )
            }
        },
    ) { padding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
                .clip(HorizonCornerRadius.level5)
                .background(HorizonColors.Surface.pageSecondary())
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
            ) {
                if (state.showFilters && state.notes.isNotEmpty()) {
                    item {
                        FilterContent(
                            state.selectedFilter,
                            state.onFilterSelected
                        )
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
                                onNoteSelected?.invoke(note) ?: mainNavController.navigate(
                                    MainNavigationRoute.ModuleItemSequence(
                                        courseId = note.courseId,
                                        moduleItemAssetType = note.objectType.value,
                                        moduleItemAssetId = note.objectId,
                                    )
                                )
                            }

                            if (state.notes.lastOrNull() != note) {
                                HorizonSpace(SpaceSize.SPACE_4)
                            }
                        }
                    }

                    if (state.isLoadingMore) {
                        item {
                            LoadingContent()
                        }
                    }
                }
            }

            // Infinite scroll: Load more when scrolled near bottom
            val shouldLoadMore = remember(state) {
                derivedStateOf {
                    val lastVisibleItem = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()
                    val totalItems = scrollState.layoutInfo.totalItemsCount
                    lastVisibleItem != null &&
                    lastVisibleItem.index >= totalItems - 1 &&
                    state.hasNextPage &&
                    !state.isLoadingMore &&
                    !state.isLoading &&
                    state.notes.isNotEmpty()
                }
            }

            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value) {
                    state.loadNextPage()
                }
            }
        }
    }
}

@Composable
private fun FilterContent(
    selectedFilter: NotebookType?,
    onFilterSelected: (NotebookType?) -> Unit,
) {
    val context = LocalContext.current

    val allNotesItem = DropdownItem(
        value = null as NotebookType?,
        label = context.getString(R.string.notebookTypeAllNotes),
        iconRes = R.drawable.menu,
        iconTint = HorizonColors.Icon.default(),
        backgroundColor = HorizonColors.PrimitivesGrey.grey12()
    )
    val importantBgColor = HorizonColors.PrimitivesSea.sea12()
    val confusingBgColor = HorizonColors.PrimitivesRed.red12()

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

    val selectedTypeItem =
        if (selectedFilter == null) allNotesItem else typeItems.find { it.value == selectedFilter }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /*FilterDropdownChip(
                items = emptyList(),
                selectedItem = null,
                onItemSelected = { },
                placeholder = stringResource(R.string.notebookFilterCoursePlaceholder),
                modifier = Modifier.weight(1f)
            )*/

            DropdownChip(
                items = typeItems,
                selectedItem = selectedTypeItem,
                onItemSelected = { item -> onFilterSelected(item?.value) },
                placeholder = stringResource(R.string.notebookFilterTypePlaceholder),
                modifier = Modifier.weight(1f),
                dropdownWidth = 178.dp,
                verticalPadding = 6.dp
            )
        }

        HorizonSpace(SpaceSize.SPACE_16)
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
                highlightedText = NoteHighlightedData(
                    "Important part of the assignment.",
                    NoteHighlightedDataRange(0, 0, "", ""),
                    NoteHighlightedDataTextPosition(0, 0)
                ),
                updatedAt = Date(),
                type = NotebookType.Important
            ),
            Note(
                id = "2",
                courseId = 123L,
                objectId = "789",
                objectType = NoteObjectType.PAGE,
                userText = "This is a note about another assignment.",
                highlightedText = NoteHighlightedData(
                    "Confusing part of the assignment.",
                    NoteHighlightedDataRange(0, 0, "", ""),
                    NoteHighlightedDataTextPosition(0, 0)
                ),
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
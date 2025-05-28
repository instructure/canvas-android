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
import androidx.compose.material.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.composable.NotebookAppBar
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
import com.instructure.horizon.horizonui.molecules.IconButton
import com.instructure.horizon.horizonui.molecules.IconButtonColor
import com.instructure.horizon.horizonui.molecules.IconButtonSize
import com.instructure.horizon.horizonui.molecules.Spinner
import com.instructure.horizon.navigation.MainNavigationRoute
import com.instructure.pandautils.utils.format

@Composable
fun NotebookScreen(
    mainNavController: NavHostController,
    state: NotebookUiState,
) {
    Scaffold(
        backgroundColor = HorizonColors.Surface.pagePrimary(),
        topBar = { NotebookAppBar(navigateBack = { mainNavController.popBackStack() }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                FilterContent(
                    state.selectedFilter,
                    state.onFilterSelected
                )
            }

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
        Spinner(color = HorizonColors.Surface.inversePrimary())
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
            .shadow(
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
                text = note.updatedAt.format("EEE MM, yyyy"),
                style = HorizonTypography.labelSmall,
                color = HorizonColors.Text.timestamp()
            )

            HorizonSpace(SpaceSize.SPACE_16)

            val lineColor = colorResource(note.type.color)
            Text(
                text = note.highlightedText,
                style = HorizonTypography.p1,
                color = HorizonColors.Text.body(),
                modifier = Modifier
                    .background(colorResource(note.type.color).copy(alpha = 0.2f))
                    .drawBehind {
                        val strokeWidthPx = 1.dp.toPx()
                        val verticalOffset = size.height - 2.sp.toPx()
                        drawLine(
                            color = lineColor,
                            strokeWidth = strokeWidthPx,
                            start = Offset(0f, verticalOffset),
                            end = Offset(size.width, verticalOffset)
                        )
                    }
            )

            HorizonSpace(SpaceSize.SPACE_16)

            if (note.userText.isNotEmpty()) {
                Text(
                    text = note.userText,
                    style = HorizonTypography.p1,
                    color = HorizonColors.Text.body(),
                )

                HorizonSpace(SpaceSize.SPACE_16)
            }

            NotebookPill(note.type)
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
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
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            iconRes = R.drawable.chevron_left,
            color = IconButtonColor.BLACK,
            size = IconButtonSize.SMALL,
            onClick = onNavigateBack,
            enabled = canNavigateBack && !isLoading
        )

        HorizonSpace(SpaceSize.SPACE_8)

        IconButton(
            iconRes = R.drawable.chevron_right,
            color = IconButtonColor.BLACK,
            size = IconButtonSize.SMALL,
            onClick = onNavigateForward,
            enabled = canNavigateForward && !isLoading
        )
    }
}
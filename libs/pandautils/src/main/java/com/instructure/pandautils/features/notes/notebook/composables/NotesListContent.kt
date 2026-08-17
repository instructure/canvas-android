/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.notes.notebook.composables

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.models.NoteType
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.indicator.Icon
import com.instructure.instui.compose.indicator.IconColor
import com.instructure.instui.compose.indicator.IconSize
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.input.Select
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.list.ListItem
import com.instructure.instui.compose.list.ListItemTrailing
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIHeading
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.icon.InstUIIcons
import com.instructure.instui.token.icon.chevronRight
import com.instructure.instui.token.icon.stickyNote
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notes.notebook.NoteTypeFilter
import com.instructure.pandautils.features.notes.notebook.NoteTypePill
import com.instructure.pandautils.features.notes.notebook.NoteUiState
import com.instructure.pandautils.features.notes.notebook.NotebookScreenState
import com.instructure.pandautils.features.notes.notebook.NotebookUiState
import com.instructure.pandautils.features.notes.notebook.accentColor

@Composable
fun NotesListContent(uiState: NotebookUiState, onDoneClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        NotebookTrayHeader(
            title = stringResource(R.string.notebookTitle),
            backLabel = stringResource(R.string.course),
            onBackClick = null,
            onDoneClick = onDoneClick,
            subtitle = uiState.levelTitle.takeIf { it.isNotBlank() }
        )
        FilterRow(
            selectedFilter = uiState.selectedFilter,
            noteCount = uiState.filteredNotes.size,
            onFilterSelected = uiState.actions.onFilterSelected
        )
        // Figma (25808-32140): the list body sits on sharedTokens/background/baseColor, distinct from
        // the muted grey the header/filter row above it inherit from the Tray's own container color.
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(InstUISemanticColors.Background.base())) {
            when {
                uiState.screenState is NotebookScreenState.Loading -> Spinner(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("notebookLoading")
                )

                uiState.screenState is NotebookScreenState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
                ) {
                    Text(
                        text = uiState.screenState.message,
                        style = InstUIText.content,
                        color = InstUIText.mutedColor
                    )
                    TextButton(text = stringResource(R.string.retry), onClick = uiState.actions.onRetryClick)
                }

                uiState.notes.isEmpty() -> NotesListEmptyState(
                    titleRes = R.string.notebookNoNotesTitle,
                    messageRes = R.string.notebookNoNotesMessage
                )

                uiState.filteredNotes.isEmpty() -> NotesListEmptyState(
                    titleRes = R.string.notebookNoFilteredNotesTitle,
                    messageRes = R.string.notebookNoFilteredNotesMessage
                )

                else -> LazyColumn(modifier = Modifier.testTag("notebookNotesList")) {
                    // No dividers between rows - matches the list Figma exactly, which has none
                    // (unlike most other InstUI lists elsewhere in this codebase).
                    itemsIndexed(uiState.filteredNotes) { _, item ->
                        NoteListItem(item = item, onNoteClick = uiState.actions.onNoteClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedFilter: NoteTypeFilter,
    noteCount: Int,
    onFilterSelected: (NoteTypeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = InstUILayoutSizes.Spacing.SpaceMd.spaceMd,
                vertical = InstUILayoutSizes.Spacing.SpaceSm.spaceSm
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Select(
            options = NoteTypeFilter.entries.map { it.optionLabel() },
            selectedIndex = NoteTypeFilter.entries.indexOf(selectedFilter),
            onSelect = { onFilterSelected(NoteTypeFilter.entries[it]) },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = pluralStringResource(R.plurals.notebookNotesCount, noteCount, noteCount),
            style = InstUIText.contentSmall,
            color = InstUIText.mutedColor,
            modifier = Modifier.padding(start = InstUILayoutSizes.Spacing.SpaceSm.spaceSm)
        )
    }
}

@Composable
private fun NoteTypeFilter.optionLabel(): String = when (this) {
    NoteTypeFilter.All -> stringResource(R.string.all)
    NoteTypeFilter.Important -> stringResource(R.string.notebookNoteTypeImportant)
    NoteTypeFilter.Confusing -> stringResource(R.string.notebookNoteTypeUnclear)
}

@Composable
private fun NoteListItem(item: NoteUiState, onNoteClick: (String) -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        onClick = { onNoteClick(item.id) },
        trailing = ListItemTrailing.Icons(icons = listOf(InstUIIcons.chevronRight)),
        trailingAlignment = Alignment.CenterVertically,
        modifier = modifier.testTag("notebookNoteListItem_${item.id}"),
        content = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                NoteTypePill(item.noteType)
                item.dateLabel?.let {
                    Text(text = it, style = InstUIText.contentSmall, color = InstUIText.mutedColor)
                }
            }
            HighlightedExcerptText(text = item.highlightedText, noteType = item.noteType)
            if (item.userText.isNotEmpty()) {
                Text(
                    text = item.userText,
                    style = InstUIText.content,
                    color = InstUISemanticColors.Text.base(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}

/**
 * The highlighted quote excerpt on a list row, capped at 3 lines with no expand affordance (per
 * the list Figma, which - unlike the detail screen's Card - has no "Show all" control). Renders a
 * translucent tint behind the text (approximating Figma's mix-blend-darken highlighter marks,
 * flat alpha compositing is close enough without a real blend mode) colored per note type, so the
 * list visually echoes the WebView highlight color without needing the accent-colored/underlined
 * treatment previously (and incorrectly) shared with the detail level's plain-text Card.
 */
@Composable
private fun HighlightedExcerptText(text: String, noteType: NoteType, modifier: Modifier = Modifier) {
    val highlightTint = noteType.accentColor().copy(alpha = 0.12f)
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(background = highlightTint)) { append(text) }
        },
        style = InstUIText.contentQuote,
        color = InstUISemanticColors.Text.base(),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun NotesListEmptyState(titleRes: Int, messageRes: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(InstUILayoutSizes.Spacing.SpaceXl.spaceXl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = InstUIIcons.stickyNote, size = IconSize.XXLarge, color = IconColor.Muted)
        Column(
            modifier = Modifier.padding(top = InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceXs.spaceXs)
        ) {
            Text(text = stringResource(titleRes), style = InstUIHeading.titleCardRegular, color = InstUISemanticColors.Text.base())
            Text(
                text = stringResource(messageRes),
                style = InstUIText.content,
                color = InstUIText.mutedColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// region Previews

private val previewNotes = listOf(
    NoteUiState(
        id = "1",
        noteType = NoteType.IMPORTANT,
        highlightedText = "Primary sources are original, first-hand accounts of an event or time period, created by people who directly witnessed or participated in it.",
        userText = "This connects to our class discussion on primary sources.",
        dateLabel = "Aug 10, 2026"
    ),
    NoteUiState(
        id = "2",
        noteType = NoteType.CONFUSING,
        highlightedText = "Secondary sources interpret primary sources.",
        userText = "",
        dateLabel = "Aug 9, 2026"
    )
)

@Preview(name = "NotesListContent Content — Light", showBackground = true)
@Preview(name = "NotesListContent Content — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotesListContentContentPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NotesListContent(
                uiState = NotebookUiState(
                    screenState = NotebookScreenState.Content,
                    levelTitle = "Home Page",
                    notes = previewNotes,
                    filteredNotes = previewNotes
                ),
                onDoneClick = {}
            )
        }
    }
}

@Preview(name = "NotesListContent Empty — Light", showBackground = true)
@Preview(name = "NotesListContent Empty — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotesListContentEmptyPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NotesListContent(
                uiState = NotebookUiState(screenState = NotebookScreenState.Content, levelTitle = "Home Page"),
                onDoneClick = {}
            )
        }
    }
}

@Preview(name = "NotesListContent Filter Empty — Light", showBackground = true)
@Preview(name = "NotesListContent Filter Empty — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotesListContentFilterEmptyPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NotesListContent(
                uiState = NotebookUiState(
                    screenState = NotebookScreenState.Content,
                    levelTitle = "Home Page",
                    notes = previewNotes,
                    filteredNotes = emptyList(),
                    selectedFilter = NoteTypeFilter.Important
                ),
                onDoneClick = {}
            )
        }
    }
}

@Preview(name = "NotesListContent Error — Light", showBackground = true)
@Preview(name = "NotesListContent Error — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotesListContentErrorPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NotesListContent(
                uiState = NotebookUiState(
                    screenState = NotebookScreenState.Error("There was a problem loading your notes."),
                    levelTitle = "Home Page"
                ),
                onDoneClick = {}
            )
        }
    }
}

// endregion

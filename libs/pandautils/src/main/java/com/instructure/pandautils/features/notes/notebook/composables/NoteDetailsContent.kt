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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.instructure.canvasapi2.models.NoteType
import com.instructure.instui.compose.InstUITheme
import com.instructure.instui.compose.container.Card
import com.instructure.instui.compose.container.Elevation
import com.instructure.instui.compose.indicator.Spinner
import com.instructure.instui.compose.input.TextArea
import com.instructure.instui.compose.input.TextButton
import com.instructure.instui.compose.text.Text
import com.instructure.instui.token.component.InstUIText
import com.instructure.instui.token.semantic.InstUILayoutSizes
import com.instructure.instui.token.semantic.InstUISemanticColors
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notes.notebook.NoteTypePill
import com.instructure.pandautils.features.notes.notebook.NoteUiState
import com.instructure.pandautils.features.notes.notebook.NotebookScreenState
import com.instructure.pandautils.features.notes.notebook.NotebookUiState

@Composable
fun NoteDetailsContent(
    uiState: NotebookUiState,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val note = uiState.selectedNote
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        NotebookTrayHeader(
            title = stringResource(R.string.notebookNoteTitle),
            backLabel = stringResource(R.string.notebookPageNotesTitle),
            backContentDescription = stringResource(R.string.a11y_notebookBackToNotesList),
            backTestTag = "notebookDetailsBack",
            onBackClick = onBackClick,
            onDoneClick = onDoneClick,
            subtitle = null
        )

        when {
            // Shown while the underlying list level's fetch is still in flight - e.g. right after
            // opening straight into a note (see NotebookViewModel.buildInitialStack), before the note
            // itself has even been fetched, so this level never has to wait behind a list-loading frame.
            uiState.screenState is NotebookScreenState.Loading -> Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Spinner(modifier = Modifier.align(Alignment.Center).testTag("notebookDetailsLoading"))
            }

            uiState.screenState is NotebookScreenState.Error -> Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(
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
            }

            note == null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.notebookNoteNotFound), style = InstUIText.content, color = InstUIText.mutedColor)
            }

            else -> Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(InstUILayoutSizes.Spacing.SpaceLg.spaceLg),
                verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    NoteTypePill(note.noteType)
                    note.dateLabel?.let {
                        Text(text = it, style = InstUIText.contentSmall, color = InstUIText.mutedColor)
                    }
                }

                Card(
                    backgroundColor = InstUISemanticColors.Background.container(),
                    elevation = Elevation.Level1,
                    contentPadding = InstUILayoutSizes.Spacing.SpaceLg.spaceLg
                ) {
                    ExpandableQuote(text = note.highlightedText, collapsedMaxLines = 3)
                }

                // Deliberately editable but NOT wired to any save/persist call - a stopgap ahead of the
                // real edit feature. Local-only state; edits are discarded when this level is left.
                var draftText by rememberSaveable(note.id) { mutableStateOf(note.userText) }
                TextArea(
                    value = draftText,
                    onValueChange = { draftText = it },
                    placeholder = stringResource(R.string.notebookYourNote),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("noteTextArea")
                )
            }
        }
    }
}

// region Previews

private val previewNote = NoteUiState(
    id = "1",
    noteType = NoteType.IMPORTANT,
    highlightedText = "Primary sources are original, first-hand accounts of an event or time period, created by people who directly witnessed or participated in it.",
    userText = "This connects to our class discussion on primary sources.",
    dateLabel = "Aug 10, 2026"
)

@Preview(name = "NoteDetailsContent — Light", showBackground = true)
@Preview(name = "NoteDetailsContent — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoteDetailsContentPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NoteDetailsContent(
                uiState = NotebookUiState(screenState = NotebookScreenState.Content, selectedNote = previewNote),
                onBackClick = {},
                onDoneClick = {}
            )
        }
    }
}

@Preview(name = "NoteDetailsContent Short — Light", showBackground = true)
@Preview(name = "NoteDetailsContent Short — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoteDetailsContentShortPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NoteDetailsContent(
                uiState = NotebookUiState(
                    screenState = NotebookScreenState.Content,
                    selectedNote = previewNote.copy(highlightedText = "Short quote.", userText = "", dateLabel = null)
                ),
                onBackClick = {},
                onDoneClick = {}
            )
        }
    }
}

@Preview(name = "NoteDetailsContent Loading — Light", showBackground = true)
@Preview(name = "NoteDetailsContent Loading — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoteDetailsContentLoadingPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NoteDetailsContent(
                uiState = NotebookUiState(screenState = NotebookScreenState.Loading),
                onBackClick = {},
                onDoneClick = {}
            )
        }
    }
}

@Preview(name = "NoteDetailsContent Not Found — Light", showBackground = true)
@Preview(name = "NoteDetailsContent Not Found — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NoteDetailsContentNotFoundPreview() {
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NoteDetailsContent(
                uiState = NotebookUiState(screenState = NotebookScreenState.Content, selectedNote = null),
                onBackClick = {},
                onDoneClick = {}
            )
        }
    }
}

// endregion

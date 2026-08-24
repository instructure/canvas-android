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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
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
import com.instructure.pandautils.features.notes.notebook.NotebookLevel
import com.instructure.pandautils.features.notes.notebook.NotebookListItem
import com.instructure.pandautils.features.notes.notebook.NotebookScreenState
import com.instructure.pandautils.features.notes.notebook.NotebookUiState
import com.instructure.pandautils.features.notes.notebook.accentColor
import com.instructure.pandautils.utils.getFragmentActivityOrNull

@Composable
fun NotesListContent(uiState: NotebookUiState, onDoneClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().fillMaxHeight()) {
        NotebookTrayHeader(
            title = stringResource(R.string.notebookTitle),
            backLabel = uiState.currentLevel.backLabelOrNull(),
            backContentDescription = uiState.currentLevel.backContentDescriptionOrNull(),
            onBackClick = if (uiState.currentLevel is NotebookLevel.GlobalNotes) null else uiState.actions.onBackClick,
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
                    // (unlike most other InstUI lists elsewhere in this codebase). uiState.items is a
                    // flat NotebookListItem list: a Page level is all NoteRows (unchanged rendering), a
                    // Course/Global level interleaves SectionHeaders with each group's NoteRows inline
                    // underneath - never a collapsed summary, per Figma.
                    itemsIndexed(uiState.items) { _, item ->
                        when (item) {
                            is NotebookListItem.SectionHeader -> SectionHeaderRow(
                                item = item,
                                onClick = uiState.actions.onSectionHeaderClick
                            )

                            is NotebookListItem.NoteRow -> NoteListItem(
                                item = item.note,
                                onNoteClick = uiState.actions.onNoteClick,
                                onNoteArrowClick = uiState.actions.onNoteArrowClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/** [NotebookLevel]-static back-control label, per the plan: [NotebookLevel.GlobalNotes] (top of the
 * hierarchy) has none, [NotebookLevel.CourseNotes] goes up to Global, [NotebookLevel.PageNotes] goes up
 * to its Course - the same "Course" label the placeholder always rendered, now actually wired. */
@Composable
private fun NotebookLevel.backLabelOrNull(): String? = when (this) {
    is NotebookLevel.GlobalNotes -> null
    is NotebookLevel.CourseNotes -> stringResource(R.string.notebookGlobalNotesBackLabel)
    is NotebookLevel.PageNotes -> stringResource(R.string.course)
    is NotebookLevel.NoteDetails -> null // NotesListContent is never shown for this level.
}

/** Content description for the same back control [backLabelOrNull] labels visually - without this,
 * [NotebookTrayHeader] falls back to announcing the raw [backLabelOrNull] text (e.g. "Global, button"),
 * which reads as a destination name rather than a back action. Mirrors [backLabelOrNull]'s per-level
 * mapping so the two never drift apart. */
@Composable
private fun NotebookLevel.backContentDescriptionOrNull(): String? = when (this) {
    is NotebookLevel.GlobalNotes -> null
    is NotebookLevel.CourseNotes -> stringResource(R.string.a11y_notebookBackToGlobalNotes)
    is NotebookLevel.PageNotes -> stringResource(R.string.a11y_notebookBackToCourseNotes)
    is NotebookLevel.NoteDetails -> null // NotesListContent is never shown for this level.
}

@Composable
private fun SectionHeaderRow(
    item: NotebookListItem.SectionHeader,
    onClick: (NotebookLevel.ListLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        onClick = { onClick(item.target) },
        trailing = ListItemTrailing.Icons(icons = listOf(InstUIIcons.chevronRight)),
        trailingAlignment = Alignment.CenterVertically,
        modifier = modifier.testTag("notebookSectionHeader_${item.target.testTagKey()}"),
        content = {
            // Per Figma (25808-32651): title/subtitle stacked, both at contentSmall size - only the
            // trailing chevron (handled by ListItem's own trailing slot) is right-aligned.
            Text(
                text = item.title,
                style = InstUIText.contentSmall,
                color = InstUISemanticColors.Text.base(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = pluralStringResource(R.plurals.notebookNotesCount, item.count, item.count),
                style = InstUIText.contentSmall,
                color = InstUIText.mutedColor
            )
        }
    )
}

private fun NotebookLevel.ListLevel.testTagKey(): String = when (this) {
    is NotebookLevel.GlobalNotes -> "global"
    is NotebookLevel.CourseNotes -> "course_$courseId"
    is NotebookLevel.PageNotes -> "page_$pageId"
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

/**
 * Two independent tap targets, per design: the main content (title/quote/text) drills into the note's
 * own detail level ([onNoteClick], unchanged); the trailing arrow instead closes the whole tray and
 * routes straight to the note's underlying page/assignment/quiz ([onNoteArrowClick]). The arrow's target
 * spans the row's full height (via [IntrinsicSize.Min] on the outer [Row] + [fillMaxHeight] on the arrow's
 * own [Box]), not just the icon's bounds, so tapping anywhere above/below it within the row still hits it.
 */
@Composable
private fun NoteListItem(
    item: NoteUiState,
    onNoteClick: (String) -> Unit,
    onNoteArrowClick: (FragmentActivity, NoteUiState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min).testTag("notebookNoteListItem_${item.id}")) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = { onNoteClick(item.id) })
                .padding(InstUILayoutSizes.Spacing.SpaceMd.spaceMd),
            verticalArrangement = Arrangement.spacedBy(InstUILayoutSizes.Spacing.Space2xs.space2xs)
        ) {
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
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .clickable(
                    role = Role.Button,
                    onClick = { context.getFragmentActivityOrNull()?.let { onNoteArrowClick(it, item) } }
                )
                .padding(horizontal = InstUILayoutSizes.Spacing.SpaceMd.spaceMd)
                .testTag("notebookNoteListItemArrow_${item.id}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = InstUIIcons.chevronRight, contentDescription = stringResource(R.string.a11y_notebookGoToLearningObject))
        }
    }
}

/**
 * The highlighted quote excerpt on a list row, capped at 3 lines with no expand affordance (per
 * the list Figma, which - unlike the detail screen's Card - has no "Show all" control). Matches the
 * WebView highlight it mirrors (see JSTextSelectionInterface's CSS) with both a translucent tint behind
 * the text (approximating Figma's mix-blend-darken highlighter marks, flat alpha compositing is close
 * enough without a real blend mode) and a per-line underline - solid for Important, dashed for
 * Confusing - both colored per note type so the list visually echoes the WebView highlight exactly.
 */
@Composable
private fun HighlightedExcerptText(text: String, noteType: NoteType, modifier: Modifier = Modifier) {
    val highlightTint = noteType.accentColor().copy(alpha = 0.12f)
    val underlineColor = noteType.accentColor()
    val dashedEffect = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) }
    val pathEffect = if (noteType == NoteType.CONFUSING) dashedEffect else null
    var lineEndXs by remember(text) { mutableStateOf(emptyList<Float>()) }
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(background = highlightTint)) { append(text) }
        },
        style = InstUIText.contentQuote,
        color = InstUISemanticColors.Text.base(),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layout -> lineEndXs = (0 until layout.lineCount).map { layout.getLineRight(it) } },
        modifier = modifier.drawWithContent {
            drawContent()
            if (lineEndXs.isEmpty()) return@drawWithContent
            val strokeWidth = 1.dp.toPx()
            val lineHeight = size.height / lineEndXs.size
            lineEndXs.forEachIndexed { index, lineEndX ->
                val y = (index + 1) * lineHeight - strokeWidth
                drawLine(color = underlineColor, strokeWidth = strokeWidth, pathEffect = pathEffect, start = Offset(0f, y), end = Offset(lineEndX, y))
            }
        }
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
                    filteredNotes = previewNotes,
                    items = previewNotes.map { NotebookListItem.NoteRow(it) }
                ),
                onDoneClick = {}
            )
        }
    }
}

@Preview(name = "NotesListContent Grouped (Course level) — Light", showBackground = true)
@Preview(name = "NotesListContent Grouped (Course level) — Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotesListContentGroupedPreview() {
    val pageOneNote = previewNotes[0].copy(learningObjectId = "1")
    val pageTwoNote = previewNotes[1].copy(learningObjectId = "2")
    val notes = listOf(pageOneNote, pageTwoNote)
    InstUITheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NotesListContent(
                uiState = NotebookUiState(
                    screenState = NotebookScreenState.Content,
                    levelTitle = "Intro to Psychology",
                    notes = notes,
                    filteredNotes = notes,
                    currentLevel = NotebookLevel.CourseNotes(courseId = 1L, title = "Intro to Psychology"),
                    items = listOf(
                        NotebookListItem.SectionHeader("Home Page", 1, NotebookLevel.PageNotes("1", "Home Page")),
                        NotebookListItem.NoteRow(pageOneNote),
                        NotebookListItem.SectionHeader("Syllabus", 1, NotebookLevel.PageNotes("2", "Syllabus")),
                        NotebookListItem.NoteRow(pageTwoNote)
                    )
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

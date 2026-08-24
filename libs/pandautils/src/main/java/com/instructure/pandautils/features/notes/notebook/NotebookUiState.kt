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
package com.instructure.pandautils.features.notes.notebook

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.canvasapi2.models.NoteType
import java.util.Date

data class NotebookUiState(
    val screenState: NotebookScreenState = NotebookScreenState.Loading,
    /** Header subtitle of the current list level - the page title (Page), course name (Course), or
     * blank (Global, the top of the hierarchy - see [NotebookLevel]). */
    val levelTitle: String = "",
    /** All notes for the current list level, newest first. Every list level reads from this one list. */
    val notes: List<NoteUiState> = emptyList(),
    val selectedFilter: NoteTypeFilter = NoteTypeFilter.All,
    /** [notes] narrowed by [selectedFilter]; computed in the ViewModel (there is no server-side filter). */
    val filteredNotes: List<NoteUiState> = emptyList(),
    /**
     * What [com.instructure.pandautils.features.notes.notebook.composables.NotesListContent] actually
     * renders: [filteredNotes] flattened together with the section headers a Course/Global level groups
     * them under (empty for a Page level, which has no grouping - see [NotebookLevel]).
     */
    val items: List<NotebookListItem> = emptyList(),
    /** Top of the VM-owned level stack - what the tray renders right now. */
    val currentLevel: NotebookLevel = NotebookLevel.PageNotes("", ""),
    /** The [NotebookLevel.ListLevel] backing [currentLevel]'s data - itself when [currentLevel] already
     * is one, or the list level directly beneath it when [currentLevel] is a [NotebookLevel.NoteDetails].
     * Used to label that level's own back control with the level it actually leads back to. */
    val listLevel: NotebookLevel.ListLevel = NotebookLevel.PageNotes("", ""),
    /** Non-null only while [currentLevel] is a [NotebookLevel.NoteDetails]; resolved from the parent level's notes. */
    val selectedNote: NoteUiState? = null,
    /** One-shot signal: a note row's arrow was tapped, so the tray should close (it never needs to be
     * "un-set" - the whole tray, and this ViewModel, are torn down as soon as it's observed). */
    val closeTrayRequested: Boolean = false,
    /** Set alongside [closeTrayRequested] only when the tapped note's arrow belongs to the page the
     * tray was opened from - the host should scroll to this note on the page underneath instead of
     * navigating anywhere (there's nowhere else to go). */
    val scrollToNoteOnDismiss: String? = null,
    val actions: NotebookActions = NotebookActions()
)

data class NoteUiState(
    val id: String,
    val noteType: NoteType,
    val highlightedText: String,
    val userText: String,
    val dateLabel: String?,
    /** Raw creation timestamp, kept alongside the already-formatted [dateLabel] because Course/Global
     * section headers order their groups by their most recent note's createdAt, not by [dateLabel]. */
    val createdAt: Date? = null,
    val learningObjectId: String = "",
    val learningObjectType: LearningObjectType = LearningObjectType.WikiPage,
    /** The course this note belongs to - always known (every note has exactly one course), but only
     * read when grouping a [NotebookLevel.GlobalNotes] level's items by course. */
    val courseId: Long? = null
)

sealed class NotebookScreenState {
    data object Loading : NotebookScreenState()
    data object Content : NotebookScreenState()
    data class Error(val message: String) : NotebookScreenState()
}

enum class NoteTypeFilter(val noteType: NoteType?) {
    All(null), Important(NoteType.IMPORTANT), Confusing(NoteType.CONFUSING)
}

data class NotebookActions(
    val onFilterSelected: (NoteTypeFilter) -> Unit = {},
    val onRetryClick: () -> Unit = {},
    val onNoteClick: (String) -> Unit = {},
    val onBackClick: () -> Unit = {},
    /** Tapping a [NotebookListItem.SectionHeader] - "zooms in" one level, to [NotebookListItem.SectionHeader.target]. */
    val onSectionHeaderClick: (NotebookLevel.ListLevel) -> Unit = {},
    /** Tapping a note row's trailing arrow - closes the whole tray and routes to the note's underlying
     * page/assignment/quiz via [NotebookRouter], instead of drilling into the note's own detail level
     * (that's [onNoteClick], unchanged). [FragmentActivity] is supplied by the caller (Compose has one
     * via `LocalContext`) since the ViewModel has none of its own. */
    val onNoteArrowClick: (FragmentActivity, NoteUiState) -> Unit = { _, _ -> }
)

/**
 * A level in the Notebook's navigation stack - see [NotebookViewModel] for the level-stack model this
 * belongs to. [GlobalNotes] (all of the current user's notes across every course, grouped by course) and
 * [CourseNotes] (all of the current user's notes in one course, grouped by page) sit above [PageNotes]
 * (a single page's notes, unchanged from the original single-level feature) in the hierarchy -
 * Global > Course > Page > [NoteDetails]. See the Notebook Course/Global levels plan.
 */
sealed interface NotebookLevel {
    /** A filterable list of notes. Every list level loads its own data. */
    sealed interface ListLevel : NotebookLevel

    /** The top of the hierarchy - no back control, per Figma (node 25808-32566). */
    data object GlobalNotes : ListLevel

    data class CourseNotes(val courseId: Long, val title: String) : ListLevel

    data class PageNotes(val pageId: String, val title: String) : ListLevel

    data class NoteDetails(val noteId: String) : NotebookLevel
}

/**
 * A single row [com.instructure.pandautils.features.notes.notebook.composables.NotesListContent] can
 * render: either a note, or - on a [NotebookLevel.CourseNotes]/[NotebookLevel.GlobalNotes] level - a
 * clickable group header. Per the Figma designs, a header is never a collapsed summary: its group's
 * [NoteRow]s always render inline, directly underneath it; the header is *additionally* a tap target
 * that drills into just that group ([target]).
 */
sealed interface NotebookListItem {
    data class SectionHeader(val title: String, val count: Int, val target: NotebookLevel.ListLevel) : NotebookListItem
    data class NoteRow(val note: NoteUiState) : NotebookListItem
}

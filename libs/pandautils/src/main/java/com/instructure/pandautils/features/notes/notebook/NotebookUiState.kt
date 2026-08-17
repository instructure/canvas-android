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

import com.instructure.canvasapi2.models.NoteType

data class NotebookUiState(
    val screenState: NotebookScreenState = NotebookScreenState.Loading,
    /** Header subtitle of the current list level (the page's title today). */
    val levelTitle: String = "",
    /** All notes for the current list level, newest first. Both level types read from this one list. */
    val notes: List<NoteUiState> = emptyList(),
    val selectedFilter: NoteTypeFilter = NoteTypeFilter.All,
    /** [notes] narrowed by [selectedFilter]; computed in the ViewModel (there is no server-side filter). */
    val filteredNotes: List<NoteUiState> = emptyList(),
    /** Top of the VM-owned level stack - what the tray renders right now. */
    val currentLevel: NotebookLevel = NotebookLevel.PageNotes("", ""),
    /** Non-null only while [currentLevel] is a [NotebookLevel.NoteDetails]; resolved from the parent level's notes. */
    val selectedNote: NoteUiState? = null,
    val actions: NotebookActions = NotebookActions()
)

data class NoteUiState(
    val id: String,
    val noteType: NoteType,
    val highlightedText: String,
    val userText: String,
    val dateLabel: String?
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
    val onBackClick: () -> Unit = {}
)

/**
 * A level in the Notebook's navigation stack. See [NotebookViewModel] for the level-stack model this
 * belongs to. Only [PageNotes] and [NoteDetails] are wired today; [ListLevel] is shaped to grow a
 * Course level (and, blocked server-side for now, a Global level) without reshaping the stack, the
 * UiState, the Tray, or the hosting code - see the Notebook Tray plan §2.3/§2.5.
 */
sealed interface NotebookLevel {
    /** A filterable list of notes. Every list level loads its own data. */
    sealed interface ListLevel : NotebookLevel

    // Future - NOT implemented now:
    //   data object AllNotes : ListLevel                            // blocked server-side (no courseId-less query)
    //   data class CourseNotes(val courseId: Long, val title: String) : ListLevel

    data class PageNotes(val pageId: String, val title: String) : ListLevel

    data class NoteDetails(val noteId: String) : NotebookLevel
}

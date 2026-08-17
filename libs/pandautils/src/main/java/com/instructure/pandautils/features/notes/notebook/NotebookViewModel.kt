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

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.models.noteType
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.notes.GetPageStudyNotesUseCase
import com.instructure.pandautils.features.notes.composable.toNoteLocalisedDateFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns a stack of [NotebookLevel]s so the Notebook can drill down (list -> detail, and in the future
 * Course -> Page) without re-fetching a level's notes every time the user navigates back to it - see
 * the Notebook Tray plan §2.2/§2.3. Scoped to a single tray opening by the one-destination [NotebookTray]
 * NavHost (so this ViewModel - and [initialNoteId] - never survives across two openings of the tray).
 */
@HiltViewModel
class NotebookViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val getPageStudyNotesUseCase: GetPageStudyNotesUseCase
) : ViewModel() {

    private val courseId: Long = savedStateHandle.get<Long>(NotebookRoute.ARG_COURSE_ID) ?: 0L
    private val initialNoteId: String? = savedStateHandle[NotebookRoute.ARG_INITIAL_NOTE_ID]
    private val forceNetwork: Boolean = savedStateHandle.get<Boolean>(NotebookRoute.ARG_FORCE_NETWORK) ?: true

    private val actions = NotebookActions(
        onFilterSelected = ::onFilterSelected,
        onRetryClick = ::onRetryClick,
        onNoteClick = ::onNoteClick,
        onBackClick = ::onBackClick
    )

    // Each list level keeps its own loaded notes + filter, so popping back to it is instant and does
    // not refetch. Adding the Course level later means pushing another LevelState - nothing else changes.
    private data class LevelState(
        val level: NotebookLevel,
        val screenState: NotebookScreenState = NotebookScreenState.Loading,
        val notes: List<NoteUiState> = emptyList(),
        val selectedFilter: NoteTypeFilter = NoteTypeFilter.All
    )

    // When opening straight into a note (a WebView highlight tap), the detail level is pushed here,
    // synchronously, before the first note is even fetched - so the very first NotebookContent
    // composition already targets NoteDetails and Compose's AnimatedContent never animates a first
    // composition. This is what makes the note "open immediately" with no list-then-slide-to-detail
    // flash; NoteDetailsContent shows its own loading/error state (from the shared list level's
    // screenState) until the fetch resolves. Back from here still pops to the list as normal.
    private var stack: List<LevelState> = buildInitialStack(savedStateHandle)

    private val _uiState = MutableStateFlow(stack.toUiState())
    val uiState = _uiState.asStateFlow()

    init { loadTopList() }

    private fun buildInitialStack(savedStateHandle: SavedStateHandle): List<LevelState> {
        val root = LevelState(rootLevel(savedStateHandle))
        val noteId = initialNoteId ?: return listOf(root)
        return listOf(root, LevelState(NotebookLevel.NoteDetails(noteId)))
    }

    private fun rootLevel(savedStateHandle: SavedStateHandle): NotebookLevel = NotebookLevel.PageNotes(
        pageId = savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID).orEmpty(),
        title = savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_TITLE).orEmpty()
    )

    /** Index of the nearest [NotebookLevel.ListLevel] at or below the top of the stack. */
    private fun topListIndex(): Int = stack.indexOfLast { it.level is NotebookLevel.ListLevel }

    private fun loadTopList() {
        val index = topListIndex()
        val level = stack[index].level as? NotebookLevel.ListLevel ?: return
        updateLevelAt(index) { it.copy(screenState = NotebookScreenState.Loading) }
        viewModelScope.launch {
            try {
                val notes = loadNotesFor(level)
                    .sortedByDescending { it.updatedAt ?: it.createdAt }
                    .map { it.toNoteUiState() }
                updateLevelAt(index) { it.copy(screenState = NotebookScreenState.Content, notes = notes) }
            } catch (e: Exception) {
                updateLevelAt(index) { it.copy(screenState = NotebookScreenState.Error(resources.getString(R.string.notebookLoadError))) }
            }
        }
    }

    private suspend fun loadNotesFor(level: NotebookLevel.ListLevel): List<StudyNote> = when (level) {
        is NotebookLevel.PageNotes ->
            getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(courseId, level.pageId, forceNetwork = forceNetwork))
        // is NotebookLevel.CourseNotes -> getCourseStudyNotesUseCase(...)  // future, see the plan's §2.5
    }

    private fun onNoteClick(noteId: String) {
        stack = stack + LevelState(NotebookLevel.NoteDetails(noteId))
        publish()
    }

    private fun onBackClick() {
        if (stack.size <= 1) return
        stack = stack.dropLast(1)
        publish()
    }

    private fun onFilterSelected(filter: NoteTypeFilter) {
        updateLevelAt(topListIndex()) { it.copy(selectedFilter = filter) }
    }

    private fun onRetryClick() = loadTopList()

    private fun updateLevelAt(index: Int, transform: (LevelState) -> LevelState) {
        stack = stack.toMutableList().also { it[index] = transform(it[index]) }
        publish()
    }

    private fun publish() {
        _uiState.update { stack.toUiState() }
    }

    private fun StudyNote.toNoteUiState(): NoteUiState = NoteUiState(
        id = id,
        noteType = noteType,
        highlightedText = highlightData?.selectedText.orEmpty(),
        userText = userText,
        dateLabel = (updatedAt ?: createdAt)?.toNoteLocalisedDateFormat()
    )

    private fun List<NoteUiState>.filtered(filter: NoteTypeFilter): List<NoteUiState> =
        filter.noteType?.let { type -> this.filter { note -> note.noteType == type } } ?: this

    private fun List<LevelState>.toUiState(): NotebookUiState {
        val top = last()
        val listState = this[indexOfLast { it.level is NotebookLevel.ListLevel }]
        val selectedNote = (top.level as? NotebookLevel.NoteDetails)?.let { detail ->
            listState.notes.find { it.id == detail.noteId }
        }
        return NotebookUiState(
            screenState = listState.screenState,
            levelTitle = (listState.level as? NotebookLevel.PageNotes)?.title.orEmpty(),
            notes = listState.notes,
            selectedFilter = listState.selectedFilter,
            filteredNotes = listState.notes.filtered(listState.selectedFilter),
            currentLevel = top.level,
            selectedNote = selectedNote,
            actions = actions
        )
    }
}

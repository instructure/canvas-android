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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.LearningObjectType
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.canvasapi2.models.noteType
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCase
import com.instructure.pandautils.domain.usecase.courses.LoadCourseUseCaseParams
import com.instructure.pandautils.domain.usecase.notes.GetCourseStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetEligibleCoursesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetGlobalStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetPageStudyNotesUseCase
import com.instructure.pandautils.domain.usecase.notes.GetPageTitlesUseCase
import com.instructure.pandautils.features.notes.composable.toNoteLocalisedDateFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Owns a stack of [NotebookLevel]s so the Notebook can drill down (Global -> Course -> Page -> detail)
 * without re-fetching a level's notes every time the user navigates back to it - see the Notebook
 * Course/Global levels plan. Scoped to a single tray opening by the one-destination [NotebookTray]
 * NavHost (so this ViewModel - and [initialNoteId] - never survives across two openings of the tray).
 *
 * Two caches make "drilling in never refetches, zooming out only fetches the first time" true beyond
 * the stack's own pop-is-free behavior:
 *  - [scopeCache] holds every [NotesScope]'s notes ever resolved this session, keyed broadly enough that
 *    a narrower scope (e.g. a Page) can always be *derived* by filtering an already-cached broader scope
 *    (its Course, or Global) instead of re-fetching - see [resolveScope].
 *  - [eligibleCoursesCache]/[pageTitlesCache] memoize the auxiliary label lookups (course names, page
 *    titles) a Course/Global level's section headers need, which are separate, smaller fetches from the
 *    notes themselves.
 */
@HiltViewModel
class NotebookViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resources: Resources,
    private val getPageStudyNotesUseCase: GetPageStudyNotesUseCase,
    private val getCourseStudyNotesUseCase: GetCourseStudyNotesUseCase,
    private val getGlobalStudyNotesUseCase: GetGlobalStudyNotesUseCase,
    private val getEligibleCoursesUseCase: GetEligibleCoursesUseCase,
    private val getPageTitlesUseCase: GetPageTitlesUseCase,
    private val notebookRouter: NotebookRouter,
    private val notebookPrefs: NotebookPrefs,
    private val loadCourseUseCase: LoadCourseUseCase
) : ViewModel() {

    private val courseId: Long = savedStateHandle.get<Long>(NotebookRoute.ARG_COURSE_ID) ?: 0L
    private val initialNoteId: String? = savedStateHandle[NotebookRoute.ARG_INITIAL_NOTE_ID]
    private val forceNetwork: Boolean = savedStateHandle.get<Boolean>(NotebookRoute.ARG_FORCE_NETWORK) ?: true
    // The page the tray itself was opened from (host passed a page id - see NotebookTrayArgs), if any.
    // Used by onNoteArrowClick to detect "this note's page is the page we're already showing", in which
    // case there's nothing to navigate to - just close and scroll there instead.
    private val openedFromPageId: String? = savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID)

    private var closeTrayRequested = false
    private var scrollToNoteOnDismiss: String? = null

    private val actions = NotebookActions(
        onFilterSelected = ::onFilterSelected,
        onRetryClick = ::onRetryClick,
        onNoteClick = ::onNoteClick,
        onBackClick = ::onBackClick,
        onSectionHeaderClick = ::onSectionHeaderClick,
        onNoteArrowClick = ::onNoteArrowClick
    )

    // Each list level keeps its own loaded notes + filter, so popping back to it is instant and does
    // not refetch. [courseId] is the course a Page/Course level's data belongs to - not part of the
    // public NotebookLevel shape (Figma has no course id on a Page level), but needed internally to
    // resolve/fetch a level's notes and to zoom a Page level out to its parent Course.
    private data class LevelState(
        val level: NotebookLevel,
        val courseId: Long? = null,
        val screenState: NotebookScreenState = NotebookScreenState.Loading,
        val notes: List<NoteUiState> = emptyList(),
        val selectedFilter: NoteTypeFilter = NoteTypeFilter.All
    )

    /** A fresh list level always starts on the last filter the user picked (any level, any tray
     * session), not [NoteTypeFilter.All] - see [NotebookPrefs]. */
    private fun newListLevelState(level: NotebookLevel.ListLevel, courseId: Long? = null) =
        LevelState(level, courseId = courseId, selectedFilter = notebookPrefs.selectedFilter)

    /** A note tagged with the course it belongs to - every note has exactly one, even though [StudyNote]
     * itself carries no course id; this is what lets [scopeCache] derive a narrower scope from a broader
     * one (Global -> Course by courseId, Course -> Page by learningObjectId) via a pure client-side filter. */
    private data class CachedNote(val note: StudyNote, val courseId: Long)

    /** A resolvable unit of notes data. See the class doc and [resolveScope]. */
    private sealed interface NotesScope {
        data object Global : NotesScope
        data class Course(val courseId: Long) : NotesScope
        data class Page(val courseId: Long, val pageId: String) : NotesScope
    }

    private val scopeCache = mutableMapOf<NotesScope, List<CachedNote>>()
    private var eligibleCoursesCache: List<Course>? = null
    private val pageTitlesCache = mutableMapOf<Long, Map<String, String>>()

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
        val root = newListLevelState(rootLevel(savedStateHandle), courseId = courseId)
        val noteId = initialNoteId ?: return listOf(root)
        return listOf(root, LevelState(NotebookLevel.NoteDetails(noteId)))
    }

    /** A Page-opening (host passed a page id, e.g. [com.instructure.pandautils.features.pages.details.PageDetailsScreen])
     * lands on [NotebookLevel.PageNotes]; a Course-opening (e.g. Student's `CourseBrowserFragment`, which
     * has no page to open) lands on [NotebookLevel.CourseNotes]. There is no direct Global entry point. */
    private fun rootLevel(savedStateHandle: SavedStateHandle): NotebookLevel.ListLevel {
        val pageId = savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_ID)
        return if (pageId != null) {
            NotebookLevel.PageNotes(pageId = pageId, title = savedStateHandle.get<String>(NotebookRoute.ARG_PAGE_TITLE).orEmpty())
        } else {
            NotebookLevel.CourseNotes(courseId = courseId, title = savedStateHandle.get<String>(NotebookRoute.ARG_COURSE_TITLE).orEmpty())
        }
    }

    /** Index of the nearest [NotebookLevel.ListLevel] at or below the top of the stack. */
    private fun topListIndex(): Int = stack.indexOfLast { it.level is NotebookLevel.ListLevel }

    private fun loadTopList() {
        val index = topListIndex()
        val entry = stack[index]
        val level = entry.level as? NotebookLevel.ListLevel ?: return
        updateLevelAt(index) { it.copy(screenState = NotebookScreenState.Loading) }
        viewModelScope.launch {
            try {
                val cachedNotes = resolveNotes(level, entry.courseId)
                ensureLabelsFor(level)
                val notes = cachedNotes
                    .sortedByDescending { it.note.updatedAt ?: it.note.createdAt }
                    .map { it.toNoteUiState() }
                updateLevelAt(index) { it.copy(screenState = NotebookScreenState.Content, notes = notes) }
            } catch (e: Exception) {
                updateLevelAt(index) { it.copy(screenState = NotebookScreenState.Error(resources.getString(R.string.notebookLoadError))) }
            }
        }
    }

    /** Fetches (or resolves from [ensureLabelsFor]'s caches) whatever a level's own section headers need
     * to render a title - a no-op for [NotebookLevel.PageNotes], which has no grouping. */
    private suspend fun ensureLabelsFor(level: NotebookLevel.ListLevel) {
        when (level) {
            is NotebookLevel.CourseNotes -> ensurePageTitles(level.courseId)
            is NotebookLevel.GlobalNotes -> getEligibleCourses()
            is NotebookLevel.PageNotes -> Unit
        }
    }

    /**
     * [forceNetwork] (from [NotebookTrayArgs]) only ever means "the host already fetched *this page's*
     * notes itself, moments ago" - it's a Page-root-only optimization (see [NotebookTrayArgs]'s doc).
     * Course/Global were never pre-fetched by anything, so their own first fetch must always hit the
     * network regardless of that flag, or navigating from a Page (opened with forceNetwork = false) out
     * to its Course/Global would silently serve stale, possibly-cached data instead of refreshing.
     */
    private suspend fun resolveNotes(level: NotebookLevel.ListLevel, entryCourseId: Long?): List<CachedNote> = when (level) {
        is NotebookLevel.PageNotes -> {
            val cid = requireNotNull(entryCourseId) { "A PageNotes level always has a known course." }
            resolveScope(NotesScope.Page(cid, level.pageId)) {
                getPageStudyNotesUseCase(GetPageStudyNotesUseCase.Params(cid, level.pageId, forceNetwork)).map { CachedNote(it, cid) }
            }
        }

        is NotebookLevel.CourseNotes -> resolveScope(NotesScope.Course(level.courseId)) {
            getCourseStudyNotesUseCase(GetCourseStudyNotesUseCase.Params(level.courseId, forceNetwork = true)).map { CachedNote(it, level.courseId) }
        }

        is NotebookLevel.GlobalNotes -> resolveScope(NotesScope.Global) {
            val courseIds = getEligibleCourses().map { it.id }
            getGlobalStudyNotesUseCase(GetGlobalStudyNotesUseCase.Params(courseIds, forceNetwork = true))
                .map { CachedNote(it.note, it.courseId) }
        }
    }

    /**
     * Exact cache hit, else derive from an already-cached broader scope (a pure client-side filter -
     * never a network call), else fetch fresh via [fetch] and cache the result. Fetching [NotesScope.Global]
     * additionally seeds each course's own [NotesScope.Course] entry from the fan-out result, so drilling
     * into any of them afterwards is a derive too, even though this was the very first fetch.
     */
    private suspend fun resolveScope(scope: NotesScope, fetch: suspend () -> List<CachedNote>): List<CachedNote> {
        scopeCache[scope]?.let { return it }
        deriveFromCache(scope)?.let {
            scopeCache[scope] = it
            return it
        }

        val fetched = fetch()
        scopeCache[scope] = fetched
        if (scope is NotesScope.Global) {
            fetched.groupBy { it.courseId }.forEach { (cid, notes) -> scopeCache.putIfAbsent(NotesScope.Course(cid), notes) }
        }
        return fetched
    }

    private fun deriveFromCache(scope: NotesScope): List<CachedNote>? = when (scope) {
        is NotesScope.Page -> {
            val broader = scopeCache[NotesScope.Course(scope.courseId)] ?: scopeCache[NotesScope.Global]?.filter { it.courseId == scope.courseId }
            broader?.filter { it.note.learningObjectId == scope.pageId }
        }

        is NotesScope.Course -> scopeCache[NotesScope.Global]?.filter { it.courseId == scope.courseId }

        is NotesScope.Global -> null
    }

    private suspend fun getEligibleCourses(): List<Course> {
        eligibleCoursesCache?.let { return it }
        val courses = getEligibleCoursesUseCase(Unit)
        eligibleCoursesCache = courses
        return courses
    }

    /** Non-fatal: a Course level's notes are still worth showing (under [fallbackSectionTitle]s) even
     * if the page-title lookup itself fails - unlike [getEligibleCourses], which a Global level cannot
     * do without. */
    private suspend fun ensurePageTitles(courseId: Long) {
        if (pageTitlesCache.containsKey(courseId)) return
        pageTitlesCache[courseId] = runCatching { getPageTitlesUseCase(GetPageTitlesUseCase.Params(courseId)) }
            .getOrDefault(emptyMap())
    }

    private fun onNoteClick(noteId: String) {
        stack = stack + LevelState(NotebookLevel.NoteDetails(noteId))
        publish()
    }

    /** Unlike [onNoteClick] (drills into the note's own detail level), this leaves the Notebook
     * entirely - except when the note's page is [openedFromPageId], the very page the tray is already
     * sitting on top of (reachable from any level, e.g. spotting one of the current page's own notes
     * while browsing at Course/Global): there's nothing to navigate to then, so just close and let the
     * host scroll to it on the page underneath instead (see [scrollToNoteOnDismiss]).
     *
     * Otherwise resolves a real, enrollment-populated [Course] first (rather than passing a bare id) -
     * the destination screen's own `canvasContext.isStudent` gate (guarding its Notebook highlight
     * fetch) would otherwise always fail, since a bare-id [CanvasContext] carries no enrollments. Falls
     * back to an id-only [CanvasContext] if that fetch fails, so a transient network error still lets
     * the user navigate - just without highlights on arrival. */
    private fun onNoteArrowClick(activity: FragmentActivity, note: NoteUiState) {
        if (note.learningObjectType == LearningObjectType.WikiPage && note.learningObjectId == openedFromPageId) {
            scrollToNoteOnDismiss = note.id
            closeTrayRequested = true
            publish()
            return
        }
        val cid = note.courseId ?: return
        viewModelScope.launch {
            val canvasContext: CanvasContext = runCatching { loadCourseUseCase(LoadCourseUseCaseParams(cid)) }
                .getOrNull() ?: CanvasContext.emptyCourseContext(cid)
            notebookRouter.routeToLearningObject(activity, canvasContext, note.learningObjectId, note.learningObjectType, note.id)
            closeTrayRequested = true
            publish()
        }
    }

    private fun onSectionHeaderClick(target: NotebookLevel.ListLevel) {
        val targetCourseId = when (target) {
            is NotebookLevel.CourseNotes -> target.courseId
            // Drilling from a Course into one of its pages - the course is whatever the level being
            // drilled from (the current top of the stack) already belongs to.
            is NotebookLevel.PageNotes -> stack.last().courseId
            is NotebookLevel.GlobalNotes -> null
        }
        pushListLevel(target, targetCourseId)
    }

    private fun onBackClick() {
        when (val level = stack.last().level) {
            is NotebookLevel.NoteDetails -> {
                stack = stack.dropLast(1)
                publish()
            }

            is NotebookLevel.PageNotes -> {
                val parentCourseId = stack.last().courseId
                zoomOutTo(
                    parentPredicate = { (it.level as? NotebookLevel.CourseNotes)?.courseId == parentCourseId },
                    buildParent = {
                        val cid = requireNotNull(parentCourseId)
                        newListLevelState(NotebookLevel.CourseNotes(cid, courseTitleFor(cid)), courseId = cid)
                    }
                )
            }

            is NotebookLevel.CourseNotes -> zoomOutTo(
                parentPredicate = { it.level is NotebookLevel.GlobalNotes },
                buildParent = { newListLevelState(NotebookLevel.GlobalNotes) }
            )

            // GlobalNotes has no back control; nothing to do if this is ever invoked anyway.
            is NotebookLevel.GlobalNotes -> Unit
        }
    }

    /**
     * "Zooming out" - if the level directly beneath the current top is already the resolved parent (true
     * whenever the current level was reached by drilling in from it), just pop, exactly like the original
     * detail-to-list pop: instant, no refetch. Otherwise (the current level is the tray's own root, with
     * nothing pushed below it) build and push the parent fresh - [loadTopList] resolves its notes via the
     * same cache-or-fetch path as any other level, so it's still only a real network call the first time.
     */
    private fun zoomOutTo(parentPredicate: (LevelState) -> Boolean, buildParent: suspend () -> LevelState) {
        val parentIndex = stack.size - 2
        if (parentIndex >= 0 && parentPredicate(stack[parentIndex])) {
            stack = stack.dropLast(1)
            publish()
            return
        }

        viewModelScope.launch {
            val parent = buildParent()
            stack = stack + parent
            publish()
            loadTopList()
        }
    }

    private suspend fun courseTitleFor(courseId: Long): String =
        runCatching { getEligibleCourses().find { it.id == courseId }?.name }.getOrNull().orEmpty()

    private fun pushListLevel(level: NotebookLevel.ListLevel, courseId: Long?) {
        stack = stack + newListLevelState(level, courseId = courseId)
        publish()
        loadTopList()
    }

    private fun onFilterSelected(filter: NoteTypeFilter) {
        notebookPrefs.selectedFilter = filter
        updateLevelAt(topListIndex()) { it.copy(selectedFilter = filter) }
    }

    private fun onRetryClick() = loadTopList()

    private fun updateLevelAt(index: Int, transform: (LevelState) -> LevelState) {
        stack = stack.toMutableList().also { it[index] = transform(it[index]) }
        publish()
    }

    private fun publish() {
        _uiState.update { stack.toUiState().copy(closeTrayRequested = closeTrayRequested, scrollToNoteOnDismiss = scrollToNoteOnDismiss) }
    }

    private fun CachedNote.toNoteUiState(): NoteUiState = NoteUiState(
        id = note.id,
        noteType = note.noteType,
        highlightedText = note.highlightData?.selectedText.orEmpty(),
        userText = note.userText,
        dateLabel = (note.updatedAt ?: note.createdAt)?.toNoteLocalisedDateFormat(),
        createdAt = note.createdAt,
        learningObjectId = note.learningObjectId,
        learningObjectType = note.learningObjectType,
        courseId = courseId
    )

    private fun List<NoteUiState>.filtered(filter: NoteTypeFilter): List<NoteUiState> =
        filter.noteType?.let { type -> this.filter { note -> note.noteType == type } } ?: this

    /** Builds the [NotebookListItem]s a Course/Global level renders - grouped by page/course respectively,
     * groups ordered by their most recent note's [NoteUiState.createdAt] descending (see the plan's
     * ordering decision). A [NotebookLevel.PageNotes] level has no grouping: one [NotebookListItem.NoteRow]
     * per note, in the same order as [notes] itself. */
    private fun buildItems(level: NotebookLevel.ListLevel, notes: List<NoteUiState>): List<NotebookListItem> = when (level) {
        is NotebookLevel.PageNotes -> notes.map { NotebookListItem.NoteRow(it) }

        is NotebookLevel.CourseNotes -> groupedItems(notes) { note ->
            NotebookLevel.PageNotes(pageId = note.learningObjectId, title = pageSectionTitle(note, level.courseId))
        }

        is NotebookLevel.GlobalNotes -> groupedItems(notes) { note ->
            val cid = note.courseId ?: 0L
            NotebookLevel.CourseNotes(courseId = cid, title = courseSectionTitle(cid))
        }
    }

    private fun groupedItems(
        notes: List<NoteUiState>,
        targetFor: (NoteUiState) -> NotebookLevel.ListLevel
    ): List<NotebookListItem> {
        return notes.groupBy(targetFor).entries
            .sortedByDescending { (_, groupNotes) -> groupNotes.maxOfOrNull { it.createdAt?.time ?: 0L } ?: 0L }
            .flatMap { (target, groupNotes) ->
                val title = when (target) {
                    is NotebookLevel.CourseNotes -> target.title
                    is NotebookLevel.PageNotes -> target.title
                    is NotebookLevel.GlobalNotes -> ""
                }
                listOf(NotebookListItem.SectionHeader(title = title, count = groupNotes.size, target = target)) +
                    groupNotes.map { NotebookListItem.NoteRow(it) }
            }
    }

    private fun pageSectionTitle(note: NoteUiState, courseId: Long): String {
        val resolvedTitle = pageTitlesCache[courseId]?.get(note.learningObjectId)
        return resolvedTitle ?: fallbackSectionTitle(note.learningObjectType, note.learningObjectId)
    }

    private fun courseSectionTitle(courseId: Long): String =
        eligibleCoursesCache?.find { it.id == courseId }?.name.orEmpty()

    /** Per the plan's v1 scope decision: today's UI only ever creates Page notes, so an Assignment/Quiz
     * note (or a Page note whose page title didn't resolve, e.g. a deleted page) is an edge case with no
     * title source - fall back to a generic label rather than silently dropping the note's group. */
    private fun fallbackSectionTitle(type: LearningObjectType, learningObjectId: String): String = when (type) {
        LearningObjectType.Assignment -> resources.getString(R.string.notebookAssignmentNoteLabel, learningObjectId)
        LearningObjectType.Quiz -> resources.getString(R.string.notebookQuizNoteLabel, learningObjectId)
        LearningObjectType.WikiPage -> resources.getString(R.string.notebookPageNoteLabel, learningObjectId)
    }

    private fun levelTitleFor(level: NotebookLevel.ListLevel): String = when (level) {
        is NotebookLevel.PageNotes -> level.title
        is NotebookLevel.CourseNotes -> level.title
        is NotebookLevel.GlobalNotes -> ""
    }

    private fun List<LevelState>.toUiState(): NotebookUiState {
        val top = last()
        val listState = this[indexOfLast { it.level is NotebookLevel.ListLevel }]
        val listLevel = listState.level as NotebookLevel.ListLevel
        val selectedNote = (top.level as? NotebookLevel.NoteDetails)?.let { detail ->
            listState.notes.find { it.id == detail.noteId }
        }
        val filteredNotes = listState.notes.filtered(listState.selectedFilter)
        return NotebookUiState(
            screenState = listState.screenState,
            levelTitle = levelTitleFor(listLevel),
            notes = listState.notes,
            selectedFilter = listState.selectedFilter,
            filteredNotes = filteredNotes,
            items = buildItems(listLevel, filteredNotes),
            currentLevel = top.level,
            listLevel = listLevel,
            selectedNote = selectedNote,
            actions = actions
        )
    }
}

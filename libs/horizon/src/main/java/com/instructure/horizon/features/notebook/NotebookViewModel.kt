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

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.common.model.mapToNotes
import com.instructure.horizon.features.notebook.navigation.NotebookRoute
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.redwood.QueryNotesQuery
import com.instructure.redwood.type.OrderDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class NotebookViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: NotebookRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var pageInfo: QueryNotesQuery.PageInfo? = null
    private var loadJob: Job? = null

    private var courseId: Long? =
        savedStateHandle.get<Long>(NotebookRoute.Notebook.COURSE_ID)
            ?: savedStateHandle.get<String>(NotebookRoute.Notebook.COURSE_ID)?.toLongOrNull()
    private var objectTypeAndId: Pair<String, String>? = getObjectTypeAndId(savedStateHandle)
    private var showTopBar: Boolean =
        savedStateHandle.get<Boolean>(NotebookRoute.Notebook.SHOW_TOP_BAR) ?: false
    private var showFilters: Boolean =
        savedStateHandle.get<Boolean>(NotebookRoute.Notebook.SHOW_FILTERS) ?: false
    private var navigateToEdit: Boolean =
        savedStateHandle.get<Boolean>(NotebookRoute.Notebook.NAVIGATE_TO_EDIT) ?: false

    private val _uiState = MutableStateFlow(
        NotebookUiState(
            loadingState = LoadingState(
                onSnackbarDismiss = ::onSnackbarDismiss,
                onRefresh = ::refresh
            ),
            loadNextPage = ::getNextPage,
            onFilterSelected = ::onFilterSelected,
            onCourseSelected = ::onCourseSelected,
            showTopBar = showTopBar,
            showFilters = showFilters,
            showCourseFilter = courseId == null,
            navigateToEdit = navigateToEdit,
            updateShowDeleteConfirmation = ::updateShowDeleteConfirmation,
            deleteNote = ::deleteNote
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true, isRefreshing = false, isError = false, errorMessage = null)) }
            fetchData()
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = false, errorMessage = null)) }
        } catch {
            _uiState.update { it.copy(
                loadingState = it.loadingState.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = context.getString(
                        R.string.notebookFailedToLoadErrorMessage
                    )
                )
            ) }
        }
    }

    fun refresh() {
        pageInfo = null
        loadJob?.cancel()
        loadJob = viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            fetchData(forceNetwork = true)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, isError = false, errorMessage = null)) }
        } catch {
            _uiState.update { it.copy(
                loadingState = it.loadingState.copy(
                    isRefreshing = false,
                    snackbarMessage = context.getString(
                        R.string.notebookFailedToLoadErrorMessage
                    )
                )
            ) }
        }
    }

    private suspend fun fetchData(forceNetwork: Boolean = false) {
        fetchCourses(forceNetwork)
        val notes = fetchNotes(forceNetwork)
        _uiState.update { it.copy(notes = notes) }
    }

    private suspend fun fetchCourses(forceNetwork: Boolean = false) {
        val courses = repository.getCourses(forceNetwork)
        _uiState.update { it.copy(courses = courses) }
    }

    private suspend fun fetchNotes(forceNetwork: Boolean = false): List<Note> {
        val notesResponse = repository.getNotes(
            after = pageInfo?.endCursor,
            before = null,
            filterType = uiState.value.selectedFilter,
            courseId = courseId,
            objectTypeAndId = objectTypeAndId,
            orderDirection = OrderDirection.descending,
            forceNetwork = forceNetwork
        )
        pageInfo = notesResponse.pageInfo

        _uiState.update {
            it.copy(hasNextPage = notesResponse.pageInfo.hasNextPage)
        }

        return notesResponse.mapToNotes()
    }

    private fun getNextPage() {
        loadJob?.cancel()
        loadJob = viewModelScope.tryLaunch {
            _uiState.update { it.copy(isLoadingMore = true) }
            val notes = fetchNotes()
            _uiState.update { it.copy(notes = it.notes + notes, isLoadingMore = false) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = "Failed to load notes")) }
        }
    }

    private fun onFilterSelected(newFilter: NotebookType?) {
        pageInfo = null
        _uiState.update { it.copy(selectedFilter = newFilter) }
        loadData()
    }

    private fun onCourseSelected(course: CourseWithProgress?) {
        pageInfo = null
        _uiState.update { it.copy(selectedCourse = course) }
        courseId = course?.courseId
        loadData()
    }

    fun updateFilters(courseId: Long? = null, objectTypeAndId: Pair<String, String>? = null) {
        pageInfo = null
        this.courseId = courseId
        this.objectTypeAndId = objectTypeAndId
        loadData()
    }

    private fun onSnackbarDismiss() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }
    }

    fun updateScreenState(
        showNoteTypeFilter: Boolean = true,
        showCourseFilter: Boolean = true,
        showTopBar: Boolean = false
    ) {
        _uiState.update { it.copy(showTopBar = showTopBar, showCourseFilter = showCourseFilter, showNoteTypeFilter = showNoteTypeFilter) }
    }

    private fun getObjectTypeAndId(savedStateHandle: SavedStateHandle): Pair<String, String>? {
        val objectType =
            savedStateHandle.get<String>(NotebookRoute.Notebook.OBJECT_TYPE) ?: return null
        val objectId = savedStateHandle.get<String>(NotebookRoute.Notebook.OBJECT_ID) ?: return null
        return Pair(objectType, objectId)
    }

    private fun updateShowDeleteConfirmation(note: Note?) {
        _uiState.update { it.copy(showDeleteConfirmationForNote = note) }
    }

    private fun deleteNote(note: Note?) {
        if (note != null) {
            viewModelScope.tryLaunch {
                _uiState.update { it.copy(deleteLoadingNote = note) }
                repository.deleteNote(note.id)
                _uiState.update { it.copy(deleteLoadingNote = null, notes = it.notes.filterNot { it == note }) }
            } catch {
                _uiState.update { it.copy(deleteLoadingNote = null) }
            }
        }
    }
}
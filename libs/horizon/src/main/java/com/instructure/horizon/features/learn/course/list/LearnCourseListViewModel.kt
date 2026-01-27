/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.course.list

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.course.list.LearnCourseFilterOption.Companion.getProgressOption
import com.instructure.horizon.horizonui.platform.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LearnCourseListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LearnCourseListRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(LearnCourseListUiState(
        loadingState = LoadingState(onRefresh = ::refresh, onSnackbarDismiss = ::dismissSnackbar),
        increaseVisibleItemCount = ::increaseVisibleItemCount,
        updateFilterValue = ::updateFilter,
        updateSearchQuery = ::updateSearchQuery
    ))
    val state = _uiState.asStateFlow()

    private val pageCount = 10
    private var allCourses: List<LearnCourseState> = emptyList()

    init {
        loadCourses()
    }

    private suspend fun fetchData(forceRefresh: Boolean = false): List<LearnCourseState> {
        val courses = repository.getCoursesWithProgress(forceRefresh).map {
            LearnCourseState(
                courseName = it.courseName,
                courseId = it.courseId,
                imageUrl = it.courseImageUrl,
                progress = it.progress
            )
        }
        allCourses = courses
        return courses
    }

    private fun loadCourses() {
        resetScreenValues()
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(
                    isLoading = true,
                ))
            }
            _uiState.value = _uiState.value.copy(
                coursesToDisplay = fetchData().applyFilters(),
            )
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(
                    isLoading = false,
                ))
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(
                    isLoading = false,
                    isError = true
                ))
            }
        }
    }

    private fun resetScreenValues() {
        _uiState.value = _uiState.value.copy(
            visibleItemCount = pageCount,
            searchQuery = TextFieldValue(""),
            selectedFilterValue = LearnCourseFilterOption.All,
        )
    }

    private fun refresh() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(
                    isRefreshing = true,
                ))
            }
            _uiState.value = _uiState.value.copy(
                coursesToDisplay = fetchData(true).applyFilters(),
            )
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(
                    isRefreshing = false,
                ))
            }
        } catch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(
                    isRefreshing = false,
                    snackbarMessage = context.getString(R.string.learnCourseListFailedToLoadCoursesMessage)
                ))
            }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(
                snackbarMessage = null
            ))
        }
    }

    private fun increaseVisibleItemCount() {
        _uiState.update {
            it.copy(visibleItemCount = it.visibleItemCount + pageCount)
        }
    }

    private fun updateFilter(filterOption: LearnCourseFilterOption) {
        _uiState.update {
            it.copy(selectedFilterValue = filterOption)
        }
        _uiState.update {
            it.copy(coursesToDisplay = allCourses.applyFilters())
        }
    }

    private fun updateSearchQuery(query: TextFieldValue) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
        _uiState.update {
            it.copy(coursesToDisplay = allCourses.applyFilters())
        }
    }

    private fun List<LearnCourseState>.applyFilters(): List<LearnCourseState> {
        return this.filter {
            it.progress.getProgressOption() == _uiState.value.selectedFilterValue
                    || _uiState.value.selectedFilterValue == LearnCourseFilterOption.All
                    && it.courseName.contains(_uiState.value.searchQuery.text.trim(), ignoreCase = true)

        }
    }
}
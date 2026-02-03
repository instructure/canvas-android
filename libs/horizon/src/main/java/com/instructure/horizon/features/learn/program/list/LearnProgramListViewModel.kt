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
package com.instructure.horizon.features.learn.program.list

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.program.list.LearnProgramFilterOption.Companion.getProgressOption
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.pandautils.utils.formatMonthDayYear
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class LearnProgramListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resources: Resources,
    private val repository: LearnProgramListRepository
): ViewModel() {

    private val pageCount = 10
    private var allPrograms: List<LearnProgramState> = emptyList()

    private val _uiState = MutableStateFlow(LearnProgramListUiState(
        loadingState = LoadingState(
            onRefresh = ::refreshData,
            onSnackbarDismiss = ::onSnackbarDismissed
        ),
        increaseVisibleItemCount = ::increaseVisibleItemCount,
        updateSearchQuery = ::updateSearchQuery,
        updateFilterValue = ::updateFilter
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            fetchData()
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false)) }
        }  catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private fun refreshData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = true)) }
            fetchData(true)
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false)) }
        }  catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isRefreshing = false, snackbarMessage = context.getString(
                R.string.learnProgramListFailedToLoadMessage
            ))) }
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean = false) {
        val programs = repository.getPrograms(forceRefresh)
        val programStates = programs.map { program ->
            val programDuration = repository.getCoursesById(
                program.sortedRequirements.map { it.courseId },
                forceRefresh
            )
                .flatMap { it.moduleItemsDuration }
                .map { Duration.parse(it) }
                .fold(Duration.ZERO) { acc, d -> acc + d }

            LearnProgramState(
                programName = program.name,
                programId = program.id,
                programProgress = program.sortedRequirements.firstOrNull()?.progress ?: 0.0,
                programChips = buildList {
                    add(
                        LearnProgramChipState(
                            label = resources.getQuantityString(
                                R.plurals.learnProgramListCourseCount,
                                program.sortedRequirements.size,
                                program.sortedRequirements.size
                            ),
                            iconRes = null
                        )
                    )

                    if (programDuration.isPositive()) {
                        add(
                            LearnProgramChipState(
                                label = programDuration.toString(),
                                iconRes = null
                            )
                        )
                    }

                    if (program.startDate != null && program.endDate != null) {
                        add(
                            LearnProgramChipState(
                                label = context.getString(
                                    R.string.programTag_DateRange,
                                    program.startDate?.formatMonthDayYear(),
                                    program.endDate?.formatMonthDayYear()
                                ),
                                iconRes = R.drawable.calendar_today
                            )
                        )
                    }
                }
            )
        }
        allPrograms = programStates

        _uiState.value = _uiState.value.copy(
            filteredPrograms = programStates.applyFilters()
        )
    }

    private fun onSnackbarDismissed() {
        _uiState.update { it.copy(loadingState = it.loadingState.copy(snackbarMessage = null)) }

    }

    private fun List<LearnProgramState>.applyFilters(): List<LearnProgramState> {
        return this.filter {
            (it.programProgress.getProgressOption() == _uiState.value.selectedFilterValue
                    || _uiState.value.selectedFilterValue == LearnProgramFilterOption.All)
                    && it.programName.contains(_uiState.value.searchQuery.text.trim(), ignoreCase = true)

        }
    }

    private fun increaseVisibleItemCount() {
        _uiState.update {
            it.copy(visibleItemCount = it.visibleItemCount + pageCount)
        }
    }

    private fun updateFilter(filterOption: LearnProgramFilterOption) {
        _uiState.update {
            it.copy(selectedFilterValue = filterOption)
        }
        _uiState.update {
            it.copy(filteredPrograms = allPrograms.applyFilters())
        }
    }

    private fun updateSearchQuery(query: TextFieldValue) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
        _uiState.update {
            it.copy(filteredPrograms = allPrograms.applyFilters())
        }
    }
}
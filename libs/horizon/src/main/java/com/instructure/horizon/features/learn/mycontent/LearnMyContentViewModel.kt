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
package com.instructure.horizon.features.learn.mycontent

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithProgress
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.learninglibrary.common.toUiState
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.pandautils.utils.formatMonthDayYear
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.sum
import com.instructure.pandautils.utils.toFormattedString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class LearnMyContentViewModel @Inject constructor(
    private val resources: Resources,
    private val repository: LearnMyContentRepository
): ViewModel() {
    val _uiState: MutableStateFlow<LearnMyContentUiState> = MutableStateFlow(LearnMyContentUiState(

    ))
    val uiState = _uiState.asStateFlow()

    private val pageSize: Int = 10

    private var learningLibraryNextCursor: String? = null

    init {
        loadCourseContent()
        loadSavedLearningLibraryItems()
    }

    private fun loadCourseContent() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    inProgressState = it.inProgressState.copy(
                        loadingState = it.inProgressState.loadingState.copy(
                            isLoading = true
                        )
                    ),
                    completedState = it.completedState.copy(
                        loadingState = it.completedState.loadingState.copy(
                            isLoading = true
                        )
                    ),
                )
            }

            fetchCourseContent()

            _uiState.update {
                it.copy(
                    inProgressState = it.inProgressState.copy(
                        loadingState = it.inProgressState.loadingState.copy(
                            isLoading = false,
                            isError = false
                        )
                    ),
                    completedState = it.completedState.copy(
                        loadingState = it.completedState.loadingState.copy(
                            isLoading = false,
                            isError = false
                        )
                    ),
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    inProgressState = it.inProgressState.copy(
                        loadingState = it.inProgressState.loadingState.copy(
                            isLoading = false,
                            isError = true
                        )
                    ),
                    completedState = it.completedState.copy(
                        loadingState = it.completedState.loadingState.copy(
                            isLoading = false,
                            isError = true
                        )
                    ),
                )
            }
        }
    }

    private fun loadSavedLearningLibraryItems() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    savedState = it.savedState.copy(
                        loadingState = it.savedState.loadingState.copy(
                            isLoading = true
                        )
                    )
                )
            }

            fetchSavedLearningLibraryItems(null)

            _uiState.update {
                it.copy(
                    savedState = it.savedState.copy(
                        loadingState = it.savedState.loadingState.copy(
                            isLoading = false,
                            isError = false
                        )
                    )
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    savedState = it.savedState.copy(
                        loadingState = it.savedState.loadingState.copy(
                            isLoading = false,
                            isError = true
                        )
                    )
                )
            }
        }
    }

    private fun refreshCourseContent() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    inProgressState = it.inProgressState.copy(
                        loadingState = it.inProgressState.loadingState.copy(
                            isRefreshing = true
                        )
                    ),
                    completedState = it.completedState.copy(
                        loadingState = it.completedState.loadingState.copy(
                            isRefreshing = true
                        )
                    ),
                )
            }

            fetchCourseContent()

            _uiState.update {
                it.copy(
                    inProgressState = it.inProgressState.copy(
                        loadingState = it.inProgressState.loadingState.copy(
                            isRefreshing = false,
                            isError = false
                        ),
                        visibleItemCount = pageSize
                    ),
                    completedState = it.completedState.copy(
                        loadingState = it.completedState.loadingState.copy(
                            isRefreshing = false,
                            isError = false
                        ),
                        visibleItemCount = pageSize
                    ),
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    inProgressState = it.inProgressState.copy(
                        loadingState = it.inProgressState.loadingState.copy(
                            isRefreshing = false,
                            snackbarMessage = resources.getString(R.string.learnMyContentProgramErrorMessage)
                        )
                    ),
                    completedState = it.completedState.copy(
                        loadingState = it.completedState.loadingState.copy(
                            isRefreshing = false,
                            snackbarMessage = resources.getString(R.string.learnMyContentProgramErrorMessage)
                        )
                    ),
                )
            }
        }
    }

    private fun refreshSavedLearningLibraryItems() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(
                    savedState = it.savedState.copy(
                        loadingState = it.savedState.loadingState.copy(
                            isRefreshing = true,
                        )
                    )
                )
            }
            fetchSavedLearningLibraryItems(null, true)

            _uiState.update {
                it.copy(
                    savedState = it.savedState.copy(
                        loadingState = it.savedState.loadingState.copy(
                            isRefreshing = false,
                            isError = false
                        )
                    )
                )
            }
        } catch {
            _uiState.update {
                it.copy(
                    savedState = it.savedState.copy(
                        loadingState = it.savedState.loadingState.copy(
                            isRefreshing = false,
                            snackbarMessage = resources.getString(R.string.learnMyContentProgramErrorMessage)
                        )
                    )
                )
            }
        }
    }

    private suspend fun fetchCourseContent(forceRefresh: Boolean = false) {
        val courses = repository.getCoursesWithProgress(forceRefresh)
        val programs = repository.getPrograms(forceRefresh)

        val inProgressCourses = courses.filter { it.progress < 100.0 }
        val completedCourses = courses.filter { it.progress == 100.0 }

        val inProgressPrograms = programs.filter { it.sortedRequirements.firstOrNull()?.progress.orDefault() < 100.0 }
        val completedPrograms = programs.filter { it.sortedRequirements.firstOrNull()?.progress.orDefault() == 100.0 }

        _uiState.update {
            it.copy(
                inProgressState = it.inProgressState.copy(
                    contentCards = inProgressCourses.map { it.toUiState() }
                        + inProgressPrograms.map { it.toUiState() },
                ),
                completedState = it.completedState.copy(
                    contentCards = completedCourses.map { it.toUiState() }
                        + completedPrograms.map { it.toUiState() },
                )
            )
        }
    }

    private suspend fun fetchSavedLearningLibraryItems(
        nextCourse: String? = learningLibraryNextCursor,
        forceRefresh: Boolean = false
    ) {
        val savedItems = repository.getBookmarkedLearningLibraryItems(
            afterCursor = nextCourse,
            limit = pageSize,
            searchQuery = uiState.value.searchQuery.text,
            sortBy = uiState.value.sortByOption.toCollectionItemSortOption(),
            forceRefresh
        )

        val recommendations = repository.getLearningLibraryRecommendedItems(forceRefresh)

        learningLibraryNextCursor = savedItems.pageInfo.nextCursor

        _uiState.update {
            it.copy(
                savedState = it.savedState.copy(
                    contentCards = savedItems.items.map { it.toUiState(resources, recommendations) },
                    totalItemCount = pageSize
                )
            )
        }
    }

    private fun CourseWithProgress.toUiState(): LearnContentCardState {
        return LearnContentCardState(
            imageUrl = this.courseImageUrl,
            name = this.courseName,
            progress = this.progress,
            route = LearnRoute.LearnCourseDetailsScreen.route(this.courseId),
            buttonLabel = if (progress == 0.0) {
                resources.getString(R.string.learnMyContentStartLearning)
            } else if (progress < 100.0) {
                resources.getString(R.string.learnMyContentResumeLearning)
            } else {
                null
            },
            cardChips = buildList {
                add(
                    LearnContentCardChipState(
                        label = resources.getString(R.string.learnMyContentCourseLabel),
                        color = StatusChipColor.Institution,
                        iconRes = R.drawable.book_2
                    )
                )
            }
        )
    }

    private suspend fun Program.toUiState(): LearnContentCardState {
        val requiredCourseIds = sortedRequirements.filter { it.required }.map { it.courseId }.toSet()
        val programDuration = repository.getCoursesById(
            sortedRequirements.map { it.courseId },
        )
            .filter { requiredCourseIds.contains(it.courseId) }
            .flatMap { it.moduleItemsDuration }
            .map { Duration.parse(it) }
            .sum()

        return LearnContentCardState(
            imageUrl = null,
            name = this.name,
            progress = this.sortedRequirements.firstOrNull()?.progress,
            route = LearnRoute.LearnProgramDetailsScreen.route(this.id),
            buttonLabel = null,
            cardChips = buildList {
                add(
                    LearnContentCardChipState(
                        label = resources.getString(R.string.learnMyContentProgramLabel),
                        color = StatusChipColor.Violet,
                        iconRes = R.drawable.book_5
                    )
                )
                add(
                    LearnContentCardChipState(
                        label = resources.getQuantityString(
                            R.plurals.learnMyContentProgramCourseCount,
                            sortedRequirements.size,
                            sortedRequirements.size
                        ),
                        iconRes = null
                    )
                )
                if (programDuration.isPositive()) {
                    add(
                        LearnContentCardChipState(
                            label = programDuration.toFormattedString(resources),
                            iconRes = null
                        )
                    )
                }

                if (startDate != null && endDate != null) {
                    add(
                        LearnContentCardChipState(
                            label = resources.getString(
                                R.string.programTag_DateRange,
                                startDate?.formatMonthDayYear(),
                                endDate?.formatMonthDayYear()
                            ),
                            iconRes = R.drawable.calendar_today
                        )
                    )
                }
            }
        )
    }
}
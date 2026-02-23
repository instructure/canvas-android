/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.learn.program.details

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.graphql.horizon.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.horizon.journey.Program
import com.instructure.canvasapi2.managers.graphql.horizon.journey.ProgramRequirement
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.dashboard.DashboardEvent
import com.instructure.horizon.features.dashboard.DashboardEventHandler
import com.instructure.horizon.features.learn.navigation.LearnRoute
import com.instructure.horizon.features.learn.program.details.components.CourseCardChipState
import com.instructure.horizon.features.learn.program.details.components.CourseCardStatus
import com.instructure.horizon.features.learn.program.details.components.ProgramCourseCardState
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressItemState
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressItemStatus
import com.instructure.horizon.features.learn.program.details.components.ProgramProgressState
import com.instructure.horizon.features.learn.program.details.components.SequentialProgramProgressProperties
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import com.instructure.pandautils.utils.formatMonthDayYear
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.sum
import com.instructure.pandautils.utils.toFormattedString
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class ProgramDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resources: Resources,
    private val repository: ProgramDetailsRepository,
    private val dashboardEventHandler: DashboardEventHandler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val programId = savedStateHandle.get<String>(LearnRoute.LearnProgramDetailsScreen.programIdAttr) ?: ""

    private val _uiState = MutableStateFlow(
        ProgramDetailsUiState(
            loadingState = LoadingState(
                onRefresh = ::refreshProgram,
                onSnackbarDismiss = ::dismissSnackbar
            ),
        )
    )
    val state = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.tryLaunch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = true)) }
            fetchData()
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = false)) }
        } catch {
            _uiState.update { it.copy(loadingState = it.loadingState.copy(isLoading = false, isError = true)) }
        }
    }

    private suspend fun fetchData(forceRefresh: Boolean = false) {
        val program = repository.getProgramDetails(programId, forceRefresh)
        val courses = repository.getCoursesById(program.sortedRequirements.map { it.courseId }, forceRefresh)
        updateUiState(program, courses)
    }

    private fun updateUiState(program: Program, courses: List<CourseWithModuleItemDurations>) {
        val progressBarStatus =
            if (program.sortedRequirements.any { it.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED }) {
                ProgressBarStatus.IN_PROGRESS
            } else {
                ProgressBarStatus.NOT_STARTED
            }
        _uiState.update {
            it.copy(
                programName = program.name,
                showProgressBar = shouldShowProgressBar(program),
                progressBarUiState = ProgressBarUiState(
                    progress = calculateProgress(program),
                    progressBarStatus = progressBarStatus
                ),
                description = program.description ?: "",
                tags = createProgramTags(program, courses),
                programProgressState = createProgramProgressState(program, courses)
            )
        }
    }

    private fun createProgramTags(program: Program, courses: List<CourseWithModuleItemDurations>): List<ProgramDetailTag> {
        val tags = mutableListOf<ProgramDetailTag>()
        if (program.startDate != null && program.endDate != null) {
            tags.add(
                ProgramDetailTag(
                    name = context.getString(
                        R.string.programTag_DateRange,
                        program.startDate?.formatMonthDayYear(),
                        program.endDate?.formatMonthDayYear()
                    ),
                    iconRes = R.drawable.calendar_today
                )
            )
        }

        val requiredCourseIds = program.sortedRequirements.filter { it.required }.map { it.courseId }.toSet()
        val moduleItemDurations = courses
            .filter { requiredCourseIds.contains(it.courseId) }
            .flatMap { it.moduleItemsDuration }
            .map { Duration.parse(it) }
        if (moduleItemDurations.isNotEmpty()) {
            val durationString = moduleItemDurations.sum().toFormattedString(resources)
            if (durationString.isNotEmpty()) {
                tags.add(ProgramDetailTag(durationString))
            }
        }

        return tags
    }

    private fun createProgramProgressState(program: Program, courses: List<CourseWithModuleItemDurations>): ProgramProgressState {
        val linear = program.variant == ProgramVariantType.LINEAR

        val courseItems = program.sortedRequirements.mapIndexed { index, requirement ->

            val courseCardStatus = when {
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.BLOCKED -> CourseCardStatus.Inactive
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.NOT_ENROLLED -> CourseCardStatus.Active
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED && requirement.progress > 0 && requirement.progress < 100 -> CourseCardStatus.InProgress
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED && requirement.progress == 100.0 -> CourseCardStatus.Completed
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED && requirement.progress == 0.0 -> CourseCardStatus.Enrolled
                else -> CourseCardStatus.Inactive // Default case
            }

            val chips = createProgramCourseChips(
                requirement,
                courses.find { it.courseId == requirement.courseId } ?: CourseWithModuleItemDurations(),
                courseCardStatus,
                linear)

            val sequentialProperties = if (linear) {
                SequentialProgramProgressProperties(
                    status = ProgramProgressItemStatus.Companion.fromCourseCardStatus(
                        courseCardStatus
                    ),
                    index = index + 1, // 1-based index for sequential properties
                    first = index == 0,
                    last = index == program.sortedRequirements.size - 1,
                    previousCompleted = index > 0 && program.sortedRequirements[index - 1].progress == 100.0
                )
            } else null

            ProgramProgressItemState(
                courseCard = ProgramCourseCardState(
                    id = requirement.courseId,
                    courseName = courses.find { it.courseId == requirement.courseId }?.courseName.orEmpty(),
                    status = courseCardStatus,
                    courseProgress = requirement.progress,
                    chips = chips,
                    dashedBorder = program.variant == ProgramVariantType.NON_LINEAR && !requirement.required && courseCardStatus != CourseCardStatus.Completed,
                    onEnrollClicked = {
                        enrollCourse(requirement.courseId, requirement.progressId)
                    },
                    enabled = requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED
                ),
                sequentialProperties = sequentialProperties
            )
        }

        val headerString = if (!linear) {
            context.resources.getQuantityString(
                R.plurals.programCourses_courseCompletionCount,
                program.sortedRequirements.size,
                program.courseCompletionCount ?: program.sortedRequirements.size,
                program.sortedRequirements.size
            )
        } else {
            null
        }

        return ProgramProgressState(courseItems, headerString)
    }

    private fun createProgramCourseChips(
        requirement: ProgramRequirement,
        course: CourseWithModuleItemDurations,
        courseCardStatus: CourseCardStatus,
        linear: Boolean
    ): List<CourseCardChipState> {
        val chips = mutableListOf<CourseCardChipState>()
        if (requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.BLOCKED) {
            chips.add(
                CourseCardChipState(
                    context.getString(R.string.programCourseTag_locked),
                    iconRes = R.drawable.lock
                )
            )
        }

        if (courseCardStatus == CourseCardStatus.Enrolled) {
            chips.add(
                CourseCardChipState(
                    context.getString(R.string.programCourseTag_enrolled),
                    overrideColor = StatusChipColor.Green,
                    iconRes = R.drawable.check_circle_full
                )
            )
        }

        if (courseCardStatus == CourseCardStatus.Completed) {
            chips.add(CourseCardChipState(context.getString(R.string.programCourseTag_completed)))
        } else {
            if (linear) {
                if (requirement.required) {
                    chips.add(CourseCardChipState(context.getString(R.string.programCourseTag_required)))
                } else {
                    chips.add(CourseCardChipState(context.getString(R.string.programCourseTag_optional)))
                }
            }
        }

        if (course.moduleItemsDuration.isNotEmpty() && courseCardStatus != CourseCardStatus.Completed) {
            val durationString = course.moduleItemsDuration.map { Duration.parse(it) }.sum().toFormattedString(resources)
            if (durationString.isNotEmpty()) {
                chips.add(CourseCardChipState(durationString))
            }
        }

        val shouldShowDate =
            courseCardStatus == CourseCardStatus.Active || courseCardStatus == CourseCardStatus.InProgress || courseCardStatus == CourseCardStatus.Enrolled
        if (course.startDate != null && course.endDate != null && shouldShowDate) {
            chips.add(
                CourseCardChipState(
                    context.getString(
                        R.string.programTag_DateRange,
                        course.startDate?.formatMonthDayYear(),
                        course.endDate?.formatMonthDayYear()
                    ), iconRes = R.drawable.calendar_today
                )
            )
        }

        return chips
    }

    private fun shouldShowProgressBar(program: Program): Boolean {
        if (program.sortedRequirements.isEmpty()) return false

        if (program.variant == ProgramVariantType.LINEAR) {
            val requiredCourses = program.sortedRequirements.filter { it.required }
            if (requiredCourses.isEmpty()) return false
        } else {
            val courseCompletionCount = program.courseCompletionCount.orDefault()
            if (courseCompletionCount == 0) return false
        }

        return true
    }

    private fun calculateProgress(program: Program): Double {
        if (program.sortedRequirements.isEmpty()) return 0.0

        if (program.variant == ProgramVariantType.LINEAR) {
            val requiredCourses = program.sortedRequirements.filter { it.required }
            if (requiredCourses.isEmpty()) return 0.0

            val totalProgress = requiredCourses.sumOf { it.progress }
            return (totalProgress / (requiredCourses.size * 100)) * 100
        } else {
            val courseCompletionCount = program.courseCompletionCount.orDefault()
            if (courseCompletionCount == 0) return 0.0

            val requiredCourses = program.sortedRequirements.filter { it.required }
            val orderedProgresses = requiredCourses.map { it.progress }.sortedDescending()
            val totalProgress = orderedProgresses.take(courseCompletionCount).sum()
            return (totalProgress / (courseCompletionCount * 100)) * 100
        }
    }

    private fun refreshProgram() {
        viewModelScope.tryLaunch {
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isRefreshing = true))
            }

            fetchData(true)

            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isRefreshing = false, isError = false))
            }
        } catch {
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(
                        isRefreshing = false,
                        snackbarMessage = context.getString(R.string.programDetails_failedToRefresh)
                    )
                )
            }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(snackbarMessage = null))
        }
    }

    private fun enrollCourse(courseId: Long, progressId: String) {
        updateCourseEnrollLoadingState(courseId, true)
        viewModelScope.launch {
            val enrollResult = repository.enrollCourse(progressId)
            if (enrollResult.isFail) {
                updateCourseEnrollLoadingState(courseId, false)
                _uiState.update {
                    it.copy(
                        loadingState = it.loadingState.copy(
                            snackbarMessage = context.getString(R.string.programDetails_enrollFailed)
                        )
                    )
                }
                return@launch
            }

            dashboardEventHandler.postEvent(DashboardEvent.ProgressRefresh)
            refreshProgram()
        }
    }

    private fun updateCourseEnrollLoadingState(courseId: Long, loading: Boolean) {
        val courseToUpdate = _uiState.value.programProgressState.courses.find { it.courseCard.id == courseId }?.courseCard
            ?: return
        val updatedCourse = courseToUpdate.copy(enrollLoading = loading)
        _uiState.update {
            val updatedCourses = it.programProgressState.courses.map { item ->
                if (item.courseCard.id == courseId) {
                    item.copy(courseCard = updatedCourse)
                } else {
                    item
                }
            }
            it.copy(
                programProgressState = it.programProgressState.copy(courses = updatedCourses)
            )
        }
    }
}
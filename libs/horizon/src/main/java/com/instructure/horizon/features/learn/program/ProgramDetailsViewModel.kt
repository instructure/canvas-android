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
package com.instructure.horizon.features.learn.program

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.CourseWithModuleItemDurations
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.managers.graphql.ProgramRequirement
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.program.components.CourseCardChipState
import com.instructure.horizon.features.learn.program.components.CourseCardStatus
import com.instructure.horizon.features.learn.program.components.ProgramCourseCardState
import com.instructure.horizon.features.learn.program.components.ProgramProgressItemState
import com.instructure.horizon.features.learn.program.components.ProgramProgressItemStatus
import com.instructure.horizon.features.learn.program.components.ProgramProgressState
import com.instructure.horizon.features.learn.program.components.SequentialProgramProgressProperties
import com.instructure.horizon.horizonui.molecules.StatusChipColor
import com.instructure.horizon.horizonui.platform.LoadingState
import com.instructure.journey.type.ProgramProgressCourseEnrollmentStatus
import com.instructure.journey.type.ProgramVariantType
import com.instructure.pandautils.utils.formatMonthDayYear
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class ProgramDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProgramDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProgramDetailsUiState(
            loadingState = LoadingState(
                onRefresh = ::refreshProgram,
                onSnackbarDismiss = ::dismissSnackbar
            ),
            onNavigateToCourse = ::onNavigateToCourse
        )
    )
    val state = _uiState.asStateFlow()

    private var currentProgramId: String? = null

    fun loadProgramDetails(program: Program, courses: List<CourseWithModuleItemDurations>) {
        currentProgramId = program.id
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
                loadingState = it.loadingState.copy(isLoading = false, isRefreshing = false),
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

    private suspend fun loadData(forceNetwork: Boolean = false) {
        currentProgramId?.let { programId ->
            val programDetails = repository.getProgramDetails(programId, forceNetwork = forceNetwork)
            val courses = repository.getCoursesById(programDetails.sortedRequirements.map { it.courseId }, forceNetwork = forceNetwork)
            updateUiState(programDetails, courses)
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
        val moduleItemDurations = courses.filter { requiredCourseIds.contains(it.courseId) }.flatMap { it.moduleItemsDuration }
        if (moduleItemDurations.isNotEmpty()) {
            val durationString = getDurationStringFromDurations(moduleItemDurations)
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
                    status = ProgramProgressItemStatus.fromCourseCardStatus(courseCardStatus),
                    index = index + 1, // 1-based index for sequential properties
                    first = index == 0,
                    last = index == program.sortedRequirements.size - 1,
                    previousCompleted = index > 0 && program.sortedRequirements[index - 1].progress == 100.0
                )
            } else null

            val courseClickable = requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED
            val courseClicked = { _uiState.update { it.copy(navigateToCourseId = requirement.courseId) } }

            ProgramProgressItemState(
                courseCard = ProgramCourseCardState(
                    courseName = courses.find { it.courseId == requirement.courseId }?.courseName.orEmpty(),
                    status = courseCardStatus,
                    courseProgress = requirement.progress,
                    chips = chips,
                    dashedBorder = program.variant == ProgramVariantType.NON_LINEAR && !requirement.required && courseCardStatus != CourseCardStatus.Completed,
                    courseClicked = if (courseClickable) courseClicked else null
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
            chips.add(CourseCardChipState(context.getString(R.string.programCourseTag_locked), iconRes = R.drawable.lock))
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
            val durationString = getDurationStringFromDurations(course.moduleItemsDuration)
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

    private fun getDurationStringFromDurations(durations: List<String>): String {
        val totalMinutes = durations.mapNotNull { duration ->
            try {
                val dur = Duration.parse(duration)
                val hours = dur.inWholeHours.toInt()
                val minutes = (dur.inWholeMinutes % 60).toInt()
                hours * 60 + minutes
            } catch (e: Exception) {
                null
            }
        }.sum()

        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        val parts = mutableListOf<String>()
        if (hours > 0) parts.add(context.resources.getQuantityString(R.plurals.durationHours, hours, hours))
        if (minutes > 0) parts.add(context.resources.getQuantityString(R.plurals.durationMins, minutes, minutes))

        return if (parts.isEmpty()) "" else parts.joinToString(" ")
    }

    private fun refreshProgram() {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(isRefreshing = true))
        }
        viewModelScope.tryLaunch {
            loadData(true)
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

    private fun onNavigateToCourse() {
        _uiState.update {
            it.copy(navigateToCourseId = null)
        }
    }
}
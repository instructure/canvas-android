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
package com.instructure.horizon.features.learn.programs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.managers.graphql.Program
import com.instructure.canvasapi2.managers.graphql.ProgramRequirement
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.horizon.R
import com.instructure.horizon.features.learn.programs.components.CourseCardChipState
import com.instructure.horizon.features.learn.programs.components.CourseCardStatus
import com.instructure.horizon.features.learn.programs.components.ProgramCourseCardState
import com.instructure.horizon.features.learn.programs.components.ProgramProgressItemState
import com.instructure.horizon.features.learn.programs.components.ProgramProgressItemStatus
import com.instructure.horizon.features.learn.programs.components.ProgramProgressState
import com.instructure.horizon.features.learn.programs.components.SequentialProgramProgressProperties
import com.instructure.horizon.horizonui.molecules.StatusChipColor
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

@HiltViewModel
class ProgramDetailsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProgramDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgramDetailsUiState())
    val state = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(loadingState = it.loadingState.copy(isLoading = true))
        }
        viewModelScope.tryLaunch {
            val programDetails = repository.getProgramDetails("")
            _uiState.update {
                it.copy(
                    loadingState = it.loadingState.copy(isLoading = false),
                    programName = programDetails.name,
                    progress = calculateProgress(programDetails),
                    description = programDetails.description ?: "",
                    tags = createProgramTags(programDetails),
                    programProgressState = createProgramProgressState(programDetails)
                )
            }
        } catch {
            // TODO Error handling
            _uiState.update {
                it.copy(loadingState = it.loadingState.copy(isLoading = false))
            }
        }
    }

    private fun createProgramTags(program: Program): List<ProgramDetailTag> {
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

        // TODO Calculate remaining minutes and add tag
        return tags
    }

    private suspend fun createProgramProgressState(program: Program): ProgramProgressState {
        val linear = program.variant == ProgramVariantType.LINEAR
        val courses = repository.getCoursesById(program.sortedRequirements.map { it.courseId })

        val courseItems = program.sortedRequirements.mapIndexed { index, requirement ->

            val courseCardStatus = when {
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.BLOCKED -> CourseCardStatus.Inactive
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.NOT_ENROLLED -> CourseCardStatus.Active
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED && requirement.progress > 0 && requirement.progress < 100 -> CourseCardStatus.InProgress
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED && requirement.progress == 100.0 -> CourseCardStatus.Completed
                requirement.enrollmentStatus == ProgramProgressCourseEnrollmentStatus.ENROLLED && requirement.progress == 0.0 -> CourseCardStatus.Enrolled
                else -> CourseCardStatus.Inactive // Default case
            }

            val chips = createProgramCourseChips(requirement, courses.find { it.id == requirement.courseId } ?: Course(), courseCardStatus)

            val sequentialProperties = if (linear) {
                SequentialProgramProgressProperties(
                    status = ProgramProgressItemStatus.fromCourseCardStatus(courseCardStatus),
                    index = index + 1, // 1-based index for sequential properties
                    first = index == 0,
                    last = index == program.sortedRequirements.size - 1,
                    previousCompleted = index > 0 && program.sortedRequirements[index - 1].progress == 100.0
                )
            } else null

            ProgramProgressItemState(
                courseCard = ProgramCourseCardState(
                    courseName = courses.find { it.id == requirement.courseId }?.name.orEmpty(),
                    status = courseCardStatus,
                    courseProgress = requirement.progress, // TODO This should be ignored and the should be requested from the Course API instead, because it's not reliable
                    chips = chips
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
        course: Course,
        courseCardStatus: CourseCardStatus
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
            if (requirement.required) {
                chips.add(CourseCardChipState(context.getString(R.string.programCourseTag_required)))
            } else {
                chips.add(CourseCardChipState(context.getString(R.string.programCourseTag_optional)))
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

        // TODO Calculate remaining minutes and add tag

        return chips
    }

    private fun calculateProgress(program: Program): Double {
        if (program.sortedRequirements.isEmpty()) return 0.0

        return if (program.variant == ProgramVariantType.LINEAR) {
            val requiredCourses = program.sortedRequirements.filter { it.required }
            val totalProgress = requiredCourses.sumOf { it.progress }
            (totalProgress / (requiredCourses.size * 100)) * 100
        } else {
            val courseCompletionCount = program.courseCompletionCount.orDefault()
            val requiredCourses = program.sortedRequirements.filter { it.required }
            val orderedProgresses = requiredCourses.map { it.progress }.sortedDescending()
            val totalProgress = orderedProgresses.take(courseCompletionCount).sum()
            (totalProgress / (courseCompletionCount * 100)) * 100
        }
    }
}
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
package com.instructure.pandautils.features.speedgrader.grade.grading

import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.SubmissionGradeQuery
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation
import com.instructure.canvasapi2.UpdateSubmissionStatusMutation.SubAssignmentSubmission
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.type.CourseGradeStatus
import com.instructure.canvasapi2.type.SubmissionStatusTagType
import com.instructure.canvasapi2.utils.capitalized
import com.instructure.pandautils.R
import com.instructure.pandautils.features.speedgrader.SpeedGraderErrorHolder
import com.instructure.pandautils.features.speedgrader.grade.GradingEvent
import com.instructure.pandautils.features.speedgrader.grade.SpeedGraderGradingEventHandler
import com.instructure.pandautils.utils.AssignmentGradedEvent
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.postSticky
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SpeedGraderGradingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderGradingRepository,
    private val resources: Resources,
    private val gradingEventHandler: SpeedGraderGradingEventHandler,
    private val speedGraderErrorHolder: SpeedGraderErrorHolder
) : ViewModel() {

    private val assignmentId = savedStateHandle.get<Long>("assignmentId")
        ?: throw IllegalArgumentException("Missing assignmentId")
    private val studentId = savedStateHandle.get<Long>("submissionId")
        ?: throw IllegalArgumentException("Missing studentId")
    private val courseId: Long = savedStateHandle.get<Long>("courseId")
        ?: throw IllegalArgumentException("Missing courseId")

    private val _uiState =
        MutableStateFlow(
            SpeedGraderGradingUiState(
                onScoreChange = this::onScoreChanged,
                onPercentageChange = this::onPercentageChanged,
                onExcuse = this::onExcuse,
                onStatusChange = this::onStatusChange,
                onLateDaysChange = this::onLateDaysChange
            )
        )
    val uiState = _uiState.asStateFlow()

    lateinit var submissionId: String

    private var debounceJob: Job? = null

    private var daysLateDebounceJob: Job? = null

    init {
        loadData()
        viewModelScope.launch {
            gradingEventHandler.events.collect {
                when (it) {
                    is GradingEvent.RubricUpdated -> {
                        loadData(forceNetwork = true)
                    }

                    is GradingEvent.PostPolicyUpdated -> {
                        loadData(forceNetwork = true)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun loadData(forceNetwork: Boolean = false) {
        viewModelScope.launch {
            try {
                val submission =
                    repository.getSubmissionGrade(assignmentId, studentId, forceNetwork).submission
                        ?: throw IllegalStateException("Submission not found")
                submissionId = submission._id
                _uiState.update {
                    it.copy(
                        pointsPossible = submission.assignment?.pointsPossible,
                        enteredScore = submission.enteredScore?.toFloat(),
                        finalScore = submission.score,
                        finalGrade = submission.grade,
                        pointsDeducted = submission.deductedPoints,
                        gradingType = submission.assignment?.gradingType,
                        loading = false,
                        error = false,
                        gradeHidden = submission.hideGradeFromStudent.orDefault(),
                        submittedAt = submission.submittedAt,
                        gradingStatuses = submission.assignment?.course?.gradeStatuses
                            ?.map {
                                GradeStatus(
                                    statusId = it.rawValue,
                                    name = getGradeStatusName(it)
                                )
                            }
                            .orEmpty() +
                                submission.assignment?.course?.customGradeStatusesConnection?.edges?.filterNotNull()
                                    ?.map {
                                        GradeStatus(
                                            id = it.node?._id?.toLongOrNull(),
                                            name = it.node?.name.orEmpty()
                                        )
                                    }.orEmpty(),
                        letterGrades = submission.assignment?.course?.gradingStandard?.data?.map { gradingStandard ->
                            GradingSchemeRow(
                                gradingStandard.letterGrade.orEmpty(),
                                gradingStandard.baseValue.orDefault()
                            )
                        }.orEmpty(),
                        gradableAssignments = getGradableAssignments(submission)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = true,
                        retryAction = {
                            loadData(forceNetwork)
                        }
                    )
                }
            }
        }
    }

    private fun getGradableAssignments(submission: SubmissionGradeQuery.Submission): List<GradableAssignment> {
        return if (!submission.subAssignmentSubmissions.isNullOrEmpty()) {
            submission.subAssignmentSubmissions?.map { subAssignment ->
                GradableAssignment(
                    pointsPossible = submission.assignment?.checkpoints?.firstOrNull { it.tag == subAssignment.subAssignmentTag }?.pointsPossible,
                    enteredGrade = subAssignment.enteredGrade
                        ?: resources.getString(R.string.not_graded),
                    enteredScore = subAssignment.enteredScore?.toFloat(),
                    grade = subAssignment.grade,
                    score = subAssignment.score,
                    tag = subAssignment.subAssignmentTag,
                    gradingStatus = subAssignment.statusTag.name.capitalized(),
                    excused = subAssignment.excused.orDefault(),
                    daysLate = if (submission.late == true) getDaysLate(submission.secondsLate) else null
                )
            } ?: emptyList()
        } else {
            listOf(
                GradableAssignment(
                    pointsPossible = submission.assignment?.pointsPossible,
                    enteredGrade = submission.enteredGrade
                        ?: resources.getString(R.string.not_graded),
                    enteredScore = submission.enteredScore?.toFloat(),
                    grade = submission.grade,
                    score = submission.score,
                    excused = submission.excused.orDefault(),
                    gradingStatus = submission.status,
                    daysLate = if (submission.late == true) getDaysLate(submission.secondsLate) else null,
                )
            )
        }
    }

    /*private fun getGradableAssignments(submission: UpdateSubmissionStatusMutation.Submission): List<GradableAssignment> {
        return if (!submission.subAssignmentSubmissions.isNullOrEmpty()) {
            submission.subAssignmentSubmissions?.map { subAssignment ->
                GradableAssignment(
                    pointsPossible = submission.assignment?.checkpoints?.firstOrNull { it.tag == subAssignment.subAssignmentTag }?.pointsPossible,
                    enteredGrade = subAssignment.enteredGrade
                        ?: resources.getString(R.string.not_graded),
                    enteredScore = subAssignment.enteredScore?.toFloat(),
                    grade = subAssignment.grade,
                    score = subAssignment.score,
                    tag = subAssignment.subAssignmentTag,
                    gradingStatus = if (subAssignment.customGradeStatusId != null) {
                        submission.assignment?.course?.customGradeStatusesConnection?.edges?.firstOrNull { edge ->
                            edge?.node?._id == subAssignment.customGradeStatusId
                        }?.node?.name
                    } else {
                        getGradeStatusName(subAssignment.statusTag)
                    },
                    excused = subAssignment.excused.orDefault(),
                    daysLate = if (submission.late == true) getDaysLate(submission.secondsLate) else null
                )
            } ?: emptyList()
        } else {
            listOf(
                GradableAssignment(
                    pointsPossible = submission.assignment?.pointsPossible,
                    enteredGrade = submission.enteredGrade
                        ?: resources.getString(R.string.not_graded),
                    enteredScore = submission.enteredScore?.toFloat(),
                    grade = submission.grade,
                    score = submission.score,
                    excused = submission.excused.orDefault(),
                    gradingStatus = submission.status,
                    daysLate = if (submission.late == true) getDaysLate(submission.secondsLate) else null,
                )
            )
        }
    }*/

    private fun getGradeStatusName(status: CourseGradeStatus): String {
        return when (status) {
            CourseGradeStatus.late -> resources.getString(R.string.gradingStatus_late)
            CourseGradeStatus.missing -> resources.getString(R.string.gradingStatus_missing)
            CourseGradeStatus.extended -> resources.getString(R.string.gradingStatus_extended)
            CourseGradeStatus.excused -> resources.getString(R.string.gradingStatus_excused)
            else -> resources.getString(R.string.gradingStatus_none)
        }
    }

    private fun getGradeStatusName(status: SubmissionStatusTagType): String {
        return when (status) {
            SubmissionStatusTagType.late -> resources.getString(R.string.gradingStatus_late)
            SubmissionStatusTagType.missing -> resources.getString(R.string.gradingStatus_missing)
            SubmissionStatusTagType.extended -> resources.getString(R.string.gradingStatus_extended)
            SubmissionStatusTagType.excused -> resources.getString(R.string.gradingStatus_excused)
            SubmissionStatusTagType.custom -> resources.getString(R.string.gradingStatus_custom)
            else -> resources.getString(R.string.gradingStatus_none)
        }
    }

    private fun onScoreChanged(score: Float?, subAssignmentTag: String? = null) {
        val enteredScore = getGradableAssignmentByTag(subAssignmentTag)?.enteredScore
        val excused = getGradableAssignmentByTag(subAssignmentTag)?.excused
        if (excused == false && score == enteredScore) return
        debounceJob?.cancel()

        debounceJob = viewModelScope.launch {
            delay(300)
            try {
                repository.updateSubmissionGrade(
                    score = score?.toString() ?: resources.getString(R.string.not_graded),
                    studentId,
                    assignmentId,
                    courseId,
                    false,
                    subAssignmentTag
                )

                AssignmentGradedEvent(assignmentId).postSticky()

                gradingEventHandler.postEvent(GradingEvent.GradeChanged)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    return@launch
                }
                speedGraderErrorHolder.postError(
                    message = resources.getString(R.string.generalUnexpectedError),
                    retryAction = {
                        onScoreChanged(score, subAssignmentTag)
                    }
                )
            } finally {
                loadData(true)
            }
        }
    }

    private fun onPercentageChanged(percentage: Float?, subAssignmentTag: String? = null,) {
        val pointsPossible = getGradableAssignmentByTag(subAssignmentTag)?.pointsPossible
        val score = percentage?.let { (it / 100) * (pointsPossible ?: 0.0) }
        onScoreChanged(score?.toFloat(), subAssignmentTag)
    }

    private fun getGradableAssignmentByTag(tag: String?): GradableAssignment? {
        return _uiState.value.gradableAssignments.firstOrNull { it.tag == tag }
            ?: uiState.value.gradableAssignments.firstOrNull()
    }

    private fun getDaysLate(secondsLate: Double?): Float? {
        return secondsLate?.let {
            (it / (60 * 60 * 24)).toFloat()
        }
    }

    private fun onExcuse(subAssignmentTag: String? = null,) {
        viewModelScope.launch {
            try {
                repository.excuseSubmission(
                    studentId,
                    assignmentId,
                    courseId,
                    subAssignmentTag
                )

                AssignmentGradedEvent(assignmentId).postSticky()

                gradingEventHandler.postEvent(GradingEvent.GradeChanged)

                loadData(forceNetwork = true)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = true,
                        retryAction = {
                            onExcuse(subAssignmentTag)
                        }
                    )
                }
            }
        }
    }

    private fun onStatusChange(gradeStatus: GradeStatus, subAssignmentTag: String? = null,) {
        viewModelScope.launch {
            try {
                val submission = repository.updateSubmissionStatus(
                    submissionId.toLong(),
                    gradeStatus.id?.toString(),
                    gradeStatus.statusId,
                    subAssignmentTag
                ).updateSubmissionGradeStatus?.submission

                AssignmentGradedEvent(assignmentId).postSticky()

                gradingEventHandler.postEvent(GradingEvent.GradeChanged)

                _uiState.update {
                    it.copy(
                        error = false,
                        loading = false,
                        enteredScore = submission?.enteredScore?.toFloat(),
                        finalScore = submission?.score,
                        finalGrade = submission?.grade,
                        pointsDeducted = submission?.deductedPoints,
                        gradableAssignments = updateSubAssignments(it.gradableAssignments, submission?.subAssignmentSubmissions)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = true,
                        retryAction = { onStatusChange(gradeStatus, subAssignmentTag) }
                    )
                }
            }
        }
    }

    private fun updateSubAssignments(currentState: List<GradableAssignment>, subAssignments: List<SubAssignmentSubmission>?): List<GradableAssignment> {
        val updatedList = mutableListOf<GradableAssignment>()
        subAssignments?.forEach { subAssignment ->
            val current = currentState.firstOrNull { it.tag == subAssignment.subAssignmentTag }
            val updated = current?.copy(
                enteredGrade = subAssignment.enteredGrade,
                enteredScore = subAssignment.enteredScore?.toFloat(),
                grade = subAssignment.grade,
                score = subAssignment.score,
                excused = subAssignment.excused ?: current.excused,
                gradingStatus = subAssignment.statusTag.name.capitalized(),
                //customGradeStatusId = subAssignment.customGradeStatusId,
                /*gradingStatus = if (subAssignment.customGradeStatusId != null) {
                    null
                } else {
                    getGradeStatusName(subAssignment.statusTag)
                },*/
            )
            updated?.let {
                updatedList.add(it)
            }
        }
        return updatedList.ifEmpty { currentState }
    }

    private fun onLateDaysChange(lateDays: Float?) {
        daysLateDebounceJob?.cancel()
        val seconds = ((lateDays ?: 0f) * 60 * 60 * 24).roundToInt()
        if (lateDays == null) return
        daysLateDebounceJob = viewModelScope.launch {
            delay(300)
            try {
                repository.updateLateSecondsOverride(
                    studentId,
                    assignmentId,
                    courseId,
                    seconds
                )

                AssignmentGradedEvent(assignmentId).postSticky()

                loadData(forceNetwork = true)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        error = true,
                        retryAction = {
                            onLateDaysChange(lateDays)
                        }
                    )
                }
            }
        }
    }
}
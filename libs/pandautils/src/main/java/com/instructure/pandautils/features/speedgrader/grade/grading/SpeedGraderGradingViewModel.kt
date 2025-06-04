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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeedGraderGradingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderGradingRepository
) : ViewModel() {

    private val assignmentId = savedStateHandle.get<Long>("assignmentId")
        ?: throw IllegalArgumentException("Missing assignmentId")
    private val studentId = savedStateHandle.get<Long>("submissionId")
        ?: throw IllegalArgumentException("Missing studentId")

    private val _uiState =
        MutableStateFlow(
            SpeedGraderGradingUiState(
                onScoreChange = this::onScoreChanged,
                onPercentageChange = this::onPercentageChanged
            )
        )
    val uiState = _uiState.asStateFlow()

    private lateinit var submissionId: String

    private var debounceJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val submission = repository.getSubmissionGrade(assignmentId, studentId).submission
                ?: throw IllegalStateException("Submission not found")
            submissionId = submission._id
            _uiState.update {
                it.copy(
                    pointsPossible = submission.assignment?.pointsPossible,
                    score = submission.score,
                    grade = submission.grade,
                    lateText = submission.secondsLate?.let {
                        "$it seconds late"
                    },
                    enteredGrade = submission.enteredGrade,
                    enteredScore = submission.enteredScore?.toFloat(),
                    pointsDeducted = submission.deductedPoints,
                    gradingType = submission.assignment?.gradingType,
                    loading = false,
                    letterGrades = submission.assignment?.course?.gradingStandard?.data?.map { gradingStandard ->
                        LetterGrade(
                            gradingStandard.baseValue.orDefault(),
                            gradingStandard.letterGrade.orEmpty()
                        )
                    }.orEmpty()
                )
            }
        }
    }

    private fun onScoreChanged(score: Float?) {
        debounceJob?.cancel()
        score?.let {
            debounceJob = viewModelScope.launch {
                delay(500)
                val submission = repository.updateSubmissionGrade(
                    it.toDouble(),
                    submissionId.toLong()
                ).updateSubmissionGrade?.submission
                    ?: throw IllegalStateException("Submission not found")
                _uiState.update {
                    it.copy(
                        pointsPossible = submission.assignment?.pointsPossible,
                        score = submission.score,
                        grade = submission.grade,
                        lateText = submission.secondsLate?.let { late -> "$late seconds late" },
                        enteredGrade = submission.enteredGrade,
                        enteredScore = submission.enteredScore?.toFloat(),
                        pointsDeducted = submission.deductedPoints,
                        gradingType = submission.assignment?.gradingType
                    )
                }
            }
        }
    }

    private fun onPercentageChanged(percentage: Float?) {
        val score = percentage?.let { (it / 100) * (_uiState.value.pointsPossible ?: 0.0) }
        onScoreChanged(score?.toFloat())
    }
}
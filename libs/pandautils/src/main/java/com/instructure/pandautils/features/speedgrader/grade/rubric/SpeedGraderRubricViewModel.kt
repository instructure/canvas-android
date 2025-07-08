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
package com.instructure.pandautils.features.speedgrader.grade.rubric

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val assessmentPrefix = "rubric_assessment["
private const val ratingIdPostFix = "][rating_id]"
private const val pointsPostFix = "][points]"
private const val commentsPostFix = "][comments]"

@HiltViewModel
class SpeedGraderRubricViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SpeedGraderRubricRepository
) : ViewModel() {

    private val assignmentId: Long = savedStateHandle.get<Long>("assignmentId")
        ?: throw IllegalArgumentException("Missing assignmentId")
    private val userId: Long = savedStateHandle.get<Long>("submissionId")
        ?: throw IllegalArgumentException("Missing submissionId")
    private val courseId: Long =
        savedStateHandle.get<Long>("courseId") ?: throw IllegalArgumentException("Missing courseId")

    private val _uiState =
        MutableStateFlow(SpeedGraderRubricUiState(onRubricClick = this::saveRubricAssessment))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        _uiState.value = _uiState.value.copy(loading = true)
        try {
            val assessments = repository.getRubrics(
                assignmentId,
                userId
            ).submission?.rubricAssessmentsConnection?.edges?.map {
                it?.node
            }?.flatMap { it?.assessmentRatings ?: emptyList() }
                ?.associate { assessment ->
                    assessment.criterion?._id.orEmpty() to (assessment._id?.let { ratingId ->
                        RubricCriterionAssessment(
                            ratingId = ratingId,
                            points = assessment.points,
                            comments = assessment.comments
                        )
                    } ?: RubricCriterionAssessment())
                } ?: emptyMap()
            val assignment = repository.getAssignmentRubric(courseId, assignmentId)
            val rubrics = assignment.rubric
            val criterions = rubrics?.map {
                RubricCriterion(
                    id = it.id.orEmpty(),
                    longDescription = it.longDescription,
                    description = it.description,
                    points = it.points,
                    ratings = it.ratings.map { rating ->
                        RubricRating(
                            id = rating.id.orEmpty(),
                            description = rating.description.orEmpty(),
                            points = rating.points,
                            longDescription = rating.longDescription,
                        )
                    }
                )
            }
            _uiState.value = _uiState.value.copy(
                loading = false,
                criterions = criterions ?: emptyList(),
                assessments = assessments,
                hidePoints = assignment.rubricSettings?.hidePoints ?: false
            )
        } catch (e: Exception) {
            // Handle exceptions, possibly update UI state to show error
        } finally {
            _uiState.value = _uiState.value.copy(loading = false)
        }
    }

    private fun saveRubricAssessment(points: Double, criterionId: String, ratingId: String) {
        viewModelScope.launch {
            val originalAssessment = _uiState.value.assessments
            val assessments = originalAssessment.toMutableMap()
            assessments[criterionId] = RubricCriterionAssessment(
                ratingId,
                points
            )
            _uiState.update {
                it.copy(assessments = assessments)
            }
            val rubricAssessmentMap = generateRubricAssessmentQueryMap(assessments)
            try {
                repository.postSubmissionRubricAssessment(
                    courseId,
                    assignmentId,
                    userId,
                    rubricAssessmentMap
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(assessments = originalAssessment)
                }
            }
        }
    }

    private fun generateRubricAssessmentQueryMap(rubricAssessment: Map<String, RubricCriterionAssessment>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for ((criterionIdKey, ratingValue) in rubricAssessment) {
            ratingValue.points?.let {
                map[assessmentPrefix + criterionIdKey + pointsPostFix] = it.toString()
            }
            ratingValue.ratingId?.let {
                map[assessmentPrefix + criterionIdKey + ratingIdPostFix] = it
            }
            map[assessmentPrefix + criterionIdKey + commentsPostFix] =
                ratingValue.comments.orEmpty()
        }
        return map
    }
}
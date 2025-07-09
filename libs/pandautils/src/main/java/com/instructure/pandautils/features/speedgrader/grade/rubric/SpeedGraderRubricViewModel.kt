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
import com.instructure.pandautils.utils.orDefault
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
        MutableStateFlow(SpeedGraderRubricUiState(onRubricSelected = this::saveRubricAssessment, onPointChanged = this::onPointChanged))
    val uiState = _uiState.asStateFlow()

    private var debouncePointChangeJob: Job? = null

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
                    useRange = it.criterionUseRange,
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

    private fun saveRubricAssessment(points: Double?, criterionId: String, ratingId: String?) {
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

    private fun onPointChanged(points: Double, criterionId: String) {
        debouncePointChangeJob?.cancel()
        debouncePointChangeJob = viewModelScope.launch {
            delay(500)
            val criterion = _uiState.value.criterions.find { it.id == criterionId }
                ?: return@launch
            val ratings = criterion.ratings.reversed()

            if (criterion.useRange) {
                var selectedRating: RubricRating? = null
                var previousRatingPoints = 0.0

                for (rating in ratings) {
                    if (points > previousRatingPoints && points <= rating.points.orDefault()) {
                        selectedRating = rating
                        break
                    }
                    previousRatingPoints = rating.points.orDefault()
                }

                if (selectedRating == null && points > 0 && ratings.isNotEmpty()) {
                    if (points > ratings.last().points.orDefault()) {
                        selectedRating = ratings.last()
                    }
                }

                selectedRating?.let { rating ->
                    saveRubricAssessment(points, criterionId, rating.id)
                } ?: run {
                    saveRubricAssessment(null, criterionId, null)
                }

            } else {
                val selectedRating = ratings.find { it.points == points }
                selectedRating?.let { rating ->
                    saveRubricAssessment(rating.points.orDefault(), criterionId, rating.id)
                } ?: run {
                    saveRubricAssessment(points, criterionId, null)
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
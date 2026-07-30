/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.ngc.features.assignmentdetails

import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.utils.NumberHelper

/**
 * Read-only rubric view data for the NGC Assignment Details bottom sheet. One [NgcRubricCriterion]
 * per rubric criterion; the rating the student was assessed at (if any) is flagged so the UI can
 * highlight it. Built by [buildNgcRubric] from the assignment's rubric + the submission's assessment,
 * mirroring the Academic `SubmissionRubricPresenter` mapping (assessed rating matched by rating id,
 * `hidePoints`, free-form, and custom-score handling).
 */
data class NgcRubricUiState(
    val criteria: List<NgcRubricCriterion>,
)

data class NgcRubricCriterion(
    val title: String,
    // criterion long description, shown under the title; null when blank.
    val description: String?,
    val ratings: List<NgcRubricRating>,
    // Formatted assessed points, null when the criterion isn't scored (or points are hidden).
    val assessedScore: String?,
    // Formatted criterion max points (the "/ Y pts" denominator).
    val maxScore: String,
    // Teacher's per-criterion feedback comment; null when blank/absent.
    val comment: String?,
)

data class NgcRubricRating(
    // Points chip label; null when points are hidden.
    val pointsLabel: String?,
    // Rating title (e.g. "Excellent"); null for free-form.
    val title: String?,
    // Rating detail; null for free-form.
    val detail: String?,
    val isAssessed: Boolean,
)

// Synthetic id for a custom/range score with no matching predefined rating (mirrors the Academic presenter).
private const val CUSTOM_RATING_ID = "_custom_rating_id_"

/**
 * Builds the NGC rubric view data, or null when the assignment has no rubric.
 * @param restrictQuantitativeData course-level setting; combined with the rubric's hidePoints.
 */
fun buildNgcRubric(assignment: Assignment?, restrictQuantitativeData: Boolean): NgcRubricUiState? {
    val criteria = assignment?.rubric.orEmpty()
    if (criteria.isEmpty()) return null

    val assessments = assignment?.submission?.rubricAssessment ?: hashMapOf()
    val hidePoints = assignment?.rubricSettings?.hidePoints == true || restrictQuantitativeData
    val freeForm = assignment?.freeFormCriterionComments == true

    return NgcRubricUiState(
        criteria = criteria.map { mapCriterion(it, assessments[it.id], hidePoints, freeForm) },
    )
}

private fun mapCriterion(
    criterion: RubricCriterion,
    rawAssessment: RubricCriterionAssessment?,
    hidePoints: Boolean,
    freeForm: Boolean,
): NgcRubricCriterion {
    var assessment = rawAssessment
    var ratings = criterion.ratings.toList()

    // A custom (no rating id) or range-based score gets a synthesized rating so it appears in the
    // list at the right position once sorted by points.
    if (assessment != null) {
        if (assessment.ratingId == null || assessment.ratingId == "null") {
            assessment = assessment.copy(ratingId = CUSTOM_RATING_ID)
            ratings = ratings + RubricCriterionRating(id = CUSTOM_RATING_ID, points = assessment.points ?: 0.0)
        } else if (criterion.criterionUseRange) {
            val assessed = ratings.firstOrNull { it.id == assessment!!.ratingId }
            if (assessed != null && assessment.points != assessed.points) {
                assessment = assessment.copy(ratingId = CUSTOM_RATING_ID)
                ratings = ratings + RubricCriterionRating(
                    id = CUSTOM_RATING_ID,
                    description = assessed.description,
                    longDescription = assessed.longDescription,
                    points = assessment.points ?: assessed.points,
                )
            }
        }
    }
    ratings = ratings.sortedBy { it.points }

    val assessedRating = if (assessment?.points == null) null else ratings.find { it.id == assessment.ratingId }

    // Free-form assessments only surface the assessed rating.
    if (freeForm) {
        ratings = ratings.filter { it.id == assessment?.ratingId }
    }

    val ratingData = ratings.map { rating ->
        NgcRubricRating(
            pointsLabel = if (hidePoints) null else NumberHelper.formatDecimal(rating.points, 2, true),
            title = if (freeForm) null else rating.description,
            detail = if (freeForm) null else rating.longDescription,
            isAssessed = rating.id == assessedRating?.id,
        )
    }

    val assessedScore = assessment?.points
        ?.takeIf { !hidePoints }
        ?.let { NumberHelper.formatDecimal(it, 2, true) }

    return NgcRubricCriterion(
        title = criterion.description.orEmpty(),
        description = criterion.longDescription?.takeIf { it.isNotBlank() },
        ratings = ratingData,
        assessedScore = assessedScore,
        maxScore = NumberHelper.formatDecimal(criterion.points, 2, true),
        comment = assessment?.comments?.takeIf { it.isNotBlank() },
    )
}

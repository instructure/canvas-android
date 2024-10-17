/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric

import android.content.Context
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.pandautils.utils.color
import com.instructure.student.R
import com.instructure.student.mobius.common.ui.Presenter

object SubmissionRubricPresenter : Presenter<SubmissionRubricModel, SubmissionRubricViewState> {

    const val customRatingId = "_custom_rating_id_"

    override fun present(model: SubmissionRubricModel, context: Context): SubmissionRubricViewState {
        val rubric = model.assignment.rubric ?: emptyList()
        val assessments = model.submission.rubricAssessment

        // Show empty state if the assignment does not use a rubric
        if (rubric.isEmpty()) return SubmissionRubricViewState(listOf(RubricListData.Empty))

        val items = mutableListOf<RubricListData>()

        // Show the grade cell only if the submission is graded and the rubric is used for grading
        if (model.submission.isGraded && model.assignment.isUseRubricForGrading) {
            val gradeState = GradeCellViewState.fromSubmission(context, model.assignment, model.submission, model.restrictQuantitativeData)
            items += RubricListData.Grade(gradeState)
        }

        // Map the rubric criteria to view state data
        items += rubric.map { criterion ->
            mapCriterion(assessments, criterion, context, model)
        }

        return SubmissionRubricViewState(items)
    }

    private fun mapCriterion(
        assessments: HashMap<String, RubricCriterionAssessment>,
        criterion: RubricCriterion,
        context: Context,
        model: SubmissionRubricModel
    ): RubricListData.Criterion {
        var assessment = assessments[criterion.id]
        var ratings = criterion.ratings.toList()

        /*
         * If this is a custom assessment (i.e. there is no associated rating ID) or if it uses ranges, we assign it a
         * custom rating ID and add a matching rating to the ratings list. We then sort that list by point value
         * to ensure the new rating appears in the correct position.
         */
        if (assessment != null) {
            if (assessment.ratingId == null || assessment.ratingId == "null") {
                assessment = assessment.copy(ratingId = customRatingId)
                ratings = ratings.plus(
                    RubricCriterionRating(
                        id = customRatingId,
                        description = context.getString(R.string.rubricCustomScore),
                        points = assessment.points ?: 0.0
                    )
                )
            } else if (criterion.criterionUseRange) {
                val assessedRating = ratings.firstOrNull { it.id == assessment!!.ratingId }
                if (assessedRating != null && assessment.points != assessedRating.points) {
                    assessment = assessment.copy(ratingId = customRatingId)
                    ratings = ratings.plus(
                        RubricCriterionRating(
                            id = customRatingId,
                            description = assessedRating.description,
                            longDescription = assessedRating.longDescription,
                            points = assessment.points ?: assessedRating.points
                        )
                    )
                }
            }
        }
        ratings = ratings.sortedBy { it.points }

        // Find the criterion rating that matches the assessment rating (if there are valid points)
        val assessedRating = if (assessment?.points == null) null else ratings.find { it.id == assessment.ratingId }

        val selectedRatingId = model.selectedRatingMap[criterion.id] ?: assessedRating?.id

        // If points are hidden, show the rating title instead of points
        val hidePoints = model.assignment.rubricSettings?.hidePoints == true || model.restrictQuantitativeData

        // Free-form assessments should only show the assessment comments and the matching assessment rating
        if (model.assignment.freeFormCriterionComments) {
            ratings = ratings
                .filter { it.id == assessment?.ratingId && !hidePoints}
                .map { it.copy(description = null) }
        }

        // Map criterion ratings to view state data
        var ratingData = ratings.map { rating ->
            RatingData(
                id = rating.id.orEmpty(),
                text = if (hidePoints) rating.description.orEmpty() else NumberHelper.formatDecimal(rating.points, 2, true),
                isSelected = rating.id == selectedRatingId,
                isAssessed = rating.id == assessedRating?.id,
                useSmallText = hidePoints
            )
        }

        // The rating for free-form assessments should include the total points
        val isFreeForm = model.assignment.freeFormCriterionComments
        if (isFreeForm) {
            ratingData = ratingData.map {
                it.copy(
                    text = String.format(
                        context.getString(
                            R.string.rangedRubricTotal,
                            it.text,
                            NumberHelper.formatDecimal(criterion.points, 2, true)
                        )
                    )
                )
            }
        }

        val selectedRating = ratings.find { it.id == selectedRatingId }

        return RubricListData.Criterion(
            title = criterion.description.orEmpty(),
            criterionId = criterion.id!!,
            showDescriptionButton = criterion.longDescription.isValid(),
            ratings = ratingData,
            comment = assessment?.comments.validOrNull(),
            ratingTitle = if (isFreeForm) null else selectedRating?.description,
            ratingDescription = if (isFreeForm) null else selectedRating?.longDescription,
            tint = CanvasContext.emptyCourseContext(model.assignment.courseId).color
        )
    }

}

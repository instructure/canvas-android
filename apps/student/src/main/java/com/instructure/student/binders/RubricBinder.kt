/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.binders

import android.content.Context
import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.RubricCriterion
import com.instructure.canvasapi2.models.RubricCriterionAssessment
import com.instructure.canvasapi2.models.RubricCriterionRating
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.student.R
import com.instructure.student.holders.RubricViewHolder
import com.instructure.student.model.RubricCommentItem
import com.instructure.student.model.RubricItem
import com.instructure.student.model.RubricRatingItem
import java.util.*

class RubricBinder : BaseBinder() {
    companion object {

        fun bind(
                context: Context,
                holder: RubricViewHolder,
                rubricItem: RubricItem,
                criterion: RubricCriterion,
                isFreeForm: Boolean,
                shouldHidePoints: Boolean,
                assessment: RubricCriterionAssessment?,
                canvasContext: CanvasContext) {
            if (holder.rubricType == RubricViewHolder.TYPE_ITEM_COMMENT) {
                val comment = (rubricItem as RubricCommentItem).comment
                val color = ColorKeeper.getOrGenerateColor(canvasContext)
                val d = ColorKeeper.getColoredDrawable(context, R.drawable.vd_chat, color)

                holder.descriptionView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null)
                holder.descriptionView.text = comment

                if (!isFreeForm || shouldHidePoints) {
                    holder.pointView.visibility = View.GONE
                } else {
                    holder.pointView.visibility = View.VISIBLE
                    holder.pointView.text = getScoreText(context, assessment?.points ?: 0.0, criterion.points, true, criterion)
                }

            } else {
                val rating = (rubricItem as RubricRatingItem).rating
                val isGrade = assessment != null && isRubricGraded(assessment, rubricItem, criterion)
                val color = getColor(isGrade, criterion, rating, context, canvasContext)

                holder.descriptionView.text = rating.description
                holder.descriptionView.setTextColor(color)
                holder.descriptionView.setTypeface(null, if(isGrade) Typeface.BOLD else Typeface.NORMAL)

                holder.checkmark.setBackgroundColor(color)

                if (shouldHidePoints) {
                    holder.pointView.visibility = View.GONE
                } else {
                    holder.pointView.visibility = View.VISIBLE
                    holder.pointView.text = getScoreText(context, rating.points, rating.points, isFreeForm, criterion, rating)
                    holder.pointView.setTextColor(color)

                    // Set the content description
                    holder.pointView.contentDescription = getScoreText(context, rating.points, rating.points, isFreeForm, criterion, rating, true)

                    // Make it bold if it's the grade
                    holder.pointView.setTypeface(null, if (isGrade) Typeface.BOLD else Typeface.NORMAL)
                }
            }
        }

        private fun getColor(isGrade: Boolean, criterion: RubricCriterion, rating: RubricCriterionRating, context: Context, canvasContext: CanvasContext): Int {
            return if (isGrade) {
                // if we're using rubric range, we only want the points summary to be the course color
                if ((criterion.criterionUseRange && rating.id?.contains("null") == true) || !criterion.criterionUseRange) {
                    ColorKeeper.getOrGenerateColor(canvasContext)
                } else {
                    ContextCompat.getColor(context, R.color.canvasTextMedium)
                }
            } else ContextCompat.getColor(context, R.color.canvasTextMedium)
        }

        /*
         * We need to check which criterion needs to be highlighted so the user can know how they did
         */
        private fun isRubricGraded(assessment: RubricCriterionAssessment, ratingItem: RubricRatingItem, criterion: RubricCriterion) : Boolean {
            if (criterion.criterionUseRange && assessment.points != null) {

                // Check if it's the total points row that we added
                if (ratingItem.rating.id?.contains("null") == true) {
                    return true
                }

                val index = criterion.ratings.indexOfFirst { rubricCriterionRating -> rubricCriterionRating.points == ratingItem.rating.points } + 1

                return if (index >= 1 && index < criterion.ratings.size) {
                    val firstPoints = criterion.ratings[index - 1].points
                    val secondPoints = criterion.ratings[index].points
                    // Check to see if the current points is in the range of the criterion
                    assessment.points!! <= firstPoints && assessment.points!! > secondPoints
                } else {

                    // If it's less than the last rubric score, the last criterion needs to be highlighted
                    assessment.points!! <= criterion.ratings[criterion.ratings.size - 1].points
                }
            } else {
                return assessment.points == ratingItem.rating.points
            }
        }

        private fun getScoreText(context: Context, value: Double, maxValue: Double, isFreeForm: Boolean, criterion: RubricCriterion, rating: RubricCriterionRating? = null, isContentDescription: Boolean = false): String {
            fun formatted(num: Double) = NumberHelper.formatDecimal(num, 1, true)

            var points = when {
                isFreeForm -> {
                    String.format(Locale.getDefault(),
                        context.getString(R.string.freeFormRubricPoints),
                        formatted(value),
                        formatted(maxValue))
                }
                else -> formatted(value)
            }

            if (criterion.criterionUseRange) {

                // Check if it's the total points row that we added
                if (rating?.id?.contains("null") == true) {
                    // Get total points for criterion
                    val criterionPoints = criterion.ratings[0].points
                    return if (isContentDescription) {
                        String.format(Locale.getDefault(),
                            context.getString(R.string.contentDescriptionRubricRangeGrade),
                            points,
                            formatted(criterionPoints))
                    } else {
                        String.format(Locale.getDefault(),
                            context.getString(R.string.rangedRubricTotal),
                            points,
                            formatted(criterionPoints))
                    }
                }

                // Get the current index of the rating
                val index = criterion.ratings.indexOfFirst { rubricCriterionRating -> rubricCriterionRating.points == maxValue } + 1

                if (index >= 1 && index < criterion.ratings.size) {
                    var nextPoints =""
                    val criterionPoints = criterion.ratings[index].points
                    if(Math.floor(criterionPoints) == criterionPoints) nextPoints += criterionPoints.toInt()
                    if (isContentDescription) {
                        return String.format(Locale.getDefault(),
                                context.getString(R.string.contentDescriptionRubricRangePointsText),
                                points,
                                formatted(criterionPoints))
                    } else {
                        points += String.format(Locale.getDefault(),
                                context.getString(R.string.rubricRangePointsText),
                                formatted(criterionPoints))
                    }
                } else {

                    // add ' > 0 pts' on the end
                    if (isContentDescription) {
                        return String.format(Locale.getDefault(),
                                context.getString(R.string.contentDescriptionRubricRangePointsText),
                                points,
                                formatted(0.0))
                    } else {
                        points += String.format(Locale.getDefault(),
                                context.getString(R.string.rubricRangePointsText),
                                formatted(0.0))
                    }
                }
            } else {
                points = String.format(Locale.getDefault(),
                        context.getString(R.string.totalPoints), points)
            }
            return points
        }
    }
}

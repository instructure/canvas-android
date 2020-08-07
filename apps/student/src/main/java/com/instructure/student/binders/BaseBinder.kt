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
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.util.StringUtilities

open class BaseBinder {
    companion object {

        private const val NO_GRADE_INDICATOR = "-"

        fun setVisible(v: View?) = v?.setVisible()

        fun setInvisible(v: View?) = v?.setInvisible()

        fun setGone(v: View?) = v?.setGone()

        fun getHtmlAsText(html: String?): String? {
            return if (!html.isNullOrBlank()) {
                StringUtilities.simplifyHTML(Html.fromHtml(html))
            } else null
        }

        // Format the points possible field
        fun getPointsPossible(points_possible: Double): String {
            return NumberHelper.formatDecimal(points_possible, 2, true)
        }

        fun getGrade(assignment: Assignment, submission: Submission?, context: Context): DisplayGrade {
            val possiblePoints = assignment.pointsPossible
            val pointsPossibleText = getPointsPossible(possiblePoints)

            // No submission
            if (submission == null) {
                return if (possiblePoints > 0) {
                    DisplayGrade(
                        context.getString(
                            R.string.gradeFormatScoreOutOfPointsPossible,
                            NO_GRADE_INDICATOR,
                            pointsPossibleText
                        ),
                        context.getString(R.string.outOfPointsFormatted, pointsPossibleText)
                    )
                } else {
                    DisplayGrade(NO_GRADE_INDICATOR, "")
                }
            }

            // Excused
            if (submission.excused) {
                return DisplayGrade(
                    context.getString(
                        R.string.gradeFormatScoreOutOfPointsPossible,
                        context.getString(R.string.excused),
                        pointsPossibleText
                    ),
                    context.getString(
                        R.string.contentDescriptionScoreOutOfPointsPossible,
                        context.getString(R.string.gradeExcused),
                        pointsPossibleText
                    )
                )
            }

            val grade = submission.grade ?: return DisplayGrade()
            val gradeContentDescription = getContentDescriptionForMinusGradeString(grade, context).validOrNull() ?: grade

            val gradingType = Assignment.getGradingTypeFromAPIString(assignment.gradingType.orEmpty())

            /*
             * For letter grade or GPA scale grading types, format grade text as "score / pointsPossible (grade)" to
             * more closely match web, e.g. "15 / 20 (2.0)" or "80 / 100 (B-)".
             */
            if (gradingType == Assignment.GradingType.LETTER_GRADE || gradingType == Assignment.GradingType.GPA_SCALE) {
                val scoreText = NumberHelper.formatDecimal(submission.score, 2, true)
                val possiblePointsText = NumberHelper.formatDecimal(possiblePoints, 2, true)
                return DisplayGrade(
                    context.getString(
                        R.string.formattedScoreWithPointsPossibleAndGrade,
                        scoreText,
                        possiblePointsText,
                        grade
                    ),
                    context.getString(
                        R.string.contentDescriptionScoreWithPointsPossibleAndGrade,
                        scoreText,
                        possiblePointsText,
                        gradeContentDescription
                    )
                )
            }

            // Numeric grade
            submission.grade?.toDoubleOrNull()?.let { parsedGrade ->
                val formattedGrade = NumberHelper.formatDecimal(parsedGrade, 2, true)
                return DisplayGrade(
                    context.getString(
                        R.string.gradeFormatScoreOutOfPointsPossible,
                        formattedGrade,
                        pointsPossibleText
                    ),
                    context.getString(
                        R.string.contentDescriptionScoreOutOfPointsPossible,
                        formattedGrade,
                        pointsPossibleText
                    )
                )
            }

            // Complete/incomplete
            return when(grade) {
                "complete" -> return DisplayGrade(context.getString(R.string.gradeComplete))
                "incomplete" -> return DisplayGrade(context.getString(R.string.gradeIncomplete))
                else -> DisplayGrade(grade, gradeContentDescription)
            }
        }

        fun setupGradeText(context: Context?, textView: TextView?, assignment: Assignment?, submission: Submission?, color: Int) {
            if (context == null || textView == null || assignment == null || submission == null) {
                return
            }

            val hasGrade = submission.grade.isValid()
            val (grade, contentDescription) = getGrade(assignment, submission, context)
            if (hasGrade) {
                textView.text = grade
                textView.contentDescription = contentDescription
                textView.setTextAppearance(context, R.style.TextStyle_Grade)
                textView.setBackgroundDrawable(createGradeIndicatorBackground(context, color))
            } else {
                textView.text = grade
                textView.setTextAppearance(context, R.style.TextStyle_NoGrade)
                textView.setBackgroundDrawable(null)
                textView.contentDescription = grade
            }
        }

        fun createIndicatorBackground(color: Int): ShapeDrawable {
            val circle = ShapeDrawable(OvalShape())
            circle.paint.color = color
            return circle
        }

        private fun createGradeIndicatorBackground(context: Context, color: Int): Drawable {
            val shape = context.resources.getDrawable(R.drawable.grade_background)
            return ColorUtils.colorIt(color, shape)
        }

        fun setCleanText(textView: TextView, text: String?) {
            if (!text.isNullOrBlank()) {
                textView.text = text
            } else {
                textView.text = ""
            }
        }

        fun getAssignmentIcon(assignment: Assignment?): Int {
            if (assignment == null) return 0

            return when {
                assignment.getSubmissionTypes().contains(Assignment.SubmissionType.ONLINE_QUIZ) -> R.drawable.vd_quiz
                assignment.getSubmissionTypes().contains(Assignment.SubmissionType.DISCUSSION_TOPIC) -> R.drawable.vd_discussion
                else -> R.drawable.vd_assignment
            }
        }

        fun updateShadows(isFirstItem: Boolean, isLastItem: Boolean, top: View, bottom: View) {
            if (isFirstItem) {
                setVisible(top)
            } else {
                setInvisible(top)
            }

            if (isLastItem) {
                setVisible(bottom)
            } else {
                setInvisible(bottom)
            }
        }
    }
}

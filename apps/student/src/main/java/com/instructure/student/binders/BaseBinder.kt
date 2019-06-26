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
import com.instructure.pandautils.utils.ColorUtils
import com.instructure.student.R
import com.instructure.student.util.StringUtilities

open class BaseBinder {
    companion object {

        private const val NO_GRADE_INDICATOR = "-"

        fun setVisible(v: View?) {
            if (v == null) return
            v.visibility = View.VISIBLE
        }

        fun setInvisible(v: View?) {
            if (v == null) return
            v.visibility = View.INVISIBLE
        }

        fun setGone(v: View?) {
            if (v == null) return
            v.visibility = View.GONE
        }

        fun ifHasTextSetVisibleElseGone(v: TextView?) {
            if (v == null) return
            if (TextUtils.isEmpty(v.text)) {
                setGone(v)
            } else {
                setVisible(v)
            }
        }

        fun getHtmlAsText(html: String?): String? {
            return if (!html.isNullOrBlank()) {
                StringUtilities.simplifyHTML(Html.fromHtml(html))
            } else null
        }

        // Format the points possible field
        fun getPointsPossible(points_possible: Double): String {
            return NumberHelper.formatDecimal(points_possible, 2, true)
        }

        fun getGrade(submission: Submission?, possiblePoints: Double, context: Context): String? {
            // No submission
            if (submission == null) {
                return if (possiblePoints > 0) {
                    NO_GRADE_INDICATOR + "/" + getPointsPossible(possiblePoints)
                } else {
                    NO_GRADE_INDICATOR
                }
            }

            // Excused
            if (submission.excused) {
                return context.getString(R.string.excused) + "/" + getPointsPossible(possiblePoints)
            }

            val grade = submission.grade ?: return submission.grade

            // Numeric grade
            if (StringUtilities.isStringNumeric(submission.grade!!, true)) {
                val parsedGrade = grade.toDoubleOrNull()
                val formattedGrade = when {
                    parsedGrade != null -> NumberHelper.formatDecimal(parsedGrade, 2, true)
                    '.' in grade -> grade.take(grade.lastIndexOf('.') + 3)
                    else -> grade
                }
                return formattedGrade + "/" + getPointsPossible(possiblePoints)
            }

            // Complete/incomplete
            return when(grade) {
                "complete" -> return context.getString(R.string.gradeComplete)
                "incomplete" -> return context.getString(R.string.gradeIncomplete)
                else -> grade
            }
        }

        private fun hasGrade(submission: Submission?): Boolean {
            return if (submission != null) {
                !TextUtils.isEmpty(submission.grade)
            } else false
        }

        fun setupGradeText(context: Context?, textView: TextView?, assignment: Assignment?, submission: Submission?, color: Int) {
            if (context == null || textView == null || assignment == null || submission == null) {
                return
            }

            val hasGrade = hasGrade(submission)
            val grade = getGrade(submission, assignment.pointsPossible, context)
            if (hasGrade) {
                textView.text = grade
                textView.setTextAppearance(context, R.style.TextStyle_Grade)
                textView.setBackgroundDrawable(createGradeIndicatorBackground(context, color))
                // Set accessibility text
                val outOf = context.resources.getString(R.string.outOf)
                textView.contentDescription = grade?.replace("/", " $outOf ")

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

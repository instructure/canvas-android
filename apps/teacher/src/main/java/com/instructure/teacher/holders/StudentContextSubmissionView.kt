/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
 */
package com.instructure.teacher.holders

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.type.SubmissionGradingStatus
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.utils.getAssignmentIcon
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.getDisplayGrade
import com.instructure.teacher.utils.getResForSubmission
import kotlinx.android.synthetic.main.adapter_student_context_submission.view.*
import java.util.Locale


@SuppressLint("ViewConstructor")
class StudentContextSubmissionView(context: Context, submission: StudentContextCardQuery.Submission, courseColor: Int) : FrameLayout(context) {

    val assignment = requireNotNull(submission.assignment)

    init {
        View.inflate(context, R.layout.adapter_student_context_submission, this)

        // Title, icon, and publish status
        assignmentTitle.text = assignment.name
        assignmentIcon.setImageResource(assignment.submissionTypes.getAssignmentIcon())
        assignmentIcon.setColorFilter(courseColor)

        // Submission status
        val (stringRes, colorRes) = getResForSubmission(submission.submissionStatus)
        if (stringRes == -1 || colorRes == -1) {
            submissionStatus.setGone()
        } else {
            submissionStatus.setText(stringRes)
            submissionStatus.setTextColor(context.getColorCompat(colorRes))
        }

        // Submission grade
        if (submission.gradingStatus == SubmissionGradingStatus.EXCUSED || submission.gradingStatus == SubmissionGradingStatus.GRADED) {
            val pointsPossible = submission.assignment?.pointsPossible ?: 0.0
            val displayGrade = getDisplayGrade(
                context = context,
                gradingStatus = submission.gradingStatus,
                gradingType = submission.assignment?.gradingType?.name?.lowercase(Locale.getDefault())
                    .orEmpty(),
                grade = submission.grade,
                enteredGrade = "",
                score = submission.score,
                enteredScore = 0.0,
                pointsPossible = pointsPossible,
                includePointsPossible = false,
                includeLatePenalty = false
            )
            submissionGradeView.text = displayGrade.text
            submissionGradeView.contentDescription = displayGrade.contentDescription
            scoreBar.progress = ((submission.score ?: 0.0) / pointsPossible).toFloat()
        } else {
            submissionGradeContainer.setGone()
            if (submission.gradingStatus == SubmissionGradingStatus.NEEDS_GRADING) {
                val submissionGradeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_generic_pill)
                val strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics)
                (submissionGradeDrawable as GradientDrawable).setStroke(strokeWidth.toInt(), ThemePrefs.brandColor)
                needsGradingPill.background = submissionGradeDrawable
                needsGradingPill.setTextColor(ThemePrefs.brandColor)
                needsGradingPill.setVisible()
            }
        }

    }

}

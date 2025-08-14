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
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.StudentContextCardQuery
import com.instructure.canvasapi2.type.SubmissionGradingStatus
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.teacher.R
import com.instructure.teacher.databinding.AdapterStudentContextSubmissionBinding
import com.instructure.teacher.utils.getAssignmentIcon
import com.instructure.teacher.utils.getColorCompat
import com.instructure.teacher.utils.getDisplayGrade
import com.instructure.teacher.utils.getResForSubmission
import java.util.Locale


@SuppressLint("ViewConstructor")
class StudentContextSubmissionView(context: Context, submission: StudentContextCardQuery.Submission, courseColor: Int) : FrameLayout(context) {

    val assignment = requireNotNull(submission.assignment)

    init {
        val binding = AdapterStudentContextSubmissionBinding.inflate(LayoutInflater.from(context), this, true)

        // Title, icon, and publish status
        binding.assignmentTitle.text = assignment.name
        binding.assignmentIcon.setImageResource(assignment.submissionTypes.getAssignmentIcon())
        binding.assignmentIcon.setColorFilter(courseColor)

        // Submission status
        if (!submission.customGradeStatus.isNullOrEmpty()) {
            binding.submissionStatus.text = submission.customGradeStatus
            binding.submissionStatus.setTextColor(context.getColorCompat(R.color.textInfo))
        } else {
            val (stringRes, colorRes) = getResForSubmission(submission.submissionStatus)
            if (stringRes == -1 || colorRes == -1) {
                binding.submissionStatus.setGone()
            } else {
                binding.submissionStatus.setText(stringRes)
                binding.submissionStatus.setTextColor(context.getColorCompat(colorRes))
            }
        }

        // Submission grade
        if (submission.gradingStatus == SubmissionGradingStatus.excused ||
            submission.gradingStatus == SubmissionGradingStatus.graded ||
            !submission.customGradeStatus.isNullOrEmpty()
        ) {
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
            binding.submissionGradeView.text = displayGrade.text
            binding.submissionGradeView.contentDescription = displayGrade.contentDescription
            binding.scoreBar.progress = ((submission.score ?: 0.0) / pointsPossible).toFloat()
        } else {
            binding.submissionGradeContainer.setGone()
            if (submission.gradingStatus == SubmissionGradingStatus.needs_grading) {
                val submissionGradeDrawable = ContextCompat.getDrawable(context, R.drawable.bg_generic_pill)
                val strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, context.resources.displayMetrics)
                (submissionGradeDrawable as GradientDrawable).setStroke(strokeWidth.toInt(), ThemePrefs.brandColor)
                binding.needsGradingPill.background = submissionGradeDrawable
                binding.needsGradingPill.setTextColor(ThemePrefs.brandColor)
                binding.needsGradingPill.setVisible()
            }
        }
    }
}

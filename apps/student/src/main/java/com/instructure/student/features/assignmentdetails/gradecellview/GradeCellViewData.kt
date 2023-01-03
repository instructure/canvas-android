package com.instructure.student.features.assignmentdetails.gradecellview

import android.content.res.Resources
import android.graphics.Color
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.pandautils.utils.getContentDescriptionForMinusGradeString
import com.instructure.pandautils.utils.orDefault
import com.instructure.pandautils.utils.textAndIconColor
import com.instructure.student.R

data class GradeCellViewData(
    val state: State,
    @ColorInt val tintColor: Int = Color.GRAY,
    val chartPercent: Float = 0f,
    val showCompleteIcon: Boolean = false,
    val showIncompleteIcon: Boolean = false,
    val showPointsLabel: Boolean = false,
    val score: String = "",
    val grade: String = "",
    val gradeContentDescription: String = "",
    val gradeCellContentDescription: String = "",
    val outOf: String = "",
    val outOfContentDescription: String = "",
    val latePenalty: String = "",
    val finalGrade: String = ""
) {
    enum class State {
        EMPTY,
        SUBMITTED,
        GRADED,
        UPLOADING,
        FAILED
    }

    companion object {
        @Suppress("DEPRECATION")
        fun fromSubmission(
            resources: Resources,
            assignment: Assignment?,
            submission: Submission?,
            uploading: Boolean = false,
            failed: Boolean = false
        ): GradeCellViewData {
            return if (uploading) {
                GradeCellViewData(State.UPLOADING)
            } else if (failed) {
                GradeCellViewData(State.FAILED)
            } else if (
                assignment == null
                || submission == null
                || (submission.submittedAt == null && !submission.isGraded)
                || assignment.gradingType == Assignment.NOT_GRADED_TYPE
            ) {
                GradeCellViewData(State.EMPTY)
            } else if (submission.submittedAt != null && !submission.isGraded && !submission.excused) {
                GradeCellViewData(State.SUBMITTED)
            } else {
                val tintColor = CanvasContext.emptyCourseContext(assignment.courseId).textAndIconColor
                val pointsPossibleText = NumberHelper.formatDecimal(assignment.pointsPossible, 2, true)
                val outOfText = resources.getString(R.string.outOfPointsAbbreviatedFormatted, pointsPossibleText)
                val outOfContentDescriptionText = resources.getString(R.string.outOfPointsFormatted, pointsPossibleText)

                if (submission.excused) {
                    GradeCellViewData(
                        state = State.GRADED,
                        tintColor = tintColor,
                        chartPercent = 1f,
                        showCompleteIcon = true,
                        grade = resources.getString(R.string.excused),
                        outOf = outOfText,
                        outOfContentDescription = outOfContentDescriptionText
                    )
                } else if (assignment.gradingType == Assignment.PASS_FAIL_TYPE) {
                    val isComplete = (submission.grade == "complete")
                    GradeCellViewData(
                        state = State.GRADED,
                        tintColor = if (isComplete) tintColor else resources.getColor(R.color.textDark),
                        chartPercent = 1f,
                        showCompleteIcon = isComplete,
                        showIncompleteIcon = !isComplete,
                        grade = resources.getString(if (isComplete) R.string.gradeComplete else R.string.gradeIncomplete),
                        outOf = outOfText,
                        outOfContentDescription = outOfContentDescriptionText
                    )
                } else {
                    val score = NumberHelper.formatDecimal(submission.enteredScore, 2, true)
                    val chartPercent = (submission.enteredScore / assignment.pointsPossible).coerceIn(0.0, 1.0).toFloat()
                    // If grading type is Points, don't show the grade since we're already showing it as the score
                    var grade = if (assignment.gradingType != Assignment.POINTS_TYPE) submission.grade.orEmpty() else ""
                    // Google talkback fails hard on "minus", so we need to remove the dash and replace it with the word
                    val accessibleGradeString = getContentDescriptionForMinusGradeString(grade, resources)
                    // We also need the entire grade cell to be read in a reasonable fashion
                    val gradeCellContentDescription = when {
                        accessibleGradeString.isNotEmpty() -> resources.getString(
                            R.string.a11y_gradeCellContentDescriptionWithLetterGrade,
                            score,
                            outOfContentDescriptionText,
                            accessibleGradeString
                        )
                        grade.isNotEmpty() -> resources.getString(
                            R.string.a11y_gradeCellContentDescriptionWithLetterGrade,
                            score,
                            outOfContentDescriptionText,
                            grade
                        )
                        else -> resources.getString(R.string.a11y_gradeCellContentDescription, score, outOfContentDescriptionText)
                    }

                    var latePenalty = ""
                    var finalGrade = ""

                    // Adjust for late penalty, if any
                    if (submission.pointsDeducted.orDefault() > 0.0) {
                        grade = "" // Grade will be shown in the 'final grade' text
                        val pointsDeducted = NumberHelper.formatDecimal(submission.pointsDeducted.orDefault(), 2, true)
                        latePenalty = resources.getString(R.string.latePenalty, pointsDeducted)
                        finalGrade = resources.getString(R.string.finalGradeFormatted, submission.grade)
                    }

                    GradeCellViewData(
                        state = State.GRADED,
                        tintColor = tintColor,
                        chartPercent = chartPercent,
                        showPointsLabel = true,
                        score = score,
                        grade = grade,
                        gradeContentDescription = accessibleGradeString,
                        gradeCellContentDescription = gradeCellContentDescription,
                        outOf = outOfText,
                        outOfContentDescription = outOfContentDescriptionText,
                        latePenalty = latePenalty,
                        finalGrade = finalGrade
                    )
                }
            }
        }
    }
}

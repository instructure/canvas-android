package com.instructure.pandautils.features.assignments.details.gradecellview

import android.content.res.Resources
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.GradingSchemeRow
import com.instructure.canvasapi2.models.Submission
import com.instructure.canvasapi2.utils.NumberHelper
import com.instructure.canvasapi2.utils.convertScoreToLetterGrade
import com.instructure.pandautils.R
import com.instructure.pandautils.features.assignments.details.mobius.gradeCell.GradeCellViewState
import com.instructure.pandautils.utils.ThemedColor
import com.instructure.pandautils.utils.getContentDescriptionForMinusGradeString
import com.instructure.pandautils.utils.orDefault

data class GradeCellViewData(
    val courseColor: ThemedColor,
    @ColorInt val submissionAndRubricLabelColor: Int,
    val state: State,
    val chartPercent: Float = 0f,
    val showCompleteIcon: Boolean = false,
    val showIncompleteIcon: Boolean = false,
    val showPointsLabel: Boolean = false,
    val score: String = "",
    val grade: String = "",
    val gradeCellContentDescription: String = "",
    val outOf: String = "",
    val yourGrade: String = "",
    val latePenalty: String = "",
    val finalGrade: String = "",
    val stats: GradeCellViewState.GradeStats? = null
) {
    val backgroundColorWithAlpha = ColorUtils.setAlphaComponent(courseColor.color(), (.25 * 255).toInt())

    enum class State {
        EMPTY,
        SUBMITTED,
        GRADED,
        UPLOADING,
        FAILED
    }

    companion object {
        fun fromSubmission(
            resources: Resources,
            courseColor: ThemedColor,
            @ColorInt submissionAndRubricLabelColor: Int,
            assignment: Assignment?,
            submission: Submission?,
            restrictQuantitativeData: Boolean = false,
            uploading: Boolean = false,
            failed: Boolean = false,
            gradingScheme: List<GradingSchemeRow> = emptyList()
        ): GradeCellViewData {
            val hideGrades = restrictQuantitativeData && assignment?.isGradingTypeQuantitative == true && submission?.excused != true && gradingScheme.isEmpty()
            val emptyGradeCell = assignment == null
                || submission == null
                || (submission.submittedAt == null && !submission.isGraded)
                || assignment.gradingType == Assignment.NOT_GRADED_TYPE
                || hideGrades

            return when {
                uploading -> GradeCellViewData(courseColor, submissionAndRubricLabelColor, State.UPLOADING)
                failed -> GradeCellViewData(courseColor, submissionAndRubricLabelColor, State.FAILED)
                emptyGradeCell -> GradeCellViewData(
                    courseColor = courseColor,
                    submissionAndRubricLabelColor = submissionAndRubricLabelColor,
                    state = State.EMPTY,
                    gradeCellContentDescription = getContentDescriptionText(
                        resources,
                        resources.getString(R.string.submissionAndRubric)
                    )
                )
                submission!!.isSubmitted -> GradeCellViewData(
                    courseColor = courseColor,
                    submissionAndRubricLabelColor = submissionAndRubricLabelColor,
                    state = State.SUBMITTED,
                    gradeCellContentDescription = getContentDescriptionText(
                        resources,
                        resources.getString(R.string.submissionStatusSuccessTitle),
                        resources.getString(R.string.submissionStatusWaitingSubtitle)
                    )
                )
                else -> createGradedViewData(resources, courseColor, submissionAndRubricLabelColor, assignment!!, submission, restrictQuantitativeData, gradingScheme)
            }
        }

        private fun createGradedViewData(
            resources: Resources,
            courseColor: ThemedColor,
            @ColorInt submissionAndRubricLabelColor: Int,
            assignment: Assignment,
            submission: Submission,
            restrictQuantitativeData: Boolean,
            gradingScheme: List<GradingSchemeRow>
        ): GradeCellViewData {
            val pointsPossibleText = NumberHelper.formatDecimal(assignment.pointsPossible, 2, true)
            val outOfText = if (restrictQuantitativeData) "" else resources.getString(R.string.outOfPointsAbbreviatedFormatted, pointsPossibleText)
            val outOfContentDescriptionText = if (restrictQuantitativeData) "" else resources.getString(
                R.string.outOfPointsFormatted, pointsPossibleText)

            return if (submission.excused) {
                GradeCellViewData(
                    courseColor = courseColor,
                    submissionAndRubricLabelColor = submissionAndRubricLabelColor,
                    state = State.GRADED,
                    chartPercent = 1f,
                    showCompleteIcon = true,
                    grade = resources.getString(R.string.excused),
                    outOf = outOfText,
                    gradeCellContentDescription = getContentDescriptionText(
                        resources,
                        resources.getString(R.string.gradeExcused),
                        outOfContentDescriptionText
                    )
                )
            } else if (assignment.gradingType == Assignment.PASS_FAIL_TYPE) {
                val isComplete = (submission.grade == "complete")
                val grade = resources.getString(if (isComplete) R.string.gradeComplete else R.string.gradeIncomplete)
                GradeCellViewData(
                    courseColor = courseColor,
                    submissionAndRubricLabelColor = submissionAndRubricLabelColor,
                    state = State.GRADED,
                    chartPercent = 1f,
                    showCompleteIcon = isComplete,
                    showIncompleteIcon = !isComplete,
                    grade = grade,
                    outOf = outOfText,
                    gradeCellContentDescription = getContentDescriptionText(
                        resources,
                        grade,
                        outOfContentDescriptionText
                    )
                )
            } else if (restrictQuantitativeData) {
                val grade = if (assignment.isGradingTypeQuantitative) {
                    convertScoreToLetterGrade(submission.score, assignment.pointsPossible, gradingScheme)
                } else {
                    submission.grade ?: ""
                }
                val accessibleGradeString = getContentDescriptionForMinusGradeString(grade, resources)
                val contentDescription = resources.getString(
                    R.string.a11y_gradeCellContentDescriptionLetterGradeOnly,
                    accessibleGradeString
                ) + System.lineSeparator() + resources.getString(R.string.a11y_gradeCellContentDescriptionHintUpdated)

                GradeCellViewData(
                    courseColor = courseColor,
                    submissionAndRubricLabelColor = submissionAndRubricLabelColor,
                    state = State.GRADED,
                    chartPercent = 1.0f,
                    showCompleteIcon = true,
                    grade = grade,
                    gradeCellContentDescription = contentDescription,
                )
            } else {
                val score = NumberHelper.formatDecimal(submission.score, 2, true)
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
                } + System.lineSeparator() + resources.getString(R.string.a11y_gradeCellContentDescriptionHintUpdated)

                var latePenalty = ""
                var finalGrade = ""
                var yourGrade = ""

                // Adjust for late penalty, if any
                if (submission.pointsDeducted.orDefault() > 0.0) {
                    grade = "" // Grade will be shown in the 'final grade' text
                    val pointsDeducted = NumberHelper.formatDecimal(submission.pointsDeducted.orDefault(), 2, true)
                    val achievedScore = NumberHelper.formatDecimal(submission.enteredScore, 2, true)
                    yourGrade = resources.getString(R.string.yourGrade, achievedScore)
                    latePenalty = resources.getString(R.string.latePenaltyUpdated, pointsDeducted)
                    finalGrade = resources.getString(R.string.finalGradeFormatted, submission.grade)
                }

                val stats = assignment.scoreStatistics?.let { stats ->
                    GradeCellViewState.GradeStats(
                        score = submission.score,
                        outOf = assignment.pointsPossible,
                        min = stats.min,
                        max = stats.max,
                        mean = stats.mean,
                        minText = resources.getString(
                            R.string.scoreStatisticsLow,
                            NumberHelper.formatDecimal(stats.min, 1, true)
                        ),
                        maxText = resources.getString(
                            R.string.scoreStatisticsHigh,
                            NumberHelper.formatDecimal(stats.max, 1, true)
                        ),
                        meanText = resources.getString(
                            R.string.scoreStatisticsMean,
                            NumberHelper.formatDecimal(stats.mean, 1, true)
                        )
                    )
                }

                GradeCellViewData(
                    courseColor = courseColor,
                    submissionAndRubricLabelColor = submissionAndRubricLabelColor,
                    state = State.GRADED,
                    chartPercent = chartPercent,
                    showPointsLabel = true,
                    score = score,
                    grade = grade,
                    gradeCellContentDescription = gradeCellContentDescription,
                    outOf = outOfText,
                    yourGrade = yourGrade,
                    latePenalty = latePenalty,
                    finalGrade = finalGrade,
                    stats = stats
                )
            }
        }

        private fun getContentDescriptionText(resources: Resources, vararg texts: String) = texts.joinToString(" ") +
                System.lineSeparator() + resources.getString(R.string.a11y_gradeCellContentDescriptionHintUpdated)

        private val Submission.isSubmitted
            get() = submittedAt != null && !isGraded && !excused
    }
}

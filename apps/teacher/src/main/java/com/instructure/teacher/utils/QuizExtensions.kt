/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.utils

import com.instructure.canvasapi2.models.Quiz
import com.instructure.canvasapi2.models.QuizSubmission
import com.instructure.canvasapi2.models.Submission
import com.instructure.teacher.R
import java.util.*

fun Quiz.quizTypeDisplayable(): Int = when (this.quizType) {
    Quiz.TYPE_PRACTICE -> R.string.practice_quiz
    Quiz.TYPE_ASSIGNMENT -> R.string.graded_quiz
    Quiz.TYPE_GRADED_SURVEY -> R.string.graded_survey
    Quiz.TYPE_SURVEY -> R.string.ungraded_survey
    // Else shouldn't happen; just here to satisfy the expression
    else -> 0
}

fun Quiz.shuffleAnswersDisplayable(): Int = if (this.shuffleAnswers) R.string.yes else R.string.no
/**
 * Anonymous Submissions only shows if the quiz type is
 * one of the survey types.
 */
fun Quiz.anonymousSubmissionsDisplayable(): Boolean = this.quizType == Quiz.TYPE_SURVEY || this.quizType == Quiz.TYPE_GRADED_SURVEY
fun Quiz.isPracticeOrUngraded(): Boolean = this.quizType == Quiz.TYPE_SURVEY || this.quizType == Quiz.TYPE_PRACTICE
fun Quiz.isUngradedSurvey(): Boolean = this.quizType == Quiz.TYPE_SURVEY
fun Submission.transformForQuizGrading() {
    submissionHistory.filterNotNull().forEach {
        it.id = id
        it.previewUrl = it.previewUrl?.replace("version=\\d+".toRegex(), "version=${it.attempt}")
    }
}

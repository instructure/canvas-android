/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.canvasapi2.models

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuizQuestion(
        override var id: Long = 0,
        @SerializedName("quiz_id")
        var quizId: Long = 0,

        // The order in which the question will be retrieved and displayed.
        var position: Int = 0,

        @SerializedName("question_name")
        var questionName: String? = null,

        @SerializedName("question_type")
        var questionTypeString: String? = null,

        @SerializedName("question_text")
        var questionText: String? = null,

        @SerializedName("points_possible")
        var pointsPossible: Int = 0,

        // The comments to display if the student answers the question correctly.
        @SerializedName("correct_comments")
        var correctComments: String? = null,

        // The comments to display if the student answers incorrectly.
        @SerializedName("incorrect_comments")
        var incorrectComments: String? = null,

        // The comments to display regardless of how the student answered.
        @SerializedName("neutral_comments")
        var neutralComments: String? = null,

        // An array of available answers to display to the student.
        var answers: Array<QuizAnswer>? = null
) : CanvasModel<QuizQuestion>() {

    val questionType: QuestionType
        get() = parseQuestionType(this.questionTypeString!!)

    enum class QuestionType {
        CALCULATED, ESSAY, FILE_UPLOAD, FILL_IN_MULTIPLE_BLANKS, MATCHING, MULTIPLE_ANSWERS, MUTIPLE_CHOICE, MULTIPLE_DROPDOWNS, NUMERICAL, SHORT_ANSWER, TEXT_ONLY, TRUE_FALSE, UNKNOWN
    }


    companion object {
        fun parseQuestionType(questionType: String): QuestionType {

            when (questionType) {
                "calculated_question" -> return QuestionType.CALCULATED
                "essay_question" -> return QuestionType.ESSAY
                "file_upload_question" -> return QuestionType.FILE_UPLOAD
                "fill_in_multiple_blanks_question" -> return QuestionType.FILL_IN_MULTIPLE_BLANKS
                "matching_question" -> return QuestionType.MATCHING
                "multiple_answers_question" -> return QuestionType.MULTIPLE_ANSWERS
                "multiple_choice_question" -> return QuestionType.MUTIPLE_CHOICE
                "multiple_dropdowns_question" -> return QuestionType.MULTIPLE_DROPDOWNS
                "numerical_question" -> return QuestionType.NUMERICAL
                "short_answer_question" -> return QuestionType.SHORT_ANSWER
                "text_only_question" -> return QuestionType.TEXT_ONLY
                "true_false_question" -> return QuestionType.TRUE_FALSE
            }
            return QuestionType.UNKNOWN
        }
    }
}

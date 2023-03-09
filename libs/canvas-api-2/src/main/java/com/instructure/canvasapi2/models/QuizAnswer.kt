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
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuizAnswer(
    // The unique identifier for the answer.  Do not supply if this answer is part of a
    // new question
        override val id: Long = 0,

    // The text of the answer.
        @SerializedName("text")
        val answerText: String? = null,

    // An integer to determine correctness of the answer. Incorrect answers should be
    // 0, correct answers should be non-negative.
        @SerializedName("answer_weight")
        val answerWeight: Int = 0,

    // Specific contextual comments for a particular answer.
        @SerializedName("answer_comments")
        val answerComments: String? = null,

    // Used in missing word questions.  The text to follow the missing word
        @SerializedName("text_after_answers")
        val textAfterAnswers: String? = null,

    // Used in matching questions.  The static value of the answer that will be
    // displayed on the left for students to match for.
        @SerializedName("answer_match_left")
        val answerMatchLeft: String? = null,

    // Used in matching questions. The correct match for the value given in
    // answer_match_left.  Will be displayed in a dropdown with the other
    // answer_match_right values..
        @SerializedName("answer_match_right")
        val answerMatchRight: String? = null,

    // Used in matching questions. A list of distractors, delimited by new lines (
    // ) that will be seeded with all the answer_match_right values.
        @SerializedName("matching_answer_incorrect_matches")
        val matchingAnswerIncorrectMatches: Array<String>? = null,

    //Used in numerical questions.  Values can be 'exact_answer' or 'range_answer'.
        @SerializedName("numerical_answer_type")
        val numericalAnswerType: String? = null,

    // Used in numerical questions of type 'exact_answer'.  The value the answer should
    // equal.
        val exact: Int = 0,

    // Used in numerical questions of type 'exact_answer'. The margin of error allowed
    // for the student's answer.
        val margin: Int = 0,

    // Used in numerical questions of type 'range_answer'. The start of the allowed
    // range (inclusive).
        val start: Int = 0,

    // Used in numerical questions of type 'range_answer'. The end of the allowed range
    // (inclusive).
        val end: Int = 0,

    // Used in fill in multiple blank and multiple dropdowns questions.
        @SerializedName("blank_id")
        val blankId: Long = 0
) : CanvasModel<QuizAnswer>()
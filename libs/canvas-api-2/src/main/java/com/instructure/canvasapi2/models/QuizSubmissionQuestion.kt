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
import kotlinx.parcelize.RawValue

@Parcelize
data class QuizSubmissionQuestion(
        // The ID of the QuizQuestion this answer is for.
        override val id: Long = 0,

        // Whether this question is flagged.
        @SerializedName("flagged")
        var isFlagged: Boolean = false,

        // The possible answers for this question when those possible answers are
        // necessary.  The presence of this parameter is dependent on permissions.
        val answers: Array<QuizSubmissionAnswer>? = null,

        val position: Int = 0,

        @SerializedName("quiz_id")
        val quizId: Long = 0,

        @SerializedName("question_name")
        val questionName: String? = null,

        // Type of the question. See QuizQuestion for QuestionType
        @SerializedName("question_type")
        val questionType: String? = null,

        @SerializedName("question_text")
        val questionText: String? = null,

        // Sometimes is a String, sometimes an array depending on the question type
        var answer: @RawValue Any? = null, // TODO: Putting @RawValue here for now, need to look into this
        val matches: Array<QuizSubmissionMatch>? = null
) : CanvasModel<QuizSubmissionQuestion>()
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
data class QuizPermission(
        // Whether the user can view the quiz
        val read: Boolean = false,

        // Whether the user may submit a submission for the quiz
        val submit: Boolean = false,

        // Whether the user may create a new quiz
        val create: Boolean = false,

        // Whether the user may edit, update, or delete the quiz
        val manage: Boolean = false,

        // Whether the user may view quiz statistics for this quiz
        @SerializedName("read_statistics")
        val readStatistics: Boolean = false,

        // Whether the user may review grades for all quiz submissions for this quiz
        @SerializedName("review_grades")
        val reviewGrades: Boolean = false,

        // Whether the user may update the quiz
        val update: Boolean = false,

        // Whether the user may delete the quiz
        val delete: Boolean = false,

        // Whether the user may grade the quiz
        val grade: Boolean = false,

        // Whether the user can view answer audits
        @SerializedName("view_answer_audits")
        val viewAnswerAudits: Boolean = false
) : CanvasModel<QuizPermission>()
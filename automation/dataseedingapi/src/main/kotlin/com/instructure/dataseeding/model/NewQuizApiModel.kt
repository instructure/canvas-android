/*
 * Copyright (C) 2026 - present Instructure, Inc.
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

package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class NewQuizListApiModel(
    val newQuizList: List<NewQuizApiModel>
)

data class NewQuizApiModel(
    val id: Long,
    val title: String,
    val instructions: String?,
    @SerializedName("points_possible")
    val pointsPossible: Double?,
    @SerializedName("due_at")
    val dueAt: String?,
    @SerializedName("lock_at")
    val lockAt: String?,
    @SerializedName("unlock_at")
    val unlockAt: String?,
    @SerializedName("is_quiz_assignment")
    val isQuizAssignment: Boolean,
    @SerializedName("is_quiz_lti_assignment")
    val isQuizLtiAssignment: Boolean,
    @SerializedName("new_quizzes_quiz_type")
    val newQuizzesQuizType: String,
    @SerializedName("quiz_lti")
    val quizLti: Int,
    @SerializedName("submission_type")
    val submissionType: String,
    val published: Boolean
)

data class CreateNewQuiz(
    val title: String,
    val instructions: String? = null,
    @SerializedName("points_possible")
    val pointsPossible: Double? = 50.0,
    @SerializedName("due_at")
    val dueAt: String? = null,
    @SerializedName("lock_at")
    val lockAt: String? = null,
    @SerializedName("unlock_at")
    val unlockAt: String? = null,
    @SerializedName("is_quiz_assignment")
    val isQuizAssignment: Boolean = false,
    @SerializedName("is_quiz_lti_assignment")
    val isQuizLtiAssignment: Boolean = true,
    @SerializedName("new_quizzes_quiz_type")
    val newQuizzesQuizType: String = "graded_quiz",
    @SerializedName("quiz_lti")
    val quizLti: Int = 1,
    @SerializedName("submission_type")
    val submissionType: String = "external_tool",
    val published: Boolean
)
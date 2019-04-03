//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package com.instructure.dataseeding.model

import com.google.gson.annotations.SerializedName

data class QuizListApiModel(
        val quizList : List<QuizApiModel>
)

data class QuizApiModel(
        val id: Long,
        val title: String,
        var description: String?,
        val published: Boolean,
        @SerializedName("lock_at")
        var lockAt: String?,
        @SerializedName("unlock_at")
        var unlockAt: String?,
        @SerializedName("due_at")
        var dueAt: String?
)

data class CreateQuiz(
        val title: String,
        var description: String? = null,
        val published: Boolean = true,
        @SerializedName("lock_at")
        var lockAt: String? = null,
        @SerializedName("unlock_at")
        var unlockAt: String? = null,
        @SerializedName("due_at")
        var dueAt: String? = null
)

data class UpdateQuiz(
        val published: Boolean = true
)

data class QuizAnswer(
        val text: String? = null,
        val comments: String? = null,
        @SerializedName("blank_id")
        val blankId: String? = null,
        val weight: Int,
        val id: Int
)

data class QuizQuestion(
        @SerializedName("question_name")
        val questionName: String? = null,
        @SerializedName("question_text")
        val questionText: String? = null,
        @SerializedName("question_type")
        val questionType: String? = null,
        @SerializedName("points_possible")
        val pointsPossible: Int,
        val answers: List<QuizAnswer>
)

data class CreateQuizQuestion(
        val question: QuizQuestion
)

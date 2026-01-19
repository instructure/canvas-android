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

package com.instructure.dataseeding.api

import com.instructure.dataseeding.model.NewQuizApiModel
import com.instructure.dataseeding.util.CanvasNetworkAdapter
import com.instructure.dataseeding.util.Randomizer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object NewQuizzesApi {
    interface NewQuizzesService {
        @POST("courses/{courseId}/quizzes")
        fun createNewQuiz(
            @Path("courseId") courseId: Long,
            @Body createNewQuiz: CreateNewQuiz
        ): Call<NewQuizApiModel>
    }

    private fun newQuizzesService(token: String): NewQuizzesService {
        return CanvasNetworkAdapter.retrofitWithToken(token).create(NewQuizzesService::class.java)
    }

    fun createNewQuiz(
        courseId: Long,
        token: String,
        withInstructions: Boolean = true,
        lockAt: String = "",
        unlockAt: String = "",
        dueAt: String = "",
        pointsPossible: Double = 50.0,
        isQuizAssignment: Boolean = false,
        isQuizLtiAssignment: Boolean = true,
        newQuizzesQuizType: String = "graded_quiz",
        quizLti: Int = 1,
        submissionType: String = "external_tool",
        published: Boolean = true,
    ): NewQuizApiModel {
        val newQuiz = CreateNewQuiz(
            Randomizer.randomNewQuiz(
                withInstructions,
                lockAt,
                unlockAt,
                dueAt,
                pointsPossible,
                isQuizAssignment,
                isQuizLtiAssignment,
                newQuizzesQuizType,
                quizLti,
                submissionType,
                published
            )
        )

        return newQuizzesService(token)
            .createNewQuiz(courseId, newQuiz)
            .execute()
            .body()!!
    }

    data class CreateNewQuiz(
        val quiz: com.instructure.dataseeding.model.CreateNewQuiz
    )
}
/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.canvasapi2.pact.canvas.apis

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.pact.canvas.logic.assertQuizPopulated
import com.instructure.canvasapi2.pact.canvas.logic.assertQuizSubmissionPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateQuizFields
import com.instructure.canvasapi2.pact.canvas.logic.populateQuizSubmissionFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class QuizzesApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService(caller: String = DEFAULT_MOBILE_STUDENT): QuizAPI.QuizInterface {
        val client = getClient(caller = caller)
        return client.create(QuizAPI.QuizInterface::class.java)
    }

    //
    // region Grab all quizzes as teacher
    //

    val getAllQuizzesTeacherQuery: String? = null
    val getAllQuizzesTeacherPath = "/api/v1/courses/3/all_quizzes"
    val getAllQuizzesTeacherResponseBody = LambdaDsl.newJsonArray { array ->
        array.`object` { obj ->
            obj.populateQuizFields("teacher", singleQuiz = false)
        }
    }.build()
    @Pact(consumer = "android")
    fun getAllQuizzesTeacherPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile course with quiz")

                .uponReceiving("Grab all quizzes as teacher")
                .path(getAllQuizzesTeacherPath)
                .method("GET")
                .query(getAllQuizzesTeacherQuery)
                .headers(mapOf(
                        "Authorization" to "Bearer some_token",
                        "Auth-User" to "Mobile Teacher",
                        "Content-Type" to "application/json"
                ))

                .willRespondWith()
                .status(200)
                .body(getAllQuizzesTeacherResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getAllQuizzesTeacherPact")
    fun `grab all quizzes as teacher`() {
        val service = createService(caller = "Mobile Teacher")

        val getQuizzesCall = service.getFirstPageQuizzes(contextId = 3)
        val getQuizzesResult = getQuizzesCall.execute()

        assertQueryParamsAndPath(getQuizzesCall, getAllQuizzesTeacherQuery, getAllQuizzesTeacherPath)

        assertNotNull("Expected non-null response body", getQuizzesResult.body())
        val quizzes = getQuizzesResult.body()!!

        assertEquals("Expected 1 quiz to be returned",1,quizzes.size)

        assertQuizPopulated(description = "returned quiz", quiz = quizzes[0], role = "teacher", singleQuiz = false)
    }
    //endregion

    //
    // region Grab single quiz as student
    //

    val getOneQuizStudentQuery: String? = null
    val getOneQuizStudentPath = "/api/v1/courses/3/quizzes/1"
    val getOneQuizStudentResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateQuizFields(role = "student", singleQuiz = true)
    }.build()
    @Pact(consumer = "android")
    fun getOneQuizStudentPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile course with quiz")

                .uponReceiving("Grab one quiz as student")
                .path(getOneQuizStudentPath)
                .method("GET")
                .query(getOneQuizStudentQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getOneQuizStudentResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getOneQuizStudentPact")
    fun `grab one quiz as student`() {
        val service = createService()

        val getQuizCall = service.getQuiz(courseId = 3, quizId = 1)
        val getQuizResult = getQuizCall.execute()

        assertQueryParamsAndPath(getQuizCall, getOneQuizStudentQuery, getOneQuizStudentPath)

        assertNotNull("Expected non-null response body", getQuizResult.body())
        val quiz = getQuizResult.body()!!

        assertQuizPopulated(description = "returned quiz", quiz = quiz, role = "student", singleQuiz = true)
    }
    //endregion

    //
    // region Grab quiz submissions
    //

    val getQuizSubmissionsQuery: String? = null
    val getQuizSubmissionsPath = "/api/v1/courses/3/quizzes/1/submissions"
    val getQuizSubmissionsResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.array("quiz_submissions") { arr ->
            arr.`object`() { submissionObj ->
                submissionObj.populateQuizSubmissionFields()
            }
        }
    }.build()
    @Pact(consumer = "android")
    fun getQuizSubmissionsPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile course with quiz")

                .uponReceiving("Grab submissions for quiz")
                .path(getQuizSubmissionsPath)
                .method("GET")
                .query(getQuizSubmissionsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getQuizSubmissionsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getQuizSubmissionsPact")
    fun `grab submissions for quiz`() {
        val service = createService()

        val getQuizSubmissionsCall = service.getFirstPageQuizSubmissions(contextType = "courses", contextId = 3, quizId = 1)
        val getQuizSubmissionsResult = getQuizSubmissionsCall.execute()

        assertQueryParamsAndPath(getQuizSubmissionsCall, getQuizSubmissionsQuery, getQuizSubmissionsPath)

        assertNotNull("Expected non-null response body", getQuizSubmissionsResult.body())
        val quizSubmissionsResponse = getQuizSubmissionsResult.body()!!

        assertNotNull("quizSubmissionResponse.quizSubmissions", quizSubmissionsResponse.quizSubmissions)
        assertEquals("Expected 1 quiz submission to be returned",1,quizSubmissionsResponse.quizSubmissions.size)

        assertQuizSubmissionPopulated(
                description = "returned quizSubmission",
                quizSubmission = quizSubmissionsResponse.quizSubmissions[0]
        )
    }
    //endregion
}
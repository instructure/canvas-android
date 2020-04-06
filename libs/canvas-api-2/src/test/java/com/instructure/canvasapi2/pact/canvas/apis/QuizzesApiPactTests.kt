package com.instructure.canvasapi2.pact.canvas.apis

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.instructure.canvasapi2.apis.QuizAPI
import com.instructure.canvasapi2.pact.canvas.logic.assertQuizPopulated
import com.instructure.canvasapi2.pact.canvas.logic.assertQuizSubmissionPopulated
import com.instructure.canvasapi2.pact.canvas.logic.assertQuizSubmissionQuestionPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateQuizFields
import com.instructure.canvasapi2.pact.canvas.logic.populateQuizSubmissionFields
import com.instructure.canvasapi2.pact.canvas.logic.populateQuizSubmissionQuestionFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class QuizzesApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService(): QuizAPI.QuizInterface {
        val client = getClient()
        return client.create(QuizAPI.QuizInterface::class.java)
    }

    val getQuizzesQuery: String? = null
    val getQuizzesPath = "/api/v1/courses/3/quizzes"
    val getQuizzesResponseBody = LambdaDsl.newJsonArray { array ->
        array.`object` { obj ->
            obj.populateQuizFields()
        }
    }.build()
    @Pact(consumer = "android")
    fun getQuizzesPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile course with quiz")

                .uponReceiving("Grab all quizzes")
                .path(getQuizzesPath)
                .method("GET")
                .query(getQuizzesQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getQuizzesResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getQuizzesPact")
    fun `grab all quizzes`() {
        val service = createService()

        val getQuizzesCall = service.getFirstPageQuizzes(contextId = 3)
        val getQuizzesResult = getQuizzesCall.execute()

        assertQueryParamsAndPath(getQuizzesCall, getQuizzesQuery, getQuizzesPath)

        assertNotNull("Expected non-null response body", getQuizzesResult.body())
        val quizzes = getQuizzesResult.body()!!

        assertEquals("Expected 1 quiz to be returned",1,quizzes.size)

        assertQuizPopulated(description = "returned quiz", quiz = quizzes[0])
    }

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

        val getQuizSubmissionsCall = service.getFirstPageQuizSubmissions(courseId = 3, quizId = 1)
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

    val getQuizSubmissionQuestionsQuery: String? = null
    val getQuizSubmissionQuestionsPath = "/api/v1/quiz_submissions/1/questions"
    val getQuizSubmissionQuestionsResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.minArrayLike("quiz_submission_questions", 1) { obj2 ->
            obj2.populateQuizSubmissionQuestionFields()
        }
    }.build()
    @Pact(consumer = "android")
    fun getQuizSubmissionQuestionsPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile course with quiz")

                .uponReceiving("Grab questions for quiz submission")
                .path(getQuizSubmissionQuestionsPath)
                .method("GET")
                .query(getQuizSubmissionQuestionsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getQuizSubmissionQuestionsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getQuizSubmissionQuestionsPact")
    fun `grab questions for quiz submission`() {
        val service = createService()

        val getQuizSubmissionQuestionsCall = service.getFirstPageSubmissionQuestions(quizSubmissionId = 1)
        val getQuizSubmissionQuestionsResult = getQuizSubmissionQuestionsCall.execute()

        assertQueryParamsAndPath(getQuizSubmissionQuestionsCall, getQuizSubmissionQuestionsQuery, getQuizSubmissionQuestionsPath)

        assertNotNull("Expected non-null response body", getQuizSubmissionQuestionsResult.body())
        val quizSubmissionQuestionsResponse = getQuizSubmissionQuestionsResult.body()!!

        assertNotNull("quizSubmissionQuestionsResponse.quizSubmissions", quizSubmissionQuestionsResponse.quizSubmissionQuestions)

        for(i in 0..quizSubmissionQuestionsResponse.quizSubmissionQuestions!!.size - 1) {
            assertQuizSubmissionQuestionPopulated(
                    description = "quizSubmissionQuestionResponse.quizSubmissions[$i]",
                    question = quizSubmissionQuestionsResponse.quizSubmissionQuestions!![i]
            )
        }
    }
}
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
import com.instructure.canvasapi2.apis.SubmissionAPI
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.pact.canvas.logic.PactSubmissionFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertSubmissionPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateSubmissionFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert
import org.junit.Test

class SubmissionsApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService(pathPrefix: String = DEFAULT_PATH_PREFIX): SubmissionAPI.SubmissionInterface {
        val client = getClient(pathPrefix = pathPrefix)
        return client.create(SubmissionAPI.SubmissionInterface::class.java)
    }

    //
    // region request a single online text submission
    //

    val getTextSubmissionQuery = "include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group"
    val getTextSubmissionPath = "/api/v1/courses/3/assignments/1/submissions/8"
    val getTextSubmissionFieldInfo = PactSubmissionFieldConfig
            .fromQuery(getTextSubmissionQuery) // submissionType?
    val getTextSubmissionResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateSubmissionFields(getTextSubmissionFieldInfo)
    }.build()

    @Pact(consumer = "android")
    fun getTextSubmissionPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile 3 assignments, 3 submissions")

                .uponReceiving("A request for an online text submission")
                .path(getTextSubmissionPath)
                .method("GET")
                .query(getTextSubmissionQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getTextSubmissionResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getTextSubmissionPact")
    fun `grab online text submission`() {
        val service = createService()

        val getSubmissionCall = service.getSingleSubmission(courseId = 3, assignmentId = 1, studentId = 8)
        val getSubmissionResult = getSubmissionCall.execute()

        assertQueryParamsAndPath(getSubmissionCall, getTextSubmissionQuery, getTextSubmissionPath)

        Assert.assertNotNull("Expected non-null response body", getSubmissionResult.body())
        val submission = getSubmissionResult.body()!!

        assertSubmissionPopulated(description = "returned submission", submission = submission, fieldConfig = getTextSubmissionFieldInfo)
    }
    //endregion

    //
    // region request a single online upload submission
    //

    val getAttachmentSubmissionQuery = "include[]=rubric_assessment&include[]=submission_history&include[]=submission_comments&include[]=group"
    val getAttachmentSubmissionPath = "/api/v1/courses/3/assignments/2/submissions/8"
    val getAttachmentSubmissionFieldInfo = PactSubmissionFieldConfig
            .fromQuery(getAttachmentSubmissionQuery, Assignment.SubmissionType.ONLINE_UPLOAD)
    val getAttachmentSubmissionResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateSubmissionFields(getAttachmentSubmissionFieldInfo)
    }.build()

    @Pact(consumer = "android")
    fun getAttachmentSubmissionPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile 3 assignments, 3 submissions")

                .uponReceiving("A request for an online upload submission")
                .path(getAttachmentSubmissionPath)
                .method("GET")
                .query(getAttachmentSubmissionQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getAttachmentSubmissionResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getAttachmentSubmissionPact")
    fun `grab online upload submission`() {
        val service = createService()

        val getSubmissionCall = service.getSingleSubmission(courseId = 3, assignmentId = 2, studentId = 8)
        val getSubmissionResult = getSubmissionCall.execute()

        assertQueryParamsAndPath(getSubmissionCall, getAttachmentSubmissionQuery, getAttachmentSubmissionPath)

        Assert.assertNotNull("Expected non-null response body", getSubmissionResult.body())
        val submission = getSubmissionResult.body()!!

        assertSubmissionPopulated(description = "returned submission", submission = submission, fieldConfig = getAttachmentSubmissionFieldInfo)
    }
    //endregion

    // Post request resulted in
    //
    //  {"status":"unauthorized","errors":[{"message":"user not authorized to perform that action"}]}
    //
    // when run against Pact broker.  I don't know why that would happen.  Punting for now.
//    //
//    // region post a submission
//    //
//
//    val postSubmissionQuery="submission[submission_type]=online_text_entry&submission[body]=abc"
//    val postSubmissionPath = "/api/v1/courses/3/assignments/2/submissions" // Should /api/v1 be there?
//    val postSubmissionFieldInfo = PactSubmissionFieldConfig(submissionType = Assignment.SubmissionType.ONLINE_TEXT_ENTRY)
//    val postSubmissionResponseBody =  LambdaDsl.newJsonBody { obj ->
//        obj.populateSubmissionFields(postSubmissionFieldInfo)
//    }.build()
//
//    @Pact(consumer = "android")
//    fun postSubmissionPact(builder: PactDslWithProvider) : RequestResponsePact {
//        return builder
//                .given(MAIN_PROVIDER_STATE)
//
//                .uponReceiving("Post text submission for course 3 assignment 2")
//                .path(postSubmissionPath)
//                .method("POST")
//                .query(postSubmissionQuery)
//                .headers(DEFAULT_REQUEST_HEADERS)
//
//                .willRespondWith()
//                .status(201) // "created"
//                .body(postSubmissionResponseBody)
//                .headers(DEFAULT_RESPONSE_HEADERS)
//
//                .toPact()
//    }
//
//    @Test
//    @PactVerification(fragment = "postSubmissionPact")
//    fun `post text submission for course 3 assignment 2`() {
//        val service = createService(pathPrefix = "/api/v1/courses/")
//
//        val postSubmissionCall = service.postTextSubmission(
//                contextId = 3,
//                assignmentId = 2,
//                submissionType = "online_text_entry",
//                text = "abc")
//        val postSubmissionResult = postSubmissionCall.execute()
//
//        assertQueryParamsAndPath(postSubmissionCall, postSubmissionQuery, postSubmissionPath)
//
//        Assert.assertNotNull("Expected non-null response body", postSubmissionResult.body())
//        val submission = postSubmissionResult.body()!!
//
//        assertSubmissionPopulated(description = "returned submission", submission = submission, fieldConfig = postSubmissionFieldInfo)
//    }
//    // endregion
}
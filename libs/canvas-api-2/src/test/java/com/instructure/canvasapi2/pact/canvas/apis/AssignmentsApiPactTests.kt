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
import com.instructure.canvasapi2.apis.AssignmentAPI
import com.instructure.canvasapi2.pact.canvas.logic.PactAssignmentFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertAssignmentPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateAssignmentFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert
import org.junit.Test

class AssignmentsApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService(caller: String = DEFAULT_MOBILE_STUDENT): AssignmentAPI.AssignmentInterface {
        val client = getClient(caller = caller)
        return client.create(AssignmentAPI.AssignmentInterface::class.java)
    }

    //
    // region request an assignment as a student
    //

    val getStudentAssignmentQuery = "include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=score_statistics"
    val getStudentAssignmentPath = "/api/v1/courses/3/assignments/1" // Has default submission type of online_text_entry
    // We won't get override info when we request the assignment as a student.
    val getStudentAssignmentFieldInfo =
            PactAssignmentFieldConfig
                    .fromQueryString(query = getStudentAssignmentQuery)
                    .copy(includeOverrides = false, role = "student")
    val getStudentAssignmentResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateAssignmentFields(getStudentAssignmentFieldInfo)
    }.build()

    @Pact(consumer = "android")
    fun getStudentAssignmentPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile 3 assignments, 3 submissions")

                .uponReceiving("Grab an assignment as student")
                .path(getStudentAssignmentPath)
                .method("GET")
                .query(getStudentAssignmentQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getStudentAssignmentResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getStudentAssignmentPact")
    fun `grab assignment as student`() {
        val service = createService()

        val getAssignmentCall = service.getAssignment(courseId = 3, assignmentId = 1)
        val getAssignmentResult = getAssignmentCall.execute()

        assertQueryParamsAndPath(getAssignmentCall, getStudentAssignmentQuery, getStudentAssignmentPath)

        Assert.assertNotNull("Expected non-null response body", getAssignmentResult.body())
        val assignment = getAssignmentResult.body()!!

        assertAssignmentPopulated(description = "returned assignment", assignment = assignment, fieldConfig = getStudentAssignmentFieldInfo)
    }
    //endregion

    //
    // region request an assignment as a teacher
    //

    val getTeacherAssignmentQuery = "include[]=submission&include[]=rubric_assessment&needs_grading_count_by_section=true&override_assignment_dates=true&all_dates=true&include[]=overrides&include[]=score_statistics"
    val getTeacherAssignmentPath = "/api/v1/courses/3/assignments/2"
    // We won't get a submission object in an assignment requested by a teacher.
    val getTeacherAssignmentFieldInfo =
            PactAssignmentFieldConfig
                    .fromQueryString(query = getTeacherAssignmentQuery)
                    .copy(includeSubmission = false, role = "teacher")
    val getTeacherAssignmentResponseBody = LambdaDsl.newJsonBody { obj ->
        obj.populateAssignmentFields(getTeacherAssignmentFieldInfo)
    }.build()

    @Pact(consumer = "android")
    fun getTeacherAssignmentPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
                .given("mobile 3 assignments, 3 submissions")

                .uponReceiving("Grab an assignment as teacher")
                .path(getTeacherAssignmentPath)
                .method("GET")
                .query(getTeacherAssignmentQuery)
                .headers(mapOf(
                        "Authorization" to "Bearer some_token",
                        "Auth-User" to "Mobile Teacher",
                        "Content-Type" to "application/json"))

                .willRespondWith()
                .status(200)
                .body(getTeacherAssignmentResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getTeacherAssignmentPact")
    fun `grab assignment as teacher`() {
        val service = createService("Mobile Teacher")

        val getAssignmentCall = service.getAssignment(courseId = 3, assignmentId = 2)
        val getAssignmentResult = getAssignmentCall.execute()

        assertQueryParamsAndPath(getAssignmentCall, getTeacherAssignmentQuery, getTeacherAssignmentPath)

        Assert.assertNotNull("Expected non-null response body", getAssignmentResult.body())
        val assignment = getAssignmentResult.body()!!

        assertAssignmentPopulated(description = "returned assignment", assignment = assignment, fieldConfig = getTeacherAssignmentFieldInfo)
    }
    //endregion

}

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
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.pact.canvas.logic.DiscussionTopicHeaderConfig
import com.instructure.canvasapi2.pact.canvas.logic.PactCourseFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertCoursePopulated
import com.instructure.canvasapi2.pact.canvas.logic.assertDiscussionEntryPopulated
import com.instructure.canvasapi2.pact.canvas.logic.assertDiscussionTopicHeaderPopulated
import com.instructure.canvasapi2.pact.canvas.logic.assertDiscussionTopicPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateCourseFields
import com.instructure.canvasapi2.pact.canvas.logic.populateDiscussionEntryFields
import com.instructure.canvasapi2.pact.canvas.logic.populateDiscussionTopicFields
import com.instructure.canvasapi2.pact.canvas.logic.populateDiscussionTopicHeaderFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import okhttp3.MediaType
import okhttp3.RequestBody
import org.junit.Assert
import org.junit.Test

class DiscussionsApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService(user: String = DEFAULT_MOBILE_STUDENT) : DiscussionAPI.DiscussionInterface {

        val client = getClient(caller = user)
        return client.create(DiscussionAPI.DiscussionInterface::class.java)
    }

    //
    //region Test grabbing discussion topics
    //

    val listDiscussionTopicsQuery = "override_assignment_dates=true&include[]=all_dates&include[]=overrides&include[]=sections"
    val listDiscussionTopicsPath = "/api/v1/courses/3/discussion_topics"
    val listDiscussionTopicsConfigs = listOf (

            // Must correspond to reverse order of topic creation in the provider state,
            // because topics are passed back in reverse order of creation.
            DiscussionTopicHeaderConfig(
                    hasAssignment = true,
                    isDelayed = true,
                    isLocked = true,
                    hasRequireInitialPost = true
            ),
            DiscussionTopicHeaderConfig(
                    hasSections = true
            )
    )
    val listDiscussionTopicsResponseBody =  LambdaDsl.newJsonArray { array ->
        for(config in listDiscussionTopicsConfigs) {
            array.`object` { obj ->
                obj.populateDiscussionTopicHeaderFields(config)
            }
        }
    }.build()
    @Pact(consumer = "android")
    fun getCourseDiscussionTopicsPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given("mobile course with discussions")

                .uponReceiving("A request for course discussion topics")
                .path(listDiscussionTopicsPath)
                .method("GET")
                .query(listDiscussionTopicsQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(listDiscussionTopicsResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getCourseDiscussionTopicsPact")
    fun `grab course discussion topics`() {
        val service = createService()

        val getCourseDiscussionTopicsCall = service.getFirstPageDiscussionTopicHeaders("courses", 3)
        val getCourseDiscussionTopicsResult = getCourseDiscussionTopicsCall.execute()

        assertQueryParamsAndPath(getCourseDiscussionTopicsCall, listDiscussionTopicsQuery, listDiscussionTopicsPath)

        Assert.assertNotNull("Expected non-null response body", getCourseDiscussionTopicsResult.body())
        val topicHeaderList = getCourseDiscussionTopicsResult.body()!!
        Assert.assertEquals("returned list size", listDiscussionTopicsConfigs.size, topicHeaderList.count())

        for(index in 0..topicHeaderList.size-1) {
            val header = topicHeaderList[index]
            val config = listDiscussionTopicsConfigs[index]

            assertDiscussionTopicHeaderPopulated(description = "topic $index", header = header, config = config)
        }
    }
    //endregion

    //
    //region Test grabbing full discussion topic
    //

    val getFullDiscussionTopicQuery = "include_new_entries=1"
    val getFullDiscussionTopicPath = "/api/v1/courses/3/discussion_topics/1/view"
    val getFullDiscussionTopicResponseBody =  LambdaDsl.newJsonBody { body ->
        body.populateDiscussionTopicFields()
    }.build()

    @Pact(consumer = "android")
    fun getFullDiscussionTopicPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given("mobile course with discussions")

                .uponReceiving("A request for a full discussion topic")
                .path(getFullDiscussionTopicPath)
                .method("GET")
                .query(getFullDiscussionTopicQuery)
                .headers(DEFAULT_REQUEST_HEADERS)

                .willRespondWith()
                .status(200)
                .body(getFullDiscussionTopicResponseBody)
                .headers(DEFAULT_RESPONSE_HEADERS)

                .toPact()
    }

    @Test
    @PactVerification(fragment = "getFullDiscussionTopicPact")
    fun `grab full discussion topic`() {
        val service = createService()

        val getFullDiscussionTopicCall = service.getFullDiscussionTopic("courses", 3, 1, includeNewEntries = 1)
        val getFullDiscussionTopicResult = getFullDiscussionTopicCall.execute()

        assertQueryParamsAndPath(getFullDiscussionTopicCall, getFullDiscussionTopicQuery, getFullDiscussionTopicPath)

        Assert.assertNotNull("Expected non-null response body", getFullDiscussionTopicResult.body())
        assertDiscussionTopicPopulated("Returned DiscussionTopic", getFullDiscussionTopicResult.body()!!)
    }
    //endregion

    //
    //region Test grabbing full discussion topic
    //

    // Punting this for now because it does not properly populate the "message" field
//    val postToDiscussionTopicQuery = null
//    val postToDiscussionTopicPath = "/api/v1/courses/3/discussion_topics/1/entries"
//    val postToTopicResponseBody =  LambdaDsl.newJsonBody { body ->
//        body.populateDiscussionEntryFields()
//    }.build()
//    @Pact(consumer = "android")
//    fun postToDiscussionTopicPact(builder: PactDslWithProvider) : RequestResponsePact {
//        return builder
//                .given("mobile course with discussions")
//
//                .uponReceiving("Post to a discussion topic")
//                .path(postToDiscussionTopicPath)
//                .method("POST")
//                .query(postToDiscussionTopicQuery)
//                //.headers(DEFAULT_REQUEST_HEADERS)
//                .headers(mapOf(
//                        "Authorization" to "Bearer some_token",
//                        //"Auth-User" to DEFAULT_MOBILE_STUDENT,
//                        "Auth-User" to "Mobile Teacher",
//                        "Content-Type" to "multipart/form-data"
//                ))
//
//                .willRespondWith()
//                .status(201)
//                .body(postToTopicResponseBody)
//                .headers(DEFAULT_RESPONSE_HEADERS)
//
//                .toPact()
//    }
//
//    @Test
//    @PactVerification(fragment = "postToDiscussionTopicPact")
//    fun `post to discussion topic`() {
//        val service = createService("Mobile Teacher")
//
//        val messagePart = RequestBody.create(MediaType.parse("multipart/form-data"), "<html><body>Posted entry</body></html>")
//        val postToDiscussionTopicCall = service.postDiscussionEntry("courses", 3, 1, messagePart)
//        val postToDiscussionTopicResult = postToDiscussionTopicCall.execute()
//
//        assertQueryParamsAndPath(postToDiscussionTopicCall, postToDiscussionTopicQuery, postToDiscussionTopicPath)
//
//        Assert.assertNotNull("Expected non-null response body", postToDiscussionTopicResult.body())
//        assertDiscussionEntryPopulated("Returned DiscussionEntry", postToDiscussionTopicResult.body()!!)
//    }
    //endregion

}
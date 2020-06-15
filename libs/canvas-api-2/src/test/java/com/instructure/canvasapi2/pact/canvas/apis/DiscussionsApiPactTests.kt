package com.instructure.canvasapi2.pact.canvas.apis

import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit.PactVerification
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import com.instructure.canvasapi2.apis.CourseAPI
import com.instructure.canvasapi2.apis.DiscussionAPI
import com.instructure.canvasapi2.pact.canvas.logic.PactCourseFieldConfig
import com.instructure.canvasapi2.pact.canvas.logic.assertCoursePopulated
import com.instructure.canvasapi2.pact.canvas.logic.assertDiscussionTopicHeaderPopulated
import com.instructure.canvasapi2.pact.canvas.logic.populateCourseFields
import com.instructure.canvasapi2.pact.canvas.logic.populateDiscussionTopicHeaderFields
import io.pactfoundation.consumer.dsl.LambdaDsl
import org.junit.Assert
import org.junit.Test

class DiscussionsApiPactTests : ApiPactTestBase() {

    // Common logic
    private fun createService() : DiscussionAPI.DiscussionInterface {

        val client = getClient()
        return client.create(DiscussionAPI.DiscussionInterface::class.java)
    }

    //
    //region Test grabbing discussion topics
    //

    val listDiscussionTopicsQuery = "override_assignment_dates=true&include[]=all_dates&include[]=overrides&include[]=sections"
    val listDiscussionTopicsPath = "/api/v1/courses/3/discussion_topics"
    val listDiscussionTopicsResponseBody =  LambdaDsl.newJsonArray { array ->
        array.`object`() { obj->
            obj.populateDiscussionTopicHeaderFields() // Just specify one?
        }
    }.build()

    @Pact(consumer = "android")
    fun getCourseDiscussionTopicsPact(builder: PactDslWithProvider) : RequestResponsePact {
        return builder
                .given("course with discussions")

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
        Assert.assertEquals("returned list size", 1, topicHeaderList.count())

        for(index in 0..topicHeaderList.size-1) {
            val header = topicHeaderList[index]

            assertDiscussionTopicHeaderPopulated(description = "topic $index", header = header)
        }
    }
    //endregion
}